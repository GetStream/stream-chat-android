package com.getstream.sdk.chat.utils.strings;

import androidx.annotation.StringRes;

public interface ChatStrings {
    String get(@StringRes int resId);
    String get(@StringRes int resId, Object... formatArgs);
}
