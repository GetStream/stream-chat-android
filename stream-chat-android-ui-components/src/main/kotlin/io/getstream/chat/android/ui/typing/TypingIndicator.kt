package io.getstream.chat.android.ui.typing

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import io.getstream.chat.android.ui.databinding.StreamTyppingIndicatorViewBinding

public class TypingIndicator : LinearLayout {

    private lateinit var binding: StreamTyppingIndicatorViewBinding

    public constructor(context: Context?) : super(context) {
        init()
    }

    public constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    public constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    public constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    ) {
        init()
    }

    private fun init() {
        binding = StreamTyppingIndicatorViewBinding.inflate(LayoutInflater.from(context), this, true)
    }
}
