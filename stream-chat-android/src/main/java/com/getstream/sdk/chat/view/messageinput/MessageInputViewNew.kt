package com.getstream.sdk.chat.view.messageinput

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.use
import androidx.core.view.isVisible
import com.getstream.sdk.chat.R
import com.getstream.sdk.chat.databinding.StreamViewMessageInputNewBinding

public class MessageInputViewNew : ConstraintLayout {

    public constructor(context: Context) : super(context) {
        init(context)
    }

    public constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    public constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }

    private lateinit var binding: StreamViewMessageInputNewBinding

    private fun init(context: Context, attr: AttributeSet? = null) {
        binding = StreamViewMessageInputNewBinding.inflate(LayoutInflater.from(context), this, true)

        context.obtainStyledAttributes(attr, R.styleable.MessageInputViewNew).use { typedArray ->
            configAttachmentButton(typedArray)
            configLightningButton(typedArray)
            configTextInput(typedArray)
        }
    }

    private fun configAttachmentButton(typedArray: TypedArray) {
        binding.ivOpenAttachment.run {
            isVisible = typedArray.getBoolean(R.styleable.MessageInputViewNew_streamShowAttachmentButtonNew, true)

            typedArray.getDrawable(R.styleable.MessageInputViewNew_streamAttachmentButtonIconNew)
                ?.let(this::setImageDrawable)

            layoutParams.width = typedArray.getDimensionPixelSize(
                R.styleable.MessageInputViewNew_streamAttachmentButtonWidthNew,
                context.resources.getDimensionPixelSize(R.dimen.stream_attachment_button_width)
            )

            layoutParams.height = typedArray.getDimensionPixelSize(
                R.styleable.MessageInputViewNew_streamAttachmentButtonHeightNew,
                context.resources.getDimensionPixelSize(R.dimen.stream_attachment_button_height)
            )
        }
    }

    private fun configLightningButton(typedArray: TypedArray) {
        binding.ivOpenEmojis.run {
            isVisible =
                typedArray.getBoolean(R.styleable.MessageInputViewNew_streamShowLightningButtonNew, true)

            typedArray.getDrawable(R.styleable.MessageInputViewNew_streamLightningButtonIconNew)
                ?.let(this::setImageDrawable)

            layoutParams.width = typedArray.getDimensionPixelSize(
                R.styleable.MessageInputViewNew_streamLightningButtonWidthNew,
                context.resources.getDimensionPixelSize(R.dimen.stream_attachment_button_width)
            )

            layoutParams.height = typedArray.getDimensionPixelSize(
                R.styleable.MessageInputViewNew_streamLightningButtonHeightNew,
                context.resources.getDimensionPixelSize(R.dimen.stream_attachment_button_height)
            )
        }
    }

    private fun showSendMessageEnabled() {
        binding.ivSendMessage.setImageResource(R.drawable.stream_ic_filled_up_arrow)
    }

    private fun hideSendMessageEnabled() {
        binding.ivSendMessage.setImageResource(R.drawable.stream_ic_filled_right_arrow)
    }

    private fun configTextInput(typedArray: TypedArray) {
        TextInputHandler(binding.etMessageTextInput, ::showSendMessageEnabled, ::hideSendMessageEnabled)

        binding.etMessageTextInput.run {
            setTextColor(
                typedArray.getColor(
                    R.styleable.MessageInputViewNew_streamInputTextColorNew,
                    android.R.attr.textColorPrimary
                )
            )

            setHintTextColor(
                typedArray.getColor(
                    R.styleable.MessageInputViewNew_streamInputHintTextColorNew,
                    android.R.attr.textColorPrimary
                )
            )

//            textSize =
//                typedArray.getDimension(
//                    R.styleable.MessageInputViewNew_streamInputTextSizeNew,
//                    context.resources.getDimension(R.dimen.stream_input_text_size)
//                )

            hint = typedArray.getText(R.styleable.MessageInputViewNew_streamInputHintNew)
        }
    }
}
