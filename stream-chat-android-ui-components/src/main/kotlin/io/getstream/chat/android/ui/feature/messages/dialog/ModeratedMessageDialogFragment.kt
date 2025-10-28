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

package io.getstream.chat.android.ui.feature.messages.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.ui.common.state.messages.list.DeleteMessage
import io.getstream.chat.android.ui.common.state.messages.list.EditMessage
import io.getstream.chat.android.ui.common.state.messages.list.ModeratedMessageOption
import io.getstream.chat.android.ui.common.state.messages.list.SendAnyway
import io.getstream.chat.android.ui.databinding.StreamUiDialogModeratedMessageBinding
import io.getstream.chat.android.ui.widgets.FullScreenDialogFragment

/**
 * Dialog that is shown when the user selects a moderated message. The options the user can select
 * are to send the message anyway, edit it or to delete it.
 */
internal class ModeratedMessageDialogFragment : FullScreenDialogFragment() {

    private var _binding: StreamUiDialogModeratedMessageBinding? = null
    private val binding: StreamUiDialogModeratedMessageBinding get() = _binding!!

    /**
     * The moderated message that the user can act upon.
     */
    private lateinit var message: Message

    /**
     * Handler that notifies of a selected dialog option.
     */
    private var selectionHandler: DialogSelectionHandler? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = StreamUiDialogModeratedMessageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState == null && ::message.isInitialized) {
            setupDialog()
        } else {
            // The process has been killed
            dismiss()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    /**
     * Initializes the dialog.
     */
    private fun setupDialog() {
        setupDismissalArea()
        setupClickListeners()
    }

    /**
     * Sets up the root click listener so taps outside the dialog dismiss it.
     */
    private fun setupDismissalArea() {
        binding.container.setOnClickListener {
            dismiss()
        }
    }

    /**
     * Initialisation of click listeners for dialog options.
     */
    private fun setupClickListeners() {
        with(binding) {
            sendAnyway.setOnClickListener {
                selectionHandler?.onModeratedOptionSelected(message, SendAnyway)
                dismiss()
            }
            editMessage.setOnClickListener {
                selectionHandler?.onModeratedOptionSelected(message, EditMessage)
                dismiss()
            }
            deleteMessage.setOnClickListener {
                selectionHandler?.onModeratedOptionSelected(message, DeleteMessage)
                dismiss()
            }
        }
    }

    /**
     * Set the handler for listening to dialog options selection.
     *
     * @param selectionHandler The handler to set.
     */
    fun setDialogSelectionHandler(selectionHandler: DialogSelectionHandler) {
        this.selectionHandler = selectionHandler
    }

    /**
     * Initializes the dialog with the moderated message.
     *
     * @param message The moderated [Message].
     */
    fun setMessage(message: Message) {
        this.message = message
    }

    /**
     * Handler that notifies when a moderated message option is selected.
     */
    interface DialogSelectionHandler {
        /**
         * @param message The moderated [Message] upon which a user can take action.
         */
        fun onModeratedOptionSelected(message: Message, action: ModeratedMessageOption)
    }

    companion object {
        const val TAG = "ModeratedMessageDialog"

        /**
         * Creates a new instance of [ModeratedMessageDialogFragment].
         *
         * @param message The moderated message.
         */
        fun newInstance(message: Message): ModeratedMessageDialogFragment = ModeratedMessageDialogFragment().apply {
            setMessage(message)
        }
    }
}
