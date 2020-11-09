package io.getstream.chat.android.ui.textinput

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.use
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.isVisible
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamMessageInputBinding
import io.getstream.chat.android.ui.utils.getColorList

public class StreamMessageInputView : ConstraintLayout {

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

    private lateinit var binding: StreamMessageInputBinding

    private fun init(context: Context, attr: AttributeSet? = null) {
        binding = StreamMessageInputBinding.inflate(LayoutInflater.from(context), this, true)

        context.obtainStyledAttributes(attr, R.styleable.StreamMessageInputView).use { typedArray ->
            configAttachmentButton(typedArray)
            configLightningButton(typedArray)
            configTextInput(typedArray)
        }
    }

    private fun configAttachmentButton(typedArray: TypedArray) {
        binding.ivOpenAttachment.run {
            isVisible = typedArray.getBoolean(R.styleable.StreamMessageInputView_streamAttachButtonEnabled, true)

            typedArray.getDrawable(R.styleable.StreamMessageInputView_streamAttachButtonIcon)
                ?.let(this::setImageDrawable)

            DrawableCompat.setTintList(
                drawable,
                getColorList(
                    typedArray.getColor(
                        R.styleable.StreamMessageInputView_streamAttachButtonIconColor,
                        ContextCompat.getColor(context, R.color.stream_gray_dark)
                    ),
                    typedArray.getColor(
                        R.styleable.StreamMessageInputView_streamAttachButtonIconPressedColor,
                        ContextCompat.getColor(context, R.color.stream_white)
                    ),
                    typedArray.getColor(
                        R.styleable.StreamMessageInputView_streamAttachButtonIconPressedColor,
                        ContextCompat.getColor(context, R.color.stream_gray_light)
                    )
                )
            )

            layoutParams.width = typedArray.getDimensionPixelSize(
                R.styleable.StreamMessageInputView_streamAttachButtonWidth,
                context.resources.getDimensionPixelSize(R.dimen.stream_attachment_button_width)
            )

            layoutParams.height = typedArray.getDimensionPixelSize(
                R.styleable.StreamMessageInputView_streamAttachButtonHeight,
                context.resources.getDimensionPixelSize(R.dimen.stream_attachment_button_height)
            )
        }
    }

    private fun configLightningButton(typedArray: TypedArray) {
        binding.ivOpenEmojis.run {
            isVisible =
                typedArray.getBoolean(R.styleable.StreamMessageInputView_streamLightningButtonEnabled, true)

            typedArray.getDrawable(R.styleable.StreamMessageInputView_streamLightningButtonIcon)
                ?.let(this::setImageDrawable)

            DrawableCompat.setTintList(
                drawable,
                getColorList(
                    typedArray.getColor(
                        R.styleable.StreamMessageInputView_streamLightningButtonIconColor,
                        ContextCompat.getColor(context, R.color.stream_gray_dark)
                    ),
                    typedArray.getColor(
                        R.styleable.StreamMessageInputView_streamLightningButtonIconPressedColor,
                        ContextCompat.getColor(context, R.color.stream_white)
                    ),
                    typedArray.getColor(
                        R.styleable.StreamMessageInputView_streamLightningButtonIconPressedColor,
                        ContextCompat.getColor(context, R.color.stream_gray_light)
                    )
                )
            )

            layoutParams.width = typedArray.getDimensionPixelSize(
                R.styleable.StreamMessageInputView_streamLightningButtonWidth,
                context.resources.getDimensionPixelSize(R.dimen.stream_attachment_button_width)
            )

            layoutParams.height = typedArray.getDimensionPixelSize(
                R.styleable.StreamMessageInputView_streamLightningButtonHeight,
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
                    R.styleable.StreamMessageInputView_streamMessageInputTextColor,
                    ContextCompat.getColor(context, R.color.stream_black)
                )
            )

            setHintTextColor(
                typedArray.getColor(
                    R.styleable.StreamMessageInputView_streamMessageInputHintTextColor,
                    ContextCompat.getColor(context, R.color.stream_gray_dark)
                )
            )

            textSize =
                typedArray.getDimensionPixelSize(
                    R.styleable.StreamMessageInputView_streamMessageInputTextSize,
                    context.resources.getDimensionPixelSize(R.dimen.stream_text_size_input)
                ).toFloat()

            hint = typedArray.getText(R.styleable.StreamMessageInputView_streamMessageInputHint)
        }
    }
}
