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
import io.getstream.chat.android.compose.ui.theme.PollFeatureConfig
import io.getstream.chat.android.compose.ui.theme.PollsConfig
import io.getstream.chat.android.ui.common.utils.PollsConstants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

/**
 * A single poll feature toggle combining visibility, enabled state, and callback.
 *
 * @property visible Whether this toggle is shown in the poll creation UI.
 * @property enabled Whether this toggle is currently on.
 * @property onCheckedChange Called when the toggle is flipped.
 */
@Immutable
internal data class PollSwitchItem(
    val visible: Boolean,
    val enabled: Boolean,
    val onCheckedChange: (Boolean) -> Unit,
) {
    constructor(config: PollFeatureConfig, onCheckedChange: (Boolean) -> Unit) :
        this(visible = config.configurable, enabled = config.defaultValue, onCheckedChange = onCheckedChange)
}

private val HiddenSwitch = PollSwitchItem(visible = false, enabled = false, onCheckedChange = {})

/**
 * Represents the UI state of the poll creation screen.
 */
@Immutable
internal data class CreatePollViewState(
    val question: String = "",
    val optionItemList: List<PollOptionItem> = emptyList(),
    val multipleVotes: PollSwitchItem = HiddenSwitch,
    val limitVotesPerPerson: PollSwitchItem = HiddenSwitch,
    val maxVotesPerPersonText: String = PollsConstants.MULTIPLE_ANSWERS_RANGE.first.toString(),
    val anonymousPoll: PollSwitchItem = HiddenSwitch,
    val suggestAnOption: PollSwitchItem = HiddenSwitch,
    val allowComments: PollSwitchItem = HiddenSwitch,
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
     * When disabled, resets the limit votes toggle.
     */
    fun updateMultipleVotes(enabled: Boolean) {
        _state.update {
            it.copy(
                multipleVotes = it.multipleVotes.copy(enabled = enabled),
                limitVotesPerPerson = it.limitVotesPerPerson.copy(enabled = enabled && it.limitVotesPerPerson.enabled),
            )
        }
    }

    /**
     * Updates the "limit votes per person" child toggle state.
     */
    fun updateLimitVotes(enabled: Boolean) {
        _state.update {
            it.copy(limitVotesPerPerson = it.limitVotesPerPerson.copy(enabled = enabled))
        }
    }

    /**
     * Updates the max votes per person value.
     */
    fun updateMaxVotes(text: String) {
        _state.update { it.copy(maxVotesPerPersonText = text) }
    }

    fun coerceMaxVotes() {
        _state.update {
            val coerced = it.maxVotesPerPersonText.toIntOrNull()
                ?.coerceIn(PollsConstants.MULTIPLE_ANSWERS_RANGE)
                ?: PollsConstants.MULTIPLE_ANSWERS_RANGE.first
            it.copy(maxVotesPerPersonText = coerced.toString())
        }
    }

    /**
     * Updates the "anonymous poll" toggle state.
     */
    fun updateAnonymousPoll(enabled: Boolean) {
        _state.update { it.copy(anonymousPoll = it.anonymousPoll.copy(enabled = enabled)) }
    }

    /**
     * Updates the "suggest an option" toggle state.
     */
    fun updateSuggestAnOption(enabled: Boolean) {
        _state.update { it.copy(suggestAnOption = it.suggestAnOption.copy(enabled = enabled)) }
    }

    /**
     * Updates the "allow comments" toggle state.
     */
    fun updateAllowComments(enabled: Boolean) {
        _state.update { it.copy(allowComments = it.allowComments.copy(enabled = enabled)) }
    }

    /**
     * Resets the poll creation state to its initial values.
     */
    fun reset() {
        _state.value = initialState()
    }

    private fun initialState(): CreatePollViewState = CreatePollViewState(
        multipleVotes = PollSwitchItem(
            config = pollsConfig.multipleVotes,
            onCheckedChange = ::updateMultipleVotes,
        ),
        limitVotesPerPerson = PollSwitchItem(
            config = pollsConfig.maxVotesPerPerson,
            onCheckedChange = ::updateLimitVotes,
        ),
        anonymousPoll = PollSwitchItem(
            config = pollsConfig.anonymousPoll,
            onCheckedChange = ::updateAnonymousPoll,
        ),
        suggestAnOption = PollSwitchItem(
            config = pollsConfig.suggestAnOption,
            onCheckedChange = ::updateSuggestAnOption,
        ),
        allowComments = PollSwitchItem(
            config = pollsConfig.allowComments,
            onCheckedChange = ::updateAllowComments,
        ),
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
