package io.getstream.chat.example.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

public class UserStorage {

    private static Context context;
    private static SharedPreferences preferences;
    private static List<UserConfig>users;

    private static final String USER_DATA_FILENAME = "users-config.json";
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
        if (currentUserId == null || users == null)
            return null;

        for (UserConfig config : users){
            if (config.getId().equals(currentUserId))
                return config;
        }

        return null;
    }

    public static void setCurrentUser(String userId){
        preferences.edit().putString(CURRENT_USER_ID, userId).apply();
    }

    @Nullable
    public static List<UserConfig> getUsers() {
        return users;
    }

    public static void logout() {
        setCurrentUser(null);
    }

    private static void parseJson(){
        try {
            String json = loadJSONFromAsset();
            Gson gson = new Gson();
            users = gson.fromJson(json, new TypeToken<List<UserConfig>>(){}.getType());
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }
    }

    private static String loadJSONFromAsset() throws FileNotFoundException {
        String json;
        try {
            InputStream is = context.getAssets().open(USER_DATA_FILENAME);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            throw new FileNotFoundException("Unable to find user data json file.");
        }
        return json;
    }
}
