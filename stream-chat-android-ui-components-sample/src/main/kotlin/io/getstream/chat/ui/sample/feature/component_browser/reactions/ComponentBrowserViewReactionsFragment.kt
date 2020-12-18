package io.getstream.chat.ui.sample.feature.component_browser.reactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.getstream.chat.ui.sample.databinding.FragmentComponentBrowserViewReactionsViewBinding
import io.getstream.chat.ui.sample.feature.component_browser.utils.randomMessageWithReactions

class ComponentBrowserViewReactionsFragment : Fragment() {
    private var _binding: FragmentComponentBrowserViewReactionsViewBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentComponentBrowserViewReactionsViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            reactionsView1.setMessage(randomMessageWithReactions(reactionsSize = 30, ownReactionsSize = 0), isMyMessage = true)
            reactionsView2.setMessage(randomMessageWithReactions(reactionsSize = 30, ownReactionsSize = 0), isMyMessage = false)
            reactionsView3.setMessage(randomMessageWithReactions(reactionsSize = 3, ownReactionsSize = 1), isMyMessage = true)
            reactionsView4.setMessage(randomMessageWithReactions(reactionsSize = 3, ownReactionsSize = 1), isMyMessage = false)
            reactionsView5.setMessage(randomMessageWithReactions(reactionsSize = 1, ownReactionsSize = 0), isMyMessage = true)
            reactionsView6.setMessage(randomMessageWithReactions(reactionsSize = 1, ownReactionsSize = 0), isMyMessage = false)
        }
    }
}
