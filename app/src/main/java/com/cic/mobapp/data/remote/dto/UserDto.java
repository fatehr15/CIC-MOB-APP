package com.cic.mobapp.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class UserDto {

    @SerializedName("id")
    public String id;

    @SerializedName("discord_id")
    public String discordId;

    @SerializedName("username")
    public String username;

    @SerializedName("avatar")
    public String avatarUrl;

    @SerializedName("email")
    public String email;

    @SerializedName("role")
    public String role;

    @SerializedName("xp")
    public int xp;

    @SerializedName("level")
    public int level;

    @SerializedName("created_at")
    public String createdAt;
}
