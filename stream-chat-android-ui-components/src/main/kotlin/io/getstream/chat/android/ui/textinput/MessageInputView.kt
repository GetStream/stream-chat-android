package io.getstream.chat.android.ui.textinput

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
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
import io.getstream.chat.android.ui.databinding.StreamUiMessageInputBinding
import io.getstream.chat.android.ui.suggestions.SuggestionListController
import io.getstream.chat.android.ui.utils.extensions.EMPTY
import io.getstream.chat.android.ui.utils.extensions.setTextSizePx
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

    private lateinit var binding: StreamUiMessageInputBinding
    private lateinit var suggestionListController: SuggestionListController
    private lateinit var attachmentController: AttachmentController

    private var iconDisabledSendButtonDrawable: Drawable? = null
    private var iconEnabledSendButtonDrawable: Drawable? = null
    private var sendAlsoToChannelCheckBoxEnabled: Boolean = true

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
        configText()
    }

    public var chatMode: ChatMode by Delegates.observable(ChatMode.GroupChat) { _, _, _ ->
        configSendAlsoToChannelCheckbox()
    }

    private var onSendButtonClickListener: OnMessageSendButtonClickListener? = null
    private var typingListener: TypingListener? = null
    private var sendMessageHandler: MessageSendHandler = EMPTY_MESSAGE_SEND_HANDLER

    public fun setOnSendButtonClickListener(listener: OnMessageSendButtonClickListener?) {
        this.onSendButtonClickListener = listener
    }

    public fun setTypingListener(listener: TypingListener?) {
        this.typingListener = listener
    }

    /**
     * Sets up [MessageSendHandler] implementation. [MessageInputView] will delegate all the message sending operations
     * to this object.
     */
    public fun setSendMessageHandler(handler: MessageSendHandler) {
        this.sendMessageHandler = handler
    }

    public fun configureMembers(members: List<Member>) {
        suggestionListController.users = members.map { it.user }
    }

    public fun configureCommands(commands: List<Command>) {
        suggestionListController.commands = commands
    }

    @SuppressLint("CustomViewStyleable")
    private fun init(context: Context, attr: AttributeSet? = null) {
        binding = StreamUiMessageInputBinding.inflate(LayoutInflater.from(context), this, true)

        context.obtainStyledAttributes(attr, R.styleable.StreamUiMessageInputView).use { typedArray ->
            configAttachmentButton(typedArray)
            configLightningButton(typedArray)
            configTextInput(typedArray)
            configSendButton(typedArray)
            configSendAlsoToChannelCheckboxVisibility(typedArray)
        }
        configSendAlsoToChannelCheckbox()
        configClearAttachmentsButton()
        configAttachmentButtonBehavior()
        configSendButtonListener()
        configText()
    }

    private fun configText() {
        inputMode.let {
            messageText = when (it) {
                is InputMode.Edit -> it.oldMessage.text
                else -> ""
            }
        }
    }

    private fun configSendButtonListener() {
        binding.ivSendMessageEnabled.setOnClickListener {
            onSendButtonClickListener?.onClick()

            inputMode.let {
                when (it) {
                    is InputMode.Normal -> {
                        if (attachmentController.selectedAttachments.isEmpty()) {
                            sendMessageHandler.sendMessage(messageText)
                        } else {
                            val attachedFiles = attachmentController.getSelectedAttachmentsFiles()
                            sendMessageHandler.sendMessageWithAttachments(messageText, attachedFiles)
                        }
                    }
                    is InputMode.Thread -> {
                        val parentMessage = it.parentMessage
                        val sendAlsoToChannel = binding.sendAlsoToChannel.isChecked
                        if (attachmentController.selectedAttachments.isEmpty()) {
                            sendMessageHandler.sendToThread(
                                parentMessage,
                                messageText,
                                binding.sendAlsoToChannel.isChecked
                            )
                        } else {
                            val attachedFiles = attachmentController.getSelectedAttachmentsFiles()
                            sendMessageHandler.sendToThreadWithAttachments(parentMessage, messageText, sendAlsoToChannel, attachedFiles)
                        }
                    }
                    is InputMode.Edit -> sendMessageHandler.editMessage(it.oldMessage, messageText)
                }
            }
            messageText = ""
            attachmentController.clearSelectedAttachments()
        }
    }

    private fun configSendAlsoToChannelCheckboxVisibility(typedArray: TypedArray) {
        sendAlsoToChannelCheckBoxEnabled =
            typedArray.getBoolean(R.styleable.StreamUiMessageInputView_streamUiShowSendAlsoToChannelCheckbox, true)
    }

    private fun configSendAlsoToChannelCheckbox() {
        val isThreadModeActive = inputMode is InputMode.Thread
        val shouldShowCheckbox = sendAlsoToChannelCheckBoxEnabled && isThreadModeActive
        if (shouldShowCheckbox) {
            val text = when (chatMode) {
                ChatMode.GroupChat -> {
                    context.getString(R.string.stream_ui_send_also_to_channel)
                }
                ChatMode.DirectChat -> {
                    context.getString(R.string.stream_ui_send_also_as_direct_message)
                }
            }
            binding.sendAlsoToChannel.text = text
        }
        binding.sendAlsoToChannel.isVisible = shouldShowCheckbox
    }

    private fun configAttachmentButton(typedArray: TypedArray) {
        binding.ivOpenAttachment.run {
            isVisible = typedArray.getBoolean(R.styleable.StreamUiMessageInputView_streamUiAttachButtonEnabled, true)

            typedArray.getDrawable(R.styleable.StreamUiMessageInputView_streamUiAttachButtonIcon)
                ?.let(this::setImageDrawable)

            DrawableCompat.setTintList(
                drawable,
                getColorList(
                    typedArray.getColor(
                        R.styleable.StreamUiMessageInputView_streamUiAttachButtonIconColor,
                        ContextCompat.getColor(context, R.color.stream_gray_dark)
                    ),
                    typedArray.getColor(
                        R.styleable.StreamUiMessageInputView_streamUiAttachButtonIconPressedColor,
                        ContextCompat.getColor(context, R.color.stream_ui_white)
                    ),
                    typedArray.getColor(
                        R.styleable.StreamUiMessageInputView_streamUiAttachButtonIconPressedColor,
                        ContextCompat.getColor(context, R.color.stream_ui_gray_light)
                    )
                )
            )

            layoutParams.width = typedArray.getDimensionPixelSize(
                R.styleable.StreamUiMessageInputView_streamUiAttachButtonWidth,
                context.resources.getDimensionPixelSize(R.dimen.stream_attachment_button_width)
            )

            layoutParams.height = typedArray.getDimensionPixelSize(
                R.styleable.StreamUiMessageInputView_streamUiAttachButtonHeight,
                context.resources.getDimensionPixelSize(R.dimen.stream_attachment_button_height)
            )
        }
    }

    private fun configLightningButton(typedArray: TypedArray) {
        binding.ivOpenEmojis.run {
            isVisible =
                typedArray.getBoolean(R.styleable.StreamUiMessageInputView_streamUiLightningButtonEnabled, true)

            typedArray.getDrawable(R.styleable.StreamUiMessageInputView_streamUiLightningButtonIcon)
                ?.let(this::setImageDrawable)

            DrawableCompat.setTintList(
                drawable,
                getColorList(
                    typedArray.getColor(
                        R.styleable.StreamUiMessageInputView_streamUiLightningButtonIconColor,
                        ContextCompat.getColor(context, R.color.stream_gray_dark)
                    ),
                    typedArray.getColor(
                        R.styleable.StreamUiMessageInputView_streamUiLightningButtonIconPressedColor,
                        ContextCompat.getColor(context, R.color.stream_ui_white)
                    ),
                    typedArray.getColor(
                        R.styleable.StreamUiMessageInputView_streamUiLightningButtonIconPressedColor,
                        ContextCompat.getColor(context, R.color.stream_ui_gray_light)
                    )
                )
            )

            layoutParams.width = typedArray.getDimensionPixelSize(
                R.styleable.StreamUiMessageInputView_streamUiLightningButtonWidth,
                context.resources.getDimensionPixelSize(R.dimen.stream_attachment_button_width)
            )

            layoutParams.height = typedArray.getDimensionPixelSize(
                R.styleable.StreamUiMessageInputView_streamUiLightningButtonHeight,
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
            handleKeyStroke()
        }

        binding.etMessageTextInput.run {
            setTextColor(
                typedArray.getColor(
                    R.styleable.StreamUiMessageInputView_streamUiMessageInputTextColor,
                    ContextCompat.getColor(context, getTextColor())
                )
            )

            setHintTextColor(
                typedArray.getColor(
                    R.styleable.StreamUiMessageInputView_streamUiMessageInputHintTextColor,
                    ContextCompat.getColor(context, R.color.stream_gray_dark)
                )
            )

            setTextSizePx(
                typedArray.getDimensionPixelSize(
                    R.styleable.StreamUiMessageInputView_streamUiMessageInputTextSize,
                    context.resources.getDimensionPixelSize(R.dimen.stream_ui_text_size_input)
                ).toFloat()
            )

            hint = typedArray.getText(R.styleable.StreamUiMessageInputView_streamUiMessageInputHint)

            isVerticalScrollBarEnabled = typedArray.getBoolean(
                R.styleable.StreamUiMessageInputView_streamUiMessageInputScrollbarEnabled,
                false
            )

            isScrollbarFadingEnabled = typedArray.getBoolean(
                R.styleable.StreamUiMessageInputView_streamUiMessageInputScrollbarFadingEnabled,
                false
            )
        }
    }

    private fun getTextColor() : Int {
        return when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_NO ->  R.color.stream_black
            Configuration.UI_MODE_NIGHT_YES -> R.color.stream_white
            else -> R.color.stream_white
        }
    }

    private fun handleKeyStroke() {
        if (messageText.isNotEmpty()) {
            typingListener?.onKeystroke()
        } else {
            typingListener?.onStopTyping()
        }
    }

    private fun configSendButton(typedArray: TypedArray) {
        iconDisabledSendButtonDrawable =
            typedArray.getDrawable(R.styleable.StreamUiMessageInputView_streamUiSendButtonDisabledIcon)
            ?: ContextCompat.getDrawable(context, R.drawable.stream_ic_filled_right_arrow)
            ?: throw IllegalStateException(NO_ICON_MESSAGE_DISABLED_STATE)

        iconDisabledSendButtonDrawable!!.setTint(ContextCompat.getColor(context, getDisableIconColor()))

        iconEnabledSendButtonDrawable =
            typedArray.getDrawable(R.styleable.StreamUiMessageInputView_streamUiSendButtonEnabledIcon)
            ?: ContextCompat.getDrawable(context, R.drawable.stream_ic_filled_up_arrow)
            ?: throw IllegalStateException(NO_ICON_MESSAGE_ENABLED_STATE)

        DrawableCompat.setTintList(
            iconEnabledSendButtonDrawable!!,
            getColorList(
                typedArray.getColor(
                    R.styleable.StreamUiMessageInputView_streamUiSendButtonEnabledIconColor,
                    ContextCompat.getColor(context, R.color.stream_input_message_send_button)
                ),
                typedArray.getColor(
                    R.styleable.StreamUiMessageInputView_streamUiSendButtonPressedIconColor,
                    ContextCompat.getColor(context, R.color.stream_gray_dark)
                ),
                typedArray.getColor(
                    R.styleable.StreamUiMessageInputView_streamUiSendButtonDisabledIconColor,
                    ContextCompat.getColor(context, R.color.stream_gray_dark)
                )
            )
        )

        DrawableCompat.setTintList(
            iconDisabledSendButtonDrawable!!,
            getColorList(
                typedArray.getColor(
                    R.styleable.StreamUiMessageInputView_streamUiSendButtonDisabledIconColor,
                    ContextCompat.getColor(context, getDisabledSendButtonIconColor())
                ),
                typedArray.getColor(
                    R.styleable.StreamUiMessageInputView_streamUiSendButtonPressedIconColor,
                    ContextCompat.getColor(context, R.color.stream_gray_dark)
                ),
                typedArray.getColor(
                    R.styleable.StreamUiMessageInputView_streamUiSendButtonDisabledIconColor,
                    ContextCompat.getColor(context, R.color.stream_gray_dark)
                )
            )
        )

        binding.ivSendMessageDisabled.setImageDrawable(iconDisabledSendButtonDrawable)
        binding.ivSendMessageEnabled.setImageDrawable(iconEnabledSendButtonDrawable)

        binding.ivSendMessageDisabled.alpha = 1F
        binding.ivSendMessageEnabled.alpha = 0F
    }

    private fun getDisabledSendButtonIconColor() : Int = R.color.stream_ui_grey_medium_light

    private fun getDisableIconColor() : Int = R.color.stream_ui_grey_medium_light

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
        val hasText = binding.etMessageTextInput.text.toString().isNotBlank()
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
            binding.etMessageTextInput.setHint(R.string.stream_ui_attachment_input_hint)
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

    public interface TypingListener {
        public fun onKeystroke()
        public fun onStopTyping()
    }
}
