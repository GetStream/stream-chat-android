package io.getstream.chat.ui.sample.feature.component_browser.typing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.getstream.chat.ui.sample.databinding.FragmentComponentBrowserTypingIndicatorBinding
import io.getstream.chat.ui.sample.feature.component_browser.utils.randomUsers

class ComponentBrowserTypingIndicatorFragment : Fragment() {

    private var _binding: FragmentComponentBrowserTypingIndicatorBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentComponentBrowserTypingIndicatorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.noTypingsView.setTypingUsers(emptyList())
        binding.oneUserTypingView.setTypingUsers(randomUsers(size = 1))
        binding.multipleUsersTypingView.setTypingUsers(randomUsers(size = 5))
    }
}
