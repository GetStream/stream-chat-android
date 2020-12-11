package io.getstream.chat.ui.sample.feature.component_browser.messages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.getstream.chat.ui.sample.databinding.FragmentComponentBrowserScrollButtonViewBinding

class ScrollButtonViewComponentBrowserFragment : Fragment() {
    private var _binding: FragmentComponentBrowserScrollButtonViewBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentComponentBrowserScrollButtonViewBinding
            .inflate(inflater, container, false)
            .apply { _binding = this }
            .root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.scrollButtonViewNoUnread.setUnreadCount(0)
        binding.scrollButtonViewSmallUnread.setUnreadCount(22)
        binding.scrollButtonViewBigUnread.setUnreadCount(1099)
    }
}
