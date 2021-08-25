package io.getstream.chat.android.client.utils.internal.toggle.dialog

import io.getstream.chat.android.client.utils.internal.toggle.ToggleService
import io.getstream.chat.android.core.internal.fsm.FiniteStateMachine
import java.lang.ref.WeakReference

internal class ToggleDialogController(private val toggleService: ToggleService) {

    private var viewRef: WeakReference<ToggleDialogFragment> = WeakReference(null)

    private val fsm: FiniteStateMachine<ToggleState, ToggleEvent> = FiniteStateMachine {
        initialState(ToggleState.Initial)

        state<ToggleState.Initial> {
            onEvent<ToggleEvent.AttachView> { _, event ->
                viewRef = WeakReference(event.view)
                val toggles = toggleService.getToggles()
                ToggleState.StateData(toggles, emptyMap()).also {
                    viewRef.get()?.renderNewState(it)
                }
            }
        }

        state<ToggleState.StateData> {
            onEvent<ToggleEvent.Dismiss> { _, _ ->
                viewRef.get()?.dismiss()
                ToggleState.Final
            }
            onEvent<ToggleEvent.CommitChanges> { state, _ ->
                state.changes.entries.forEach { (toggle, value) -> toggleService.setToggle(toggle, value) }
                ToggleState.Final
            }
            onEvent<ToggleEvent.ToggleChanged> { stateData, event ->
                handleToggleChanged(stateData, event)
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
        viewRef.get()?.renderNewState(newState)

        return newState
    }

    fun attachView(toggleDialogFragment: ToggleDialogFragment) {
        fsm.sendEvent(ToggleEvent.AttachView(toggleDialogFragment))
    }

    fun onToggleSwitchClicked(toggleName: String, isChecked: Boolean) {
        fsm.sendEvent(ToggleEvent.ToggleChanged(toggleName, isChecked))
    }

    fun onSaveButtonClicked() {
        fsm.sendEvent(ToggleEvent.CommitChanges)
    }

    fun onDismissButtonClicked() {
        fsm.sendEvent(ToggleEvent.Dismiss)
    }

    internal sealed class ToggleState {
        object Initial : ToggleState()
        data class StateData(val initialToggles: Map<String, Boolean>, val changes: Map<String, Boolean>) :
            ToggleState() {
            fun hasChanges() = changes.isNotEmpty()
            val dataSnapshot
                get() = (initialToggles + changes).toList().sortedBy { it.first }
        }

        object Final : ToggleState()
    }

    private sealed class ToggleEvent {
        class AttachView(val view: ToggleDialogFragment) : ToggleEvent()
        class ToggleChanged(val toggleName: String, val value: Boolean) : ToggleEvent()
        object Dismiss : ToggleEvent()
        object CommitChanges : ToggleEvent()
    }
}
