package io.getstream.chat.ui.sample.feature.component_browser.channels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.image
import io.getstream.chat.android.client.models.name
import io.getstream.chat.ui.sample.application.AppConfig
import io.getstream.chat.ui.sample.databinding.FragmentComponentBrowserChannelsHeaderViewBinding

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
        val sampleUser = AppConfig.availableUsers.first()
        val user = User().apply {
            id = sampleUser.id
            image = sampleUser.image
            name = sampleUser.name
        }
        binding.headerOnlineStatus.setUser(user)
        binding.headerOnlineStatus.showOnlineTitle()

        binding.headerOfflineStatus.setUser(user)
        binding.headerOfflineStatus.showOfflineTitle()

        binding.headerOfflineStatusWithoutProgress.setUser(user)
        binding.headerOfflineStatusWithoutProgress.showOfflineTitle()

        binding.headerWithoutStatus.setUser(user)
        binding.headerWithoutStatus.hideTitle()

        binding.headerWithoutUserAvatar.setUser(user)
        binding.headerWithoutUserAvatar.showOfflineTitle()

        binding.headerWithoutAddChannel.setUser(user)
        binding.headerWithoutAddChannel.showOfflineTitle()

        binding.headerWithoutUserAvatarAndAddChannel.setUser(user)
        binding.headerWithoutUserAvatarAndAddChannel.showOfflineTitle()
    }
}
