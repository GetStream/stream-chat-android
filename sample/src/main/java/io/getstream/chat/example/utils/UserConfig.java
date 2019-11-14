package io.getstream.chat.example.utils;

import androidx.annotation.NonNull;

public class UserConfig {

    private String id;
    private String name;
    private String image;
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
