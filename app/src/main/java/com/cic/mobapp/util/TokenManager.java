package com.cic.mobapp.util;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import com.cic.mobapp.util.Constants;

public class TokenManager {

    private final SharedPreferences prefs;

    public TokenManager(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            prefs = EncryptedSharedPreferences.create(
                    context,
                    Constants.PREF_AUTH,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize TokenManager", e);
        }
    }

    public void saveTokens(String accessToken, String refreshToken, String userId) {
        prefs.edit()
                .putString(Constants.KEY_ACCESS_TOKEN, accessToken)
                .putString(Constants.KEY_REFRESH_TOKEN, refreshToken)
                .putString(Constants.KEY_USER_ID, userId)
                .apply();
    }

    public String getAccessToken() {
        return prefs.getString(Constants.KEY_ACCESS_TOKEN, null);
    }

    public String getRefreshToken() {
        return prefs.getString(Constants.KEY_REFRESH_TOKEN, null);
    }

    public String getUserId() {
        return prefs.getString(Constants.KEY_USER_ID, null);
    }

    public boolean isLoggedIn() {
        return getAccessToken() != null;
    }

    public void clearTokens() {
        prefs.edit().clear().apply();
    }
}
