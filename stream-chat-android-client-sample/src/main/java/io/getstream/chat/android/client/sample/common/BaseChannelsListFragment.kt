package io.getstream.chat.android.client.sample.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import io.getstream.chat.android.client.sample.R
import kotlinx.android.synthetic.main.fragment_channels.*

abstract class BaseChannelsListFragment : Fragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerChannels.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_channels, container, false)
    }

    protected fun drawLoading() {
        viewError.visibility = View.GONE
        viewLoading.visibility = View.VISIBLE
        viewAllLoaded.visibility = View.GONE
        recyclerChannels.visibility = View.VISIBLE
    }

    protected fun drawError(t: Throwable) {
        viewError.visibility = View.VISIBLE
        viewLoading.visibility = View.GONE
        viewAllLoaded.visibility = View.GONE
        recyclerChannels.visibility = View.VISIBLE
    }

    protected fun drawSuccess(channels: List<Channel>) {
        viewError.visibility = View.GONE
        viewLoading.visibility = View.GONE
        viewAllLoaded.visibility = View.GONE
        recyclerChannels.visibility = View.VISIBLE

        // updateAdapter(channels)
    }

    protected fun drawAllLoaded() {
        viewError.visibility = View.GONE
        viewLoading.visibility = View.GONE
        viewAllLoaded.visibility = View.VISIBLE
        recyclerChannels.visibility = View.VISIBLE
    }

    protected open fun updateAdapter(channels: List<Channel>) {
        recyclerChannels.adapter = ChannelsListAdapter(channels)
    }

    abstract fun reload()
}
