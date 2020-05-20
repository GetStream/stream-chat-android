package com.getstream.sdk.chat.enums;

import androidx.annotation.StringRes;

import com.getstream.sdk.chat.Chat;
import com.getstream.sdk.chat.R;

public enum Dates {
    TODAY(R.string.stream_today),
    YESTERDAY(R.string.stream_yesterday),
    JUST_NOW(R.string.stream_just_now);

    @StringRes
    private final int labelId;

    public String getLabel() {
        return Chat.getInstance().getStrings().get(labelId);
    }

    Dates(@StringRes int labelId) {
        this.labelId = labelId;
    }
}
