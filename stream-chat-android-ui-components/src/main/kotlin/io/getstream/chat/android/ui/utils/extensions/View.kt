/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.FragmentActivity
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.ui.helper.ViewPadding

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

internal fun View.setPaddingCompat(padding: ViewPadding) {
    ViewCompat.setPaddingRelative(
        this,
        padding.start,
        padding.top,
        padding.end,
        padding.bottom,
    )
}

/**
 * Applies window inset padding for edge-to-edge layouts.
 *
 * This overwrites the view's padding with the requested insets.
 * Use on root containers where the base padding should be fully replaced by insets.
 *
 * @param typeMask A bitmask from [WindowInsetsCompat.Type] (for example:
 * `WindowInsetsCompat.Type.systemBars()` or `WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()`).
 */
internal fun View.applyEdgeToEdgePadding(
    typeMask: Int,
) {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
        val insets = windowInsets.getInsets(typeMask)
        view.updatePadding(
            left = insets.left,
            top = insets.top,
            right = insets.right,
            bottom = insets.bottom,
        )
        WindowInsetsCompat.CONSUMED
    }
    ViewCompat.requestApplyInsets(this)
}
