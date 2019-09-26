package io.getstream.chat.example;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.rest.core.ApiClientOptions;
import com.getstream.sdk.chat.rest.interfaces.DeviceCallback;
import com.getstream.sdk.chat.rest.response.DevicesResponse;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;

import io.fabric.sdk.android.Fabric;


public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        FirebaseApp.initializeApp(getApplicationContext());
        StreamChat.init("qk4nn7rpcn75", new ApiClientOptions.Builder().Timeout(6666).build(), getApplicationContext());
        FirebaseInstanceId.getInstance().getInstanceId()
            .addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    return;
                }
                StreamChat.getInstance(getApplicationContext()).addDevice(task.getResult().getToken(), new DeviceCallback() {
                    @Override
                    public void onSuccess(DevicesResponse response) {
                        // device is now registered!
                    }
                    @Override
                    public void onError(String errMsg, int errCode) {
                        // something went wrong registering this device, ouch!
                    }
                });
            }
        );
    }
}
