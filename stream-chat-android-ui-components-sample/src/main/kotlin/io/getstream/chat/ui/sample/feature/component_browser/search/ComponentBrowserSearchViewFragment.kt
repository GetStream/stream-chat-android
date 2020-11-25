package io.getstream.chat.ui.sample.feature.component_browser.search

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.getstream.chat.ui.sample.common.showToast
import io.getstream.chat.ui.sample.databinding.FragmentComponentBrowserSearchViewBinding

class ComponentBrowserSearchViewFragment : Fragment() {

    private val TAG = ComponentBrowserSearchViewFragment::class.simpleName

    private var _binding: FragmentComponentBrowserSearchViewBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentComponentBrowserSearchViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.searchView.apply {
            setDebouncedInputChangedListener { query ->
                Log.d(TAG, "Debounced input: '$query'")
            }
            setSearchStartedListener { query ->
                Log.d(TAG, "Search: '$query'")
                showToast("Search: '$query'")
            }
        }

        binding.searchView2.setQuery("Gaming")
    }
}
