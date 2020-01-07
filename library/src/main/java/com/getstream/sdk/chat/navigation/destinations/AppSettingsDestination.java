package com.getstream.sdk.chat.navigation.destinations;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

public class AppSettingsDestination extends ChatDestination {

    public AppSettingsDestination(Context context) {
        super(context);
    }

    @Override
    public void navigate() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
        intent.setData(uri);
        start(intent);
    }
}
