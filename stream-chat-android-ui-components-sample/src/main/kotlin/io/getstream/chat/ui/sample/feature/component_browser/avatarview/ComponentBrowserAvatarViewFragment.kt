package io.getstream.chat.ui.sample.feature.component_browser.avatarview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.getstream.chat.ui.sample.databinding.FragmentComponentBrowserAvatarViewBinding
import io.getstream.chat.ui.sample.feature.component_browser.utils.randomChannel
import io.getstream.chat.ui.sample.feature.component_browser.utils.randomUser

class ComponentBrowserAvatarViewFragment : Fragment() {

    private var _binding: FragmentComponentBrowserAvatarViewBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
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

        binding.avatarViewSmall.setUserData(randomUser())
        binding.avatarViewMedium.setUserData(randomUser())
        binding.avatarViewLarge.setUserData(randomUser())

        binding.avatarView1.setChannelData(
            randomChannel(),
            listOf(
                randomUser(),
            ),
        )
        binding.avatarView2.setChannelData(
            randomChannel(),
            listOf(
                randomUser(),
                randomUser(),
            ),
        )
        binding.avatarView3.setChannelData(
            randomChannel(),
            listOf(
                randomUser(),
                randomUser(),
                randomUser(),
            )
        )
        binding.avatarView4.setChannelData(
            randomChannel(),
            listOf(
                randomUser(),
                randomUser(),
                randomUser(),
                randomUser(),
            )
        )

        binding.avatarViewMissing1.setChannelData(
            randomChannel(),
            listOf(
                randomUser(),
                randomUser(withImage = false),
                randomUser(),
                randomUser(),
            )
        )
        binding.avatarViewMissing2.setChannelData(
            randomChannel(),
            listOf(
                randomUser(),
                randomUser(withImage = false),
                randomUser(withImage = false),
                randomUser(),
            )
        )
        binding.avatarViewMissing3.setChannelData(
            randomChannel(),
            listOf(
                randomUser(),
                randomUser(withImage = false),
                randomUser(withImage = false),
                randomUser(withImage = false),
            )
        )

        binding.avatarViewSmallIndicator.apply {
            setUserData(randomUser(isOnline = true))
        }
        binding.avatarViewMediumIndicator.apply {
            setUserData(randomUser(isOnline = true))
        }
        binding.avatarViewLargeIndicator.apply {
            setUserData(randomUser(isOnline = true))
        }
    }
}
