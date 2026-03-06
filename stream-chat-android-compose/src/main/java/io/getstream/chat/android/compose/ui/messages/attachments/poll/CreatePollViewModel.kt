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

package io.getstream.chat.android.compose.ui.messages.attachments.poll

import androidx.compose.runtime.Immutable
import androidx.compose.ui.util.fastAny
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.compose.ui.theme.PollsConfig
import io.getstream.chat.android.ui.common.utils.PollsConstants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

/**
 * Represents the UI state of the poll creation screen.
 *
 * @property question The poll question text.
 * @property optionItemList The list of poll options.
 * @property multipleVotesEnabled Whether the "multiple votes" toggle is on.
 * @property limitVotesEnabled Whether the "limit votes per person" child toggle is on.
 * @property maxVotesPerUserText The max votes per user text for the stepper.
 * @property anonymousPollEnabled Whether the "anonymous poll" toggle is on.
 * @property suggestAnOptionEnabled Whether the "suggest an option" toggle is on.
 * @property allowCommentsEnabled Whether the "allow comments" toggle is on.
 * @property hasError Whether there are validation errors in the poll.
 * @property isCreationEnabled Whether the poll can be created (all required fields are valid).
 * @property hasChanges Whether there are unsaved changes.
 */
@Immutable
internal data class CreatePollViewState(
    val question: String = "",
    val optionItemList: List<PollOptionItem> = emptyList(),
    val multipleVotesEnabled: Boolean = false,
    val limitVotesEnabled: Boolean = false,
    val maxVotesPerUserText: String = PollsConstants.MULTIPLE_ANSWERS_RANGE.first.toString(),
    val anonymousPollEnabled: Boolean = false,
    val suggestAnOptionEnabled: Boolean = false,
    val allowCommentsEnabled: Boolean = false,
    val hasError: Boolean = false,
    val isCreationEnabled: Boolean = false,
    val hasChanges: Boolean = false,
)

/**
 * ViewModel for managing the state of the poll creation screen.
 *
 * @param pollsConfig The polls configuration determining which features are shown and their defaults.
 */
internal class CreatePollViewModel(private val pollsConfig: PollsConfig) : ViewModel() {

    private val _state = MutableStateFlow(initialState())

    /**
     * The current UI state of the poll creation screen.
     */
    val state: StateFlow<CreatePollViewState> = _state
        .map { state ->
            state.copy(
                hasError = hasError(state),
                isCreationEnabled = isCreationEnabled(state),
                hasChanges = hasChanges(state),
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
            initialValue = _state.value,
        )

    /**
     * The list of [PollSwitchState]s to display, derived from the current state and config.
     */
    val switchItems: StateFlow<List<PollSwitchState>> = _state
        .map { current ->
            buildList {
                if (pollsConfig.multipleVotes.configurable) {
                    add(
                        PollSwitchState.MultipleVotes(
                            enabled = current.multipleVotesEnabled,
                            onCheckedChange = ::updateMultipleVotes,
                            limitVotesEnabled = current.limitVotesEnabled,
                            onLimitVotesCheckedChange = ::updateLimitVotes,
                            maxVotesPerUserText = current.maxVotesPerUserText,
                            onMaxVotesChange = ::updateMaxVotes,
                            onMaxVotesFocusLost = ::coerceMaxVotes,
                        ),
                    )
                }
                if (pollsConfig.anonymousPoll.configurable) {
                    add(PollSwitchState.AnonymousPoll(current.anonymousPollEnabled, ::updateAnonymousPoll))
                }
                if (pollsConfig.suggestAnOption.configurable) {
                    add(PollSwitchState.SuggestAnOption(current.suggestAnOptionEnabled, ::updateSuggestAnOption))
                }
                if (pollsConfig.allowComments.configurable) {
                    add(PollSwitchState.AllowComments(current.allowCommentsEnabled, ::updateAllowComments))
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
            initialValue = emptyList(),
        )

    /**
     * Updates the poll question.
     */
    fun updateQuestion(newQuestion: String) {
        _state.update { it.copy(question = newQuestion) }
    }

    /**
     * Updates the list of poll options.
     */
    fun updateOptions(newOptions: List<PollOptionItem>) {
        _state.update { it.copy(optionItemList = newOptions) }
    }

    /**
     * Updates the "multiple votes" toggle state.
     * When disabled, resets limitVotesEnabled and maxVotesPerUser.
     */
    fun updateMultipleVotes(enabled: Boolean) {
        _state.update {
            it.copy(
                multipleVotesEnabled = enabled,
                limitVotesEnabled = if (enabled) it.limitVotesEnabled else false,
            )
        }
    }

    /**
     * Updates the "limit votes per person" child toggle state.
     */
    fun updateLimitVotes(enabled: Boolean) {
        _state.update {
            it.copy(limitVotesEnabled = enabled)
        }
    }

    /**
     * Updates the max votes per user value.
     */
    fun updateMaxVotes(text: String) {
        _state.update { it.copy(maxVotesPerUserText = text) }
    }

    fun coerceMaxVotes() {
        _state.update {
            val coerced = it.maxVotesPerUserText.toIntOrNull()
                ?.coerceIn(PollsConstants.MULTIPLE_ANSWERS_RANGE)
                ?: PollsConstants.MULTIPLE_ANSWERS_RANGE.first
            it.copy(maxVotesPerUserText = coerced.toString())
        }
    }

    /**
     * Updates the "anonymous poll" toggle state.
     */
    fun updateAnonymousPoll(enabled: Boolean) {
        _state.update { it.copy(anonymousPollEnabled = enabled) }
    }

    /**
     * Updates the "suggest an option" toggle state.
     */
    fun updateSuggestAnOption(enabled: Boolean) {
        _state.update { it.copy(suggestAnOptionEnabled = enabled) }
    }

    /**
     * Updates the "allow comments" toggle state.
     */
    fun updateAllowComments(enabled: Boolean) {
        _state.update { it.copy(allowCommentsEnabled = enabled) }
    }

    /**
     * Resets the poll creation state to its initial values.
     */
    fun reset() {
        _state.value = initialState()
    }

    private fun initialState(): CreatePollViewState = CreatePollViewState(
        multipleVotesEnabled = pollsConfig.multipleVotes.defaultValue,
        anonymousPollEnabled = pollsConfig.anonymousPoll.defaultValue,
        suggestAnOptionEnabled = pollsConfig.suggestAnOption.defaultValue,
        allowCommentsEnabled = pollsConfig.allowComments.defaultValue,
    )

    private fun isCreationEnabled(state: CreatePollViewState): Boolean {
        return state.question.isNotBlank() &&
            state.optionItemList.any { it.title.isNotBlank() } &&
            !hasError(state)
    }

    private fun hasChanges(state: CreatePollViewState): Boolean {
        return state.question.isNotBlank() || state.optionItemList.any { it.title.isNotBlank() }
    }

    private fun hasError(state: CreatePollViewState): Boolean {
        return state.optionItemList.fastAny { it.pollOptionError != null }
    }
}
