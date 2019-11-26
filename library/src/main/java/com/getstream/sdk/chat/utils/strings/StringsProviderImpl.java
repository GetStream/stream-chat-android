package com.getstream.sdk.chat.utils.strings;

import android.content.Context;

public class StringsProviderImpl implements StringsProvider {

    private final Context appContext;

    public StringsProviderImpl(Context appContext) {

        this.appContext = appContext;
    }

    @Override
    public String get(int resId) {
        return appContext.getString(resId);
    }
}
