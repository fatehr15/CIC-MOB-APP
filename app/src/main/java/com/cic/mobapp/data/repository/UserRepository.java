package com.cic.mobapp.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.cic.mobapp.data.local.AppDatabase;
import com.cic.mobapp.data.local.dao.UserDao;
import com.cic.mobapp.data.local.entity.UserEntity;
import com.cic.mobapp.data.remote.ApiService;
import com.cic.mobapp.data.remote.RetrofitClient;
import com.cic.mobapp.data.remote.dto.UserDto;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserRepository {

    private final UserDao userDao;
    private final ApiService apiService;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public UserRepository(Application application) {
        userDao = AppDatabase.getInstance(application).userDao();
        apiService = RetrofitClient.getApiService();
    }

    public LiveData<UserEntity> getMe(String userId) {
        refreshMe();
        return userDao.getById(userId);
    }

    private void refreshMe() {
        apiService.getMe().enqueue(new Callback<UserDto>() {
            @Override
            public void onResponse(Call<UserDto> call, Response<UserDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    executor.execute(() -> userDao.upsert(toEntity(response.body())));
                }
            }

            @Override
            public void onFailure(Call<UserDto> call, Throwable t) {}
        });
    }

    private UserEntity toEntity(UserDto dto) {
        UserEntity e = new UserEntity();
        e.id = dto.id;
        e.discordId = dto.discordId;
        e.username = dto.username;
        e.avatarUrl = dto.avatarUrl;
        e.email = dto.email;
        e.role = dto.role;
        e.xp = dto.xp;
        e.level = dto.level;
        e.createdAt = dto.createdAt;
        return e;
    }
}
