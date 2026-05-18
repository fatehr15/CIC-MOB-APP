package com.cic.mobapp.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "announcements")
public class AnnouncementEntity {

    @PrimaryKey
    @NonNull
    public String id;

    public String  title;
    public String  body;
    public String  type;
    public String  priority;
    public boolean isPinned;
    public String  createdAt;
}
