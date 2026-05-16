package com.cic.mobapp.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class AnnouncementDto {

    @SerializedName("id")
    public String id;

    @SerializedName("title")
    public String title;

    @SerializedName("body")
    public String body;

    @SerializedName("type")
    public String type;

    @SerializedName("priority")
    public String priority;

    @SerializedName("is_pinned")
    public boolean isPinned;

    @SerializedName("created_at")
    public String createdAt;

    @SerializedName("author_id")
    public String authorId;
}
