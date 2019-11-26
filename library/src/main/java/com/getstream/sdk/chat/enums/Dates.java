package com.getstream.sdk.chat.enums;

import androidx.annotation.StringRes;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.StreamChat;

public enum Dates {
    TODAY(R.string.stream_today),
    YESTERDAY(R.string.stream_yesterday),
    JUST_NOW(R.string.stream_just_now);

    @StringRes
    private final int labelId;

    public String getLabel() {
        return StreamChat.getStrings().get(labelId);
    }

    Dates(@StringRes int labelId) {
        this.labelId = labelId;
    }
}
