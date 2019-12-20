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
import io.getstream.chat.example.utils.AppDataConfig;


public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        FirebaseApp.initializeApp(getApplicationContext());

        AppDataConfig.init(this);

        ApiClientOptions apiClientOptions = new ApiClientOptions.Builder()
                .BaseURL(AppDataConfig.getApiEndpoint())
                .Timeout(AppDataConfig.getApiTimeout())
                .CDNTimeout(AppDataConfig.getCdnTimeout())
                .build();

        StreamChatStyle style = new StreamChatStyle.Builder()
                //.setDefaultFont(R.font.lilyofthe_valley)
                //.setDefaultFont("fonts/odibeesans_regular.ttf")
                .build();

        StreamChat.Config configuration = new StreamChat.Config(this, AppDataConfig.getCurrentApiKey());
        configuration.setApiClientOptions(apiClientOptions);
        configuration.setStyle(style);
        StreamChat.init(configuration);

        Crashlytics.setString("apiKey", AppDataConfig.getCurrentApiKey());
    }
}
