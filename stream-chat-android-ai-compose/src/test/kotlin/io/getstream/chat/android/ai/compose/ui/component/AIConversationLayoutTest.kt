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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class AIConversationLayoutTest {

    @get:Rule
    val rule = createComposeRule()

    private data class Message(val id: String, val isFromUser: Boolean, val height: Dp = 40.dp)

    private fun question(id: String) = Message(id, isFromUser = true)
    private fun reply(id: String, height: Dp = 40.dp) = Message(id, isFromUser = false, height = height)

    private var messages by mutableStateOf(emptyList<Message>())
    private var isThinking by mutableStateOf(false)
    private var loadOlderMessages: (suspend () -> Unit)? by mutableStateOf(null)
    private var contentPadding by mutableStateOf(PaddingValues())

    private fun setContent() {
        rule.setContent { Conversation() }
    }

    /** A composer floating over the list, its height passed as padding once it is laid out. */
    private fun setContentWithFloatingComposer(height: () -> Dp) {
        rule.setContent {
            var composerHeight by remember { mutableStateOf(0.dp) }
            val density = LocalDensity.current
            Box(modifier = Modifier.size(width = 300.dp, height = ListHeight)) {
                Conversation(contentPadding = PaddingValues(bottom = composerHeight))
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(height())
                        .onSizeChanged { composerHeight = with(density) { it.height.toDp() } },
                )
            }
        }
    }

    @Composable
    private fun Conversation(contentPadding: PaddingValues = this.contentPadding) {
        Box(modifier = Modifier.size(width = 300.dp, height = ListHeight)) {
            AIConversation(
                messages = messages,
                key = Message::id,
                isFromUser = Message::isFromUser,
                isThinking = isThinking,
                loadOlderMessages = loadOlderMessages,
                footer = { Text("footer") },
                emptyContent = { Text("How can I help?") },
                contentPadding = contentPadding,
            ) { message ->
                Text(text = message.id, modifier = Modifier.height(message.height))
            }
        }
    }

    private fun top(text: String): Dp = rule.onNodeWithText(text).getUnclippedBoundsInRoot().top

    private fun scrollToStart() {
        rule.onNode(hasScrollToIndexAction()).performScrollToIndex(0)
        rule.waitForIdle()
    }

    private fun assertNear(expected: Dp, actual: Dp) {
        assertEquals(expected.value, actual.value, 1f)
    }

    @Test
    fun `no messages show the empty content in the middle of the list`() {
        setContent()

        val bounds = rule.onNodeWithText("How can I help?").getUnclippedBoundsInRoot()
        assertNear(ListHeight / 2, (bounds.top + bounds.bottom) / 2)
        rule.onAllNodesWithText("footer").assertCountEquals(0)
    }

    @Test
    fun `the latest turn ends with the thinking indicator and the footer`() {
        messages = listOf(question("q1"), reply("a1"), question("q2"))
        isThinking = true
        setContent()
        // Shows the start, where the earlier turn and the top of the latest one both fit.
        scrollToStart()

        rule.onAllNodesWithText("Thinking", substring = true, useUnmergedTree = true).assertCountEquals(1)
        rule.onAllNodesWithText("footer").assertCountEquals(1)
        assertTrue(top("Thinking", substring = true) > top("q2"))
        assertTrue(top("footer") > top("q2"))

        isThinking = false
        rule.waitForIdle()
        rule.onAllNodesWithText("Thinking", substring = true, useUnmergedTree = true).assertCountEquals(0)
    }

    @Test
    fun `the latest turn is at least as tall as the list and the list opens at its end`() {
        messages = listOf(question("q1"), reply("a1"), question("q2"), reply("a2"))
        setContent()

        // A short latest turn fills the list, so at the end its question is at the top.
        assertNear(0.dp, top("q2"))
        rule.onAllNodesWithText("q1").assertCountEquals(0)
    }

    @Test
    fun `a question the person sends goes to the top and its reply grows below it`() {
        messages = listOf(question("q1"), reply("a1"))
        setContent()

        messages = messages + question("q2")
        rule.waitForIdle()
        // The turn's top is at the top of the list, and its question just under the turn's padding.
        assertNear(TurnTopPadding, top("q2"))

        messages = messages + reply("a2", height = ListHeight * 2)
        rule.waitForIdle()
        assertNear(TurnTopPadding, top("q2"))
    }

    @Test
    fun `older messages load once the spinner is in view, and again for each first turn`() {
        var loads = 0
        loadOlderMessages = { loads++ }
        messages = listOf(question("q1"), reply("a1"), question("q2"), reply("a2"))
        setContent()
        rule.mainClock.advanceTimeBy(LoadDelayMillis)
        assertEquals("the spinner is out of view at the end", 0, loads)

        scrollToStart()
        rule.mainClock.advanceTimeBy(LoadDelayMillis)
        assertEquals(1, loads)

        // The older turn is short, so the spinner stays in view and asks for more.
        messages = listOf(question("q0"), reply("a0")) + messages
        rule.waitForIdle()
        scrollToStart()
        rule.mainClock.advanceTimeBy(LoadDelayMillis)
        assertEquals(2, loads)
    }

    @Test
    fun `older messages arriving above keep what is on screen in place`() {
        messages = listOf(question("q1"), reply("a1"), question("q2"), reply("a2"))
        setContent()
        val shown = top("a2")

        messages = listOf(question("q0"), reply("a0")) + messages
        rule.waitForIdle()

        assertNear(shown, top("a2"))
    }

    @Test
    fun `older messages arriving under the spinner keep what is on screen in place`() {
        loadOlderMessages = {}
        messages = listOf(question("q1"), reply("a1"), question("q2"), reply("a2"))
        setContent()
        scrollToStart()
        val shown = top("q1")

        messages = listOf(question("q0"), reply("a0")) + messages
        rule.waitForIdle()
        assertNear(shown, top("q1"))

        // And when there is nothing older left, and the spinner goes away.
        loadOlderMessages = null
        rule.waitForIdle()
        assertNear(shown, top("q1"))
    }

    @Test
    fun `a button brings the end back into view`() {
        messages = listOf(question("q1"), reply("a1"), question("q2"), reply("a2"))
        setContent()
        rule.onAllNodesWithContentDescription(ScrollToLatest).assertCountEquals(0)

        scrollToStart()
        rule.onNodeWithContentDescription(ScrollToLatest).performClick()
        rule.waitForIdle()

        assertNear(0.dp, top("q2"))
        rule.onAllNodesWithContentDescription(ScrollToLatest).assertCountEquals(0)
    }

    @Test
    fun `older messages arriving above a reply that was first keep it in place`() {
        loadOlderMessages = {}
        messages = listOf(reply("r1"), question("q2"), reply("a2"))
        setContent()
        scrollToStart()
        val shown = top("r1")

        messages = listOf(question("q1")) + messages
        rule.waitForIdle()

        assertNear(shown, top("r1"))
    }

    @Test
    fun `older messages arriving above a latest turn with no question leave the list where it is`() {
        loadOlderMessages = {}
        messages = listOf(reply("r1"), reply("r2"))
        setContent()
        scrollToStart()
        val shown = top("r1")

        messages = listOf(question("q1")) + messages
        rule.waitForIdle()

        assertNear(shown, top("r1"))
    }

    @Test
    fun `a restored list stays where the reader left it`() {
        messages = listOf(question("q1"), reply("a1"), question("q2"), reply("a2"))
        val restoration = StateRestorationTester(rule)
        restoration.setContent { Conversation() }
        scrollToStart()
        val shown = top("q1")

        restoration.emulateSavedInstanceStateRestore()
        rule.waitForIdle()

        assertNear(shown, top("q1"))
    }

    @Test
    fun `with padding, the empty content is in the middle of what it leaves visible`() {
        contentPadding = PaddingValues(top = TopBarHeight, bottom = ComposerHeight)
        setContent()

        val bounds = rule.onNodeWithText("How can I help?").getUnclippedBoundsInRoot()
        assertNear((TopBarHeight + ListHeight - ComposerHeight) / 2, (bounds.top + bounds.bottom) / 2)
    }

    @Test
    fun `with padding, the list opens at its end with the latest turn under the top padding`() {
        contentPadding = PaddingValues(top = TopBarHeight, bottom = ComposerHeight)
        messages = listOf(question("q1"), reply("a1"), question("q2"), reply("a2"))
        setContent()

        // The latest turn fills what the padding leaves visible, so its question is just under it.
        assertNear(TopBarHeight, top("q2"))
        // The turn before scrolls under the top padding.
        assertTrue(bottom("q1") <= TopBarHeight)
        rule.onAllNodesWithContentDescription(ScrollToLatest).assertCountEquals(0)
    }

    @Test
    fun `with padding, a long reply ends clear of the bottom padding`() {
        contentPadding = PaddingValues(bottom = ComposerHeight)
        messages = listOf(question("q1"), reply("a1", height = ListHeight * 2))
        setContent()

        // The footer ends the turn, above the list's own spacing and the padding.
        assertNear(ListHeight - ComposerHeight - EndSpacing, bottom("footer"))
        rule.onAllNodesWithContentDescription(ScrollToLatest).assertCountEquals(0)
    }

    @Test
    fun `with padding, a question the person sends goes to the top of what is visible`() {
        contentPadding = PaddingValues(top = TopBarHeight, bottom = ComposerHeight)
        messages = listOf(question("q1"), reply("a1"))
        setContent()

        messages = messages + question("q2")
        rule.waitForIdle()
        assertNear(TopBarHeight + TurnTopPadding, top("q2"))

        // The reply has the rest of the visible list to grow into without moving anything.
        messages = messages + reply("a2", height = 120.dp)
        rule.waitForIdle()
        assertNear(TopBarHeight + TurnTopPadding, top("q2"))
        rule.onAllNodesWithContentDescription(ScrollToLatest).assertCountEquals(0)
    }

    @Test
    fun `with padding, the button shows above the bottom padding and brings the end back`() {
        contentPadding = PaddingValues(bottom = ComposerHeight)
        messages = listOf(question("q1"), reply("a1"), question("q2"), reply("a2"))
        setContent()

        scrollToStart()
        val button = rule.onNodeWithContentDescription(ScrollToLatest).getUnclippedBoundsInRoot()
        assertTrue(button.bottom <= ListHeight - ComposerHeight)

        rule.onNodeWithContentDescription(ScrollToLatest).performClick()
        rule.waitForIdle()

        assertNear(0.dp, top("q2"))
        rule.onAllNodesWithContentDescription(ScrollToLatest).assertCountEquals(0)
    }

    @Test
    fun `with padding, older messages load once the spinner is out from under the top padding`() {
        var loads = 0
        loadOlderMessages = { loads++ }
        contentPadding = PaddingValues(top = TopBarHeight)
        messages = listOf(question("q1"), reply("a1"), question("q2"), reply("a2"))
        setContent()

        // The first turn just under the top padding, and the spinner above it, under the padding.
        rule.onNode(hasScrollToIndexAction()).performScrollToIndex(1)
        rule.waitForIdle()
        rule.mainClock.advanceTimeBy(LoadDelayMillis)
        assertEquals("the spinner is covered", 0, loads)

        scrollToStart()
        rule.mainClock.advanceTimeBy(LoadDelayMillis)
        assertEquals(1, loads)
    }

    @Test
    fun `with a floating composer measured after the first frame, a long reply opens at its end`() {
        messages = listOf(question("q1"), reply("a1", height = ListHeight * 2))
        setContentWithFloatingComposer { ComposerHeight }

        assertNear(ListHeight - ComposerHeight - EndSpacing, bottom("footer"))
        rule.onAllNodesWithContentDescription(ScrollToLatest).assertCountEquals(0)
    }

    @Test
    fun `with padding, the end stays in view as the bottom padding grows and shrinks`() {
        var composerHeight by mutableStateOf(ComposerHeight)
        messages = listOf(question("q1"), reply("a1", height = ListHeight * 2))
        setContentWithFloatingComposer { composerHeight }

        composerHeight = ComposerHeight * 2
        rule.waitForIdle()
        assertNear(ListHeight - ComposerHeight * 2 - EndSpacing, bottom("footer"))
        rule.onAllNodesWithContentDescription(ScrollToLatest).assertCountEquals(0)

        composerHeight = ComposerHeight
        rule.waitForIdle()
        assertNear(ListHeight - ComposerHeight - EndSpacing, bottom("footer"))
        rule.onAllNodesWithContentDescription(ScrollToLatest).assertCountEquals(0)
    }

    @Test
    fun `with padding, a person who scrolled up stays where they are as the padding changes`() {
        contentPadding = PaddingValues(bottom = ComposerHeight)
        messages = listOf(question("q1"), reply("a1"), question("q2"), reply("a2", height = ListHeight * 2))
        setContent()
        scrollToStart()
        val shown = top("q1")

        contentPadding = PaddingValues(bottom = ComposerHeight * 2)
        rule.waitForIdle()
        assertNear(shown, top("q1"))

        contentPadding = PaddingValues()
        rule.waitForIdle()
        assertNear(shown, top("q1"))
        rule.onNodeWithContentDescription(ScrollToLatest).assertExists()
    }

    @Test
    fun `with padding, a question the person sends stays at the top as the padding changes`() {
        contentPadding = PaddingValues(bottom = ComposerHeight * 2)
        messages = listOf(question("q1"), reply("a1"))
        setContent()

        // The composer shrinks back as the question is sent, in the same frame.
        messages = messages + question("q2")
        contentPadding = PaddingValues(bottom = ComposerHeight)
        rule.waitForIdle()
        assertNear(TurnTopPadding, top("q2"))

        contentPadding = PaddingValues(bottom = ComposerHeight * 2)
        rule.waitForIdle()
        assertNear(TurnTopPadding, top("q2"))

        contentPadding = PaddingValues(bottom = ComposerHeight)
        rule.waitForIdle()
        assertNear(TurnTopPadding, top("q2"))
    }

    private fun bottom(text: String): Dp = rule.onNodeWithText(text).getUnclippedBoundsInRoot().bottom

    private fun top(text: String, substring: Boolean): Dp =
        rule.onNodeWithText(text, substring = substring, useUnmergedTree = true).getUnclippedBoundsInRoot().top
}

private val ListHeight = 400.dp
private val TurnTopPadding = 16.dp
private val EndSpacing = 16.dp
private val TopBarHeight = 56.dp
private val ComposerHeight = 80.dp
private const val LoadDelayMillis = 350L
private const val ScrollToLatest = "Scroll to latest"
