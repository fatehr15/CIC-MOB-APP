package com.cic.mobapp.ui.announcements;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.cic.mobapp.data.remote.ApiService;
import com.cic.mobapp.data.remote.RetrofitClient;
import com.cic.mobapp.data.remote.dto.AnnouncementDto;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AnnouncementsViewModel extends AndroidViewModel {

    private final ApiService api;

    private final MutableLiveData<List<AnnouncementDto>> announcements = new MutableLiveData<>();
    private final MutableLiveData<Boolean>               loading       = new MutableLiveData<>(true);
    private final MutableLiveData<String>                error         = new MutableLiveData<>(null);

    public AnnouncementsViewModel(@NonNull Application application) {
        super(application);
        api = RetrofitClient.getApiService();
        load();
    }

    public LiveData<List<AnnouncementDto>> getAnnouncements() { return announcements; }
    public LiveData<Boolean>               isLoading()        { return loading; }
    public LiveData<String>                getError()         { return error; }

    public void refresh() { load(); }

    private void load() {
        loading.setValue(true);
        error.setValue(null);
        api.getAnnouncements(null).enqueue(new Callback<List<AnnouncementDto>>() {
            @Override public void onResponse(Call<List<AnnouncementDto>> c, Response<List<AnnouncementDto>> r) {
                loading.postValue(false);
                if (r.isSuccessful() && r.body() != null) {
                    announcements.postValue(r.body());
                } else {
                    error.postValue("Server error " + r.code());
                }
            }
            @Override public void onFailure(Call<List<AnnouncementDto>> c, Throwable t) {
                loading.postValue(false);
                error.postValue("Network unavailable — showing cached data");
            }
        });
    }
}
