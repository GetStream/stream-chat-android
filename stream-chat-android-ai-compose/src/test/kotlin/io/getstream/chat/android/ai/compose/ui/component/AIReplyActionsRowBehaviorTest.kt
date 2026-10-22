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

package io.getstream.chat.android.ai.compose.ui.component

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.ai.compose.conversation.AIMessageRating
import io.getstream.chat.android.ai.compose.conversation.AIReplyActions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf

@RunWith(AndroidJUnit4::class)
internal class AIReplyActionsRowBehaviorTest {

    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `copy and share are always offered, regenerate and rating only when handled`() {
        compose.setContent { AIReplyActionsRow(text = TEXT, actions = AIReplyActions()) }

        compose.onNodeWithContentDescription("Copy").assertIsDisplayed()
        compose.onNodeWithContentDescription("Share").assertIsDisplayed()
        compose.onNodeWithContentDescription("Regenerate").assertDoesNotExist()
        compose.onNodeWithContentDescription("Good response").assertDoesNotExist()
        compose.onNodeWithContentDescription("Bad response").assertDoesNotExist()
    }

    @Test
    fun `regenerate calls back`() {
        var regenerated = 0
        compose.setContent {
            AIReplyActionsRow(text = TEXT, actions = AIReplyActions(onRegenerate = { regenerated++ }))
        }

        compose.onNodeWithContentDescription("Regenerate").performClick()

        assertEquals(1, regenerated)
        compose.onNodeWithContentDescription("Good response").assertDoesNotExist()
    }

    @Test
    fun `rating picks a thumb, switches to the other and takes it back`() {
        val rates = mutableListOf<AIMessageRating?>()
        var rating by mutableStateOf<AIMessageRating?>(null)
        compose.setContent {
            AIReplyActionsRow(
                text = TEXT,
                actions = AIReplyActions(
                    rating = rating,
                    onRate = {
                        rates += it
                        rating = it
                    },
                ),
            )
        }
        compose.onNodeWithContentDescription("Regenerate").assertDoesNotExist()
        compose.onNodeWithContentDescription("Good response").assert(isSelected(false))
        compose.onNodeWithContentDescription("Bad response").assert(isSelected(false))

        compose.onNodeWithContentDescription("Good response").performClick()
        compose.onNodeWithContentDescription("Good response").assert(isSelected(true))
        compose.onNodeWithContentDescription("Bad response").assert(isSelected(false))

        compose.onNodeWithContentDescription("Bad response").performClick()
        compose.onNodeWithContentDescription("Good response").assert(isSelected(false))
        compose.onNodeWithContentDescription("Bad response").assert(isSelected(true))

        compose.onNodeWithContentDescription("Bad response").performClick()
        compose.onNodeWithContentDescription("Bad response").assert(isSelected(false))

        assertEquals(listOf(AIMessageRating.Positive, AIMessageRating.Negative, null), rates)
    }

    @Test
    fun `copy puts the text on the clipboard and shows it was copied for a moment`() {
        compose.mainClock.autoAdvance = false
        compose.setContent { AIReplyActionsRow(text = TEXT, actions = AIReplyActions()) }

        compose.onNodeWithContentDescription("Copy").performClick()
        // The tap and its recomposition take a frame or two to land.
        var frames = 0
        while (!isShown("Copied") && frames++ < MAX_FRAMES) compose.mainClock.advanceTimeByFrame()

        val clipboard = compose.activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        assertEquals(TEXT, clipboard.primaryClip?.getItemAt(0)?.text?.toString())
        compose.onNodeWithContentDescription("Copied").assertIsDisplayed()
        compose.onNodeWithContentDescription("Copy").assertDoesNotExist()

        compose.mainClock.advanceTimeBy(1_000)
        compose.onNodeWithContentDescription("Copied").assertIsDisplayed()

        compose.mainClock.advanceTimeBy(600)
        compose.onNodeWithContentDescription("Copy").assertIsDisplayed()
        compose.onNodeWithContentDescription("Copied").assertDoesNotExist()
    }

    @Test
    fun `share opens the share sheet with the text`() {
        compose.setContent { AIReplyActionsRow(text = TEXT, actions = AIReplyActions()) }

        compose.onNodeWithContentDescription("Share").performClick()

        val started = shadowOf(compose.activity).nextStartedActivity
        assertEquals(Intent.ACTION_CHOOSER, started.action)
        @Suppress("DEPRECATION")
        val send = started.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)!!
        assertEquals(Intent.ACTION_SEND, send.action)
        assertEquals("text/plain", send.type)
        assertEquals(TEXT, send.getStringExtra(Intent.EXTRA_TEXT))
        assertNull(shadowOf(compose.activity).nextStartedActivity)
    }

    private fun isShown(description: String) =
        compose.onAllNodesWithContentDescription(description).fetchSemanticsNodes().isNotEmpty()

    private fun isSelected(selected: Boolean) = SemanticsMatcher.expectValue(SemanticsProperties.Selected, selected)

    private companion object {
        const val TEXT = "Here is the weather for today."
        const val MAX_FRAMES = 10
    }
}
