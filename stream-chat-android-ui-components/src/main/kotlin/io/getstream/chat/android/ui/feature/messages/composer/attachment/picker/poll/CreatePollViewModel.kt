/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.ui.feature.messages.composer.attachment.picker.poll

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.models.PollConfig
import io.getstream.chat.android.models.VotingVisibility
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.utils.PollsConstants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel class for creating a poll.
 * It handles the creation of the poll and its configuration.
 */
public class CreatePollViewModel : ViewModel() {

    private val _titleStateFlow: MutableStateFlow<String> = MutableStateFlow("")
    private val _options: MutableStateFlow<LinkedHashMap<Int, PollAnswer>> = MutableStateFlow(LinkedHashMap())
    private val createPoll: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private var suggestAnOption = false
    private var annonymousPoll = false
    private var allowMultipleVotes = MutableStateFlow(false)
    private var maxAnswers: MutableStateFlow<Int?> = MutableStateFlow(null)

    /**
     * The options for the poll.
     */
    public val options: StateFlow<List<PollAnswer>> = _options
        .map { it.values.toList() }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    /**
     * The error message for the maximum number of answers allowed.
     */
    @Deprecated(
        "Use multipleAnswersError instead. This property will be removed in future versions.",
        ReplaceWith("multipleAnswersError"),
        level = DeprecationLevel.WARNING,
    )
    public val maxAnswerError: StateFlow<Int?> =
        multipleAnswersErrorFlow().stateIn(viewModelScope, SharingStarted.Lazily, null)

    /**
     * A shared flow that emits an error when the range of multiple answers is invalid.
     */
    public val multipleAnswersError: SharedFlow<Int?> =
        multipleAnswersErrorFlow().shareIn(viewModelScope, SharingStarted.Lazily)

    private fun multipleAnswersErrorFlow() = combine(allowMultipleVotes, maxAnswers) { allowMultipleVotes, maxAnswer ->
        when {
            !allowMultipleVotes || maxAnswer == null -> null
            maxAnswer
                !in PollsConstants.MIN_NUMBER_OF_MULTIPLE_ANSWERS..PollsConstants.MAX_NUMBER_OF_MULTIPLE_ANSWERS ->
                R.string.stream_ui_poll_multiple_answers_error

            else -> null
        }
    }

    /**
     * The poll configuration.
     * If the poll is not ready to be created, it will be null.
     */
    public val pollConfig: StateFlow<PollConfig?> =
        combine(
            createPoll,
            _titleStateFlow,
            options,
            allowMultipleVotes,
            maxAnswers,
        ) { createPoll, title, options, allowMultipleVotes, maxAnswers ->
            if (!createPoll) {
                null
            } else if (title.isNotBlank() &&
                options.isNotEmpty() &&
                options.all { it.text.isNotBlank() && !it.duplicateError } &&
                (!allowMultipleVotes || (maxAnswers != null && maxAnswers > 0 && maxAnswers <= options.size))
            ) {
                PollConfig(
                    name = title,
                    options = options.map { it.text },
                    votingVisibility = if (annonymousPoll) VotingVisibility.ANONYMOUS else VotingVisibility.PUBLIC,
                    allowUserSuggestedOptions = suggestAnOption,
                    maxVotesAllowed = maxAnswers.takeIf { allowMultipleVotes } ?: 1,
                    enforceUniqueVote = !allowMultipleVotes,
                )
            } else {
                null
            }
        }
            .stateIn(viewModelScope, SharingStarted.Lazily, null)

    /**
     * Indicates if the poll is ready to be created.
     */
    public val pollIsReady: StateFlow<Boolean> =
        combine(
            _options,
            _titleStateFlow,
            allowMultipleVotes,
            maxAnswers,
        ) { options, title, allowMultipleVotes, maxAnswers ->
            options.values.let {
                it.isNotEmpty() && it.all { it.text.isNotBlank() && !it.duplicateError }
            } &&
                title.isNotBlank() &&
                (!allowMultipleVotes || (maxAnswers != null && maxAnswers > 0 && maxAnswers <= options.size))
        }
            .stateIn(viewModelScope, SharingStarted.Lazily, false)

    /**
     * Set the title of the poll.
     *
     * @param title The title of the poll.
     */
    public fun onTitleChanged(title: String) {
        _titleStateFlow.value = title
    }

    /**
     * Create a new empty option for the poll.
     */
    public fun createOption() {
        _options.value = LinkedHashMap(_options.value).apply {
            val id = _options.value.size
            put(id, PollAnswer(id = id, text = "", duplicateError = false))
        }
    }

    /**
     * Update the text of the option.
     * If the text already exists in another option, it will set the duplicate error to true.
     *
     * @param id The id of the option.
     * @param text The text of the option.
     */
    public fun onOptionTextChanged(id: Int, text: String) {
        _options.value.let { options ->
            val previousOptions = options.values
                .filterNot { it.id == id }
                .map { it.text }
                .filter { it.isNotEmpty() }
            options[id]?.let { option ->
                _options.value = LinkedHashMap(options).apply {
                    put(id, option.copy(text = text, duplicateError = previousOptions.contains(text)))
                }
            }
        }
    }

    /**
     * Create a new poll config.
     */
    public fun createPollConfig() {
        createPoll.value = true
    }

    /**
     * Set if the poll allows multiple votes.
     *
     * @param allowMultipleVotes True if the poll allows multiple votes, false otherwise.
     */
    public fun setAllowMultipleVotes(allowMultipleVotes: Boolean) {
        this.allowMultipleVotes.value = allowMultipleVotes
    }

    /**
     * Set the maximum number of answers allowed.
     *
     * @param maxAnswer The maximum number of answers allowed.
     */
    public fun setMaxAnswer(maxAnswer: Int?) {
        this.maxAnswers.value = maxAnswer
    }

    /**
     * Set if the poll allows users to suggest new options.
     *
     * @param suggestAnOption True if the poll allows users to suggest new options, false otherwise.
     */
    public fun setSuggestAnOption(suggestAnOption: Boolean) {
        this.suggestAnOption = suggestAnOption
    }

    /**
     * Set if the poll is annonymous.
     *
     * @param annonymousPoll True if the poll is annonymous, false otherwise.
     */
    public fun setAnnonymousPoll(annonymousPoll: Boolean) {
        this.annonymousPoll = annonymousPoll
    }
}
