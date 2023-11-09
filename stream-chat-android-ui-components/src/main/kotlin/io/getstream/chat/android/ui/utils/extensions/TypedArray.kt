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

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import androidx.annotation.StyleableRes
import androidx.appcompat.content.res.AppCompatResources

/**
 * Retrieves the Drawable for the attribute.
 *
 * Unlike [TypedArray.getDrawable], gracefully handles vector drawables with tints on API 21.
 *
 * @param context The context to inflate against.
 * @param id The index of attribute to retrieve.
 * @return An object that can be used to draw this resource.
 */
internal fun TypedArray.getDrawableCompat(context: Context, @StyleableRes id: Int): Drawable? {
    val resource = getResourceId(id, 0)
    if (resource != 0) {
        return AppCompatResources.getDrawable(context, resource)
    }
    return null
}

/**
 * Retrieves the ColorStateList for the attribute.
 *
 * @param context The context to inflate against.
 * @param id The index of attribute to retrieve.
 * @return An object that can be used to draw this resource.
 */
internal fun TypedArray.getColorStateListCompat(context: Context, @StyleableRes id: Int): ColorStateList? {
    val resource = getResourceId(id, 0)
    if (resource != 0) {
        return AppCompatResources.getColorStateList(context, resource)
    }
    return null
}
