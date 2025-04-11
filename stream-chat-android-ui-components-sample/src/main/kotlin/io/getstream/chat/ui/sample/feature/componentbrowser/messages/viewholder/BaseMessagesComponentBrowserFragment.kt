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

package io.getstream.chat.ui.sample.feature.componentbrowser.messages.viewholder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItem
import io.getstream.chat.android.ui.model.MessageListItemWrapper
import io.getstream.chat.ui.sample.databinding.FragmentComponentBrowserMessageListViewHolderBinding
import io.getstream.chat.ui.sample.feature.componentbrowser.utils.randomUser

abstract class BaseMessagesComponentBrowserFragment : Fragment() {
    private var _binding: FragmentComponentBrowserMessageListViewHolderBinding? = null
    protected val binding get() = _binding!!

    protected val currentUser = randomUser()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentComponentBrowserMessageListViewHolderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.messageListView.apply {
            setMessageClickListener {}
            setMessageLongClickListener {}
            setMessageRetryListener {}
            setThreadClickListener {}
            setAttachmentClickListener { _, _ -> }
            setAttachmentDownloadClickListener {}
            setReactionViewClickListener {}
            setUserClickListener {}
            setModeratedMessageLongClickListener {}

            init(Channel())
            displayNewMessages(MessageListItemWrapper(getItems()))
        }
    }

    protected abstract fun getItems(): List<MessageListItem>
}
