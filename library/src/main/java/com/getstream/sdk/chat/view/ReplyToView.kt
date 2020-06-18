package com.getstream.sdk.chat.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import com.getstream.sdk.chat.databinding.StreamReplyToViewBinding
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.name

class ReplyToView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
		RelativeLayout(context, attrs, defStyleAttr) {
	private val binding: StreamReplyToViewBinding =
			StreamReplyToViewBinding.inflate(LayoutInflater.from(context), this, true).apply {
				ivClose.setOnClickListener { onCloseClick() }
			}
	var onCloseClick: () -> Unit = { }
	fun setMessage(message: Message) {
		binding.tvMessage.text = message.text
		binding.tvUserName.text = message.user.name
	}
}