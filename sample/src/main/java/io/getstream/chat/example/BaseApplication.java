package io.getstream.chat.example;

import android.app.Application;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.getstream.sdk.chat.MarkDown;
import com.getstream.sdk.chat.MarkdownImpl;
import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.rest.core.ApiClientOptions;
import com.getstream.sdk.chat.rest.interfaces.CompletableCallback;
import com.getstream.sdk.chat.rest.response.CompletableResponse;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;

import io.fabric.sdk.android.Fabric;


public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        FirebaseApp.initializeApp(getApplicationContext());
        StreamChat.init(BuildConfig.API_KEY, new ApiClientOptions.Builder().Timeout(6666).build(), getApplicationContext());
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

//        MarkdownImpl.setMarkdownListener(new MarkdownImpl.MarkdownListener() {
//            @Override
//            public void setText(TextView textView, String text) {
//
//            }
//        });

    }
}
