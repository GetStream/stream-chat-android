package io.getstream.chat.ui.sample.feature.component_browser.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.getstream.sdk.chat.ChatUI
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.ui.common.ReactionType
import io.getstream.chat.ui.sample.R
import io.getstream.chat.ui.sample.common.navigateSafely
import io.getstream.chat.ui.sample.databinding.FragmentComponentBrowserHomeBinding
import io.getstream.chat.ui.sample.feature.component_browser.utils.randomChannel
import io.getstream.chat.ui.sample.feature.component_browser.utils.randomMember
import io.getstream.chat.ui.sample.feature.component_browser.utils.randomMessage
import io.getstream.chat.ui.sample.feature.component_browser.utils.randomUser

@InternalStreamChatApi
class ComponentBrowserHomeFragment : Fragment() {

    private var _binding: FragmentComponentBrowserHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Init ChatUI to have access to fonts
        ChatUI.Builder(requireContext().applicationContext).build()
    }

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
        binding.avatarView.setChannelData(randomChannel(listOf(randomMember())))
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
            message = randomMessage().apply {
                reactionCounts = mutableMapOf(
                    ReactionType.LOVE.type to 10,
                    ReactionType.WUT.type to 20,
                    ReactionType.LOL.type to 20,
                    ReactionType.THUMBS_UP.type to 20
                )
                ownReactions = mutableListOf(
                    Reaction(type = ReactionType.LOVE.type),
                    Reaction(type = ReactionType.WUT.type)
                )
            },
            isMyMessage = true
        )
        binding.viewReactionsViewContainer.setOnClickListener {
            findNavController().navigateSafely(R.id.action_componentBrowserHomeFragment_to_componentBrowserViewReactionsFragment)
        }
    }

    private fun setupEditReactionsView() {
        binding.editReactionsView.setMessage(
            message = randomMessage().apply {
                ownReactions = mutableListOf(
                    Reaction(type = ReactionType.LOVE.type),
                    Reaction(type = ReactionType.WUT.type)
                )
            },
            isMyMessage = false
        )
        binding.editReactionsViewContainer.setOnClickListener {
            findNavController().navigateSafely(R.id.action_componentBrowserHomeFragment_to_componentBrowserEditReactionsFragment)
        }
    }

    private fun setupUserReactionsView() {
        val currentUser = randomUser()
        binding.userReactionsView.setMessage(
            message = randomMessage().apply {
                latestReactions = mutableListOf(
                    Reaction(type = ReactionType.LOVE.type, user = currentUser),
                    Reaction(type = ReactionType.LOVE.type, user = randomUser()),
                )
            },
            currentUser = currentUser
        )
        binding.userReactionsViewContainer.setOnClickListener {
            findNavController().navigateSafely(R.id.action_componentBrowserHomeFragment_to_componentBrowserUserReactionsFragment)
        }
    }
}
