package io.getstream.chat.ui.sample.feature.component_browser.channels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.getstream.chat.ui.sample.databinding.FragmentComponentBrowserChannelsHeaderViewBinding
import io.getstream.chat.ui.sample.feature.component_browser.utils.randomUser

class ComponentBrowserChannelsHeaderViewFragment : Fragment() {

    private var _binding: FragmentComponentBrowserChannelsHeaderViewBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentComponentBrowserChannelsHeaderViewBinding.inflate(inflater, container, false)
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

        binding.headerWithoutStatus.setUser(randomUser())
        binding.headerWithoutStatus.hideTitle()

        binding.headerWithoutUserAvatar.setUser(randomUser())
        binding.headerWithoutUserAvatar.showOfflineTitle()

        binding.headerWithoutAddChannel.setUser(randomUser())
        binding.headerWithoutAddChannel.showOfflineTitle()

        binding.headerWithoutUserAvatarAndAddChannel.setUser(randomUser())
        binding.headerWithoutUserAvatarAndAddChannel.showOfflineTitle()
    }
}
