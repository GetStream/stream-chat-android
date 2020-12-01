package io.getstream.chat.ui.sample.feature.component_browser.reactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import io.getstream.chat.ui.sample.databinding.FragmentComponentBrowserEditReactionsViewBinding
import io.getstream.chat.ui.sample.feature.component_browser.utils.randomMessage

class ComponentBrowserEditReactionsFragment : Fragment() {
    private var _binding: FragmentComponentBrowserEditReactionsViewBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentComponentBrowserEditReactionsViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            reactionsView1.setMessage(randomMessage(reactionsSize = 30, ownReactionsSize = 0), isMyMessage = true)
            reactionsView2.setMessage(randomMessage(reactionsSize = 30, ownReactionsSize = 0), isMyMessage = false)
            reactionsView3.setMessage(randomMessage(reactionsSize = 0, ownReactionsSize = 0), isMyMessage = true)
            reactionsView4.setMessage(randomMessage(reactionsSize = 0, ownReactionsSize = 0), isMyMessage = false)
            reactionsView5.setMessage(randomMessage(reactionsSize = 1, ownReactionsSize = 1), isMyMessage = true)
            reactionsView6.setMessage(randomMessage(reactionsSize = 1, ownReactionsSize = 1), isMyMessage = false)
            reactionsView6.setReactionClickListener { Toast.makeText(context, it.type, Toast.LENGTH_SHORT).show() }
        }
    }
}
