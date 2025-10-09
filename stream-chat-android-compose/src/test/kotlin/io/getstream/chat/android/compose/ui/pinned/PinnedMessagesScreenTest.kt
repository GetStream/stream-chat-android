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

package io.getstream.chat.android.compose.ui.pinned

import androidx.annotation.UiThread
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.client.test.MockedChatClientTest
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.pinned.PinnedMessageListViewModel
import io.getstream.chat.android.previewdata.PreviewPinnedMessageData
import io.getstream.chat.android.ui.common.model.MessageResult
import io.getstream.chat.android.ui.common.state.pinned.PinnedMessageListState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
internal class PinnedMessagesScreenTest : MockedChatClientTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    val mockViewModel: PinnedMessageListViewModel = mock {
        on { errorEvents } doReturn MutableSharedFlow()
    }

    @Test
    @UiThread
    fun `loading pinned messages`() {
        whenever(mockViewModel.state) doReturn MutableStateFlow(PinnedMessageListState())

        composeTestRule.setContent {
            ChatTheme {
                PinnedMessageList(
                    viewModel = mockViewModel,
                )
            }
        }

        composeTestRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
            .assertExists()
    }

    @Test
    @UiThread
    fun `empty pinned messages`() {
        whenever(mockViewModel.state) doReturn MutableStateFlow(
            PinnedMessageListState(
                isLoading = false,
            ),
        )

        composeTestRule.setContent {
            ChatTheme {
                PinnedMessageList(
                    viewModel = mockViewModel,
                )
            }
        }

        composeTestRule.onNodeWithText("No pinned messages").assertExists()
    }

    @Test
    @UiThread
    fun `loaded pinned messages`() {
        whenever(mockViewModel.state) doReturn MutableStateFlow(
            PinnedMessageListState(
                results = PreviewPinnedMessageData
                    .pinnedMessageList
                    .map { MessageResult(message = it, channel = null) },
                isLoading = false,
            ),
        )

        composeTestRule.setContent {
            ChatTheme {
                PinnedMessageList(
                    viewModel = mockViewModel,
                )
            }
        }

        composeTestRule.onNode(
            hasText("Some very long pinned message in the chat from a while ago.", substring = true),
        ).assertExists()

        composeTestRule.onNode(hasText("Important message pinned to the chat.", substring = true))
            .assertExists()
    }
}
