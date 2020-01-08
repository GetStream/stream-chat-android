package com.getstream.sdk.chat.navigation.destinations;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.navigation.destinations.ChatDestination;

public class WebLinkDestination extends ChatDestination {

    public final String url;

    public WebLinkDestination(String url, Context context) {
        super(context);
        String signedUrl = StreamChat.getInstance(context).getUploadStorage().signFileUrl(url);
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
