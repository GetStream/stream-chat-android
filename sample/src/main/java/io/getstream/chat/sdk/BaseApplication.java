package io.getstream.chat.sdk;

import android.app.Application;
import com.google.firebase.FirebaseApp;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;


public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        FirebaseApp.initializeApp(getApplicationContext());
    }
}
