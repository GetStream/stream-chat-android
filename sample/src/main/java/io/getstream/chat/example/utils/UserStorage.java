package io.getstream.chat.example.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class UserStorage {

    private static Context context;
    private static SharedPreferences preferences;
    private static List<UserConfig>users;
    private static final String USER_DATA_FILENAME = "users-config.json";

    public static void init(Context mContext) {
        context = mContext;
        preferences = context.getSharedPreferences("AppPreferences", Activity.MODE_PRIVATE);
        parseJson();
    }

    @Nullable
    public static UserConfig getCurrentUser() {
        String currentUserId = preferences.getString("current-user-id", null);
        if (currentUserId == null || users == null)
            return null;

        for (UserConfig config : users){
            if (config.getId().equals(currentUserId))
                return config;
        }

        return null;
    }

    public static void setCurrentUser(String userId){
        preferences.edit().putString("current-user-id", userId).apply();
    }

    @Nullable
    public static List<UserConfig> getUsers() {
        return users;
    }

    public static boolean isLoggedIn() {
        return preferences.getBoolean("logged-in", false);
    }

    public static void logout() {
        setCurrentUser(null);
    }

    private static List<UserConfig> parseJson(){
        String json = loadJSONFromAsset();
        if (json == null)
            return null;

        Gson gson = new Gson();
        users = gson.fromJson(json, new TypeToken<List<UserConfig>>(){}.getType());
        return users;
    }

    private static String loadJSONFromAsset() {
        String json;
        try {
            InputStream is = context.getAssets().open(USER_DATA_FILENAME);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }
}
