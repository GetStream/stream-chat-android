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

package io.getstream.chat.android.ui.common.utils.extensions

import android.content.Context
import io.getstream.chat.android.randomOption
import io.getstream.chat.android.randomPoll
import io.getstream.chat.android.randomPollAnswer
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.randomVote
import io.getstream.chat.android.ui.common.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class PollExtensionsTest {

    private val context: Context = mock()

    @Test
    fun `getSubtitle should return single answer subtitle when maxVotesAllowed is 1`() {
        val poll = randomPoll(enforceUniqueVote = false, maxVotesAllowed = 1, closed = false)
        whenever(context.getString(R.string.stream_ui_poll_description_single_answer))
            .thenReturn("You can select only one answer.")

        val result = poll.getSubtitle(context)

        assertEquals("You can select only one answer.", result)
    }

    @Test
    fun `getSubtitle should return single answer subtitle when enforceUniqueVote is true and maxVotesAllowed is null`() {
        val poll = randomPoll(enforceUniqueVote = true, maxVotesAllowed = null, closed = false)
        whenever(context.getString(R.string.stream_ui_poll_description_single_answer))
            .thenReturn("You can select only one answer.")

        val result = poll.getSubtitle(context)

        assertEquals("You can select only one answer.", result)
    }

    @Test
    fun `getSubtitle should return single answer subtitle when enforceUniqueVote is true and maxVotesAllowed is 1`() {
        val poll = randomPoll(enforceUniqueVote = true, maxVotesAllowed = 1, closed = false)
        whenever(context.getString(R.string.stream_ui_poll_description_single_answer))
            .thenReturn("You can select only one answer.")

        val result = poll.getSubtitle(context)

        assertEquals("You can select only one answer.", result)
    }

    @Test
    fun `getSubtitle should return multiple answers subtitle when maxVotesAllowed is greater than 1`() {
        val poll = randomPoll(
            options = listOf(randomOption(), randomOption()),
            enforceUniqueVote = false,
            maxVotesAllowed = 2,
            closed = false,
        )
        whenever(context.getString(R.string.stream_ui_poll_description_multiple_answers, 2))
            .thenReturn("You can select up to 2 answers.")

        val result = poll.getSubtitle(context)

        assertEquals("You can select up to 2 answers.", result)
    }

    @Test
    fun `getSubtitle should return unlimited answers subtitle when the poll has no vote limit`() {
        val poll = randomPoll(
            options = listOf(randomOption(), randomOption()),
            enforceUniqueVote = false,
            maxVotesAllowed = null,
            closed = false,
        )
        whenever(context.getString(R.string.stream_ui_poll_description_unlimited_answers))
            .thenReturn("Select one or more.")

        val result = poll.getSubtitle(context)

        assertEquals("Select one or more.", result)
    }

    @Test
    fun `getSubtitle should respect the minimum of maxVotesAllowed and options size`() {
        val poll = randomPoll(enforceUniqueVote = false, maxVotesAllowed = 3, closed = false)
        whenever(context.getString(R.string.stream_ui_poll_description_single_answer))
            .thenReturn("You can select only one answer.")

        val result = poll.getSubtitle(context)

        assertEquals("You can select only one answer.", result)
    }

    @Test
    fun `getSubtitle should return closed subtitle when poll is closed`() {
        val poll = randomPoll(closed = true)
        whenever(context.getString(R.string.stream_ui_poll_description_closed))
            .thenReturn("This poll is closed.")

        val result = poll.getSubtitle(context)

        assertEquals("This poll is closed.", result)
    }

    @Test
    fun `canCastVote should return true when maxVotesAllowed is null`() {
        val poll = randomPoll(maxVotesAllowed = null, ownVotes = List(5) { randomVote() })

        assertTrue(poll.canCastVote())
    }

    @Test
    fun `canCastVote should return true when ownVotes size is below maxVotesAllowed`() {
        val poll = randomPoll(maxVotesAllowed = 3, ownVotes = listOf(randomVote(), randomVote()))

        assertTrue(poll.canCastVote())
    }

    @Test
    fun `canCastVote should return false when ownVotes size has reached maxVotesAllowed`() {
        val poll = randomPoll(maxVotesAllowed = 2, ownVotes = listOf(randomVote(), randomVote()))

        assertFalse(poll.canCastVote())
    }

    @Test
    fun `displayedName should return the translation in the user language`() {
        val poll = randomPoll(name = "Lunch?", nameI18n = mapOf("language" to "en", "it_text" to "Pranzo?"))

        assertEquals("Pranzo?", poll.displayedName(randomUser(language = "it"), autoTranslationEnabled = true))
    }

    @Test
    fun `displayedName should return the original when auto-translation is disabled`() {
        val poll = randomPoll(name = "Lunch?", nameI18n = mapOf("language" to "en", "it_text" to "Pranzo?"))

        assertEquals("Lunch?", poll.displayedName(randomUser(language = "it"), autoTranslationEnabled = false))
    }

    @Test
    fun `displayedName should return the original when the translation or the user language is missing`() {
        val poll = randomPoll(name = "Lunch?", nameI18n = mapOf("language" to "en", "it_text" to "Pranzo?"))

        assertEquals("Lunch?", poll.displayedName(randomUser(language = "fr"), autoTranslationEnabled = true))
        assertEquals("Lunch?", poll.displayedName(randomUser(language = ""), autoTranslationEnabled = true))
        assertEquals("Lunch?", poll.displayedName(null, autoTranslationEnabled = true))
    }

    @Test
    fun `Option displayedText should return the translation in the user language`() {
        val option = randomOption(text = "Pizza", textI18n = mapOf("language" to "en", "it_text" to "Pizza!"))

        assertEquals("Pizza!", option.displayedText(randomUser(language = "it"), autoTranslationEnabled = true))
        assertEquals("Pizza", option.displayedText(randomUser(language = "fr"), autoTranslationEnabled = true))
    }

    @Test
    fun `Answer displayedText should return the translation in the user language`() {
        val answer = randomPollAnswer(text = "Sushi", textI18n = mapOf("language" to "en", "it_text" to "Sushi!"))

        assertEquals("Sushi!", answer.displayedText(randomUser(language = "it"), autoTranslationEnabled = true))
        assertEquals("Sushi", answer.displayedText(randomUser(language = "it"), autoTranslationEnabled = false))
    }

    @Test
    fun `hasTranslation should be true when the name or an option is translated`() {
        val user = randomUser(language = "it")
        val option = randomOption(text = "Pizza", textI18n = mapOf("language" to "en", "it_text" to "Pizza!"))

        assertTrue(randomPoll(name = "Lunch?", nameI18n = mapOf("it_text" to "Pranzo?")).hasTranslation(user))
        assertTrue(randomPoll(nameI18n = emptyMap(), options = listOf(randomOption(), option)).hasTranslation(user))
    }

    @Test
    fun `hasTranslation should be false when nothing differs in the user language`() {
        val poll = randomPoll(
            name = "Lunch?",
            nameI18n = mapOf("language" to "en", "en_text" to "Lunch?", "it_text" to "Pranzo?"),
            options = listOf(randomOption(textI18n = emptyMap())),
        )

        assertFalse(poll.hasTranslation(randomUser(language = "en")))
        assertFalse(poll.hasTranslation(randomUser(language = "fr")))
        assertFalse(poll.hasTranslation(null))
    }
}
