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

package io.getstream.chat.ui.sample.feature.componentbrowser.messages.header

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.getstream.chat.android.models.Member
import io.getstream.chat.ui.sample.databinding.FragmentComponentBrowserMessagesHeaderViewBinding
import io.getstream.chat.ui.sample.feature.componentbrowser.utils.randomChannel
import io.getstream.chat.ui.sample.feature.componentbrowser.utils.randomMember
import io.getstream.chat.ui.sample.feature.componentbrowser.utils.randomUser
import io.getstream.chat.ui.sample.feature.componentbrowser.utils.randomUsers

class ComponentBrowserMessagesHeaderViewFragment : Fragment() {
    private var _binding: FragmentComponentBrowserMessagesHeaderViewBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentComponentBrowserMessagesHeaderViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.headerOnlineStatus.apply {
            showBackButtonBadge("23")
            setTitle("Chat title")
            setOnlineStateSubtitle("Chat status")
            setAvatar(randomChannel())
            showOnlineStateSubtitle()
        }
        binding.headerOnlineLongBadge.apply {
            showBackButtonBadge("2334")
            setTitle("Chat title")
            setOnlineStateSubtitle("Chat status")
            setAvatar(randomChannel())
            showOnlineStateSubtitle()
        }
        binding.headerOnlineNoBadgeStatus.apply {
            setTitle("Chat title")
            setOnlineStateSubtitle("Chat status")
            setAvatar(randomChannel(listOf(randomMember())))
            showBackButtonBadge("")
            showOnlineStateSubtitle()
        }
        binding.headerOnlineAvatar.apply {
            setTitle("Chat title")
            setOnlineStateSubtitle("Chat status")
            val channel = randomChannel().copy(
                type = "messaging",
                id = "!members-p28xtFuiwKzLG4IgCFCN0v1WQSwTGH0ZcnINefSxdS4",
                members = listOf(Member(user = randomUser(isOnline = true))),
            )
            setAvatar(channel)
            showOnlineStateSubtitle()
        }
        binding.headerOfflineAvatar.apply {
            setTitle("Chat title")
            setOnlineStateSubtitle("Chat status")
            setAvatar(randomChannel(listOf(randomMember())))
            showOnlineStateSubtitle()
        }
        binding.headerSearchingForNetwork.apply {
            setTitle("Chat title")
            setOnlineStateSubtitle("Chat status")
            setAvatar(randomChannel(listOf(randomMember())))
            showSearchingForNetworkLabel()
        }
        binding.headerDeviceOffline.apply {
            setTitle("Chat title")
            setOnlineStateSubtitle("Chat status")
            setAvatar(randomChannel(listOf(randomMember())))
            showOfflineStateLabel()
        }
        binding.headerTypingSubtitle.apply {
            setTitle("Chat title")
            setOnlineStateSubtitle("Chat status")
            setAvatar(randomChannel(listOf(randomMember())))
            showTypingStateLabel(randomUsers(3))
        }
    }
}
