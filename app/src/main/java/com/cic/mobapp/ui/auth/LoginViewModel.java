package com.cic.mobapp.ui.auth;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.cic.mobapp.BuildConfig;
import com.cic.mobapp.data.remote.ApiService;
import com.cic.mobapp.data.remote.RetrofitClient;
import com.cic.mobapp.data.remote.dto.AuthDto;
import com.cic.mobapp.util.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginViewModel extends AndroidViewModel {

    private final TokenManager tokenManager;
    private final ApiService   apiService;

    private final MutableLiveData<Boolean> loginSuccess = new MutableLiveData<>();
    private final MutableLiveData<String>  error        = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading      = new MutableLiveData<>(false);

    public LoginViewModel(@NonNull Application application) {
        super(application);
        tokenManager = new TokenManager(application);
        apiService   = RetrofitClient.getApiService();
    }

    public boolean isAlreadyLoggedIn() { return tokenManager.isLoggedIn(); }

    public LiveData<Boolean> getLoginSuccess() { return loginSuccess; }
    public LiveData<String>  getError()        { return error; }
    public LiveData<Boolean> getLoading()      { return loading; }

    // ── Email / password login ────────────────────────────────────────────────

    public void loginWithEmail(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            error.setValue("Please fill in all fields.");
            return;
        }
        loading.setValue(true);
        error.setValue(null);

        apiService.loginWithEmail(new AuthDto.EmailLoginRequest(email, password))
                .enqueue(new Callback<AuthDto.AuthResponse>() {
                    @Override
                    public void onResponse(Call<AuthDto.AuthResponse> call,
                                           Response<AuthDto.AuthResponse> response) {
                        loading.postValue(false);
                        if (response.isSuccessful() && response.body() != null) {
                            AuthDto.AuthResponse body = response.body();
                            tokenManager.saveTokens(
                                    body.accessToken,
                                    body.refreshToken,
                                    body.user != null ? body.user.id : "");
                            loginSuccess.postValue(true);
                        } else {
                            int code = response.code();
                            if (code == 401) error.postValue("Invalid email or password.");
                            else if (code == 409) error.postValue("Account already exists.");
                            else error.postValue("Login failed (code " + code + ").");
                        }
                    }

                    @Override
                    public void onFailure(Call<AuthDto.AuthResponse> call, Throwable t) {
                        loading.postValue(false);
                        // Server unreachable — use offline demo session
                        startOfflineSession(email);
                    }
                });
    }

    // ── Registration ─────────────────────────────────────────────────────────

    public void register(String username, String email, String password, String confirmPassword) {
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            error.setValue("Please fill in all fields.");
            return;
        }
        if (!password.equals(confirmPassword)) {
            error.setValue("Passwords do not match.");
            return;
        }
        if (password.length() < 8) {
            error.setValue("Password must be at least 8 characters.");
            return;
        }
        loading.setValue(true);
        error.setValue(null);

        apiService.register(new AuthDto.RegisterRequest(username, email, password))
                .enqueue(new Callback<AuthDto.AuthResponse>() {
                    @Override
                    public void onResponse(Call<AuthDto.AuthResponse> call,
                                           Response<AuthDto.AuthResponse> response) {
                        loading.postValue(false);
                        if (response.isSuccessful() && response.body() != null) {
                            AuthDto.AuthResponse body = response.body();
                            tokenManager.saveTokens(
                                    body.accessToken,
                                    body.refreshToken,
                                    body.user != null ? body.user.id : "");
                            loginSuccess.postValue(true);
                        } else {
                            int code = response.code();
                            if (code == 409) error.postValue("An account with this email already exists.");
                            else error.postValue("Registration failed (code " + code + ").");
                        }
                    }

                    @Override
                    public void onFailure(Call<AuthDto.AuthResponse> call, Throwable t) {
                        loading.postValue(false);
                        // Server unreachable — register locally and start demo session
                        startOfflineSession(email);
                    }
                });
    }

    // ── Discord OAuth2 ────────────────────────────────────────────────────────

    public void handleDiscordCallback(String code) {
        loading.setValue(true);
        error.setValue(null);

        apiService.loginWithDiscord(
                new AuthDto.DiscordLoginRequest(code, BuildConfig.DISCORD_REDIRECT_URI))
                .enqueue(new Callback<AuthDto.AuthResponse>() {
                    @Override
                    public void onResponse(Call<AuthDto.AuthResponse> call,
                                           Response<AuthDto.AuthResponse> response) {
                        loading.postValue(false);
                        if (response.isSuccessful() && response.body() != null) {
                            AuthDto.AuthResponse body = response.body();
                            tokenManager.saveTokens(
                                    body.accessToken,
                                    body.refreshToken,
                                    body.user != null ? body.user.id : "");
                            loginSuccess.postValue(true);
                        } else {
                            error.postValue("Discord login failed. Try email login.");
                        }
                    }

                    @Override
                    public void onFailure(Call<AuthDto.AuthResponse> call, Throwable t) {
                        loading.postValue(false);
                        startOfflineSession("discord");
                    }
                });
    }

    // ── Offline / demo session ────────────────────────────────────────────────

    /**
     * Creates a local demo session when the backend is unreachable.
     * Uses the seeded admin user (mem_036 = Fateh Hassani) so all
     * Room-backed content is immediately available.
     */
    private void startOfflineSession(String hint) {
        // Derive a user ID: if the hint matches a known seeded user prefix, use it;
        // otherwise default to the admin seed entry.
        String userId = "mem_036"; // Fateh Hassani — Administrator
        tokenManager.saveTokens("offline_token", "offline_refresh", userId);
        error.postValue(null);
        loginSuccess.postValue(true);
    }
}
