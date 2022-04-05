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
 
package io.getstream.chat.android.client.setup

import androidx.annotation.VisibleForTesting
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.core.internal.InternalStreamChatApi

/**
 * Coordinates the initialization of the Chat SDK
 */
@InternalStreamChatApi
public class InitializationCoordinator private constructor() {

    private val userDisconnectedListeners: MutableList<(User?) -> Unit> = mutableListOf()
    private val userConnectedListeners: MutableList<(User) -> Unit> = mutableListOf()

    /**
     * Adds a listener to user connection.
     */
    public fun addUserConnectedListener(listener: (User) -> Unit) {
        userConnectedListeners.add(listener)
    }

    /**
     * Adds a listener to user disconnection.
     */
    public fun addUserDisconnectedListener(listener: (User?) -> Unit) {
        userDisconnectedListeners.add(listener)
    }

    /**
     * Notifies user connection
     */
    internal fun userConnected(user: User) {
        userConnectedListeners.forEach { function -> function.invoke(user) }
    }

    /**
     * Notifies user disconnection
     */
    internal fun userDisconnected(user: User?) {
        userDisconnectedListeners.forEach { function -> function.invoke(user) }
    }

    public companion object {
        private var instance: InitializationCoordinator? = null

        /**
         * Gets the initialization coordinator or creates it if necessary.
         */
        public fun getOrCreate(): InitializationCoordinator =
            instance ?: create().also { instance = it }

        @VisibleForTesting
        internal fun create(): InitializationCoordinator = InitializationCoordinator()
    }
}
