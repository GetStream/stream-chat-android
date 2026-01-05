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
 * @property switchItemList The list of poll configuration switches.
 * @property hasError Whether there are validation errors in the poll.
 * @property isCreationEnabled Whether the poll can be created (all required fields are valid).
 * @property hasChanges Whether there are unsaved changes.
 */
@Immutable
internal data class CreatePollViewState(
    val question: String = "",
    val optionItemList: List<PollOptionItem> = emptyList(),
    val switchItemList: List<PollSwitchItem> = emptyList(),
    val hasError: Boolean = false,
    val isCreationEnabled: Boolean = false,
    val hasChanges: Boolean = false,
)

/**
 * ViewModel for managing the state of the poll creation screen.
 *
 * @param configSwitches The initial list of poll configuration switches.
 */
internal class CreatePollViewModel(private val configSwitches: List<PollSwitchItem>) : ViewModel() {

    private val _state = MutableStateFlow(CreatePollViewState(switchItemList = configSwitches))

    /**
     * The current UI state of the poll creation screen.
     */
    val state: StateFlow<CreatePollViewState> = _state
        .map { state ->
            state.copy(
                hasError = hasError(state.optionItemList, state.switchItemList),
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
     *
     * @param newQuestion The new question text.
     */
    fun updateQuestion(newQuestion: String) {
        _state.update { it.copy(question = newQuestion) }
    }

    /**
     * Updates the list of poll options.
     *
     * @param newOptions The new list of poll options.
     */
    fun updateOptions(newOptions: List<PollOptionItem>) {
        _state.update { it.copy(optionItemList = newOptions) }
    }

    /**
     * Updates the list of poll switches.
     *
     * @param newSwitches The new list of poll switches.
     */
    fun updateSwitches(newSwitches: List<PollSwitchItem>) {
        _state.update { it.copy(switchItemList = newSwitches) }
    }

    /**
     * Resets the poll creation state to its initial values.
     */
    fun reset() {
        _state.value = CreatePollViewState(switchItemList = configSwitches)
    }

    /**
     * Determines if the poll can be created based on the current state.
     *
     * @param state The current poll view state.
     * @return True if the poll can be created, false otherwise.
     */
    private fun isCreationEnabled(state: CreatePollViewState): Boolean {
        return state.question.isNotBlank() &&
            state.optionItemList.any { it.title.isNotBlank() } &&
            !hasError(state.optionItemList, state.switchItemList)
    }

    /**
     * Determines if there are unsaved changes in the poll.
     *
     * @param state The current poll view state.
     * @return True if there are unsaved changes, false otherwise.
     */
    private fun hasChanges(state: CreatePollViewState): Boolean {
        return state.question.isNotBlank() || state.optionItemList.any { it.title.isNotBlank() }
    }

    /**
     * Checks if there are validation errors in the poll options or switches.
     *
     * @param options The list of poll options.
     * @param switches The list of poll switches.
     * @return True if there are errors, false otherwise.
     */
    private fun hasError(
        options: List<PollOptionItem>,
        switches: List<PollSwitchItem>,
    ): Boolean {
        // Check errors in options
        val hasErrorInOptions = options.fastAny { item ->
            item.pollOptionError != null
        }
        // Check errors or missing fields in switches
        val hasErrorInSwitches = switches.fastAny { item ->
            val hasError = item.pollOptionError != null
            val isMissingMandatoryInput = item.enabled &&
                item.pollSwitchInput != null &&
                item.pollSwitchInput.value.toString().isEmpty()
            hasError || isMissingMandatoryInput
        }
        return hasErrorInOptions || hasErrorInSwitches
    }
}
