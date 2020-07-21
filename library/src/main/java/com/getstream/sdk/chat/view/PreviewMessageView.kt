package com.getstream.sdk.chat.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import com.getstream.sdk.chat.R
import com.getstream.sdk.chat.databinding.StreamReplyToViewBinding
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.name

class PreviewMessageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    RelativeLayout(context, attrs, defStyleAttr) {
    private val binding: StreamReplyToViewBinding =
        StreamReplyToViewBinding.inflate(LayoutInflater.from(context), this, true).apply {
            ivClose.setOnClickListener { onCloseClick() }
        }
    var onCloseClick: () -> Unit = { }
    fun setMessage(message: Message, mode: Mode) {
        binding.tvMessage.text = message.text
        binding.tvUserName.text = message.user.name
        binding.ivMode.setImageResource(mode.drawable)
    }

    enum class Mode {
        EDIT,
        REPLY_TO
    }
}

private val PreviewMessageView.Mode.drawable: Int
    get() = when (this) {
        PreviewMessageView.Mode.EDIT -> R.drawable.stream_ic_edit
        PreviewMessageView.Mode.REPLY_TO -> R.drawable.stream_ic_reply
    }
