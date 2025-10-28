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

package io.getstream.chat.android.ui.common.images.internal

import android.content.Context
import coil3.ImageLoader
import coil3.SingletonImageLoader
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.ui.common.images.StreamImageLoaderFactory

@InternalStreamChatApi
public object StreamCoil {

    private var imageLoader: ImageLoader? = null
    private var imageLoaderFactory: SingletonImageLoader.Factory? = null

    @Synchronized
    public fun setImageLoader(factory: SingletonImageLoader.Factory) {
        imageLoaderFactory = factory
        imageLoader = null
    }

    internal fun imageLoader(context: Context): ImageLoader = imageLoader ?: newImageLoader(context)

    @Synchronized
    private fun newImageLoader(context: Context): ImageLoader {
        imageLoader?.let { return it }

        val imageLoaderFactory = imageLoaderFactory ?: newImageLoaderFactory()
        return imageLoaderFactory.newImageLoader(context).apply {
            imageLoader = this
        }
    }

    private fun newImageLoaderFactory(): SingletonImageLoader.Factory = StreamImageLoaderFactory().apply {
        imageLoaderFactory = this
    }

    internal inline val Context.streamImageLoader: ImageLoader
        get() = imageLoader(this)
}
