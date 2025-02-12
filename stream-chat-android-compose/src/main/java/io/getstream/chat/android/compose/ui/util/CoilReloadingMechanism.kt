/*
 * Copyright (c) 2014-2023 Stream.io Inc. All rights reserved.
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

import coil.compose.AsyncImagePainter
import coil.network.HttpException
import com.skydoves.landscapist.coil.CoilImageState
import io.getstream.chat.android.models.ConnectionState
import java.net.SocketTimeoutException

/**
 * Used to automatically reload the image once all conditions are satisfied, such as
 * internet connection regained and the painter being in an error state.
 *
 * @param data The data containing the image.
 * @param connectionState The state of the network connection
 * @param coilImageState The state of the async image painter
 * @param onReload The lambda function called when the conditions have been met and the image needs to be reloaded.
 */
internal fun onImageNeedsToReload(
    data: Any?,
    connectionState: ConnectionState,
    coilImageState: CoilImageState,
    onReload: () -> Unit,
) {
    if (data != null && connectionState is ConnectionState.Connected &&
        coilImageState is CoilImageState.Failure
    ) {
        val errorCode = (coilImageState.reason as? HttpException)?.response?.code

        if (errorCode == UnsatisfiableRequest) {
            onReload()
        }
    }
}

/**
 * Triggers [onReload] when the connection is established and the image load has failed due to a timeout.
 *
 * @param data The data to load.
 * @param connectionState The state of the network connection.
 * @param imageState The state of the async image painter.
 * @param onReload The lambda function called when the conditions to reload have been met.
 */
internal fun onImageNeedsToReload(
    data: Any?,
    connectionState: ConnectionState,
    imageState: AsyncImagePainter.State,
    onReload: () -> Unit,
) {
    if (data != null && connectionState is ConnectionState.Connected &&
        imageState is AsyncImagePainter.State.Error
    ) {
        if (imageState.result.throwable is SocketTimeoutException) {
            onReload()
        }
    }
}

/**
 * Represents the HTTP code thrown when the COIL image loader has timed out and is unable to fetch the
 * image from the web.
 */
private const val UnsatisfiableRequest = 504
