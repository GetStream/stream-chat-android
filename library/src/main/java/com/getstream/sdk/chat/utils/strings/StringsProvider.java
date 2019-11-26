package com.getstream.sdk.chat.utils.strings;

import androidx.annotation.StringRes;

public interface StringsProvider {
    String get(@StringRes int resId);
}
