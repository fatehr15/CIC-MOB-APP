package com.cic.mobapp.ui.home;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import com.cic.mobapp.data.local.AppDatabase;
import com.cic.mobapp.data.local.entity.AnnouncementEntity;
import com.cic.mobapp.data.local.entity.EventEntity;
import com.cic.mobapp.data.local.entity.ResourceEntity;
import com.cic.mobapp.data.local.entity.UserEntity;
import com.cic.mobapp.data.remote.ApiService;
import com.cic.mobapp.data.remote.RetrofitClient;
import com.cic.mobapp.data.remote.dto.AnnouncementDto;
import com.cic.mobapp.data.repository.EventRepository;
import com.cic.mobapp.data.repository.ResourceRepository;
import com.cic.mobapp.data.repository.UserRepository;
import com.cic.mobapp.util.TokenManager;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeViewModel extends AndroidViewModel {

    private final UserRepository     userRepository;
    private final EventRepository    eventRepository;
    private final ResourceRepository resourceRepository;
    private final AppDatabase        db;
    private final ApiService         apiService;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        userRepository     = new UserRepository(application);
        eventRepository    = new EventRepository(application);
        resourceRepository = new ResourceRepository(application);
        db                 = AppDatabase.getInstance(application);
        apiService         = RetrofitClient.getApiService();

        eventRepository.refreshFromApi(null, null);
        resourceRepository.refreshFromApi(null, null, null);
        syncAnnouncementsFromApi();
    }

    public LiveData<UserEntity> getCurrentUser() {
        String userId = new TokenManager(getApplication()).getUserId();
        return userRepository.getMe(userId != null ? userId : "");
    }

    public LiveData<List<EventEntity>> getUpcomingEvents() {
        return eventRepository.getEventsLiveData();
    }

    public LiveData<List<ResourceEntity>> getResources() {
        return resourceRepository.getResourcesLiveData(null);
    }

    /** Announcements come from Room — updated by admin CRUD and API sync. */
    public LiveData<List<AnnouncementDto>> getAnnouncements() {
        return Transformations.map(
                db.announcementDao().getAll(),
                entities -> {
                    if (entities == null) return new ArrayList<>();
                    return entities.stream()
                            .map(HomeViewModel::entityToDto)
                            .collect(Collectors.toList());
                });
    }

    /** Best-effort: pull API data into Room so Room LiveData updates automatically. */
    private void syncAnnouncementsFromApi() {
        apiService.getAnnouncements(null).enqueue(new Callback<List<AnnouncementDto>>() {
            @Override public void onResponse(Call<List<AnnouncementDto>> call,
                                             Response<List<AnnouncementDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<AnnouncementEntity> entities = response.body().stream()
                            .map(HomeViewModel::dtoToEntity)
                            .collect(Collectors.toList());
                    new Thread(() -> db.announcementDao().upsertAll(entities)).start();
                }
            }
            @Override public void onFailure(Call<List<AnnouncementDto>> call, Throwable t) {}
        });
    }

    private static AnnouncementDto entityToDto(AnnouncementEntity e) {
        AnnouncementDto d = new AnnouncementDto();
        d.id = e.id; d.title = e.title; d.body = e.body;
        d.type = e.type; d.priority = e.priority;
        d.isPinned = e.isPinned; d.createdAt = e.createdAt;
        return d;
    }

    private static AnnouncementEntity dtoToEntity(AnnouncementDto d) {
        AnnouncementEntity e = new AnnouncementEntity();
        e.id = d.id != null ? d.id : ("ann_" + System.currentTimeMillis());
        e.title = d.title; e.body = d.body; e.type = d.type;
        e.priority = d.priority; e.isPinned = d.isPinned; e.createdAt = d.createdAt;
        return e;
    }
}
