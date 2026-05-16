package com.cic.mobapp.ui.profile;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.cic.mobapp.data.local.entity.UserEntity;
import com.cic.mobapp.data.repository.UserRepository;
import com.cic.mobapp.util.TokenManager;

public class ProfileViewModel extends AndroidViewModel {

    private final LiveData<UserEntity> user;

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        UserRepository repository = new UserRepository(application);
        String userId = new TokenManager(application).getUserId();
        user = repository.getMe(userId != null ? userId : "");
    }

    public LiveData<UserEntity> getUser() {
        return user;
    }
}
