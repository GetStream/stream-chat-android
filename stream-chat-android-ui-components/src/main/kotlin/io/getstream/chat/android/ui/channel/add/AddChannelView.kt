package io.getstream.chat.android.ui.channel.add

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import io.getstream.chat.android.ui.databinding.StreamAddChannelViewBinding

public class AddChannelView : FrameLayout {

    private val binding = StreamAddChannelViewBinding.inflate(LayoutInflater.from(context), this, true)
    private val controller = AddChannelViewController(binding.headerView)

    public constructor(context: Context) : super(context) {
        init(null)
    }

    public constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    public constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
    }
}
