/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.pages

import androidx.test.uiautomator.By
import io.getstream.chat.android.models.ConnectionState

class JwtPage {

    companion object {
        val connectionButton = By.res("Stream_JWT_ConnectionButton")
        val statusConnected = By.res("Stream_JWT_ConnectionStatus_${ConnectionState.Connected}")
        val statusOffline = By.res("Stream_JWT_HasBeenDisconnected_true")
    }
}
