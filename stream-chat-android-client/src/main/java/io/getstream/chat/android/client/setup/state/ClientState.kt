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

package io.getstream.chat.android.client.setup.state

import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.models.InitializationState
import io.getstream.chat.android.models.User
import kotlinx.coroutines.flow.StateFlow

/**
 * The current state of the SDK. With this class you can get the current user, the connection state, initialization
 * state...
 */
public interface ClientState {

    /**
     * The state of the initialization process of the SDK.
     */
    public val initializationState: StateFlow<InitializationState>

    /**
     * The current user if connected.
     */
    public val user: StateFlow<User?>

    /**
     * StateFlow<ConnectionState> that indicates if we are currently online, connecting of offline.
     */
    public val connectionState: StateFlow<ConnectionState>

    /**
     * If the WebSocket is connected.
     *
     * @return True if the WebSocket is connected, otherwise false.
     */
    public val isOnline: Boolean

    /**
     * If the WebSocket is disconnected.
     *
     * @return True if the WebSocket is disconnected, otherwise false.
     */
    public val isOffline: Boolean

    /**
     * If connection is in connecting state.
     *
     * @return True if the connection is in connecting state.
     */
    public val isConnecting: Boolean

    /**
     * If internet is available or not. This is not related to the connection of the SDK, it returns
     * if internet is available in the device.
     */
    public val isNetworkAvailable: Boolean
}
