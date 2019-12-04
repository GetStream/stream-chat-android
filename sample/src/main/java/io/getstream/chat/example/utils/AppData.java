package io.getstream.chat.example.utils;


import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AppData {
    @SerializedName("api_key")
    private String api_key;

    @SerializedName("users")
    private List<UserConfig>userConfigs;

    public String getApi_key() {
        return api_key;
    }

    public List<UserConfig> getUserConfigs() {
        return userConfigs;
    }
}
