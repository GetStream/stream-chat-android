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

package io.getstream.chat.android.uitests.snapshot.compose

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.karumi.shot.ScreenshotTest
import io.getstream.chat.android.compose.ui.components.reactionoptions.ReactionOptions
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import org.junit.Rule
import org.junit.Test

class ReactionOptionsTest : ScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun reactionOptions() {
        composeRule.setContent {
            ChatTheme {
                ReactionOptions(
                    ownReactions = emptyList(),
                    onReactionOptionSelected = {},
                    onShowMoreReactionsSelected = { }
                )
            }
        }

        compareScreenshot(composeRule.onRoot())
    }
}
