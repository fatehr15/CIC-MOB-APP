package com.cic.mobapp.data.local.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(
    tableName = "event_registrations",
    indices   = {@Index(value = {"eventId", "userId"}, unique = true)}
)
public class EventRegistrationEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull public String eventId   = "";
    @NonNull public String userId    = "";
             public String username;
             public String avatarUrl;
             public String userRole;
             public String registeredAt;
             public String status; // REGISTERED | ATTENDING | ABSENT
}
