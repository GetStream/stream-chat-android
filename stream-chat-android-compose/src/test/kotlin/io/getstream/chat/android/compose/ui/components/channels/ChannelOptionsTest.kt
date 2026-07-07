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

package io.getstream.chat.android.compose.ui.components.channels

import androidx.compose.runtime.Composable
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
import io.getstream.chat.android.compose.ui.PIXEL_2_HDPI
import io.getstream.chat.android.compose.ui.PaparazziComposeTest
import io.getstream.chat.android.compose.ui.theme.ChannelListConfig
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.ChatUiConfig
import io.getstream.chat.android.compose.viewmodel.channels.ChannelListViewModel
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.previewdata.PreviewChannelData
import io.getstream.chat.android.previewdata.PreviewUserData
import io.getstream.chat.android.ui.common.state.channels.actions.DeleteConversation
import io.getstream.chat.android.ui.common.state.channels.actions.MuteChannel
import io.getstream.chat.android.ui.common.state.channels.actions.ViewInfo
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.util.Date

internal class ChannelOptionsTest : PaparazziComposeTest {

    @get:Rule
    override val paparazzi = Paparazzi(
        deviceConfig = PIXEL_2_HDPI,
        renderingMode = SessionParams.RenderingMode.SHRINK,
    )

    @Test
    fun `channel options`() {
        val channel = PreviewChannelData.channelWithMessages
        snapshotWithDarkMode {
            ChannelOptions(
                actions = listOf(
                    ViewInfo(
                        channel = channel,
                        label = "Channel Info",
                        onAction = {},
                    ),
                    MuteChannel(
                        channel = channel,
                        label = "Mute Channel",
                        onAction = {},
                    ),
                    DeleteConversation(
                        channel = channel,
                        label = "Delete Conversation",
                        onAction = {},
                    ),
                ),
                onChannelOptionConfirm = {},
            )
        }
    }

    @Test
    fun `default dm channel actions`() {
        val viewModel = mockChannelListViewModel()
        snapshotWithDarkMode {
            DefaultChannelOptions(
                channel = DmChannel,
                ownCapabilities = setOf(ChannelCapabilities.DELETE_CHANNEL),
                viewModel = viewModel,
            )
        }
    }

    @Test
    fun `default dm channel actions for muted and blocked counterpart`() {
        val viewModel = mockChannelListViewModel(isUserMuted = true, isUserBlocked = true)
        snapshotWithDarkMode {
            DefaultChannelOptions(
                channel = DmChannel.copy(membership = PinnedAndArchivedMembership),
                ownCapabilities = setOf(ChannelCapabilities.DELETE_CHANNEL),
                viewModel = viewModel,
            )
        }
    }

    @Test
    fun `default group channel actions for owner`() {
        val viewModel = mockChannelListViewModel()
        snapshotWithDarkMode {
            DefaultChannelOptions(
                channel = PreviewChannelData.channelWithFewMembers,
                ownCapabilities = setOf(
                    ChannelCapabilities.LEAVE_CHANNEL,
                    ChannelCapabilities.DELETE_CHANNEL,
                ),
                viewModel = viewModel,
            )
        }
    }

    @Test
    fun `default group channel actions for member`() {
        val viewModel = mockChannelListViewModel()
        snapshotWithDarkMode {
            DefaultChannelOptions(
                channel = PreviewChannelData.channelWithFewMembers.copy(
                    membership = PinnedAndArchivedMembership,
                ),
                ownCapabilities = setOf(ChannelCapabilities.LEAVE_CHANNEL),
                viewModel = viewModel,
            )
        }
    }

    private fun mockChannelListViewModel(
        isUserMuted: Boolean = false,
        isUserBlocked: Boolean = false,
    ): ChannelListViewModel = mock {
        on { user } doReturn MutableStateFlow(PreviewUserData.user1)
        on { isUserMuted(any()) } doReturn isUserMuted
        on { isUserBlocked(any()) } doReturn isUserBlocked
    }

    @Composable
    private fun DefaultChannelOptions(
        channel: Channel,
        ownCapabilities: Set<String>,
        viewModel: ChannelListViewModel,
    ) {
        ChatTheme(
            colors = ChatTheme.colors,
            config = ChatUiConfig(
                channelList = ChannelListConfig(
                    optionsVisibility = ChannelOptionsVisibility(
                        isArchiveChannelVisible = true,
                        isPinChannelVisible = true,
                    ),
                ),
            ),
        ) {
            ChannelOptions(
                actions = buildDefaultChannelActions(
                    selectedChannel = channel,
                    ownCapabilities = ownCapabilities,
                    viewModel = viewModel,
                    onViewInfoAction = {},
                ),
                onChannelOptionConfirm = {},
            )
        }
    }
}

/**
 * A distinct 1-to-1 channel between the current user (user1) and user2.
 */
private val DmChannel = Channel(
    type = "messaging",
    id = "!members-dm",
    members = listOf(
        Member(user = PreviewUserData.user1),
        Member(user = PreviewUserData.user2),
    ),
    memberCount = 2,
)

private val PinnedAndArchivedMembership = Member(
    user = PreviewUserData.user1,
    pinnedAt = Date(0),
    archivedAt = Date(0),
)
