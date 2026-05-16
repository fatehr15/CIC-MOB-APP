package com.cic.mobapp.data.remote;

import android.content.Context;
import com.cic.mobapp.BuildConfig;
import com.cic.mobapp.util.SessionManager;
import com.cic.mobapp.util.TokenManager;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class RetrofitClient {

    private static ApiService apiService;
    private static TokenManager tokenManager;

    public static void init(Context context) {
        tokenManager = new TokenManager(context);

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(BuildConfig.DEBUG
                ? HttpLoggingInterceptor.Level.BASIC
                : HttpLoggingInterceptor.Level.NONE);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(authInterceptor())
                .addNetworkInterceptor(sessionInterceptor())
                .addInterceptor(logging)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);
    }

    // Attaches JWT to every non-auth request
    private static Interceptor authInterceptor() {
        return chain -> {
            String token = tokenManager.getAccessToken();
            Request original = chain.request();
            if (token == null) return chain.proceed(original);
            Request authenticated = original.newBuilder()
                    .header("Authorization", "Bearer " + token)
                    .build();
            return chain.proceed(authenticated);
        };
    }

    // Watches for 401 on non-auth routes → forces logout
    private static Interceptor sessionInterceptor() {
        return chain -> {
            Response response = chain.proceed(chain.request());
            if (response.code() == 401) {
                String path = chain.request().url().encodedPath();
                boolean isAuthEndpoint = path.contains("/auth/login")
                        || path.contains("/auth/register")
                        || path.contains("/auth/discord");
                if (!isAuthEndpoint) {
                    tokenManager.clearTokens();
                    SessionManager.get().onTokenInvalid();
                }
            }
            return response;
        };
    }

    public static ApiService getApiService() {
        if (apiService == null) throw new IllegalStateException("RetrofitClient not initialized");
        return apiService;
    }
}
