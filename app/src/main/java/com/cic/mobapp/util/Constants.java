package com.cic.mobapp.util;

public final class Constants {

    private Constants() {}

    public static final String PREF_AUTH = "cic_auth_prefs";
    public static final String KEY_ACCESS_TOKEN = "access_token";
    public static final String KEY_REFRESH_TOKEN = "refresh_token";
    public static final String KEY_USER_ID = "user_id";

    public static final int TOKEN_EXPIRY_BUFFER_SECONDS = 60;
}
