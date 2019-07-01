package com.getstream.sdk.chat.utils;

import android.util.Log;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Push {
    private static final String TAG = Push.class.getSimpleName();
    public static void sendPushNotification2(String to, String title, String text) {
        Log.d(TAG,"User Token:" + to);
        String url = "https://fcm.googleapis.com/fcm/send";
        Map<String, Object> body = new HashMap<>();
        body.put("title", title);
        body.put("body", text);
        body.put("badge", "1");
        body.put("sound", "default");

        Map<String, Object> notification = new HashMap<>();
        notification.put("notification", body);
        notification.put("to", to);

        String jsonStr = new JSONObject(notification).toString();

        Log.d(TAG, "Body : " + jsonStr);
        sendPushnotification(url, jsonStr);

    }

    public static void sendPushnotification(String url, String params) {
        final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, params);

        OkHttpClient client = new OkHttpClient();
        Log.d(TAG,"Server Key: " + Global.fcmserverkey);
        final Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Authorization", Global.fcmserverkey)
                .addHeader("Content-Type", "application/json")
                .build();


        Log.d(TAG, "Push Notification sending...");
        Call call = client.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    Log.d(TAG, response.message());
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d(TAG, "Error : " + e.getLocalizedMessage());
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "Failure:" + e);
            }
        });
    }
}
