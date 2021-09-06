package io.getstream.chat.android.client.utils.internal.toggle.dialog

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import io.getstream.chat.android.client.R
import io.getstream.chat.android.client.utils.internal.toggle.ToggleService
import io.getstream.chat.android.core.internal.InternalStreamChatApi

@InternalStreamChatApi
public class ToggleDialogFragment : DialogFragment(R.layout.stream_toggle_dialog_fragment) {

    public var togglesChangesCommittedListener: (changedToggles: List<Pair<String, Boolean>>) -> Unit = {}

    private var _controller: ToggleDialogController? = null
    private val controller: ToggleDialogController
        get() = requireNotNull(_controller)
    private val listView: ListView
        get() = requireView().findViewById(R.id.listView)
    private val saveButton: Button
        get() = requireView().findViewById(R.id.saveButton)
    private val dismissButton: Button
        get() = requireView().findViewById(R.id.dismissButton)
    private var adapter: ToggleAdapter? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        runCatching { ToggleService.instance() }
            .onFailure { error ->
                Toast.makeText(requireContext(), error.message, Toast.LENGTH_LONG).show()
                dismiss()
            }.onSuccess { service ->
                adapter = ToggleAdapter(requireContext())
                _controller = ToggleDialogController(service).apply { attachView(this@ToggleDialogFragment) }
                adapter?.listener = ToggleSwitchListener(controller::onToggleSwitchClicked)
                listView.adapter = adapter
                saveButton.isEnabled = false
                saveButton.setOnClickListener { controller.onSaveButtonClicked(togglesChangesCommittedListener) }
                dismissButton.setOnClickListener { controller.onDismissButtonClicked() }
                requireDialog().setCanceledOnTouchOutside(false)
            }
    }

    internal fun showData(data: List<Pair<String, Boolean>>, saveButtonEnabled: Boolean) {
        adapter?.addData(data)
        saveButton.isEnabled = saveButtonEnabled
    }
}
