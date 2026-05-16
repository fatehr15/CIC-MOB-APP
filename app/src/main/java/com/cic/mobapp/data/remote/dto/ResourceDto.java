package com.cic.mobapp.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class ResourceDto {

    @SerializedName("id")
    public String id;

    @SerializedName("title")
    public String title;

    @SerializedName("category")
    public String category;

    @SerializedName("type")
    public String type;

    @SerializedName("difficulty")
    public String difficulty;

    @SerializedName("file_url")
    public String fileUrl;

    @SerializedName("description")
    public String description;

    @SerializedName("tags")
    public String[] tags;

    @SerializedName("uploaded_by")
    public String uploadedBy;

    @SerializedName("created_at")
    public String createdAt;
}
