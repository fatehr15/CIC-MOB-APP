package com.cic.mobapp.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "resources")
public class ResourceEntity {

    @PrimaryKey
    @NonNull
    public String id;

    public String title;
    public String category;
    public String type;
    public String difficulty;
    public String fileUrl;
    public String description;
    public String tags;
    public String uploadedBy;
    public String createdAt;
}
