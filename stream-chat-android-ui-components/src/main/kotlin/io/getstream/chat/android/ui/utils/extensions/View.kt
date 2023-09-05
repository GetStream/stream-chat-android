/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.ui.utils.extensions

import android.content.ContextWrapper
import android.content.res.ColorStateList
import android.graphics.drawable.RippleDrawable
import android.view.View
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.annotation.StringRes
import androidx.fragment.app.FragmentActivity
import io.getstream.chat.android.core.internal.InternalStreamChatApi

/**
 * Helper method for adding ripple effect to views
 *
 * @param color Color of the ripple.
 */
internal fun View.setBorderlessRipple(@ColorInt color: Int?) {
    background = if (color != null) {
        val rippleColor = ColorStateList.valueOf(color)
        RippleDrawable(rippleColor, null, background)
    } else {
        null
    }
}

/**
 * Ensures the context being accessed in a View can be cast to Activity.
 */
@InternalStreamChatApi
public val View.activity: FragmentActivity?
    get() {
        var context = context
        while (context is ContextWrapper) {
            if (context is FragmentActivity) {
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

internal fun View.setPaddingStart(@Px start: Int) {
    val isRtl = context.isRtlLayout

    if (isRtl) {
        setPadding(paddingLeft, paddingTop, start, paddingBottom)
    } else {
        setPadding(start, paddingTop, paddingRight, paddingBottom)
    }
}

internal fun View.setPaddingEnd(@Px start: Int) {
    val isRtl = context.isRtlLayout

    if (isRtl) {
        setPadding(start, paddingTop, paddingRight, paddingBottom)
    } else {
        setPadding(paddingLeft, paddingTop, start, paddingBottom)
    }
}
