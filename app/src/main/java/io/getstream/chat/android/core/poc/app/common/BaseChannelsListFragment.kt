package io.getstream.chat.android.core.poc.app.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import io.getstream.chat.android.core.poc.R
import kotlinx.android.synthetic.main.fragment_channels.*

abstract class BaseChannelsListFragment : Fragment() {

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
        recyclerChannels.visibility = View.GONE
    }

    protected fun drawError(t: Throwable) {
        viewError.visibility = View.VISIBLE
        viewLoading.visibility = View.GONE
        recyclerChannels.visibility = View.GONE
    }

    protected fun drawSuccess(channels: List<Channel>) {

        viewError.visibility = View.GONE
        viewLoading.visibility = View.GONE
        recyclerChannels.visibility = View.VISIBLE

        recyclerChannels.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerChannels.adapter = ChannelsListAdapter(channels)
    }

    abstract fun reload()
}