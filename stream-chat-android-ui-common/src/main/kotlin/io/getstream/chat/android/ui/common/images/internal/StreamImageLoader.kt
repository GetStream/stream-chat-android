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

package io.getstream.chat.android.ui.common.images.internal

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.annotation.Px
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.ui.common.disposable.Disposable
import io.getstream.chat.android.ui.common.helper.ImageAssetTransformer
import io.getstream.chat.android.ui.common.helper.ImageHeadersProvider

@InternalStreamChatApi
public sealed interface StreamImageLoader {
    public companion object {
        public fun instance(): StreamImageLoader = CoilStreamImageLoader
    }

    public var imageHeadersProvider: ImageHeadersProvider
    public var imageAssetTransformer: ImageAssetTransformer

    @Suppress("LongParameterList")
    public fun load(
        target: ImageView,
        data: Any?,
        @DrawableRes placeholderResId: Int? = null,
        transformation: ImageTransformation = ImageTransformation.None,
        onStart: () -> Unit = {},
        onComplete: () -> Unit = {},
    ): Disposable

    @Suppress("LongParameterList")
    public fun load(
        target: ImageView,
        data: Any?,
        placeholderDrawable: Drawable? = null,
        transformation: ImageTransformation = ImageTransformation.None,
        onStart: () -> Unit = {},
        onComplete: () -> Unit = {},
    ): Disposable

    @Suppress("LongParameterList")
    public suspend fun loadAndResize(
        target: ImageView,
        data: Any?,
        placeholderDrawable: Drawable? = null,
        transformation: ImageTransformation = ImageTransformation.None,
        onStart: () -> Unit = {},
        onComplete: () -> Unit = {},
    )

    @Suppress("LongParameterList")
    public fun loadVideoThumbnail(
        target: ImageView,
        uri: Uri?,
        @DrawableRes placeholderResId: Int? = null,
        transformation: ImageTransformation = ImageTransformation.None,
        onStart: () -> Unit = {},
        onComplete: () -> Unit = {},
    ): Disposable

    public suspend fun loadAsBitmap(
        context: Context,
        url: String,
        transformation: ImageTransformation = ImageTransformation.None,
    ): Bitmap?

    public sealed class ImageTransformation {
        public object None : ImageTransformation() {
            override fun toString(): String = "None"
        }
        public object Circle : ImageTransformation() {
            override fun toString(): String = "Circle"
        }
        public data class RoundedCorners(@Px public val radius: Float) : ImageTransformation()
    }
}
