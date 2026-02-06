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

package io.getstream.chat.android.ui.helper

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.state.GlobalState
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.User

/**
 * Provides the currently logged in user.
 */
@InternalStreamChatApi
public fun interface CurrentUserProvider {

    /**
     * Returns the currently logged in user.
     *
     *  @return The currently logged in user.
     */
    public fun getCurrentUser(): User?

    public companion object {
        /**
         * Builds the default current user provider.
         */
        public fun defaultCurrentUserProvider(): CurrentUserProvider {
            return DefaultCurrentUserProvider()
        }
    }
}

/**
 * The default implementation of [CurrentUserProvider] that returns a user
 * from [GlobalState] object.
 */
private class DefaultCurrentUserProvider : CurrentUserProvider {

    /**
     * Returns the currently logged in user.
     *
     *  @return The currently logged in user.
     */
    override fun getCurrentUser(): User? {
        return ChatClient.instance().getCurrentUser()
    }
}
