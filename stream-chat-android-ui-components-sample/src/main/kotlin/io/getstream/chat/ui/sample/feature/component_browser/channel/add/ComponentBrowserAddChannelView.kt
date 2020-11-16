package io.getstream.chat.ui.sample.feature.component_browser.channel.add

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.getstream.chat.ui.sample.databinding.FragmentComponentBrowserAddChannelViewBinding
import io.getstream.chat.ui.sample.feature.component_browser.utils.randomUsers

class ComponentBrowserAddChannelView : Fragment() {

    private var _binding: FragmentComponentBrowserAddChannelViewBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentComponentBrowserAddChannelViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.addChannelView.setUsers(randomUsers())
    }
}
