package io.getstream.chat.example.utils;


import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AppData {
    @SerializedName("api_key")
    private String api_key;

    @SerializedName("api_endpoint")
    private String api_endpoint;

    @SerializedName("api_timeout")
    private int api_timeout;

    @SerializedName("cdn_timeout")
    private int cdn_timeout;

    @SerializedName("users")
    private List<UserConfig>userConfigs;

    public String getApi_key() {
        return api_key;
    }

    public String getApi_endpoint() {
        return api_endpoint;
    }

    public int getApi_timeout() {
        return api_timeout;
    }

    public int getCdn_timeout() {
        return cdn_timeout;
    }

    public List<UserConfig> getUserConfigs() {
        return userConfigs;
    }
}
