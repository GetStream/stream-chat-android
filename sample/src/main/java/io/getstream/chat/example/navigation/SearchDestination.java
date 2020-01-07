package io.getstream.chat.example.navigation;

import android.content.Context;

import com.getstream.sdk.chat.navigation.destinations.ChatDestination;

import androidx.annotation.Nullable;
import io.getstream.chat.example.search.MessageSearchActivity;

public class SearchDestination extends ChatDestination {

    private final String cid;

    public SearchDestination(@Nullable String cid, Context context) {
        super(context);
        this.cid = cid;
    }

    @Override
    public void navigate() {

        if (cid == null) {
            start(MessageSearchActivity.searchAndOpenChannel(context));
        } else {
            start(MessageSearchActivity.search(context, cid));
        }


    }
}
