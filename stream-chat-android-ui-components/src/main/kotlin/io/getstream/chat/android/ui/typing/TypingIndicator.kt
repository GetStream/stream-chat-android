package io.getstream.chat.android.ui.typing

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import io.getstream.chat.android.ui.databinding.StreamTyppingIndicatorViewBinding

public class TypingIndicator : LinearLayout {

    private val binding: StreamTyppingIndicatorViewBinding =
        StreamTyppingIndicatorViewBinding.inflate(LayoutInflater.from(context), this, true)

    public var message: String
        get() = binding.tvUserTyping.text.toString()
        set(value) {
            binding.tvUserTyping.text = value
        }

    public constructor(context: Context?) : super(context)

    public constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    public constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    public constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    )
}
