package io.getstream.chat.android.ui.textinput

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.use
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import io.getstream.chat.android.client.models.Command
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.attachments.AttachmentController
import io.getstream.chat.android.ui.databinding.StreamMessageInputBinding
import io.getstream.chat.android.ui.suggestions.SuggestionListController
import io.getstream.chat.android.ui.textinput.MessageInputView.OnMessageSendButtonClickListener
import io.getstream.chat.android.ui.utils.extensions.EMPTY
import io.getstream.chat.android.ui.utils.getColorList
import java.io.File
import kotlin.properties.Delegates

private const val NO_ICON_MESSAGE_DISABLED_STATE =
    "No icon for disabled state of send message button. Please set it in XML."

private const val NO_ICON_MESSAGE_ENABLED_STATE =
    "No icon for enabled state of send message button. Please set it in XML."

private const val ANIMATION_DURATION = 100L

public class MessageInputView : ConstraintLayout {

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

    private var currentAnimatorSet: AnimatorSet? = null

    private lateinit var binding: StreamMessageInputBinding
    private lateinit var suggestionListController: SuggestionListController
    private lateinit var attachmentController: AttachmentController

    private var iconDisabledSendButtonDrawable: Drawable? = null
    private var iconEnabledSendButtonDrawable: Drawable? = null

    public var messageText: String
        get() = binding.etMessageTextInput.text.toString()
        set(text) {
            binding.etMessageTextInput.apply {
                requestFocus()
                setText(text)
                setSelection(getText()?.length ?: 0)
            }
        }

    public var inputMode: InputMode by Delegates.observable(InputMode.Normal) { _, _, _ ->
        configSendAlsoToChannelCheckbox()
    }

    public var chatMode: ChatMode by Delegates.observable(ChatMode.GroupChat) { _, _, _ ->
        configSendAlsoToChannelCheckbox()
    }

    public var sendMessageHandler: MessageSendHandler = EMPTY_MESSAGE_SEND_HANDLER

    public var onSendButtonClickListener: OnMessageSendButtonClickListener = OnMessageSendButtonClickListener {}

    public fun configureMembers(members: List<Member>) {
        suggestionListController.users = members.map { it.user }
    }

    public fun configureCommands(commands: List<Command>) {
        suggestionListController.commands = commands
    }

    private fun init(context: Context, attr: AttributeSet? = null) {
        binding = StreamMessageInputBinding.inflate(LayoutInflater.from(context), this, true)

        context.obtainStyledAttributes(attr, R.styleable.StreamMessageInputView).use { typedArray ->
            configAttachmentButton(typedArray)
            configLightningButton(typedArray)
            configTextInput(typedArray)
            configSendButton(typedArray)
            configClearAttachmentsButton()
            configAttachmentButtonBehavior()
        }
        configSendAlsoToChannelCheckbox()
        configSendButtonListener()
    }

    private fun configSendButtonListener() {
        binding.ivSendMessageEnabled.setOnClickListener {
            onSendButtonClickListener.onClick()

            inputMode.let {
                when (it) {
                    is InputMode.Normal -> sendMessageHandler.sendMessage(messageText)
                    is InputMode.Thread -> {
                        sendMessageHandler.sendToThread(
                            it.parentMessage,
                            messageText,
                            binding.sendAlsoToChannel.isChecked
                        )
                    }
                    is InputMode.Edit -> TODO("Not supported yet")
                }
            }

            messageText = ""
        }
    }

    private fun configSendAlsoToChannelCheckbox() {
        val isThreadModeActive = inputMode is InputMode.Thread
        if (isThreadModeActive) {
            val text = when (chatMode) {
                ChatMode.GroupChat -> {
                    context.getString(R.string.stream_send_also_to_channel)
                }
                ChatMode.DirectChat -> {
                    context.getString(R.string.stream_send_also_as_direct_message)
                }
            }
            binding.sendAlsoToChannel.text = text
        }
        binding.sendAlsoToChannel.isVisible = isThreadModeActive
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
        val fadeAnimator = binding.ivSendMessageDisabled.run {
            ObjectAnimator.ofFloat(this, "alpha", alpha, 0F).setDuration(ANIMATION_DURATION)
        }

        val appearAnimator = binding.ivSendMessageEnabled.run {
            ObjectAnimator.ofFloat(this, "alpha", alpha, 1F).setDuration(ANIMATION_DURATION)
        }

        currentAnimatorSet?.cancel()

        currentAnimatorSet = AnimatorSet().apply {
            playSequentially(fadeAnimator, appearAnimator)
            start()
        }
    }

    private fun showSendMessageDisabled() {
        val fadeAnimator = binding.ivSendMessageEnabled.run {
            ObjectAnimator.ofFloat(this, "alpha", alpha, 0F).setDuration(ANIMATION_DURATION)
        }

        val appearAnimator = binding.ivSendMessageDisabled.run {
            ObjectAnimator.ofFloat(this, "alpha", alpha, 1F).setDuration(ANIMATION_DURATION)
        }

        currentAnimatorSet?.cancel()

        currentAnimatorSet = AnimatorSet().apply {
            playSequentially(fadeAnimator, appearAnimator)
            start()
        }
    }

