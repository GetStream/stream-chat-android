package com.getstream.sdk.chat.utils.extensions

import java.util.Date

internal fun Date.isInLastMinute(): Boolean = (Date().time - 60000 < time)
