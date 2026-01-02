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

package io.getstream.chat.android.ui.initializer

import android.content.Context
import androidx.startup.Initializer
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.header.VersionPrefixHeader
import io.getstream.chat.android.ui.ChatUI

/**
 * Jetpack Startup Initializer for Stream's Chat UI Components.
 */
public class ChatUIInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        ChatClient.VERSION_PREFIX_HEADER = VersionPrefixHeader.UiComponents
        ChatUI.appContext = context
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> = mutableListOf()
}
