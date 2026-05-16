package com.cic.mobapp.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "events")
public class EventEntity {

    @PrimaryKey
    @NonNull
    public String id;

    public String title;
    public String description;
    public String bannerUrl;
    public String location;
    public String date;
    public String type;
    public String difficulty;
    public int capacity;
    public int registeredCount;
    public boolean isRegistered;
    public String organizerId;
}
