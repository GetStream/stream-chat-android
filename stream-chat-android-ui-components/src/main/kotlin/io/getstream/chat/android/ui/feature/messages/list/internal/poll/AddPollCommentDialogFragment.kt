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
import io.getstream.chat.android.ui.databinding.StreamUiDialogAddPollCommentBinding
import io.getstream.chat.android.ui.feature.messages.list.internal.poll.AddPollCommentDialogFragment.Companion.BUNDLE_KEY_ANSWER
import io.getstream.chat.android.ui.feature.messages.list.internal.poll.AddPollCommentDialogFragment.Companion.BUNDLE_KEY_MESSAGE_ID
import io.getstream.chat.android.ui.feature.messages.list.internal.poll.AddPollCommentDialogFragment.Companion.BUNDLE_KEY_POLL_ID
import io.getstream.chat.android.ui.feature.messages.list.internal.poll.AddPollCommentDialogFragment.Companion.REQUEST_KEY
import io.getstream.chat.android.ui.utils.extensions.createStreamThemeWrapper
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater

/**
 * Dialog that lets a voter add (or edit) a free-text comment / answer on a poll.
 *
 * Delivers the entered comment via the fragment-result API under [REQUEST_KEY], with
 * the message id in [BUNDLE_KEY_MESSAGE_ID], the poll id in [BUNDLE_KEY_POLL_ID],
 * and the trimmed answer in [BUNDLE_KEY_ANSWER].
 */
internal class AddPollCommentDialogFragment : AppCompatDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val args = requireArguments()
        val messageId = args.getString(ARG_MESSAGE_ID) ?: error("Message ID not found in arguments")
        val pollId = args.getString(ARG_POLL_ID) ?: error("Poll ID not found in arguments")
        val initialText = args.getString(ARG_INITIAL_TEXT)

        val binding = StreamUiDialogAddPollCommentBinding.inflate(requireContext().streamThemeInflater)
        if (savedInstanceState == null && !initialText.isNullOrEmpty()) {
            binding.answerInput.setText(initialText)
            binding.answerInput.setSelection(initialText.length)
        }

        val titleRes = if (initialText.isNullOrEmpty()) {
            R.string.stream_ui_poll_add_a_comment_label
        } else {
            R.string.stream_ui_poll_update_comment_label
        }

        val dialog = AlertDialog.Builder(requireContext().createStreamThemeWrapper())
            .setTitle(titleRes)
            .setView(binding.root)
            .setPositiveButton(R.string.stream_ui_poll_add_comment_dialog_submit) { _, _ ->
                val text = binding.answerInput.text?.toString()?.trim().orEmpty()
                if (text.isNotEmpty()) {
                    parentFragmentManager.setFragmentResult(
                        REQUEST_KEY,
                        bundleOf(
                            BUNDLE_KEY_MESSAGE_ID to messageId,
                            BUNDLE_KEY_POLL_ID to pollId,
                            BUNDLE_KEY_ANSWER to text,
                        ),
                    )
                }
            }
            .setNegativeButton(R.string.stream_ui_poll_add_comment_dialog_cancel, null)
            .create()

        dialog.setOnShowListener {
            val confirm = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            confirm.isEnabled = binding.answerInput.text?.toString()?.trim()?.isNotEmpty() == true
            binding.answerInput.doAfterTextChanged { editable ->
                confirm.isEnabled = editable?.toString()?.trim()?.isNotEmpty() == true
            }
            binding.answerInput.requestFocus()
            dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }

        return dialog
    }

    companion object {
        const val TAG: String = "AddPollCommentDialogFragment"
        const val REQUEST_KEY: String = "stream_ui_add_poll_comment_request"
        const val BUNDLE_KEY_MESSAGE_ID: String = "stream_ui_add_poll_comment_message_id"
        const val BUNDLE_KEY_POLL_ID: String = "stream_ui_add_poll_comment_poll_id"
        const val BUNDLE_KEY_ANSWER: String = "stream_ui_add_poll_comment_answer"

        private const val ARG_MESSAGE_ID: String = "arg_message_id"
        private const val ARG_POLL_ID: String = "arg_poll_id"
        private const val ARG_INITIAL_TEXT: String = "arg_initial_text"

        fun newInstance(
            messageId: String,
            pollId: String,
            initialText: String? = null,
        ): AddPollCommentDialogFragment =
            AddPollCommentDialogFragment().apply {
                arguments = bundleOf(
                    ARG_MESSAGE_ID to messageId,
                    ARG_POLL_ID to pollId,
                    ARG_INITIAL_TEXT to initialText,
                )
            }
    }
}
