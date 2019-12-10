package io.getstream.chat.example;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.rest.core.ApiClientOptions;
import com.getstream.sdk.chat.rest.interfaces.CompletableCallback;
import com.getstream.sdk.chat.rest.response.CompletableResponse;
import com.getstream.sdk.chat.style.StreamChatStyle;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;

import io.fabric.sdk.android.Fabric;


public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        FirebaseApp.initializeApp(getApplicationContext());

        ApiClientOptions apiClientOptions = new ApiClientOptions.Builder()
                .BaseURL(BuildConfig.API_ENDPOINT)
                .Timeout(BuildConfig.API_TIMEOUT)
                .CDNTimeout(BuildConfig.CDN_TIMEOUT)
                .build();

        new StreamChat.Builder(this)
                .setApiKey(BuildConfig.API_KEY)
                .setApiClientOptions(apiClientOptions)
                .build();

        StreamChat.initStyle(
                new StreamChatStyle.Builder()
                        //.setDefaultFont(R.font.lilyofthe_valley)
                        //.setDefaultFont("fonts/odibeesans_regular.ttf")
                        .build()
        );
        Crashlytics.setString("apiKey", BuildConfig.API_KEY);
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(task -> {
                            if (!task.isSuccessful()) {
                                return;
                            }
                            StreamChat.getInstance(getApplicationContext()).addDevice(task.getResult().getToken(), new CompletableCallback() {
                                @Override
                                public void onSuccess(CompletableResponse response) {
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
