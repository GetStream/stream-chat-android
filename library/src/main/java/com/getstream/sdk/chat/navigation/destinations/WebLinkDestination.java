package com.getstream.sdk.chat.navigation.destinations;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.getstream.sdk.chat.Chat;

public class WebLinkDestination extends ChatDestination {

    public final String url;

    public WebLinkDestination(String url, Context context) {
        super(context);
        String signedUrl = Chat.getInstance().urlSigner().signFileUrl(url);
        this.url = signedUrl == null ? "" : signedUrl;
    }

    @Override
    public void navigate() {
        Intent browserIntent = new Intent(
                Intent.ACTION_VIEW,
                Uri.parse(url)
        );
        start(browserIntent);
    }
}
