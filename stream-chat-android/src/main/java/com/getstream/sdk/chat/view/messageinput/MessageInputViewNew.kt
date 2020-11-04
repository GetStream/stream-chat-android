package com.getstream.sdk.chat.view.messageinput

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.getstream.sdk.chat.databinding.StreamViewMessageInputNewBinding

public class MessageInputViewNew: ConstraintLayout  {

    public constructor(context: Context) : super(context)
    public constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    public constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val binding: StreamViewMessageInputNewBinding =
        StreamViewMessageInputNewBinding.inflate(LayoutInflater.from(context), this, true)


}
