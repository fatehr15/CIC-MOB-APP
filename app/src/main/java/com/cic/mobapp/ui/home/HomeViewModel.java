package com.cic.mobapp.ui.home;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.cic.mobapp.data.local.entity.EventEntity;
import com.cic.mobapp.data.local.entity.UserEntity;
import com.cic.mobapp.data.remote.ApiService;
import com.cic.mobapp.data.remote.RetrofitClient;
import com.cic.mobapp.data.remote.dto.AnnouncementDto;
import com.cic.mobapp.data.repository.EventRepository;
import com.cic.mobapp.data.repository.UserRepository;
import com.cic.mobapp.util.TokenManager;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeViewModel extends AndroidViewModel {

    private final UserRepository  userRepository;
    private final EventRepository eventRepository;
    private final ApiService      apiService;

    private final MutableLiveData<List<AnnouncementDto>> announcements = new MutableLiveData<>();

    public HomeViewModel(@NonNull Application application) {
        super(application);
        userRepository  = new UserRepository(application);
        eventRepository = new EventRepository(application);
        apiService      = RetrofitClient.getApiService();

        // Trigger background refresh — data flows through Room LiveData
        eventRepository.refreshFromApi(null, null);
        loadAnnouncements();
    }

    public LiveData<UserEntity> getCurrentUser() {
        String userId = new TokenManager(getApplication()).getUserId();
        return userRepository.getMe(userId != null ? userId : "");
    }

    public LiveData<List<EventEntity>> getUpcomingEvents() {
        return eventRepository.getEventsLiveData();
    }

    public LiveData<List<AnnouncementDto>> getAnnouncements() {
        return announcements;
    }

    private void loadAnnouncements() {
        apiService.getAnnouncements(null).enqueue(new Callback<List<AnnouncementDto>>() {
            @Override public void onResponse(Call<List<AnnouncementDto>> call,
                                             Response<List<AnnouncementDto>> response) {
                if (response.isSuccessful() && response.body() != null)
                    announcements.postValue(response.body());
            }
            @Override public void onFailure(Call<List<AnnouncementDto>> call, Throwable t) {}
        });
    }
}
