package io.getstream.chat.ui.sample.feature.component_browser.reactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.getstream.chat.ui.sample.common.showToast
import io.getstream.chat.ui.sample.databinding.FragmentComponentBrowserUserReactionsViewBinding
import io.getstream.chat.ui.sample.feature.component_browser.utils.randomMessage

class ComponentBrowserUserReactionsFragment : Fragment() {
    private var _binding: FragmentComponentBrowserUserReactionsViewBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentComponentBrowserUserReactionsViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            userReactionsView1.setMessage(randomMessage(reactionsSize = 1, ownReactionsSize = 0))
            userReactionsView1.setReactionClickListener { showToast(it.type) }
            userReactionsView2.setMessage(randomMessage(reactionsSize = 2, ownReactionsSize = 1))
            userReactionsView3.setMessage(randomMessage(reactionsSize = 5, ownReactionsSize = 2))
            userReactionsView4.setMessage(randomMessage(reactionsSize = 30, ownReactionsSize = 5))
        }
    }
}
