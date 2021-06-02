package com.getstream.sdk.chat.utils.extensions

import android.content.ContextWrapper
import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import io.getstream.chat.android.core.internal.InternalStreamChatApi

/**
 * Ensures the context being accessed in a View can be cast to Activity
 */
@InternalStreamChatApi
public val View.activity: AppCompatActivity?
    get() {
        var context = context
        while (context is ContextWrapper) {
            if (context is AppCompatActivity) {
                return context
            }
            context = context.baseContext
        }
        return null
    }

@InternalStreamChatApi
public fun View.showToast(@StringRes resId: Int) {
    Toast.makeText(context, context.getString(resId), Toast.LENGTH_SHORT).show()
}
