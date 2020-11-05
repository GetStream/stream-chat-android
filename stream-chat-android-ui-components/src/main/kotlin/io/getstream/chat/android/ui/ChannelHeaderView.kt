package io.getstream.chat.android.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import io.getstream.chat.android.ui.databinding.StreamChannelHeaderViewBinding

public class ChannelHeaderView : ConstraintLayout {
    public constructor(context: Context) : super(context)
    public constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    public constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    public constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    private val binding: StreamChannelHeaderViewBinding =
        StreamChannelHeaderViewBinding.inflate(LayoutInflater.from(context), this, true)
}