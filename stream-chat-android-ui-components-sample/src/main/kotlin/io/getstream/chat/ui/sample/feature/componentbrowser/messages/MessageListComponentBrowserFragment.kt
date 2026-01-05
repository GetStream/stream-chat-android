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

package io.getstream.chat.ui.sample.feature.componentbrowser.messages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import io.getstream.chat.ui.sample.R
import io.getstream.chat.ui.sample.common.navigateSafely
import io.getstream.chat.ui.sample.databinding.FragmentComponentBrowserMessageListBinding

class MessageListComponentBrowserFragment : Fragment() {
    private var _binding: FragmentComponentBrowserMessageListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentComponentBrowserMessageListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.deletedMessages.setOnClickListener {
            findNavController().navigateSafely(R.id.action_componentBrowserMessageList_to_componentBrowserDeletedMessages)
        }
        binding.dateDivider.setOnClickListener {
            findNavController().navigateSafely(R.id.action_componentBrowserMessageList_to_componentBrowserDateDividerFragment)
        }
        binding.plainTextMessages.setOnClickListener {
            findNavController().navigateSafely(R.id.action_componentBrowserMessageList_to_componentBrowserPlainTextMessages)
        }
        binding.onlyMediaAttachments.setOnClickListener {
            findNavController().navigateSafely(R.id.action_componentBrowserMessageList_to_componentBrowserOnlyMediaAttachmentsMessages)
        }
        binding.plainTextWithMediaAttachments.setOnClickListener {
            findNavController().navigateSafely(R.id.action_componentBrowserMessageList_to_componentBrowserPlainTextWithMediaAttachmentsMessages)
        }
        binding.onlyFileAttachments.setOnClickListener {
            findNavController().navigateSafely(R.id.action_componentBrowserMessageList_to_componentBrowserOnlyFileAttachmentsMessages)
        }
        binding.plainTextWithFileAttachments.setOnClickListener {
            findNavController().navigateSafely(R.id.action_componentBrowserMessageList_to_componentBrowserPlainTextWithFileAttachmentsMessages)
        }
        binding.giphyMessage.setOnClickListener {
            findNavController().navigateSafely(R.id.action_componentBrowserMessageList_to_componentBrowserGiphyMessages)
        }
        binding.repliedMessages.setOnClickListener {
            findNavController().navigateSafely(R.id.action_componentBrowserMessageList_to_componentBrowserRepliedMessages)
        }
    }
}
