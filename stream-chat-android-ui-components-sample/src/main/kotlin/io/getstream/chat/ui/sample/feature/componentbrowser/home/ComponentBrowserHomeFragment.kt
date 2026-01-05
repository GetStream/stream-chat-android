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

package io.getstream.chat.ui.sample.feature.componentbrowser.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.ReactionGroup
import io.getstream.chat.android.ui.helper.SupportedReactions.DefaultReactionTypes.LOL
import io.getstream.chat.android.ui.helper.SupportedReactions.DefaultReactionTypes.LOVE
import io.getstream.chat.android.ui.helper.SupportedReactions.DefaultReactionTypes.THUMBS_UP
import io.getstream.chat.android.ui.helper.SupportedReactions.DefaultReactionTypes.WUT
import io.getstream.chat.ui.sample.R
import io.getstream.chat.ui.sample.common.navigateSafely
import io.getstream.chat.ui.sample.databinding.FragmentComponentBrowserHomeBinding
import io.getstream.chat.ui.sample.feature.componentbrowser.utils.randomChannel
import io.getstream.chat.ui.sample.feature.componentbrowser.utils.randomMember
import io.getstream.chat.ui.sample.feature.componentbrowser.utils.randomMessage
import io.getstream.chat.ui.sample.feature.componentbrowser.utils.randomUser
import java.util.Date

@InternalStreamChatApi
class ComponentBrowserHomeFragment : Fragment() {

    private var _binding: FragmentComponentBrowserHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentComponentBrowserHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAvatarView()
        setupChannelsHeaderView()
        setupMessagesHeaderView()
        setupSearchView()
        setupViewReactionsView()
        setupEditReactionsView()
        setupUserReactionsView()
        setupMessageList()
        setupTypingIndicator()
    }

    private fun setupMessageList() {
        binding.messageListComponentBrowser.setOnClickListener {
            findNavController().navigateSafely(R.id.action_componentBrowserHomeFragment_to_componentBrowserMessageListBrowserFragment)
        }
    }

    private fun setupTypingIndicator() {
        binding.typingIndicatorBrowser.setOnClickListener {
            findNavController().navigateSafely(R.id.action_componentBrowserHomeFragment_to_componentBrowserTypingIndicatorFragment)
        }
    }

    private fun setupAvatarView() {
        binding.channelAvatarView.setChannel(randomChannel(listOf(randomMember())))
        binding.avatarViewContainer.setOnClickListener {
            findNavController().navigateSafely(R.id.action_componentBrowserHomeFragment_to_componentBrowserAvatarViewFragment)
        }
    }

    private fun setupChannelsHeaderView() {
        binding.channelListHeaderView.setUser(randomUser())
        binding.channelListHeaderView.showOnlineTitle()
        binding.channelListHeaderViewContainer.setOnClickListener {
            findNavController().navigateSafely(R.id.action_componentBrowserHomeFragment_to_componentBrowserChannelsHeaderViewFragment)
        }
    }

    private fun setupMessagesHeaderView() {
        binding.messagesHeaderView.setAvatar(randomChannel(listOf(randomMember())))
        binding.messagesHeaderView.showBackButtonBadge("5")
        binding.messagesHeaderView.setTitle("Chat title")
        binding.messagesHeaderView.setOnlineStateSubtitle("Last active 10 min ago")
        binding.messagesHeaderViewContainer.setOnClickListener {
            findNavController().navigateSafely(R.id.action_componentBrowserHomeFragment_to_componentBrowserMessagesHeaderFragment)
        }
    }

    private fun setupSearchView() {
        binding.searchViewContainer.setOnClickListener {
            findNavController().navigateSafely(R.id.action_componentBrowserHomeFragment_to_componentBrowserSearchViewFragment)
        }
    }

    private fun setupViewReactionsView() {
        binding.viewReactionsView.setMessage(
            message = randomMessage().copy(
                reactionGroups = mutableMapOf(
                    LOVE to ReactionGroup(
                        type = LOVE,
                        count = 10,
                        sumScore = 10,
                        firstReactionAt = Date(),
                        lastReactionAt = Date(),
                    ),
                    WUT to ReactionGroup(
                        type = WUT,
                        count = 20,
                        sumScore = 20,
                        firstReactionAt = Date(),
                        lastReactionAt = Date(),
                    ),
                    LOL to ReactionGroup(
                        type = LOL,
                        count = 20,
                        sumScore = 20,
                        firstReactionAt = Date(),
                        lastReactionAt = Date(),
                    ),
                    THUMBS_UP to ReactionGroup(
                        type = THUMBS_UP,
                        count = 20,
                        sumScore = 20,
                        firstReactionAt = Date(),
                        lastReactionAt = Date(),
                    ),
                ),
                ownReactions = mutableListOf(
                    Reaction(type = LOVE),
                    Reaction(type = WUT),
                ),
            ),
            isMyMessage = true,
        )
        binding.viewReactionsViewContainer.setOnClickListener {
            findNavController().navigateSafely(R.id.action_componentBrowserHomeFragment_to_componentBrowserViewReactionsFragment)
        }
    }

    private fun setupEditReactionsView() {
        binding.editReactionsView.setMessage(
            message = randomMessage().copy(
                ownReactions = mutableListOf(
                    Reaction(type = LOVE),
                    Reaction(type = WUT),
                ),
            ),
            isMyMessage = false,
        )
        binding.editReactionsViewContainer.setOnClickListener {
            findNavController().navigateSafely(R.id.action_componentBrowserHomeFragment_to_componentBrowserEditReactionsFragment)
        }
    }

    private fun setupUserReactionsView() {
        val currentUser = randomUser()
        binding.userReactionsView.setMessage(
            message = randomMessage().copy(
                latestReactions = mutableListOf(
                    Reaction(type = LOVE, user = currentUser),
                    Reaction(type = WUT, user = randomUser()),
                ),
            ),
            currentUser = currentUser,
        )
        binding.userReactionsViewContainer.setOnClickListener {
            findNavController().navigateSafely(R.id.action_componentBrowserHomeFragment_to_componentBrowserUserReactionsFragment)
        }
    }
}
