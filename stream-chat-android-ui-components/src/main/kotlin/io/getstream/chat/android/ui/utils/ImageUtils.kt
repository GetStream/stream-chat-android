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

package io.getstream.chat.android.ui.utils

import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import androidx.annotation.DrawableRes
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.ui.common.disposable.Disposable
import io.getstream.chat.android.ui.common.images.internal.StreamImageLoader
import io.getstream.chat.android.ui.common.images.internal.StreamImageLoader.ImageTransformation

@InternalStreamChatApi
public fun ImageView.load(
    data: Any?,
    @DrawableRes placeholderResId: Int? = null,
    transformation: ImageTransformation = ImageTransformation.None,
    onStart: () -> Unit = {},
    onComplete: () -> Unit = {},
): Disposable = StreamImageLoader.instance().load(
    target = this,
    data = data,
    placeholderResId = placeholderResId,
    transformation = transformation,
    onStart = onStart,
    onComplete = onComplete,
)

@InternalStreamChatApi
public fun ImageView.load(
    data: Any?,
    placeholderDrawable: Drawable?,
    transformation: ImageTransformation = ImageTransformation.None,
    onStart: () -> Unit = {},
    onComplete: () -> Unit = {},
): Disposable = StreamImageLoader.instance().load(
    target = this,
    data = data,
    placeholderDrawable = placeholderDrawable,
    transformation = transformation,
    onStart = onStart,
    onComplete = onComplete,
)

/**
 * Loads an image into a drawable and then applies the drawable to the container, resizing it based on the scale types
 * and the given configuration.
 *
 * @param data The data to load.
 * @param placeholderDrawable Drawable that's shown while the image is loading.
 * @param transformation The transformation for the image before applying to the target.
 * @param onStart The callback when the load has started.
 * @param onComplete The callback when the load has finished.
 */
@InternalStreamChatApi
public suspend fun ImageView.loadAndResize(
    data: Any?,
    placeholderDrawable: Drawable?,
    transformation: ImageTransformation = ImageTransformation.None,
    onStart: () -> Unit = {},
    onComplete: () -> Unit = {},
) {
    StreamImageLoader.instance().loadAndResize(
        target = this,
        data = data,
        placeholderDrawable = placeholderDrawable,
        transformation = transformation,
        onStart = onStart,
        onComplete = onComplete,
    )
}

@InternalStreamChatApi
public fun ImageView.loadVideoThumbnail(
    uri: Uri?,
    @DrawableRes placeholderResId: Int? = null,
    transformation: ImageTransformation = ImageTransformation.None,
    onStart: () -> Unit = {},
    onComplete: () -> Unit = {},
): Disposable = StreamImageLoader.instance().loadVideoThumbnail(
    target = this,
    uri = uri,
    placeholderResId = placeholderResId,
    transformation = transformation,
    onStart = onStart,
    onComplete = onComplete,
)
