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

package io.getstream.chat.android.ui.feature.messages.list.internal.poll

import android.app.Dialog
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamUiDialogSuggestPollOptionBinding
import io.getstream.chat.android.ui.utils.extensions.createStreamThemeWrapper
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater

/**
 * Dialog that lets a voter suggest a new option for a poll.
 *
 * Delivers the entered option via the fragment-result API under [REQUEST_KEY], with
 * the poll id in [BUNDLE_KEY_POLL_ID] and the trimmed option text in [BUNDLE_KEY_OPTION_TEXT].
 */
public class SuggestPollOptionDialogFragment : AppCompatDialogFragment() {

    private val pollId: String
        get() = requireArguments().getString(ARG_POLL_ID)
            ?: error("Poll ID not found in arguments")

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = StreamUiDialogSuggestPollOptionBinding.inflate(requireContext().streamThemeInflater)

        val dialog = AlertDialog.Builder(requireContext().createStreamThemeWrapper())
            .setTitle(R.string.stream_ui_poll_suggest_option_dialog_title)
            .setView(binding.root)
            .setPositiveButton(R.string.stream_ui_poll_suggest_option_dialog_submit) { _, _ ->
                val text = binding.optionInput.text?.toString()?.trim().orEmpty()
                if (text.isNotEmpty()) {
                    parentFragmentManager.setFragmentResult(
                        REQUEST_KEY,
                        bundleOf(
                            BUNDLE_KEY_POLL_ID to pollId,
                            BUNDLE_KEY_OPTION_TEXT to text,
                        ),
                    )
                }
            }
            .setNegativeButton(R.string.stream_ui_poll_suggest_option_dialog_cancel, null)
            .create()

        dialog.setOnShowListener {
            val confirm = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            confirm.isEnabled = false
            binding.optionInput.doAfterTextChanged { editable ->
                confirm.isEnabled = editable?.toString()?.trim()?.isNotEmpty() == true
            }
            binding.optionInput.requestFocus()
            dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }

        return dialog
    }

    public companion object {
        internal const val TAG: String = "SuggestPollOptionDialogFragment"
        internal const val REQUEST_KEY: String = "stream_ui_suggest_poll_option_request"
        internal const val BUNDLE_KEY_POLL_ID: String = "stream_ui_suggest_poll_option_poll_id"
        internal const val BUNDLE_KEY_OPTION_TEXT: String = "stream_ui_suggest_poll_option_text"

        private const val ARG_POLL_ID: String = "arg_poll_id"

        public fun newInstance(pollId: String): SuggestPollOptionDialogFragment =
            SuggestPollOptionDialogFragment().apply {
                arguments = bundleOf(ARG_POLL_ID to pollId)
            }
    }
}
