package com.getstream.sdk.chat.utils.extensions

import io.getstream.chat.android.core.internal.InternalStreamChatApi
import java.util.Date

@InternalStreamChatApi
public fun Date.isInLastMinute(): Boolean = (Date().time - 60000 < time)
