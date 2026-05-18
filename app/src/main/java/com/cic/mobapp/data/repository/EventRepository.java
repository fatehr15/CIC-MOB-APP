package com.cic.mobapp.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.cic.mobapp.data.local.AppDatabase;
import com.cic.mobapp.data.local.dao.EventDao;
import com.cic.mobapp.data.local.entity.EventEntity;
import com.cic.mobapp.data.remote.ApiService;
import com.cic.mobapp.data.remote.RetrofitClient;
import com.cic.mobapp.data.remote.dto.EventDto;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventRepository {

    private final EventDao        eventDao;
    private final ApiService      apiService;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public EventRepository(Application application) {
        eventDao   = AppDatabase.getInstance(application).eventDao();
        apiService = RetrofitClient.getApiService();
    }

    public LiveData<List<EventEntity>> getEventsLiveData() {
        return eventDao.getAll();
    }

    public LiveData<EventEntity> getEvent(String id) {
        return eventDao.getById(id);
    }

    public void refreshFromApi(Runnable onSuccess, Consumer<String> onError) {
        apiService.getEvents(null, null, null).enqueue(new Callback<List<EventDto>>() {
            @Override public void onResponse(Call<List<EventDto>> call, Response<List<EventDto>> r) {
                if (r.isSuccessful() && r.body() != null) {
                    List<EventEntity> entities = new ArrayList<>();
                    for (EventDto dto : r.body()) entities.add(toEntity(dto));
                    executor.execute(() -> {
                        eventDao.upsertAll(entities);
                        if (onSuccess != null) onSuccess.run();
                    });
                } else {
                    if (onError != null) onError.accept("Server error " + r.code());
                }
            }
            @Override public void onFailure(Call<List<EventDto>> call, Throwable t) {
                if (onError != null) onError.accept("Network unavailable — showing cached data");
            }
        });
    }

    // ── Registration — local-first when API is offline ────────────────────────

    public void registerForEvent(String eventId, Runnable onSuccess, Runnable onError) {
        apiService.registerForEvent(eventId).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> c, Response<Void> r) {
                if (r.isSuccessful()) {
                    applyLocalRegistration(eventId, true);
                    onSuccess.run();
                } else {
                    onError.run();
                }
            }
            @Override public void onFailure(Call<Void> c, Throwable t) {
                // API unreachable — register locally so the user sees the change immediately
                applyLocalRegistration(eventId, true);
                onSuccess.run();
            }
        });
    }

    public void unregisterFromEvent(String eventId, Runnable onSuccess, Runnable onError) {
        apiService.unregisterFromEvent(eventId).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> c, Response<Void> r) {
                if (r.isSuccessful()) {
                    applyLocalRegistration(eventId, false);
                    onSuccess.run();
                } else {
                    onError.run();
                }
            }
            @Override public void onFailure(Call<Void> c, Throwable t) {
                applyLocalRegistration(eventId, false);
                onSuccess.run();
            }
        });
    }

    /**
     * Writes the registration state directly into Room so Room LiveData
     * notifies all observers (EventDetailActivity, HomeFragment, etc.) immediately.
     */
    private void applyLocalRegistration(String eventId, boolean register) {
        executor.execute(() -> {
            EventEntity ev = eventDao.getByIdSync(eventId);
            if (ev == null) return;
            if (register && !ev.isRegistered) {
                ev.isRegistered    = true;
                ev.registeredCount = ev.registeredCount + 1;
            } else if (!register && ev.isRegistered) {
                ev.isRegistered    = false;
                ev.registeredCount = Math.max(0, ev.registeredCount - 1);
            }
            eventDao.upsert(ev);
        });
    }

    private EventEntity toEntity(EventDto dto) {
        EventEntity e = new EventEntity();
        e.id              = dto.id;
        e.title           = dto.title;
        e.description     = dto.description;
        e.bannerUrl       = dto.bannerUrl;
        e.location        = dto.location;
        e.date            = dto.date;
        e.type            = dto.type;
        e.difficulty      = dto.difficulty;
        e.capacity        = dto.capacity;
        e.registeredCount = dto.registeredCount;
        e.isRegistered    = dto.isRegistered;
        e.organizerId     = dto.organizerId;
        return e;
    }
}
