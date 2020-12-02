package io.getstream.chat.ui.sample.feature.component_browser.messages.viewholder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.ui.sample.databinding.FragmentComponentBrowserMessageListViewHolderBinding

abstract class BaseMessagesComponentBrowserFragment : Fragment() {
    private var _binding: FragmentComponentBrowserMessageListViewHolderBinding? = null
    protected val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentComponentBrowserMessageListViewHolderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.recyclerView.adapter = createAdapter()
    }

    protected abstract fun createAdapter(): RecyclerView.Adapter<*>
}
