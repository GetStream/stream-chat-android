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

package io.getstream.chat.android.client.setup.state

import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.models.InitializationState

/**
 * Mutable version of [ClientState]. The class makes possible to change state of the SDK. Should only be used
 * internally by the SDK.
 */
internal interface ClientMutableState : ClientState {

    /**
     * Sets the [ConnectionState]
     *
     * @param connectionState [ConnectionState]
     */
    fun setConnectionState(connectionState: ConnectionState)

    /**
     * Sets initialized
     *
     * @param state [InitializationState]
     */
    fun setInitializationState(state: InitializationState)

    /**
     * Clears the state of [ClientMutableState].
     */
    public fun clearState()
}
