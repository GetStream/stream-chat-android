package io.getstream.chat.example.navigation;

import android.content.Context;
import android.content.Intent;

import com.getstream.sdk.chat.navigation.destinations.ChatDestination;

import io.getstream.chat.example.LoginActivity;

public class LoginDestination extends ChatDestination {

    public LoginDestination(Context context) {
        super(context);
    }

    @Override
    protected void navigate() {
        start(new Intent(context, LoginActivity.class));
    }
}
