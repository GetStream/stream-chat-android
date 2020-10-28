package io.getstream.chat.android.client.sample.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import io.getstream.chat.android.client.sample.databinding.FragmentChannelsBinding

abstract class BaseChannelsListFragment : Fragment() {

    private var _binding: FragmentChannelsBinding? = null
    protected val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerChannels.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChannelsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    protected fun drawLoading() {
        binding.apply {
            viewError.visibility = View.GONE
            viewLoading.visibility = View.VISIBLE
            viewAllLoaded.visibility = View.GONE
            recyclerChannels.visibility = View.VISIBLE
        }
    }

    protected fun drawError(t: Throwable) {
        binding.apply {
            viewError.visibility = View.VISIBLE
            viewLoading.visibility = View.GONE
            viewAllLoaded.visibility = View.GONE
            recyclerChannels.visibility = View.VISIBLE
        }
    }

    protected fun drawSuccess(channels: List<Channel>) {
        binding.apply {
            viewError.visibility = View.GONE
            viewLoading.visibility = View.GONE
            viewAllLoaded.visibility = View.GONE
            recyclerChannels.visibility = View.VISIBLE
        }
    }

    protected fun drawAllLoaded() {
        binding.apply {
            viewError.visibility = View.GONE
            viewLoading.visibility = View.GONE
            viewAllLoaded.visibility = View.VISIBLE
            recyclerChannels.visibility = View.VISIBLE
        }
    }

    protected open fun updateAdapter(channels: List<Channel>) {
        binding.recyclerChannels.adapter = ChannelsListAdapter(channels)
    }

    abstract fun reload()
}
