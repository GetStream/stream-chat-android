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

package io.getstream.chat.android.ui.common.helper

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.annotation.DrawableRes
import okhttp3.HttpUrl
import java.io.File
import java.nio.ByteBuffer

/**
 * Transforms image assets before loading.
 *
 * @deprecated Use [io.getstream.chat.android.client.cdn.CDN] instead. Configure a custom CDN via
 * [io.getstream.chat.android.client.ChatClient.Builder.cdn] to transform URLs for all image, file,
 * and download requests.
 */
@Deprecated("Use CDN instead. Configure via ChatClient.Builder.cdn().")
public interface ImageAssetTransformer {

    /**
     * Returns a transformed asset to be used for the image loading request.
     *
     * The default supported data types are:
     * - [String] (mapped to a [Uri])
     * - [Uri] ("android.resource", "content", "file", "http", and "https" schemes only)
     * - [HttpUrl]
     * - [File]
     * - [DrawableRes]
     * - [Drawable]
     * - [Bitmap]
     * - [ByteArray]
     * - [ByteBuffer]
     *
     * @param asset The asset to load.
     * @return A transformed asset to be used for the image loading request.
     */
    public fun transform(asset: Any): Any
}

/**
 * Default implementation of [ImageAssetTransformer] that doesn't provide any headers.
 *
 * @deprecated Use [io.getstream.chat.android.client.cdn.CDN] instead. Configure a custom CDN via
 * [io.getstream.chat.android.client.ChatClient.Builder.cdn] to transform URLs for all image, file,
 * and download requests.
 */
@Deprecated("Use CDN instead. Configure via ChatClient.Builder.cdn().")
public object DefaultImageAssetTransformer : ImageAssetTransformer {
    override fun transform(asset: Any): Any = asset
}
