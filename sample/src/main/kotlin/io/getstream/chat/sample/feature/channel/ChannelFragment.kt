package io.getstream.chat.sample.feature.channel

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.getstream.sdk.chat.viewmodel.ChannelHeaderViewModel
import com.getstream.sdk.chat.viewmodel.MessageInputViewModel
import com.getstream.sdk.chat.viewmodel.bindView
import io.getstream.chat.sample.R
import kotlinx.android.synthetic.main.fragment_channel.channelHeaderView
import kotlinx.android.synthetic.main.fragment_channel.messageInputView

class ChannelFragment: Fragment(R.layout.fragment_channel) {

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		val cid = navArgs<ChannelFragmentArgs>().value.cid
		ChannelHeaderViewModel(cid)
				.bindView(channelHeaderView.apply {
					onBackClick = { findNavController().navigateUp() }
				}, this)
		MessageInputViewModel(cid).bindView(messageInputView, this)
	}
}