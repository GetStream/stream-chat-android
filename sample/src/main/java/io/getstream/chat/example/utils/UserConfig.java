package io.getstream.chat.example.utils;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class UserConfig {

    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("image")
    private String image;

    @SerializedName("token")
    private String token;

    @NonNull
    public String getId() {
        return id;
    }

    @NonNull
    public String getToken() {
        return token;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }
}
