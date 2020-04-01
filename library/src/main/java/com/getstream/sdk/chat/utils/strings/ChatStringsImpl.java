package com.getstream.sdk.chat.utils.strings;

import android.content.Context;

public class ChatStringsImpl implements ChatStrings {

    private final Context appContext;

    public ChatStringsImpl(Context appContext) {

        this.appContext = appContext;
    }

    @Override
    public String get(int resId) {
        return appContext.getString(resId);
    }

    @Override
    public String get(int resId, Object... formatArgs) {
        return appContext.getString(resId, formatArgs);
    }
}
