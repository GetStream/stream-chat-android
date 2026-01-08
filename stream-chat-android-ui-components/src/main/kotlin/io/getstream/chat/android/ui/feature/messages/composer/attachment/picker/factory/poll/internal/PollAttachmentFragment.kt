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

package io.getstream.chat.android.ui.feature.messages.composer.attachment.picker.factory.poll.internal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.getstream.chat.android.models.PollConfig
import io.getstream.chat.android.ui.databinding.StreamUiFragmentAttachmentPollBinding
import io.getstream.chat.android.ui.feature.messages.composer.attachment.picker.factory.AttachmentsPickerTabListener
import io.getstream.chat.android.ui.feature.messages.composer.attachment.picker.poll.CreatePollDialogFragment
import io.getstream.chat.android.ui.utils.extensions.getFragmentManager
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater

/**
 * Represents the tab of the attachment picker.
 */
internal class PollAttachmentFragment : Fragment() {

    private var _binding: StreamUiFragmentAttachmentPollBinding? = null
    private val binding get() = _binding!!

    /**
     * A listener invoked when attachments are selected in the attachment tab.
     */
    private var attachmentsPickerTabListener: AttachmentsPickerTabListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding =
            StreamUiFragmentAttachmentPollBinding.inflate(requireContext().streamThemeInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        context.getFragmentManager()?.let {
            CreatePollDialogFragment.newInstance(object : CreatePollDialogFragment.CreatePollDialogListener {
                override fun onCreatePoll(pollConfig: PollConfig) {
                    attachmentsPickerTabListener?.onPollSubmitted(pollConfig)
                }

                override fun onDismiss() {
                    attachmentsPickerTabListener?.onPollSubmitted(null)
                }
            })
                .show(it, CreatePollDialogFragment.TAG)
        }
    }

    /**
     * Sets the listener invoked when attachments are selected in the attachment tab.
     */
    fun setAttachmentsPickerTabListener(attachmentsPickerTabListener: AttachmentsPickerTabListener) {
        this.attachmentsPickerTabListener = attachmentsPickerTabListener
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        /**
         * Creates a new instance of [PollAttachmentFragment].
         *
         * @return A new instance of the Fragment.
         */
        fun newInstance(): PollAttachmentFragment {
            return PollAttachmentFragment()
        }
    }
}
