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

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
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
                _controller = ToggleDialogController(lifecycleScope, service)
                    .apply { attachView(this@ToggleDialogFragment) }
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
