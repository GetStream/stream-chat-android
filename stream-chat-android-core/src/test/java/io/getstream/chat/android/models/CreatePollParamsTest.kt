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

package io.getstream.chat.android.models

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class CreatePollParamsTest {

    @Test
    fun `deprecated constructor with List of Strings properly maps parameters to default constructor`() {
        val name = "Test Poll"
        val options = listOf("Option 1", "Option 2", "Option 3")
        val description = "Test Description"
        val votingVisibility = VotingVisibility.ANONYMOUS
        val enforceUniqueVote = false
        val maxVotesAllowed = 5
        val allowUserSuggestedOptions = true
        val allowAnswers = true

        @Suppress("DEPRECATION")
        val createPollParams = CreatePollParams(
            name = name,
            options = options,
            description = description,
            votingVisibility = votingVisibility,
            enforceUniqueVote = enforceUniqueVote,
            maxVotesAllowed = maxVotesAllowed,
            allowUserSuggestedOptions = allowUserSuggestedOptions,
            allowAnswers = allowAnswers,
        )

        Assertions.assertEquals(name, createPollParams.name)
        Assertions.assertEquals(description, createPollParams.description)
        Assertions.assertEquals(votingVisibility, createPollParams.votingVisibility)
        Assertions.assertEquals(enforceUniqueVote, createPollParams.enforceUniqueVote)
        Assertions.assertEquals(maxVotesAllowed, createPollParams.maxVotesAllowed)
        Assertions.assertEquals(allowUserSuggestedOptions, createPollParams.allowUserSuggestedOptions)
        Assertions.assertEquals(allowAnswers, createPollParams.allowAnswers)
        Assertions.assertEquals(options, createPollParams.options)
        Assertions.assertEquals(emptyMap<String, Any>(), createPollParams.extraData)
        Assertions.assertEquals(
            options.map { text -> PollOption(text = text) },
            createPollParams.optionsWithExtraData,
        )
    }

    @Test
    fun `deprecated constructor with defaults applies default values correctly`() {
        val name = "Simple Poll"
        val options = listOf("Yes", "No")

        @Suppress("DEPRECATION")
        val createPollParams = CreatePollParams(
            name = name,
            options = options,
        )

        Assertions.assertEquals(name, createPollParams.name)
        Assertions.assertEquals("", createPollParams.description)
        Assertions.assertEquals(VotingVisibility.PUBLIC, createPollParams.votingVisibility)
        Assertions.assertEquals(true, createPollParams.enforceUniqueVote)
        Assertions.assertEquals(1, createPollParams.maxVotesAllowed)
        Assertions.assertEquals(false, createPollParams.allowUserSuggestedOptions)
        Assertions.assertEquals(false, createPollParams.allowAnswers)
        Assertions.assertEquals(options, createPollParams.options)
        Assertions.assertEquals(emptyMap<String, Any>(), createPollParams.extraData)
    }

    @Test
    fun `new constructor with List of PollOptions properly maps parameters to default constructor`() {
        val name = "Poll With Extra Data"
        val option1 = PollOption("Option A", mapOf("color" to "red"))
        val option2 = PollOption("Option B", mapOf("color" to "blue"))
        val options = listOf(option1, option2)
        val description = "Poll Description"
        val votingVisibility = VotingVisibility.PUBLIC
        val enforceUniqueVote = true
        val maxVotesAllowed = 3
        val allowUserSuggestedOptions = false
        val allowAnswers = true
        val extraData = mapOf("priority" to "high", "category" to "survey")

        val createPollParams = CreatePollParams(
            name = name,
            options = options,
            description = description,
            votingVisibility = votingVisibility,
            enforceUniqueVote = enforceUniqueVote,
            maxVotesAllowed = maxVotesAllowed,
            allowUserSuggestedOptions = allowUserSuggestedOptions,
            allowAnswers = allowAnswers,
            extraData = extraData,
        )

        Assertions.assertEquals(name, createPollParams.name)
        Assertions.assertEquals(description, createPollParams.description)
        Assertions.assertEquals(votingVisibility, createPollParams.votingVisibility)
        Assertions.assertEquals(enforceUniqueVote, createPollParams.enforceUniqueVote)
        Assertions.assertEquals(maxVotesAllowed, createPollParams.maxVotesAllowed)
        Assertions.assertEquals(allowUserSuggestedOptions, createPollParams.allowUserSuggestedOptions)
        Assertions.assertEquals(allowAnswers, createPollParams.allowAnswers)
        Assertions.assertEquals(options, createPollParams.optionsWithExtraData)
        Assertions.assertEquals(extraData, createPollParams.extraData)
        Assertions.assertEquals(listOf("Option A", "Option B"), createPollParams.options)
    }

    @Test
    fun `new constructor with defaults applies default values correctly`() {
        val name = "Minimal Poll"
        val options = listOf(
            PollOption(text = "Option 1"),
            PollOption(text = "Option 2"),
        )

        val createPollParams = CreatePollParams(
            name = name,
            options = options,
        )

        Assertions.assertEquals(name, createPollParams.name)
        Assertions.assertEquals("", createPollParams.description)
        Assertions.assertEquals(VotingVisibility.PUBLIC, createPollParams.votingVisibility)
        Assertions.assertEquals(true, createPollParams.enforceUniqueVote)
        Assertions.assertEquals(1, createPollParams.maxVotesAllowed)
        Assertions.assertEquals(false, createPollParams.allowUserSuggestedOptions)
        Assertions.assertEquals(false, createPollParams.allowAnswers)
        Assertions.assertEquals(options, createPollParams.optionsWithExtraData)
        Assertions.assertEquals(emptyMap<String, Any>(), createPollParams.extraData)
    }

    @Test
    fun `options property getter extracts text from optionsWithExtraData correctly`() {
        val options = listOf(
            PollOption("First", mapOf("id" to 1)),
            PollOption("Second", mapOf("id" to 2)),
            PollOption("Third", mapOf("id" to 3)),
        )

        val createPollParams = CreatePollParams(
            name = "Test",
            options = options,
        )

        Assertions.assertEquals(listOf("First", "Second", "Third"), createPollParams.options)
    }

    @Test
    fun `deprecated constructor creates PollOption objects with empty extra data`() {
        val optionTexts = listOf("Choice 1", "Choice 2", "Choice 3")

        @Suppress("DEPRECATION")
        val createPollParams = CreatePollParams(
            name = "Test",
            options = optionTexts,
        )

        createPollParams.optionsWithExtraData.forEach { option ->
            Assertions.assertEquals(emptyMap<String, Any>(), option.extraData)
        }
    }

    @Test
    fun `all parameters are preserved through constructor chain`() {
        val name = "Complete Poll"
        val options = listOf(
            PollOption("Option 1", mapOf("order" to 1)),
            PollOption("Option 2", mapOf("order" to 2)),
        )
        val description = "Complete Description"
        val votingVisibility = VotingVisibility.ANONYMOUS
        val enforceUniqueVote = false
        val maxVotesAllowed = 10
        val allowUserSuggestedOptions = true
        val allowAnswers = false
        val extraData = mapOf("testKey" to "testValue")

        val createPollParams = CreatePollParams(
            name = name,
            options = options,
            description = description,
            votingVisibility = votingVisibility,
            enforceUniqueVote = enforceUniqueVote,
            maxVotesAllowed = maxVotesAllowed,
            allowUserSuggestedOptions = allowUserSuggestedOptions,
            allowAnswers = allowAnswers,
            extraData = extraData,
        )

        // Verify all properties are correctly set
        Assertions.assertEquals(name, createPollParams.name)
        Assertions.assertEquals(description, createPollParams.description)
        Assertions.assertEquals(votingVisibility, createPollParams.votingVisibility)
        Assertions.assertEquals(enforceUniqueVote, createPollParams.enforceUniqueVote)
        Assertions.assertEquals(maxVotesAllowed, createPollParams.maxVotesAllowed)
        Assertions.assertEquals(allowUserSuggestedOptions, createPollParams.allowUserSuggestedOptions)
        Assertions.assertEquals(allowAnswers, createPollParams.allowAnswers)
        Assertions.assertEquals(options, createPollParams.optionsWithExtraData)
        Assertions.assertEquals(extraData, createPollParams.extraData)

        // Verify derived properties
        Assertions.assertEquals(
            mapOf("Option 1" to "order:1", "Option 2" to "order:2"),
            createPollParams.options.zip(options).associate { (text, option) ->
                text to option.extraData.entries.joinToString(":") { "${it.key}:${it.value}" }
            },
        )
    }
}
