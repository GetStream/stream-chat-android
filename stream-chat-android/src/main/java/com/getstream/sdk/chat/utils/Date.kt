package com.getstream.sdk.chat.utils

import java.util.Date

internal fun Date.isInLastMinute(): Boolean = (Date().time - 60000 < time)
