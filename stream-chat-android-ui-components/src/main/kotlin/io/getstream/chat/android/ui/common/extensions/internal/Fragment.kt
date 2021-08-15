package io.getstream.chat.android.ui.common.extensions.internal

import androidx.fragment.app.Fragment

internal inline fun <reified T> Fragment.findListener(): T? {
    return when {
        parentFragment is T -> parentFragment as T
        activity is T -> activity as T
        else -> null
    }
}
