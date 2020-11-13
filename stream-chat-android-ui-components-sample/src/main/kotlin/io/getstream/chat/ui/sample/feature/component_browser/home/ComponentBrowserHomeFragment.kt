package io.getstream.chat.ui.sample.feature.component_browser.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.getstream.sdk.chat.ChatUI
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.image
import io.getstream.chat.android.client.models.name
import io.getstream.chat.ui.sample.R
import io.getstream.chat.ui.sample.application.AppConfig
import io.getstream.chat.ui.sample.common.navigateSafely
import io.getstream.chat.ui.sample.databinding.FragmentComponentBrowserHomeBinding
import io.getstream.chat.ui.sample.feature.component_browser.avatarview.ComponentBrowserAvatarViewFragment

class ComponentBrowserHomeFragment : Fragment() {

    private var _binding: FragmentComponentBrowserHomeBinding? = null
    private val binding get() = _binding!!

    private val user: User

    init {
        val sampleUser = AppConfig.availableUsers.first()
        user = User().apply {
            id = sampleUser.id
            image = sampleUser.image
            name = sampleUser.name
        }
    }

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
        setupSearchView()
        setupAddChannelView()
    }

    private fun setupAvatarView() {
        binding.avatarView.setChannelData(
            ComponentBrowserAvatarViewFragment.randomChannel(),
            listOf(
                ComponentBrowserAvatarViewFragment.randomUser(),
            ),
        )
        binding.avatarViewContainer.setOnClickListener {
            findNavController().navigateSafely(R.id.action_componentBrowserHomeFragment_to_componentBrowserAvatarViewFragment)
        }
    }

    private fun setupChannelsHeaderView() {
        binding.channelsHeaderView.setUser(user)
        binding.channelsHeaderView.showOnlineTitle()
        binding.channelsHeaderViewContainer.setOnClickListener {
            findNavController().navigateSafely(R.id.action_componentBrowserHomeFragment_to_componentBrowserChannelsHeaderViewFragment)
        }
    }

    private fun setupSearchView() {
        binding.searchViewContainer.setOnClickListener {
            findNavController().navigateSafely(R.id.action_componentBrowserHomeFragment_to_componentBrowserSearchViewFragment)
        }
    }

    private fun setupAddChannelView() {
        binding.addChannelViewContainer.setOnClickListener {
            findNavController().navigateSafely(R.id.action_componentBrowserHomeFragment_to_componentBrowserAddChannelViewFragment)
        }
    }
}
