package io.getstream.chat.example.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import io.getstream.chat.example.BuildConfig;

public class AppDataStorage {

    private static Context context;
    private static SharedPreferences preferences;
    private static AppData appData;
    private static String currentApiKey = BuildConfig.API_KEY;

    private static final String USER_DATA_FILENAME = "app-data.json";
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

    public static void setCurrentUser(String userId){
        preferences.edit().putString(CURRENT_USER_ID, userId).apply();
    }



    @Nullable
    public static List<UserConfig> getUsers() {
        return appData.getUserConfigs();
    }



    public static void parseJson(){

        String json = BuildConfig.USERS_CONFIG;
        Log.d("AppDataStorage", json);

        Gson gson = new Gson();
        try {
            JSONArray array = new JSONArray(json);
            appData = gson.fromJson(json, new TypeToken<AppData>(){}.getType());
            currentApiKey = appData.getApi_key();
        }catch (Exception e){
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
