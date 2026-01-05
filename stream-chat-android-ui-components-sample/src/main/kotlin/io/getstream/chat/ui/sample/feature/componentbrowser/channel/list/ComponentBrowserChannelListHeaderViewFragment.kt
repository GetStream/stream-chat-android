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

package io.getstream.chat.ui.sample.feature.componentbrowser.channel.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.getstream.chat.ui.sample.databinding.FragmentComponentBrowserChannelListHeaderViewBinding
import io.getstream.chat.ui.sample.feature.componentbrowser.utils.randomUser

class ComponentBrowserChannelListHeaderViewFragment : Fragment() {

    private var _binding: FragmentComponentBrowserChannelListHeaderViewBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentComponentBrowserChannelListHeaderViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.headerOnlineStatus.setUser(randomUser())
        binding.headerOnlineStatus.showOnlineTitle()

        binding.headerOfflineStatus.setUser(randomUser())
        binding.headerOfflineStatus.showOfflineTitle()

        binding.headerOfflineStatusWithoutProgress.setUser(randomUser())
        binding.headerOfflineStatusWithoutProgress.showOfflineTitle()

        binding.headerWithoutUserAvatar.setUser(randomUser())
        binding.headerWithoutUserAvatar.showOfflineTitle()

        binding.headerWithoutAddChannel.setUser(randomUser())
        binding.headerWithoutAddChannel.showOfflineTitle()

        binding.headerWithoutUserAvatarAndAddChannel.setUser(randomUser())
        binding.headerWithoutUserAvatarAndAddChannel.showOfflineTitle()
    }
}
