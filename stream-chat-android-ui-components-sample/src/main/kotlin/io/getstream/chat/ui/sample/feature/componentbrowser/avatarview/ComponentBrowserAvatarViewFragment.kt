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

package io.getstream.chat.ui.sample.feature.componentbrowser.avatarview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.getstream.chat.ui.sample.databinding.FragmentComponentBrowserAvatarViewBinding
import io.getstream.chat.ui.sample.feature.componentbrowser.utils.randomChannel
import io.getstream.chat.ui.sample.feature.componentbrowser.utils.randomMember
import io.getstream.chat.ui.sample.feature.componentbrowser.utils.randomUser

class ComponentBrowserAvatarViewFragment : Fragment() {

    private var _binding: FragmentComponentBrowserAvatarViewBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentComponentBrowserAvatarViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.avatarViewSmall.setUser(randomUser())
        binding.avatarViewMedium.setUser(randomUser())
        binding.avatarViewLarge.setUser(randomUser())

        binding.avatarView1.setChannel(
            randomChannel(
                listOf(randomMember()),
            ),
        )
        binding.avatarView2.setChannel(
            randomChannel(
                listOf(
                    randomMember(),
                    randomMember(),
                ),
            ),
        )
        binding.avatarView3.setChannel(
            randomChannel(
                listOf(
                    randomMember(),
                    randomMember(),
                    randomMember(),
                ),
            ),
        )
        binding.avatarView4.setChannel(
            randomChannel(
                listOf(
                    randomMember(),
                    randomMember(),
                    randomMember(),
                    randomMember(),
                ),
            ),
        )

        binding.avatarViewMissing1.setChannel(
            randomChannel(
                listOf(
                    randomMember(),
                    randomMember(withImage = false),
                    randomMember(),
                    randomMember(),
                ),
            ),
        )
        binding.avatarViewMissing2.setChannel(
            randomChannel(
                listOf(
                    randomMember(),
                    randomMember(withImage = false),
                    randomMember(withImage = false),
                    randomMember(),
                ),
            ),
        )
        binding.avatarViewMissing3.setChannel(
            randomChannel(
                listOf(
                    randomMember(),
                    randomMember(withImage = false),
                    randomMember(withImage = false),
                    randomMember(withImage = false),
                ),
            ),
        )
        binding.avatarViewMissing4.setChannel(
            randomChannel(
                listOf(randomMember(withImage = false)),
            ),
        )

        val user1 = randomUser(isOnline = true)
        val user2 = randomUser(isOnline = true)
        val user3 = randomUser(isOnline = true)
        binding.avatarViewSmallIndicator.apply {
            setUser(user1)
        }
        binding.avatarViewMediumIndicator.apply {
            setUser(user2)
        }
        binding.avatarViewLargeIndicator.apply {
            setUser(user3)
        }
        binding.avatarViewIndicatorTopLeft.apply {
            setUser(user2)
        }
        binding.avatarViewIndicatorBottomLeft.apply {
            setUser(user2)
        }
        binding.avatarViewIndicatorTopRight.apply {
            setUser(user2)
        }
        binding.avatarViewIndicatorBottomRight.apply {
            setUser(user2)
        }
        binding.avatarViewSmallIndicatorBorder.apply {
            setUser(user1)
        }
        binding.avatarViewMediumIndicatorBorder.apply {
            setUser(user2)
        }
        binding.avatarViewLargeIndicatorBorder.apply {
            setUser(user3)
        }
        binding.avatarViewSmallColors.apply {
            setUser(user1)
        }
        binding.avatarViewMediumColors.apply {
            setUser(user2)
        }
        binding.avatarViewLargeColors.apply {
            setUser(user3)
        }
    }
}
