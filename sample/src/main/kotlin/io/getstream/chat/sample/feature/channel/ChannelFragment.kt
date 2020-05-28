package io.getstream.chat.sample.feature.channel

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.getstream.sdk.chat.viewmodel.ChannelHeaderViewModel
import com.getstream.sdk.chat.viewmodel.MessageInputViewModel
import com.getstream.sdk.chat.viewmodel.MessageListViewModel
import com.getstream.sdk.chat.viewmodel.bindView
import io.getstream.chat.sample.R
import io.getstream.chat.sample.common.visible
import kotlinx.android.synthetic.main.fragment_channel.*

class ChannelFragment : Fragment(R.layout.fragment_channel) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val cid = navArgs<ChannelFragmentArgs>().value.cid
        ChannelHeaderViewModel(cid)
                .bindView(channelHeaderView.apply {
                    onBackClick = { findNavController().navigateUp() }
                }, this)
        MessageInputViewModel(cid).bindView(messageInputView, viewLifecycleOwner)
        MessageListViewModel(cid)
                .apply { bindView(messageListView, viewLifecycleOwner) }
                .apply {
                    state.observe(viewLifecycleOwner, Observer {
                        when (it) {
                            is MessageListViewModel.State.Loading -> progressBar.visible(true)
                            is MessageListViewModel.State.Result -> progressBar.visible(false)
                        }
                    })
                }

    }
}