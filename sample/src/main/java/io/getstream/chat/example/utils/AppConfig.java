package io.getstream.chat.example.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import androidx.annotation.Nullable;
import io.getstream.chat.example.BuildConfig;

public class AppConfig {

    private SharedPreferences preferences;
    private Config appConfig;

    private final String CURRENT_USER_ID = "current-user-id";
    private final String APP_PREFERENCE = "AppPreferences";
    private final String API_KEY = "apiKey";
    private final String API_ENDPOINT = "apiEndPoint";
    private final String API_TIMEOUT = "apiTimeout";
    private final String CDN_TIMEOUT = "cdnTimeout";

    public AppConfig(Context context) {
        preferences = context.getSharedPreferences(APP_PREFERENCE, Activity.MODE_PRIVATE);
        String json = BuildConfig.USERS_CONFIG;
        appConfig = new Gson().fromJson(json, Config.class);
    }

    public UserConfig getCurrentUser() {
        String currentUserId = preferences.getString(CURRENT_USER_ID, null);

        List<UserConfig> userConfigs = getUsers();

        if (currentUserId == null || userConfigs == null)
            return null;

        for (UserConfig config : userConfigs) {
            if (config.getId().equals(currentUserId))
                return config;
        }

        return null;
    }

    public void setCurrentUser(@Nullable String userId) {
        preferences.edit().putString(CURRENT_USER_ID, userId).apply();
    }

    @Nullable
    public List<UserConfig> getUsers() {
        if (appConfig == null)
            return null;
        return appConfig.userConfigs;
    }

    public String getApiKey() {
        String string = preferences.getString(API_KEY, appConfig.apiKey);
        return string;
    }

    public String getApiEndPoint() {
        String result = preferences.getString(API_ENDPOINT, appConfig.apiEndpoint);
        return result;
    }

    public int getApiTimeout() {
        int result = preferences.getInt(API_TIMEOUT, appConfig.apiTimeout);
        return result;
    }

    public int getCdnTimeout() {
        int result = preferences.getInt(CDN_TIMEOUT, appConfig.cdnTimeout);
        return result;
    }

    public void reset() {
        preferences.edit().remove(API_KEY).apply();
        preferences.edit().remove(API_ENDPOINT).apply();
        preferences.edit().remove(API_TIMEOUT).apply();
        preferences.edit().remove(CDN_TIMEOUT).apply();
    }

    public void setApiKey(String value) {
        preferences.edit().putString(API_KEY, value).apply();
    }

    public void setApiEndPoint(String value) {
        preferences.edit().putString(API_ENDPOINT, value).apply();
    }

    public void setApiTimeout(int value) {
        preferences.edit().putInt(API_TIMEOUT, value).apply();
    }

    public void setCdnTimeout(int value) {
        preferences.edit().putInt(CDN_TIMEOUT, value).apply();
    }

    static class Config {
        @SerializedName("api_key")
        String apiKey;

        @SerializedName("api_endpoint")
        String apiEndpoint;

        @SerializedName("api_timeout")
        int apiTimeout;

        @SerializedName("cdn_timeout")
        int cdnTimeout;

        @SerializedName("users")
        List<UserConfig> userConfigs;
    }

}
