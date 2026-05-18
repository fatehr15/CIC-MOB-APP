package com.cic.mobapp.data.remote;

import com.cic.mobapp.data.remote.dto.AnnouncementDto;
import com.cic.mobapp.data.remote.dto.AuthDto;
import com.cic.mobapp.data.remote.dto.EventDto;
import com.cic.mobapp.data.remote.dto.ResourceDto;
import com.cic.mobapp.data.remote.dto.UserDto;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // Auth — email/password
    @POST("auth/login")
    Call<AuthDto.AuthResponse> loginWithEmail(@Body AuthDto.EmailLoginRequest request);

    @POST("auth/register")
    Call<AuthDto.AuthResponse> register(@Body AuthDto.RegisterRequest request);

    // Auth — Discord OAuth2
    @POST("auth/discord")
    Call<AuthDto.AuthResponse> loginWithDiscord(@Body AuthDto.DiscordLoginRequest request);

    @POST("auth/refresh")
    Call<AuthDto.AuthResponse> refreshToken(@Body AuthDto.RefreshRequest request);

    @POST("auth/logout")
    Call<Void> logout();

    // Users
    @GET("users/me")
    Call<UserDto> getMe();

    @GET("users/{id}")
    Call<UserDto> getUser(@Path("id") String id);

    @PATCH("users/me")
    Call<UserDto> updateMe(@Body UserDto user);

    // Admin — Users
    @GET("users")
    Call<List<UserDto>> getAllUsers();

    @PATCH("users/{id}")
    Call<UserDto> updateUser(@Path("id") String id, @Body UserDto user);

    @DELETE("users/{id}")
    Call<Void> deleteUser(@Path("id") String id);

    // Events
    @GET("events")
    Call<List<EventDto>> getEvents(
            @Query("type") String type,
            @Query("from") String from,
            @Query("to") String to
    );

    @GET("events/{id}")
    Call<EventDto> getEvent(@Path("id") String id);

    @POST("events")
    Call<EventDto> createEvent(@Body EventDto event);

    @POST("events/{id}/register")
    Call<Void> registerForEvent(@Path("id") String id);

    @POST("events/{id}/unregister")
    Call<Void> unregisterFromEvent(@Path("id") String id);

    // Admin — Events
    @DELETE("events/{id}")
    Call<Void> deleteEvent(@Path("id") String id);

    // Resources
    @GET("resources")
    Call<List<ResourceDto>> getResources(
            @Query("category") String category,
            @Query("difficulty") String difficulty,
            @Query("q") String query
    );

    @GET("resources/{id}")
    Call<ResourceDto> getResource(@Path("id") String id);

    @POST("resources")
    Call<ResourceDto> uploadResource(@Body ResourceDto resource);

    // Announcements
    @GET("announcements")
    Call<List<AnnouncementDto>> getAnnouncements(@Query("pinned") Boolean pinnedOnly);

    // Admin — Edit
    @PATCH("events/{id}")
    Call<EventDto> updateEvent(@Path("id") String id, @Body EventDto event);

    @PATCH("resources/{id}")
    Call<ResourceDto> updateResource(@Path("id") String id, @Body ResourceDto resource);

    @PATCH("announcements/{id}")
    Call<AnnouncementDto> updateAnnouncement(@Path("id") String id, @Body AnnouncementDto announcement);

    // Admin — Stats
    @GET("admin/stats")
    Call<java.util.Map<String, Integer>> getAdminStats();

    // Admin — Resources
    @DELETE("resources/{id}")
    Call<Void> deleteResource(@Path("id") String id);

    // Admin — Announcements
    @POST("announcements")
    Call<AnnouncementDto> createAnnouncement(@Body AnnouncementDto announcement);

    @DELETE("announcements/{id}")
    Call<Void> deleteAnnouncement(@Path("id") String id);
}
