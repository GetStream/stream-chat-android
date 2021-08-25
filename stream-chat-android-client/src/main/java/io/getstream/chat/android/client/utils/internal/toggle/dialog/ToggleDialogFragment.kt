package io.getstream.chat.android.client.utils.internal.toggle.dialog

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.client.R
import io.getstream.chat.android.client.utils.internal.toggle.ToggleService
import io.getstream.chat.android.core.internal.InternalStreamChatApi

@InternalStreamChatApi
public class ToggleDialogFragment : DialogFragment(R.layout.stream_toggle_dialog_fragment) {

    private var _controller: ToggleDialogController? = null
    private val controller = requireNotNull(_controller)
    private val recyclerView: RecyclerView
        get() = requireView().findViewById(R.id.recyclerView)
    private val saveButton: Button
        get() = requireView().findViewById(R.id.saveButton)
    private val dismissButton: Button
        get() = requireView().findViewById(R.id.dismissButton)
    private lateinit var adapter: ToggleAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        runCatching { ToggleService.instance() }
            .onFailure { error ->
                Toast.makeText(requireContext(), error.message, Toast.LENGTH_LONG).show()
                dismiss()
            }.onSuccess { service ->
                _controller = ToggleDialogController(service).apply { attachView(this@ToggleDialogFragment) }
                adapter = ToggleAdapter(controller::onToggleSwitchClicked)
                recyclerView.adapter = adapter
                saveButton.isEnabled = false
                saveButton.setOnClickListener { controller.onSaveButtonClicked() }
                dismissButton.setOnClickListener { controller.onDismissButtonClicked() }
            }
    }

    internal fun renderNewState(newState: ToggleDialogController.ToggleState.StateData) {
        adapter.setData(newState.dataSnapshot)
        saveButton.isEnabled = newState.hasChanges()
    }
}
