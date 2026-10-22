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
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.core.net.toUri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.ai.compose.conversation.AIMessageAttachment
import io.getstream.chat.android.ai.compose.conversation.AIMessageRole
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class AIUserMessageTest {

    @get:Rule
    val rule = createComposeRule()

    private val text = "What's the weather in Lisbon?"

    @Test
    fun `holding the message copies its text`() {
        rule.setContent { AIUserMessage(text = text) }

        rule.onNodeWithText(text).performTouchInput { longClick() }
        rule.onNodeWithText("Copy").performClick()
        rule.waitForIdle()

        val clipboard = ApplicationProvider.getApplicationContext<Context>()
            .getSystemService(ClipboardManager::class.java)
        assertEquals(text, clipboard.primaryClip?.getItemAt(0)?.text?.toString())
        rule.onNodeWithText("Copy").assertDoesNotExist()
    }

    @Test
    fun `the menu offers to edit only given onEdit`() {
        rule.setContent { AIUserMessage(text = text) }

        rule.onNodeWithText(text).performTouchInput { longClick() }

        rule.onNodeWithText("Copy").assertExists()
        rule.onNodeWithText("Edit").assertDoesNotExist()
    }

    @Test
    fun `edit calls onEdit`() {
        var edits = 0
        rule.setContent { AIUserMessage(text = text, onEdit = { edits++ }) }

        rule.onNodeWithText(text).performTouchInput { longClick() }
        rule.onNodeWithText("Edit").performClick()

        rule.runOnIdle { assertEquals(1, edits) }
        rule.onNodeWithText("Edit").assertDoesNotExist()
    }

    @Test
    fun `attachments show through the factory`() {
        val factory = RecordingAttachmentFactory()
        val attachments = listOf(
            AIMessageAttachment.image("https://example.com/photo.jpg".toUri()),
            AIMessageAttachment.file("https://example.com/itinerary.pdf".toUri()),
        )
        rule.setContent {
            CompositionLocalProvider(LocalChatAiComponentFactory provides factory) {
                AIUserMessage(text = "", attachments = attachments)
            }
        }

        rule.onNodeWithText("user: photo.jpg").assertExists()
        rule.onNodeWithText("user: itinerary.pdf").assertExists()
        rule.runOnIdle { assertEquals(attachments, factory.shown.map { it.attachment }) }
    }
}

/** Shows each attachment as its role and file name, and notes what it was asked to show. */
internal class RecordingAttachmentFactory : ChatAiComponentFactory {
    val shown = mutableListOf<AIConversationAttachmentParams>()

    @Composable
    override fun AIConversationAttachment(params: AIConversationAttachmentParams) {
        shown += params
        BasicText("${params.role}: ${params.attachment.uri?.lastPathSegment}")
    }

    fun roles(): Set<AIMessageRole> = shown.map { it.role }.toSet()
}
