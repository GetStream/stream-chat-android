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

import io.getstream.chat.android.client.models.ConnectionState
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.core.internal.InternalStreamChatApi

/**
 * Mutable version of [ClientState]. The class makes possible to change state of the SDK. Should only be used
 * internally by the SDK.
 */
@InternalStreamChatApi
public interface ClientMutableState : ClientState {

    /**
     * Sets the [User]
     *
     * @param user [User]
     */
    public fun setUser(user: User)

    /**
     * Sets the [ConnectionState]
     *
     * @param connectionState [ConnectionState]
     */
    public fun setConnectionState(connectionState: ConnectionState)

    /**
     * Sets initialized
     *
     * @param initialized Boolean
     */
    public fun setInitialized(initialized: Boolean)
}
