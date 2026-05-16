package com.cic.mobapp.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "users")
public class UserEntity {

    @PrimaryKey
    @NonNull
    public String id;

    public String discordId;
    public String username;
    public String avatarUrl;
    public String email;
    public String role;
    public int xp;
    public int level;
    public String createdAt;
}
