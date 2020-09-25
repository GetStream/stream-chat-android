package com.getstream.sdk.chat.utils.strings

import androidx.annotation.StringRes

interface ChatStrings {
    fun get(@StringRes resId: Int): String
    fun get(@StringRes resId: Int, vararg formatArgs: Any?): String
}
