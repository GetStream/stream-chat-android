package io.getstream.chat.example;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.rest.core.ApiClientOptions;
import com.google.firebase.FirebaseApp;

import io.fabric.sdk.android.Fabric;


public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        FirebaseApp.initializeApp(getApplicationContext());
        StreamChat.init(BuildConfig.API_KEY, new ApiClientOptions.Builder().Timeout(6666).build(), getApplicationContext());
        Crashlytics.setString("apiKey", BuildConfig.API_KEY);
    }
}
