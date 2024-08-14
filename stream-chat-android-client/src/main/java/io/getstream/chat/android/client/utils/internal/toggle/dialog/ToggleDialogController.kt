/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.utils.internal.toggle.dialog

import io.getstream.chat.android.client.utils.internal.toggle.ToggleService
import io.getstream.chat.android.core.internal.fsm.FiniteStateMachine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

internal class ToggleDialogController(
    private val scope: CoroutineScope,
    private val toggleService: ToggleService,
) {

    private var viewRef: WeakReference<ToggleDialogFragment> = WeakReference(null)

    private val fsm: FiniteStateMachine<ToggleState, ToggleEvent> = FiniteStateMachine {
        initialState(ToggleState.Initial)

        state<ToggleState.Initial> {
            onEvent<ToggleEvent.AttachView> { event ->
                viewRef = WeakReference(event.view)
                val toggles = toggleService.getToggles()
                ToggleState.StateData(toggles, emptyMap()).also {
                    viewRef.get()?.showData(it.dataSnapshot, it.hasChanges())
                }
            }
        }

        state<ToggleState.StateData> {
            onEvent<ToggleEvent.Dismiss> {
                viewRef.get()?.dismiss()
                ToggleState.Final
            }
            onEvent<ToggleEvent.CommitChanges> { event ->
                changes.entries.forEach { (toggle, value) -> toggleService.setToggle(toggle, value) }
                changes.toList().let(event.togglesChangesCommittedListener::invoke)
                viewRef.get()?.dismiss()
                ToggleState.Final
            }
            onEvent<ToggleEvent.ToggleChanged> { event ->
                handleToggleChanged(this, event)
            }
        }
    }

    private fun handleToggleChanged(
        stateData: ToggleState.StateData,
        event: ToggleEvent.ToggleChanged,
    ): ToggleState.StateData {
        val newState = if (stateData.changes.containsKey(event.toggleName)) {
            stateData.copy(changes = stateData.changes - event.toggleName)
        } else {
            stateData.copy(changes = stateData.changes + (event.toggleName to event.value))
        }
        viewRef.get()?.showData(newState.dataSnapshot, newState.hasChanges())

        return newState
    }

    fun attachView(toggleDialogFragment: ToggleDialogFragment) {
        scope.launch { fsm.sendEvent(ToggleEvent.AttachView(toggleDialogFragment)) }
    }

    fun onToggleSwitchClicked(toggleName: String, isChecked: Boolean) {
        scope.launch { fsm.sendEvent(ToggleEvent.ToggleChanged(toggleName, isChecked)) }
    }

    fun onSaveButtonClicked(togglesChangesCommittedListener: (changedToggles: List<Pair<String, Boolean>>) -> Unit) {
        scope.launch { fsm.sendEvent(ToggleEvent.CommitChanges(togglesChangesCommittedListener)) }
    }

    fun onDismissButtonClicked() {
        scope.launch { fsm.sendEvent(ToggleEvent.Dismiss) }
    }

    internal sealed class ToggleState {
        object Initial : ToggleState() { override fun toString(): String = "Initial" }
        data class StateData(val initialToggles: Map<String, Boolean>, val changes: Map<String, Boolean>) :
            ToggleState() {
            fun hasChanges() = changes.isNotEmpty()
            val dataSnapshot
                get() = (initialToggles + changes).toList().sortedBy { it.first }
        }

        object Final : ToggleState() { override fun toString(): String = "Final" }
    }

    private sealed class ToggleEvent {
        data class AttachView(val view: ToggleDialogFragment) : ToggleEvent()
        data class ToggleChanged(val toggleName: String, val value: Boolean) : ToggleEvent()
        object Dismiss : ToggleEvent() { override fun toString(): String = "Dismiss" }
        data class CommitChanges(
            val togglesChangesCommittedListener: (changedToggles: List<Pair<String, Boolean>>) -> Unit,
        ) : ToggleEvent()
    }
}
