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

@file:OptIn(ExperimentalComposeUiApi::class)

package io.getstream.chat.android.compose.sample.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import io.getstream.chat.android.compose.sample.ChatApp
import io.getstream.chat.android.compose.ui.theme.ChannelOptionsTheme
import io.getstream.chat.android.compose.ui.theme.ChatComponentFactory
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.ChatUiConfig

/**
 * Sample app wrapper around [ChatTheme] that enables test tags as resource IDs for UIAutomator
 * E2E tests and sets [ChatApp.dateFormatter] as defaults.
 */
@Composable
internal fun SampleChatTheme(
    config: ChatUiConfig = ChatUiConfig(),
    componentFactory: ChatComponentFactory = object : ChatComponentFactory {},
    channelOptionsTheme: ChannelOptionsTheme = ChannelOptionsTheme.defaultTheme(),
    content: @Composable () -> Unit,
) {
    Box(modifier = Modifier.semantics { testTagsAsResourceId = true }) {
        ChatTheme(
            dateFormatter = ChatApp.dateFormatter,
            config = config,
            componentFactory = componentFactory,
            channelOptionsTheme = channelOptionsTheme,
            content = content,
        )
    }
}
