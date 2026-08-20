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

package io.getstream.chat.android.compose.ui.components

import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsPropertiesAndroid
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
internal class FullscreenDialogTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `renders the content inside the dialog window`() {
        composeTestRule.setContent {
            FullscreenDialog(onDismissRequest = {}) {
                Text(
                    text = "dialog content",
                    modifier = Modifier.testTag("DialogContent"),
                )
            }
        }

        composeTestRule.onNodeWithTag("DialogContent").assertIsDisplayed()
    }

    @Test
    fun `exposes test tags inside the dialog window as resource ids`() {
        composeTestRule.setContent {
            FullscreenDialog(onDismissRequest = {}) {
                Text(
                    text = "dialog content",
                    modifier = Modifier.testTag("DialogContent"),
                )
            }
        }

        // The dialog window has its own composition, so the app-level flag does not reach it;
        // an ancestor inside the dialog has to carry `testTagsAsResourceId` for UiAutomator to
        // resolve the tags below it as resource ids.
        composeTestRule.onNodeWithTag("DialogContent").assert(
            hasAnyAncestor(SemanticsMatcher.expectValue(SemanticsPropertiesAndroid.TestTagsAsResourceId, true)),
        )
    }
}
