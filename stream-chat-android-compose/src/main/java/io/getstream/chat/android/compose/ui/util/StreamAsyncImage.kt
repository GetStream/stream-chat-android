/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.ui.util

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.ui.common.helper.ImageAssetTransformer
import io.getstream.chat.android.ui.common.helper.ImageHeadersProvider

/**
 * Displays an image asynchronously using `Coil` and the [LocalStreamImageLoader].
 * It transforms the image URL and provides headers before loading the image.
 *
 * @param data The data to load the image from. Can be a URL, URI, resource ID, etc.
 * @param modifier Modifier for styling.
 * @param contentScale The scale to be used for the content.
 * @param content A composable function that defines the content to be displayed based on the image loading state.
 *
 * @see ImageAssetTransformer
 * @see ImageHeadersProvider
 */
@Composable
internal fun StreamAsyncImage(
    data: Any?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    content: @Composable BoxScope.(state: AsyncImagePainter.State) -> Unit,
) {
    StreamAsyncImage(
        imageRequest = ImageRequest.Builder(LocalContext.current)
            .data(data)
            .build(),
        modifier = modifier,
        contentScale = contentScale,
        content = content,
    )
}

/**
 * Displays an image asynchronously using `Coil` and the [LocalStreamImageLoader].
 * It transforms the image URL and provides headers before loading the image.
 *
 * @see ImageAssetTransformer
 * @see ImageHeadersProvider
 *
 * @param imageRequest The request to load the image.
 * @param modifier Modifier for styling.
 * @param contentScale The scale to be used for the content.
 * @param content A composable function that defines the content to be displayed based on the image loading state.
 */
@Composable
internal fun StreamAsyncImage(
    imageRequest: ImageRequest,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    content: @Composable BoxScope.(state: AsyncImagePainter.State) -> Unit,
) {
    var imageSize by remember { mutableStateOf(Size.ORIGINAL) }
    Box(
        modifier = modifier.onSizeChanged { size -> imageSize = Size(size.width, size.height) },
    ) {
        if (imageSize == Size.ORIGINAL) {
            content(AsyncImagePainter.State.Empty)
        } else {
            val context = LocalContext.current
            val imageAssetTransformer = ChatTheme.streamImageAssetTransformer
            val imageHeaderProvider = ChatTheme.streamImageHeadersProvider
            val asyncImagePainter = rememberAsyncImagePainter(
                model = imageRequest
                    .convertUrl(context, imageAssetTransformer)
                    .provideHeaders(context, imageHeaderProvider)
                    .size(context, imageSize),
                imageLoader = LocalStreamImageLoader.current,
                contentScale = contentScale,
            )
            content(asyncImagePainter.state)
        }
    }
}
