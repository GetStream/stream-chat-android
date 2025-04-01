/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.ui.messages

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import io.getstream.chat.android.compose.ui.SnapshotTest
import io.getstream.chat.android.compose.ui.components.messageoptions.defaultMessageOptionsState
import io.getstream.chat.android.compose.ui.components.selectedmessage.SelectedMessageMenu
import io.getstream.chat.android.compose.util.extensions.toSet
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.previewdata.PreviewMessageData
import io.getstream.chat.android.previewdata.PreviewUserData
import org.junit.Rule
import org.junit.Test

internal class SelectedMessageMenuTest : SnapshotTest {

    @get:Rule
    override val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_2)

    @Test
    fun `my message`() {
        runTest(
            currentUser = PreviewUserData.user1,
            selectedMessage = PreviewMessageData.message1.copy(
                user = PreviewUserData.user1,
            ),
        )
    }

    @Test
    fun `my message in dark mode`() {
        runTest(
            currentUser = PreviewUserData.user1,
            selectedMessage = PreviewMessageData.message1.copy(
                user = PreviewUserData.user1,
            ),
            inDarkMode = true,
        )
    }

    @Test
    fun `their message`() {
        runTest(
            currentUser = PreviewUserData.user1,
            selectedMessage = PreviewMessageData.message2.copy(
                user = PreviewUserData.user2,
            ),
        )
    }

    @Test
    fun `their message in dark mode`() {
        runTest(
            currentUser = PreviewUserData.user1,
            selectedMessage = PreviewMessageData.message2.copy(
                user = PreviewUserData.user2,
            ),
            inDarkMode = true,
        )
    }

    private fun runTest(
        currentUser: User,
        selectedMessage: Message,
        inDarkMode: Boolean = false,
    ) {
        snapshot(isInDarkMode = inDarkMode) {
            val channelCapabilities = ChannelCapabilities.toSet() - setOf(
                ChannelCapabilities.DELETE_ANY_MESSAGE,
                ChannelCapabilities.UPDATE_ANY_MESSAGE,
            )

            val messageOptions = defaultMessageOptionsState(
                selectedMessage = selectedMessage,
                currentUser = currentUser,
                isInThread = false,
                ownCapabilities = channelCapabilities,
            )

            Box(modifier = Modifier.fillMaxSize()) {
                SelectedMessageMenu(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    message = selectedMessage,
                    messageOptions = messageOptions,
                    onMessageAction = {},
                    onShowMoreReactionsSelected = {},
                    ownCapabilities = channelCapabilities,
                )
            }
        }
    }
}
