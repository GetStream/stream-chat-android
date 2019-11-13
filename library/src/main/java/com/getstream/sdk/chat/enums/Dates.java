package com.getstream.sdk.chat.enums;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.StreamChat;

public enum Dates {
    TODAY(StreamChat.getContext().getString(R.string.stream_today)),
    YESTERDAY(StreamChat.getContext().getString(R.string.stream_yesterday)),
    JUST_NOW(StreamChat.getContext().getString(R.string.stream_just_now));

    public final String label;

    Dates(String label) {
        this.label = label;
    }
}
