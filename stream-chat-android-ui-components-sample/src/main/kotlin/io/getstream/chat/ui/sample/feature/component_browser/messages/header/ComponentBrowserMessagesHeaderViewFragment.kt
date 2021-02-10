package io.getstream.chat.ui.sample.feature.component_browser.messages.header

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.ui.sample.databinding.FragmentComponentBrowserMessagesHeaderViewBinding
import io.getstream.chat.ui.sample.feature.component_browser.utils.randomChannel
import io.getstream.chat.ui.sample.feature.component_browser.utils.randomMember
import io.getstream.chat.ui.sample.feature.component_browser.utils.randomUser
import io.getstream.chat.ui.sample.feature.component_browser.utils.randomUsers

class ComponentBrowserMessagesHeaderViewFragment : Fragment() {
    private var _binding: FragmentComponentBrowserMessagesHeaderViewBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
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
        }
        binding.headerOnlineLongBadge.apply {
            showBackButtonBadge("2334")
            setTitle("Chat title")
            setOnlineStateSubtitle("Chat status")
            setAvatar(randomChannel())
        }
        binding.headerOnlineNoBadgeStatus.apply {
            setTitle("Chat title")
            setOnlineStateSubtitle("Chat status")
            setAvatar(randomChannel(listOf(randomMember())))
            showBackButtonBadge("")
        }
        binding.headerOnlineAvatar.apply {
            setTitle("Chat title")
            setOnlineStateSubtitle("Chat status")
            val channel = randomChannel().apply {
                cid = "messaging:!members-p28xtFuiwKzLG4IgCFCN0v1WQSwTGH0ZcnINefSxdS4"
                members = listOf(Member(user = randomUser(isOnline = true)))
            }
            setAvatar(channel)
        }
        binding.headerOfflineAvatar.apply {
            setTitle("Chat title")
            setOnlineStateSubtitle("Chat status")
            setAvatar(randomChannel(listOf(randomMember())))
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
