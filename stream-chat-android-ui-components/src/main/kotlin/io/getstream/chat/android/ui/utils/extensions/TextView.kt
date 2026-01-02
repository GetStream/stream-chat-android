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

import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.widget.TextView
import androidx.annotation.DimenRes
import androidx.annotation.Px

internal fun TextView.setTextSizePx(@Px size: Float) {
    setTextSize(TypedValue.COMPLEX_UNIT_PX, size)
}

/**
 * Sets the start drawable of a [TextView] with a desired size.
 *
 * @param drawable [Drawable].
 * @param sizeRes Dimension id.
 */
internal fun TextView.setStartDrawableWithSize(drawable: Drawable?, @DimenRes sizeRes: Int) {
    val size = resources.getDimensionPixelSize(sizeRes)
    val isRtl = context.isRtlLayout

    drawable?.setBounds(0, 0, size, size)

    if (isRtl) {
        setCompoundDrawables(null, null, drawable, null)
    } else {
        setCompoundDrawables(drawable, null, null, null)
    }
}

/**
 * Set the [drawable] to appear to the left of the text. If [drawable] is null,
 * nothing will be drawn on the left side of the text.
 */
internal fun TextView.setStartDrawable(drawable: Drawable?) {
    val isRtl = context.isRtlLayout

    if (isRtl) {
        setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
    } else {
        setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
    }
}
