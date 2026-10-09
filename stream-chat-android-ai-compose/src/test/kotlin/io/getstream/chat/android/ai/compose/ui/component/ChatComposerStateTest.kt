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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextInputSelection
import androidx.compose.ui.text.TextRange
import androidx.core.net.toUri
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class ChatComposerStateTest {

    @get:Rule
    val rule = createComposeRule()

    private val textField get() = rule.onNode(hasSetTextAction())

    private val fieldText: String
        get() = textField.fetchSemanticsNode().config[SemanticsProperties.EditableText].text

    @Test
    fun `stateful composer clears the textField after a send`() {
        var sent: MessageData? = null
        rule.setContent {
            ChatComposer(onSendClick = { sent = it }, onStopClick = {}, isGenerating = false)
        }

        textField.performTextInput("hi")
        textField.performImeAction()

        rule.runOnIdle {
            assertEquals("hi", sent?.text)
            assertEquals("", fieldText)
        }
    }

    @Test
    fun `hoisted composer keeps the text when the caller restores it in onSendClick`() {
        var message by mutableStateOf(MessageData())
        rule.setContent {
            ChatComposer(
                messageData = message,
                onMessageDataChange = { message = it },
                onSendClick = { sent -> message = sent },
                onStopClick = {},
                isGenerating = false,
            )
        }

        textField.performTextInput("hi")
        textField.performImeAction()

        rule.runOnIdle {
            assertEquals("hi", message.text)
            assertEquals("hi", fieldText)
        }
    }

    @Test
    fun `hoisted composer shows a message the caller restores after the send`() {
        var message by mutableStateOf(MessageData())
        var sent: MessageData? = null
        rule.setContent {
            ChatComposer(
                messageData = message,
                onMessageDataChange = { message = it },
                onSendClick = { sent = it },
                onStopClick = {},
                isGenerating = false,
            )
        }

        textField.performTextInput("hi")
        textField.performImeAction()
        rule.runOnIdle { assertEquals("", fieldText) }
        rule.runOnIdle { message = requireNotNull(sent) }

        rule.runOnIdle { assertEquals("hi", fieldText) }
    }

    @Test
    fun `typing after text set from outside appends it at the end`() {
        var message by mutableStateOf(MessageData())
        rule.setContent {
            ChatComposer(
                messageData = message,
                onMessageDataChange = { message = it },
                onSendClick = {},
                onStopClick = {},
                isGenerating = false,
            )
        }

        textField.performTextInput("abc")
        rule.runOnIdle { message = message.copy(text = "Build me a short presentation about ") }
        textField.performTextInput("cats")

        rule.runOnIdle { assertEquals("Build me a short presentation about cats", message.text) }
    }

    @Test
    fun `typing keeps the cursor where the person put it`() {
        var message by mutableStateOf(MessageData())
        rule.setContent {
            ChatComposer(
                messageData = message,
                onMessageDataChange = { message = it },
                onSendClick = {},
                onStopClick = {},
                isGenerating = false,
            )
        }

        textField.performTextInput("ac")
        textField.performTextInputSelection(TextRange(1))
        textField.performTextInput("b")

        rule.runOnIdle { assertEquals("abc", message.text) }
    }

    @Test
    fun `stateful composer puts the cursor in the field through its focus requester`() {
        val focusRequester = FocusRequester()
        rule.setContent {
            ChatComposer(
                onSendClick = {},
                onStopClick = {},
                isGenerating = false,
                focusRequester = focusRequester,
            )
        }

        rule.runOnIdle { focusRequester.requestFocus() }

        textField.assertIsFocused()
    }

    @Test
    fun `saver restores the text and attachments`() {
        val saver: Saver<MessageData, Any> = MessageData.Saver
        val message = MessageData(text = "hi", attachments = setOf("content://media/1".toUri()))

        val saved = with(saver) { SaverScope { true }.save(message) }

        assertEquals(message, saver.restore(requireNotNull(saved)))
    }
}
