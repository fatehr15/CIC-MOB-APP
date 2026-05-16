package com.cic.mobapp.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class AuthDto {

    public static class DiscordLoginRequest {
        @SerializedName("code")
        public String code;

        @SerializedName("redirect_uri")
        public String redirectUri;

        public DiscordLoginRequest(String code, String redirectUri) {
            this.code = code;
            this.redirectUri = redirectUri;
        }
    }

    public static class EmailLoginRequest {
        @SerializedName("email")
        public String email;

        @SerializedName("password")
        public String password;

        public EmailLoginRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }
    }

    public static class RegisterRequest {
        @SerializedName("username")
        public String username;

        @SerializedName("email")
        public String email;

        @SerializedName("password")
        public String password;

        public RegisterRequest(String username, String email, String password) {
            this.username = username;
            this.email = email;
            this.password = password;
        }
    }

    public static class AuthResponse {
        @SerializedName("access_token")
        public String accessToken;

        @SerializedName("refresh_token")
        public String refreshToken;

        @SerializedName("user")
        public UserDto user;
    }

    public static class RefreshRequest {
        @SerializedName("refresh_token")
        public String refreshToken;

        public RefreshRequest(String refreshToken) {
            this.refreshToken = refreshToken;
        }
    }
}
