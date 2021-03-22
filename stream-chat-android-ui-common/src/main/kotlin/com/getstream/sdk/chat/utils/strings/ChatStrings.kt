package com.getstream.sdk.chat.utils.strings

import androidx.annotation.StringRes

public interface ChatStrings {
    public fun get(@StringRes resId: Int): String
    public fun get(@StringRes resId: Int, vararg formatArgs: Any?): String
}
