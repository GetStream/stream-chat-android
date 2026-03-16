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

package io.getstream.chat.android.uitests.snapshot.compose.components

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.karumi.shot.ScreenshotTest
import io.getstream.chat.android.compose.ui.channels.info.SelectedChannelMenu
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.ui.common.model.UserPresence
import io.getstream.chat.android.ui.common.state.channels.actions.LeaveGroup
import io.getstream.chat.android.ui.common.state.channels.actions.ViewInfo
import io.getstream.chat.android.uitests.util.FakeImageLoader
import io.getstream.chat.android.uitests.util.TestData
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class DocsUserPresenceTest : ScreenshotTest {

    private val context: Context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @get:Rule
    val composeRule = createComposeRule()

    private fun presenceChannel(): Channel {
        return Channel().copy(
            type = "messaging",
            id = "presence_test",
            members = listOf(
                Member(user = TestData.alex().copy(online = true)),
                Member(user = TestData.elena().copy(online = true)),
                Member(user = TestData.sarah().copy(online = true)),
            ),
            memberCount = 3,
        )
    }

    private fun screenshotWithPresence(
        showOnlineIndicator: Boolean,
        countAsOnlineMember: Boolean,
        name: String,
    ) {
        val channel = presenceChannel()
        val presence = UserPresence(
            currentUser = UserPresence.DisplayOptions(
                showOnlineIndicator = showOnlineIndicator,
                countAsOnlineMember = countAsOnlineMember,
            ),
            otherUsers = UserPresence.DisplayOptions(
                showOnlineIndicator = showOnlineIndicator,
                countAsOnlineMember = countAsOnlineMember,
            ),
        )
        composeRule.setContent {
            ChatTheme(
                userPresence = presence,
                imageLoaderFactory = { FakeImageLoader(context) },
            ) {
                Box {
                    SelectedChannelMenu(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .align(Alignment.BottomCenter),
                        selectedChannel = channel,
                        currentUser = TestData.alex(),
                        channelActions = listOf(
                            ViewInfo(channel = channel, label = "View info", onAction = {}),
                            LeaveGroup(channel = channel, label = "Leave group", onAction = {}),
                        ),
                        overlayColor = Color.Transparent,
                        onChannelOptionConfirm = {},
                        onDismiss = {},
                    )
                }
            }
        }
        compareScreenshot(rule = composeRule, name = name)
    }

    @Test
    fun indicatorTrue_countFalse() {
        screenshotWithPresence(
            showOnlineIndicator = true,
            countAsOnlineMember = false,
            name = "user_presence_showonlineindicator_true_countasonlineuser_false",
        )
    }

    @Test
    fun indicatorTrue_countTrue() {
        screenshotWithPresence(
            showOnlineIndicator = true,
            countAsOnlineMember = true,
            name = "user_presence_showonlineindicator_true_countasonlineuser_true",
        )
    }

    @Test
    fun indicatorFalse_countFalse() {
        screenshotWithPresence(
            showOnlineIndicator = false,
            countAsOnlineMember = false,
            name = "user_presence_showonlineindicator_false_countasonlineuser_false",
        )
    }

    @Test
    fun indicatorFalse_countTrue() {
        screenshotWithPresence(
            showOnlineIndicator = false,
            countAsOnlineMember = true,
            name = "user_presence_showonlineindicator_false_countasonlineuser_true",
        )
    }
}