    private fun configTextInput(typedArray: TypedArray) {
        suggestionListController = SuggestionListController(binding.suggestionListView, binding.etMessageTextInput)

        binding.etMessageTextInput.doAfterTextChanged {
            suggestionListController.onTextChanged(it?.toString() ?: "")
            refreshControlsState()
        }

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

    private fun configSendButton(typedArray: TypedArray) {
        iconDisabledSendButtonDrawable =
            typedArray.getDrawable(R.styleable.StreamMessageInputView_streamSendButtonDisabledIcon)
                ?: ContextCompat.getDrawable(context, R.drawable.stream_ic_filled_right_arrow)
                    ?: throw IllegalStateException(NO_ICON_MESSAGE_DISABLED_STATE)

        iconEnabledSendButtonDrawable =
            typedArray.getDrawable(R.styleable.StreamMessageInputView_streamSendButtonEnabledIcon)
                ?: ContextCompat.getDrawable(context, R.drawable.stream_ic_filled_up_arrow)
                    ?: throw IllegalStateException(NO_ICON_MESSAGE_ENABLED_STATE)

        DrawableCompat.setTintList(
            iconEnabledSendButtonDrawable!!,
            getColorList(
                typedArray.getColor(
                    R.styleable.StreamMessageInputView_streamSendButtonEnabledIconColor,
                    ContextCompat.getColor(context, R.color.stream_input_message_send_button)
                ),
                typedArray.getColor(
                    R.styleable.StreamMessageInputView_streamSendButtonPressedIconColor,
                    ContextCompat.getColor(context, R.color.stream_gray_dark)
                ),
                typedArray.getColor(
                    R.styleable.StreamMessageInputView_streamSendButtonDisabledIconColor,
                    ContextCompat.getColor(context, R.color.stream_gray_dark)
                )
            )
        )

        DrawableCompat.setTintList(
            iconDisabledSendButtonDrawable!!,
            getColorList(
                typedArray.getColor(
                    R.styleable.StreamMessageInputView_streamSendButtonDisabledIconColor,
                    ContextCompat.getColor(context, R.color.stream_black)
                ),
                typedArray.getColor(
                    R.styleable.StreamMessageInputView_streamSendButtonPressedIconColor,
                    ContextCompat.getColor(context, R.color.stream_gray_dark)
                ),
                typedArray.getColor(
                    R.styleable.StreamMessageInputView_streamSendButtonDisabledIconColor,
                    ContextCompat.getColor(context, R.color.stream_gray_dark)
                )
            )
        )

        binding.ivSendMessageDisabled.setImageDrawable(iconDisabledSendButtonDrawable)
        binding.ivSendMessageEnabled.setImageDrawable(iconEnabledSendButtonDrawable)

        binding.ivSendMessageDisabled.alpha = 1F
        binding.ivSendMessageEnabled.alpha = 0F
    }

    private fun configClearAttachmentsButton() {
        binding.clearMessageInputButton.setOnClickListener {
            attachmentController.clearSelectedAttachments()
            binding.etMessageTextInput.setText(String.EMPTY)
            refreshControlsState()
        }
    }

    private fun configAttachmentButtonBehavior() {
        attachmentController = AttachmentController(context, binding.mediaComposer, binding.fileComposer) {
            refreshControlsState()
        }
        binding.ivOpenAttachment.setOnClickListener {
            attachmentController.openAttachmentDialog()
        }
    }

    private fun refreshControlsState() {
        val hasText = !binding.etMessageTextInput.text.toString().isNullOrBlank()
        val hasAttachments = attachmentController.selectedAttachments.isNotEmpty()

        if (hasText || hasAttachments) {
            showSendMessageEnabled()
        } else {
            showSendMessageDisabled()
        }

        if (hasAttachments) {
            binding.ivOpenEmojis.isVisible = false
            binding.ivOpenAttachment.isVisible = false
            binding.clearMessageInputButton.isVisible = true
            binding.etMessageTextInput.setHint(R.string.stream_attachment_input_hint)
        } else {
            binding.ivOpenEmojis.isVisible = true
            binding.ivOpenAttachment.isVisible = true
            binding.clearMessageInputButton.isVisible = false
            binding.etMessageTextInput.hint = null
        }
    }

    private companion object {
        val EMPTY_MESSAGE_SEND_HANDLER = object : MessageSendHandler {
            override fun sendMessage(messageText: String) {
                throw IllegalStateException("MessageInputView#messageSendHandler needs to be configured to send messages")
            }

            override fun sendMessageWithAttachments(message: String, attachmentsFiles: List<File>) {
                throw IllegalStateException("MessageInputView#messageSendHandler needs to be configured to send messages")
            }

            override fun sendToThread(
                parentMessage: Message,
                messageText: String,
                alsoSendToChannel: Boolean
            ) {
                throw IllegalStateException("MessageInputView#messageSendHandler needs to be configured to send messages")
            }

            override fun sendToThreadWithAttachments(
                parentMessage: Message,
                message: String,
                alsoSendToChannel: Boolean,
                attachmentsFiles: List<File>
            ) {
                throw IllegalStateException("MessageInputView#messageSendHandler needs to be configured to send messages")
            }

            override fun editMessage(oldMessage: Message, newMessageText: String) {
                throw IllegalStateException("MessageInputView#messageSendHandler needs to be configured to send messages")
            }
        }
    }

    public sealed class InputMode {
        public object Normal : InputMode()
        public data class Thread(val parentMessage: Message) : InputMode()
        public data class Edit(val oldMessage: Message) : InputMode()
    }

    public enum class ChatMode {
        DirectChat,
        GroupChat
    }

    public interface MessageSendHandler {
        public fun sendMessage(messageText: String)
        public fun sendMessageWithAttachments(message: String, attachmentsFiles: List<File>)
        public fun sendToThread(parentMessage: Message, messageText: String, alsoSendToChannel: Boolean)
        public fun sendToThreadWithAttachments(
            parentMessage: Message,
            message: String,
            alsoSendToChannel: Boolean,
            attachmentsFiles: List<File>
        )

        public fun editMessage(oldMessage: Message, newMessageText: String)
    }

    public fun interface OnMessageSendButtonClickListener {
        public fun onClick()
    }
}
