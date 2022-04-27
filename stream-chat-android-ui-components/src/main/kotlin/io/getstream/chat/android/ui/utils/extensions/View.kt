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

import android.content.res.ColorStateList
import android.graphics.drawable.RippleDrawable
import android.view.View
import androidx.annotation.ColorInt

/**
 * Helper method for adding ripple effect to views
 *
 * @param color Color of the ripple.
 */
internal fun View.setBorderlessRipple(@ColorInt color: Int?) {
    background = if (color == null) {
        null
    } else {
        val rippleColor = ColorStateList.valueOf(color)
        RippleDrawable(rippleColor, null, background)
    }
}
