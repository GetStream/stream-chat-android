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

package io.getstream.chat.android.compose.ui.messages.scheduled

import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.ScheduledMessagesBannerParams
import io.getstream.chat.android.ui.common.state.messages.composer.ScheduledMessagesBannerState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.util.Date

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
internal class ScheduledMessagesBannerScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `the banner announces its changes politely and opens the scheduled messages`() {
        var clicks = 0
        composeTestRule.setContent {
            ChatTheme {
                ScheduledMessagesBanner(state = ScheduledMessagesBannerState.Failed(2), onClick = { clicks++ })
            }
        }

        composeTestRule.onNodeWithTag(BannerTag)
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite))
            .assert(SemanticsMatcher.keyIsDefined(SemanticsActions.OnClick))
            .performClick()
        composeTestRule.onNodeWithText("2 failed to send", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("View all", useUnmergedTree = true).assertExists()
        assertEquals(1, clicks)
    }

    @Test
    fun `the default component factory shows the banner and passes the click through`() {
        var clicks = 0
        composeTestRule.setContent {
            ChatTheme {
                ChatTheme.componentFactory.ScheduledMessagesBanner(
                    params = ScheduledMessagesBannerParams(
                        state = ScheduledMessagesBannerState.Scheduled(
                            count = 3,
                            nextScheduledAt = Date(),
                        ),
                        onClick = { clicks++ },
                    ),
                )
            }
        }

        composeTestRule.onNodeWithTag(BannerTag).performClick()
        composeTestRule.onNodeWithText("3 scheduled messages", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("View all", useUnmergedTree = true).assertExists()
        assertEquals(1, clicks)
    }

    @Test
    fun `the banner without a click action has no View link`() {
        composeTestRule.setContent {
            ChatTheme {
                ScheduledMessagesBanner(
                    state = ScheduledMessagesBannerState.LimitReached(count = 10, limit = 10),
                    onClick = null,
                )
            }
        }

        composeTestRule.onNodeWithTag(BannerTag).assertHasNoClickAction()
        composeTestRule.onNodeWithText("10 of 10 scheduled messages", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("View all", useUnmergedTree = true).assertDoesNotExist()
    }
}

private const val BannerTag = "Stream_ScheduledMessagesBanner"
