package com.cic.mobapp.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class EventDto {

    @SerializedName("id")
    public String id;

    @SerializedName("title")
    public String title;

    @SerializedName("description")
    public String description;

    @SerializedName("banner_url")
    public String bannerUrl;

    @SerializedName("location")
    public String location;

    @SerializedName("date")
    public String date;

    @SerializedName("type")
    public String type;

    @SerializedName("difficulty")
    public String difficulty;

    @SerializedName("capacity")
    public int capacity;

    @SerializedName("registered_count")
    public int registeredCount;

    @SerializedName("is_registered")
    public boolean isRegistered;

    @SerializedName("organizer_id")
    public String organizerId;
}
