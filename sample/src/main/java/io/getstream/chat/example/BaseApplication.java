package io.getstream.chat.example;

import android.app.Application;

import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.rest.core.ApiClientOptions;
import com.google.firebase.FirebaseApp;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;


public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        FirebaseApp.initializeApp(getApplicationContext());
        StreamChat.init("qk4nn7rpcn75", new ApiClientOptions.Builder().Timeout(6666).build(), getApplicationContext());
    }
}
