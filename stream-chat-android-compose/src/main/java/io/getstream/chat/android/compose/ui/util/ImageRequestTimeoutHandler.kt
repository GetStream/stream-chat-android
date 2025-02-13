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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import coil.compose.AsyncImagePainter
import io.getstream.chat.android.models.ConnectionState
import java.net.SocketTimeoutException

/**
 * Triggers [onTimeout] when the connection is established and the image load has failed due to a timeout.
 *
 * @param data The data to load.
 * @param connectionState The state of the network connection.
 * @param imageState The state of the async image painter.
 * @param onTimeout The lambda function called when the timeout conditions have been met.
 */
@Composable
internal fun ImageRequestTimeoutHandler(
    data: Any?,
    connectionState: ConnectionState,
    imageState: AsyncImagePainter.State,
    onTimeout: () -> Unit,
) {
    LaunchedEffect(data, connectionState, imageState) {
        if (data != null &&
            connectionState is ConnectionState.Connected &&
            imageState is AsyncImagePainter.State.Error
        ) {
            if (imageState.result.throwable is SocketTimeoutException) {
                onTimeout()
            }
        }
    }
}
