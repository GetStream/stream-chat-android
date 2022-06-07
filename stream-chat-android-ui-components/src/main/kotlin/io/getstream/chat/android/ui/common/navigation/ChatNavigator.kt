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

package io.getstream.chat.android.ui.common.navigation

import com.getstream.sdk.chat.navigation.ChatNavigationHandler
import com.getstream.sdk.chat.navigation.destinations.ChatDestination

public class ChatNavigator(private val handler: ChatNavigationHandler = EMPTY_HANDLER) {
    public fun navigate(destination: ChatDestination) {
        val handled = handler.navigate(destination)
        if (!handled) {
            performDefaultNavigation(destination)
        }
    }

    private fun performDefaultNavigation(destination: ChatDestination) {
        destination.navigate()
    }

    public companion object {
        @JvmField
        public val EMPTY_HANDLER: ChatNavigationHandler = object : ChatNavigationHandler {
            override fun navigate(destination: ChatDestination) = false
        }
    }
}
