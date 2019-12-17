package io.getstream.chat.example.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.List;

import io.getstream.chat.example.BuildConfig;

public class AppDataConfig {

    private static Context context;
    private static SharedPreferences preferences;
    private static AppData appData;
    private static String currentApiKey;

    private static final String CURRENT_USER_ID = "current-user-id";
    private static final String APP_PREFERENCE = "AppPreferences";

    public static void init(Context mContext) {
        context = mContext;
        preferences = context.getSharedPreferences(APP_PREFERENCE, Activity.MODE_PRIVATE);
        parseJson();
    }


    @Nullable
    public static UserConfig getCurrentUser() {
        String currentUserId = preferences.getString(CURRENT_USER_ID, null);

        List<UserConfig> userConfigs = getUsers();

        if (currentUserId == null || userConfigs == null)
            return null;

        for (UserConfig config : userConfigs){
            if (config.getId().equals(currentUserId))
                return config;
        }

        return null;
    }

    public static void setCurrentUser(@Nullable String userId){
        preferences.edit().putString(CURRENT_USER_ID, userId).apply();
    }

    @Nullable
    public static List<UserConfig> getUsers() {
        return appData.getUserConfigs();
    }

    public static void parseJson(){
        String json = BuildConfig.USERS_CONFIG;
        try {
            appData = new Gson().fromJson(json, new TypeToken<AppData>(){}.getType());
            currentApiKey = appData.getApi_key();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static String getCurrentApiKey() {
        return currentApiKey;
    }
}
