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
import io.getstream.chat.ui.sample.feature.component_browser.avatarview.ComponentBrowserAvatarViewFragment

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
        setupSearchView()
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

    private fun setupSearchView() {
        binding.searchViewContainer.setOnClickListener {
            findNavController().navigateSafely(R.id.action_componentBrowserHomeFragment_to_componentBrowserSearchViewFragment)
        }
    }
}
