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

import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.core.graphics.drawable.DrawableCompat

/**
 * Applies a tint for a drawable. Returns the original Drawable if the input color
 * is null.
 *
 * @param tintColor The color used for tinting this Drawable.
 */
internal fun Drawable.applyTint(@ColorInt tintColor: Int?): Drawable {
    if (tintColor == null) return this

    val tintedDrawable = DrawableCompat.wrap(this).mutate()
    DrawableCompat.setTint(tintedDrawable, tintColor)
    DrawableCompat.setTintMode(tintedDrawable, PorterDuff.Mode.SRC_IN)
    return tintedDrawable
}
