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

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.ContextWrapper
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.ArrayRes
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.Px
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.ui.R

/**
 * Helper method to check is the system requests RTL direction.
 */
internal val Context.isRtlLayout: Boolean
    get() = resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL

@Px
@InternalStreamChatApi
public fun Context.getDimension(@DimenRes dimen: Int): Int {
    return resources.getDimensionPixelSize(dimen)
}

internal fun Context.getIntArray(@ArrayRes id: Int): IntArray {
    return resources.getIntArray(id)
}

@ColorInt
@InternalStreamChatApi
public fun Context.getColorCompat(@ColorRes color: Int): Int {
    return ContextCompat.getColor(this, color)
}

@InternalStreamChatApi
public fun Context.getColorStateListCompat(@ColorRes color: Int): ColorStateList? {
    return ContextCompat.getColorStateList(this, color)
}

@InternalStreamChatApi
public fun Context.getDrawableCompat(@DrawableRes id: Int): Drawable? {
    return ContextCompat.getDrawable(this, id)
}

internal fun Context?.getFragmentManager(): FragmentManager? {
    return when (this) {
        is AppCompatActivity -> supportFragmentManager
        is ContextWrapper -> baseContext.getFragmentManager()
        else -> null
    }
}

internal fun Context.copyToClipboard(text: String) {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(null, text))
}

internal val Context.streamThemeInflater: LayoutInflater
    get() = LayoutInflater.from(this.createStreamThemeWrapper())

internal fun Context.createStreamThemeWrapper(): Context {
    val typedValue = TypedValue()
    return when {
        theme.resolveAttribute(R.attr.streamUiValidTheme, typedValue, true) -> this
        theme.resolveAttribute(R.attr.streamUiTheme, typedValue, true) -> ContextThemeWrapper(this, typedValue.resourceId)
        else -> ContextThemeWrapper(this, R.style.StreamUiTheme)
    }
}
