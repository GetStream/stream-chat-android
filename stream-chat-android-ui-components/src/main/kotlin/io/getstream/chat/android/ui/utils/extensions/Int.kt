package io.getstream.chat.android.ui.utils.extensions

import android.content.res.Resources

internal fun Int.dpToPx() = (this * Resources.getSystem().displayMetrics.density).toInt()
