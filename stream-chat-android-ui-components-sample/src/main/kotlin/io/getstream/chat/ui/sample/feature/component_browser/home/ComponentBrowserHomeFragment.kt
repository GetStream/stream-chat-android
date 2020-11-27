package io.getstream.chat.ui.sample.feature.component_browser.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.getstream.sdk.chat.ChatUI
import io.getstream.chat.ui.sample.R
import io.getstream.chat.ui.sample.common.navigateSafely
import io.getstream.chat.ui.sample.databinding.FragmentComponentBrowserHomeBinding
import io.getstream.chat.ui.sample.feature.component_browser.utils.randomChannel
import io.getstream.chat.ui.sample.feature.component_browser.utils.randomUser
import io.getstream.chat.ui.sample.feature.component_browser.utils.randomUsers

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
        savedInstanceState: Bundle?
    ): View? {
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
        setupAttachmentGallery()
    }

    private fun setupAvatarView() {
        binding.avatarView.setChannelData(randomChannel(), randomUsers(size = 1))
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
        binding.messagesHeaderView.setAvatar(randomChannel(), listOf(randomUser()))
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

    private fun setupAttachmentGallery() {
        binding.attachmentGalleryMultiPictureLabel.setOnClickListener {
            arrayOf(
                "https://getstream.io/random_png/?id=80c26629-bc25-4ee5-a8ae-4824f8097b53&name=chani",
                "https://getstream.io/random_png/?id=80c26629-bc25-4ee5-a8ae-4824f8097b53&name=paul",
                "https://getstream.io/random_png/?id=80c26629-bc25-4ee5-a8ae-4824f8097b53&name=letto",
                "https://getstream.io/random_png/?id=80c26629-bc25-4ee5-a8ae-4824f8097b53&name=duncan"
            )
                .let(
                    ComponentBrowserHomeFragmentDirections
                    ::actionComponentBrowserHomeFragmentToComponentBrowserAttachmentGalleryFragment
                )
                .let(findNavController()::navigateSafely)
        }

        binding.attachmentGalleryOnePictureLabel.setOnClickListener {
            arrayOf("https://getstream.io/random_png/?id=80c26629-bc25-4ee5-a8ae-4824f8097b53&name=chani")
                .let(
                    ComponentBrowserHomeFragmentDirections
                    ::actionComponentBrowserHomeFragmentToComponentBrowserAttachmentGalleryFragment
                )
                .let(findNavController()::navigateSafely)
        }
    }
}
