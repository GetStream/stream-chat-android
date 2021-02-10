package io.getstream.chat.android.ui.message.input

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.use
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.isVisible
import com.getstream.sdk.chat.model.AttachmentMetaData
import com.getstream.sdk.chat.utils.extensions.focusAndShowKeyboard
import io.getstream.chat.android.client.models.Command
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.getColorCompat
import io.getstream.chat.android.ui.common.extensions.internal.getDrawableCompat
import io.getstream.chat.android.ui.common.extensions.internal.getFragmentManager
import io.getstream.chat.android.ui.common.internal.getColorList
import io.getstream.chat.android.ui.databinding.StreamUiMessageInputBinding
import io.getstream.chat.android.ui.message.input.attachment.internal.AttachmentDialogFragment
import io.getstream.chat.android.ui.message.input.attachment.internal.AttachmentSelectionListener
import io.getstream.chat.android.ui.message.input.attachment.internal.AttachmentSource
import io.getstream.chat.android.ui.message.input.internal.MessageInputFieldView
import io.getstream.chat.android.ui.suggestion.internal.SuggestionListController
import io.getstream.chat.android.ui.suggestion.list.SuggestionListView
import java.io.File
import kotlin.properties.Delegates

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

    private var iconDisabledSendButtonDrawable: Drawable? = null
    private var iconEnabledSendButtonDrawable: Drawable? = null
    private var sendAlsoToChannelCheckBoxEnabled: Boolean = true
    private var isSendButtonEnabled: Boolean = true

    public var inputMode: InputMode by Delegates.observable(InputMode.Normal) { _, previousValue, newValue ->
        configSendAlsoToChannelCheckbox()
        configInputMode(previousValue, newValue)
    }

    private fun configInputMode(previousValue: InputMode, newValue: InputMode) {
        when (newValue) {
            is InputMode.Reply -> {
                binding.inputModeHeader.isVisible = true
                binding.headerLabel.text = context.getString(R.string.stream_ui_reply_to_message)
                binding.inputModeIcon.setImageResource(R.drawable.stream_ui_ic_arrow_curve_left)
                binding.messageInputFieldView.onReply(newValue.repliedMessage)
                binding.messageInputFieldView.binding.messageEditText.focusAndShowKeyboard()
            }

            is InputMode.Edit -> {
                binding.inputModeHeader.isVisible = true
                binding.headerLabel.text = context.getString(R.string.stream_ui_message_option_edit)
                binding.inputModeIcon.setImageResource(R.drawable.stream_ui_ic_edit)
                binding.messageInputFieldView.onEdit(newValue.oldMessage)
                binding.messageInputFieldView.binding.messageEditText.focusAndShowKeyboard()
            }

            else -> {
                binding.inputModeHeader.isVisible = false
                if (previousValue is InputMode.Reply) {
                    binding.messageInputFieldView.onReplyDismissed()
                }
            }
        }
    }

    public var chatMode: ChatMode by Delegates.observable(ChatMode.GROUP_CHAT) { _, _, _ ->
        configSendAlsoToChannelCheckbox()
    }

    private var onSendButtonClickListener: OnMessageSendButtonClickListener? = null
    private var typingListener: TypingListener? = null
    private var sendMessageHandler: MessageSendHandler = EMPTY_MESSAGE_SEND_HANDLER
    private var suggestionListController: SuggestionListController? = null

    private val attachmentSelectionListener = object : AttachmentSelectionListener {
        override fun onAttachmentsSelected(attachments: Set<AttachmentMetaData>, attachmentSource: AttachmentSource) {
            if (attachments.isNotEmpty()) {
                when (attachmentSource) {
                    AttachmentSource.MEDIA,
                    AttachmentSource.CAMERA,
                    -> {
                        binding.messageInputFieldView.mode =
                            MessageInputFieldView.Mode.MediaAttachmentMode(attachments.toList())
                    }
                    AttachmentSource.FILE -> {
                        binding.messageInputFieldView.mode =
                            MessageInputFieldView.Mode.FileAttachmentMode(attachments.toList())
                    }
                }
            }
        }
    }

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

    public fun setMembers(members: List<Member>) {
        suggestionListController?.users = members.map { it.user }
    }

    public fun setCommands(commands: List<Command>) {
        suggestionListController?.commands = commands
    }

    public fun enableSendButton() {
        isSendButtonEnabled = true
        refreshControlsState()
    }

    public fun disableSendButton() {
        isSendButtonEnabled = false
        refreshControlsState()
    }

    public fun setSuggestionListView(suggestionListView: SuggestionListView) {
        suggestionListView.setOnSuggestionClickListener(
            object : SuggestionListView.OnSuggestionClickListener {
                override fun onMentionClick(user: User) {
                    binding.messageInputFieldView.autoCompleteUser(user)
                }

                override fun onCommandClick(command: Command) {
                    binding.messageInputFieldView.autoCompleteCommand(command)
                }
            }
        )
        suggestionListController = SuggestionListController(suggestionListView) {
            binding.commandsButton.isSelected = false
        }
    }

    public fun setMaxMessageLength(maxMessageLength: Int) {
        binding.messageInputFieldView.setMaxMessageLength(maxMessageLength)
    }

    @SuppressLint("CustomViewStyleable")
    private fun init(context: Context, attr: AttributeSet? = null) {
        binding = StreamUiMessageInputBinding.inflate(LayoutInflater.from(context), this, true)

        context.obtainStyledAttributes(attr, R.styleable.MessageInputView).use { typedArray ->
            configAttachmentButton(typedArray)
            configLightningButton(typedArray)
            configTextInput(typedArray)
            configSendButton(typedArray)
            configSendAlsoToChannelCheckboxVisibility(typedArray)
        }
        configSendAlsoToChannelCheckbox()
        configSendButtonListener()
        binding.dismissInputMode.setOnClickListener { dismissInputMode(inputMode) }
    }

    private fun dismissInputMode(inputMode: InputMode) {
        if (inputMode is InputMode.Reply) {
            sendMessageHandler.dismissReply()
        }

        this.inputMode = InputMode.Normal
    }

    private fun configSendButtonListener() {
        binding.sendMessageButtonEnabled.setOnClickListener {
            onSendButtonClickListener?.onClick()
            inputMode.let {
                when (it) {
                    is InputMode.Normal -> sendMessage()
                    is InputMode.Thread -> sendThreadMessage(it.parentMessage)
                    is InputMode.Edit -> editMessage(it.oldMessage)
                    is InputMode.Reply -> sendMessage(it.repliedMessage)
                }
            }
            binding.messageInputFieldView.clearContent()
        }
    }

    private fun configSendAlsoToChannelCheckboxVisibility(typedArray: TypedArray) {
        sendAlsoToChannelCheckBoxEnabled =
            typedArray.getBoolean(R.styleable.MessageInputView_streamUiShowSendAlsoToChannelCheckbox, true)
    }

    private fun configSendAlsoToChannelCheckbox() {
        val isThreadModeActive = inputMode is InputMode.Thread
        val shouldShowCheckbox = sendAlsoToChannelCheckBoxEnabled && isThreadModeActive
        if (shouldShowCheckbox) {
            val text = when (chatMode) {
                ChatMode.GROUP_CHAT -> {
                    context.getString(R.string.stream_ui_send_also_to_channel)
                }
                ChatMode.DIRECT_CHAT -> {
                    context.getString(R.string.stream_ui_send_also_as_direct_message)
                }
            }
            binding.sendAlsoToChannel.text = text
        }
        binding.sendAlsoToChannel.isVisible = shouldShowCheckbox
    }

    private fun configAttachmentButton(typedArray: TypedArray) {
        binding.attachmentsButton.run {
            isVisible = typedArray.getBoolean(R.styleable.MessageInputView_streamUiAttachButtonEnabled, true)

            typedArray.getDrawable(R.styleable.MessageInputView_streamUiAttachButtonIcon)
                ?.let(this::setImageDrawable)

            DrawableCompat.setTintList(
                drawable,
                getColorList(
                    normalColor = typedArray.getColor(
                        R.styleable.MessageInputView_streamUiAttachButtonIconColor,
                        context.getColorCompat(R.color.stream_ui_grey)
                    ),
                    selectedColor = typedArray.getColor(
                        R.styleable.MessageInputView_streamUiAttachButtonIconPressedColor,
                        context.getColorCompat(R.color.stream_ui_accent_blue)
                    ),
                    disabledColor = typedArray.getColor(
                        R.styleable.MessageInputView_streamUiAttachButtonIconDisabledColor,
                        context.getColorCompat(R.color.stream_ui_grey_gainsboro)
                    )
                )
            )

            setOnClickListener {
                context.getFragmentManager()?.let {
                    AttachmentDialogFragment.newInstance()
                        .apply { setAttachmentSelectionListener(attachmentSelectionListener) }
                        .show(it, AttachmentDialogFragment.TAG)
                }
            }
        }
    }

    private fun configLightningButton(typedArray: TypedArray) {
        binding.commandsButton.run {
            isVisible =
                typedArray.getBoolean(R.styleable.MessageInputView_streamUiLightningButtonEnabled, true)

            typedArray.getDrawable(R.styleable.MessageInputView_streamUiLightningButtonIcon)
                ?.let(this::setImageDrawable)

            DrawableCompat.setTintList(
                drawable,
                getColorList(
                    normalColor = typedArray.getColor(
                        R.styleable.MessageInputView_streamUiLightningButtonIconColor,
                        context.getColorCompat(R.color.stream_ui_grey)
                    ),
                    selectedColor = typedArray.getColor(
                        R.styleable.MessageInputView_streamUiLightningButtonIconPressedColor,
                        context.getColorCompat(R.color.stream_ui_accent_blue)
                    ),
                    disabledColor = typedArray.getColor(
                        R.styleable.MessageInputView_streamUiLightningButtonIconDisabledColor,
                        context.getColorCompat(R.color.stream_ui_grey_gainsboro)
                    )
                )
            )

            setOnClickListener {
                suggestionListController?.let {
                    if (it.isSuggestionListVisible()) {
                        it.hideSuggestionList()
                    } else {
                        isSelected = true
                        it.showAvailableCommands()
                    }
                }
            }
        }
    }

    private fun setSendMessageButtonEnabled(isEnabled: Boolean) {
        val isSendButtonEnabled = isEnabled && this.isSendButtonEnabled
        if (binding.sendMessageButtonEnabled.isEnabled == isSendButtonEnabled) return

        currentAnimatorSet?.cancel()

        val (fadeInView, fadeOutView) = if (isSendButtonEnabled) {
            binding.sendMessageButtonEnabled to binding.sendMessageButtonDisabled
        } else {
            binding.sendMessageButtonDisabled to binding.sendMessageButtonEnabled
        }
        currentAnimatorSet = AnimatorSet().apply {
            duration = 300
            playTogether(
                ObjectAnimator.ofFloat(fadeOutView, "alpha", fadeOutView.alpha, 0F),
                ObjectAnimator.ofFloat(fadeInView, "alpha", fadeInView.alpha, 1F)
            )
            start()
        }

        binding.sendMessageButtonEnabled.isEnabled = isSendButtonEnabled
    }

    private fun configTextInput(typedArray: TypedArray) {
        binding.messageInputFieldView.setContentChangeListener(
            object : MessageInputFieldView.ContentChangeListener {
                override fun onMessageTextChanged(messageText: String) {
                    refreshControlsState()
                    handleKeyStroke()
                    suggestionListController?.showSuggestions(messageText)
                }

                override fun onSelectedAttachmentsChanged(selectedAttachments: List<AttachmentMetaData>) {
                    refreshControlsState()
                }

                override fun onModeChanged(mode: MessageInputFieldView.Mode) {
                    refreshControlsState()
                }
            }
        )

        binding.messageInputFieldView.run {
            setTextColor(
                typedArray.getColor(
                    R.styleable.MessageInputView_streamUiMessageInputTextColor,
                    context.getColorCompat(R.color.stream_ui_text_color_primary)
                )
            )

            setHintTextColor(
                typedArray.getColor(
                    R.styleable.MessageInputView_streamUiMessageInputHintTextColor,
                    context.getColorCompat(R.color.stream_ui_text_color_hint)
                )
            )

            setTextSizePx(
                typedArray.getDimensionPixelSize(
                    R.styleable.MessageInputView_streamUiMessageInputTextSize,
                    context.resources.getDimensionPixelSize(R.dimen.stream_ui_text_size_input)
                ).toFloat()
            )

            setInputFieldScrollBarEnabled(
                typedArray.getBoolean(
                    R.styleable.MessageInputView_streamUiMessageInputScrollbarEnabled,
                    false
                )
            )

            setInputFieldScrollbarFadingEnabled(
                typedArray.getBoolean(
                    R.styleable.MessageInputView_streamUiMessageInputScrollbarFadingEnabled,
                    false
                )
            )
        }
    }

    private fun handleKeyStroke() {
        if (binding.messageInputFieldView.messageText.isNotEmpty()) {
            typingListener?.onKeystroke()
        } else {
            typingListener?.onStopTyping()
        }
    }

    private fun configSendButton(typedArray: TypedArray) {
        iconDisabledSendButtonDrawable =
            typedArray.getDrawable(R.styleable.MessageInputView_streamUiSendButtonDisabledIcon)
            ?: context.getDrawableCompat(R.drawable.stream_ui_ic_filled_right_arrow)

        iconEnabledSendButtonDrawable =
            typedArray.getDrawable(R.styleable.MessageInputView_streamUiSendButtonEnabledIcon)
            ?: context.getDrawableCompat(R.drawable.stream_ui_ic_filled_up_arrow)

        isSendButtonEnabled = typedArray.getBoolean(R.styleable.MessageInputView_streamUiSendButtonEnabled, true)

        DrawableCompat.setTintList(
            iconEnabledSendButtonDrawable!!,
            getColorList(
                normalColor = typedArray.getColor(
                    R.styleable.MessageInputView_streamUiSendButtonEnabledIconColor,
                    context.getColorCompat(R.color.stream_ui_accent_blue)
                ),
                selectedColor = typedArray.getColor(
                    R.styleable.MessageInputView_streamUiSendButtonPressedIconColor,
                    context.getColorCompat(R.color.stream_ui_accent_blue)
                ),
                disabledColor = typedArray.getColor(
                    R.styleable.MessageInputView_streamUiSendButtonDisabledIconColor,
                    context.getColorCompat(R.color.stream_ui_grey_gainsboro)
                )
            )
        )

        DrawableCompat.setTintList(
            iconDisabledSendButtonDrawable!!,
            getColorList(
                normalColor = typedArray.getColor(
                    R.styleable.MessageInputView_streamUiSendButtonDisabledIconColor,
                    context.getColorCompat(R.color.stream_ui_accent_blue)
                ),
                selectedColor = typedArray.getColor(
                    R.styleable.MessageInputView_streamUiSendButtonPressedIconColor,
                    context.getColorCompat(R.color.stream_ui_accent_blue)
                ),
                disabledColor = typedArray.getColor(
                    R.styleable.MessageInputView_streamUiSendButtonDisabledIconColor,
                    context.getColorCompat(R.color.stream_ui_grey_gainsboro)
                )
            )
        )

        binding.sendMessageButtonDisabled.setImageDrawable(iconDisabledSendButtonDrawable)
        binding.sendMessageButtonEnabled.setImageDrawable(iconEnabledSendButtonDrawable)

        binding.sendMessageButtonDisabled.alpha = 1F
        binding.sendMessageButtonEnabled.alpha = 0F

        binding.sendMessageButtonDisabled.isEnabled = false
        binding.sendMessageButtonEnabled.isEnabled = false
    }

    private fun refreshControlsState() {
        val isCommandMode = binding.messageInputFieldView.mode is MessageInputFieldView.Mode.CommandMode
        val hasContent = binding.messageInputFieldView.hasContent()
        val maxLMessageLengthExceeded = binding.messageInputFieldView.isMaxMessageLengthExceeded()

        binding.attachmentsButton.isVisible = !isCommandMode
        binding.commandsButton.isVisible = !isCommandMode
        binding.commandsButton.isEnabled = !hasContent
        setSendMessageButtonEnabled(hasContent && !maxLMessageLengthExceeded)
    }

    private fun sendMessage(messageReplyTo: Message? = null) {
        if (binding.messageInputFieldView.hasAttachments()) {
            sendMessageHandler.sendMessageWithAttachments(
                binding.messageInputFieldView.messageText,
                binding.messageInputFieldView.getAttachedFiles(),
                messageReplyTo
            )
        } else {
            sendMessageHandler.sendMessage(binding.messageInputFieldView.messageText, messageReplyTo)
        }
    }

    private fun sendThreadMessage(parentMessage: Message) {
        val sendAlsoToChannel = binding.sendAlsoToChannel.isChecked
        if (binding.messageInputFieldView.hasAttachments()) {
            sendMessageHandler.sendToThreadWithAttachments(
                parentMessage,
                binding.messageInputFieldView.messageText,
                sendAlsoToChannel,
                binding.messageInputFieldView.getAttachedFiles()
            )
        } else {
            sendMessageHandler.sendToThread(
                parentMessage,
                binding.messageInputFieldView.messageText,
                sendAlsoToChannel
            )
        }
    }

    private fun editMessage(oldMessage: Message) {
        sendMessageHandler.editMessage(oldMessage, binding.messageInputFieldView.messageText)
        inputMode = InputMode.Normal
    }

    private companion object {
        val EMPTY_MESSAGE_SEND_HANDLER = object : MessageSendHandler {
            override fun sendMessage(messageText: String, messageReplyTo: Message?) {
                throw IllegalStateException("MessageInputView#messageSendHandler needs to be configured to send messages")
            }

            override fun sendMessageWithAttachments(
                message: String,
                attachmentsFiles: List<File>,
                messageReplyTo: Message?,
            ) {
                throw IllegalStateException("MessageInputView#messageSendHandler needs to be configured to send messages")
            }

            override fun sendToThread(
                parentMessage: Message,
                messageText: String,
                alsoSendToChannel: Boolean,
            ) {
                throw IllegalStateException("MessageInputView#messageSendHandler needs to be configured to send messages")
            }

            override fun sendToThreadWithAttachments(
                parentMessage: Message,
                message: String,
                alsoSendToChannel: Boolean,
                attachmentsFiles: List<File>,
            ) {
                throw IllegalStateException("MessageInputView#messageSendHandler needs to be configured to send messages")
            }

            override fun editMessage(oldMessage: Message, newMessageText: String) {
                throw IllegalStateException("MessageInputView#messageSendHandler needs to be configured to send messages")
            }

            override fun dismissReply() {
                throw IllegalStateException("MessageInputView#messageSendHandler needs to be configured to send messages")
            }
        }
    }

    public sealed class InputMode {
        public object Normal : InputMode()
        public data class Thread(val parentMessage: Message) : InputMode()
        public data class Edit(val oldMessage: Message) : InputMode()
        public data class Reply(val repliedMessage: Message) : InputMode()
    }

    public enum class ChatMode {
        DIRECT_CHAT,
        GROUP_CHAT,
    }

    public interface MessageSendHandler {
        public fun sendMessage(
            messageText: String,
            messageReplyTo: Message? = null,
        )

        public fun sendMessageWithAttachments(
            message: String,
            attachmentsFiles: List<File>,
            messageReplyTo: Message? = null,
        )

        public fun sendToThread(
            parentMessage: Message,
            messageText: String,
            alsoSendToChannel: Boolean,
        )

        public fun sendToThreadWithAttachments(
            parentMessage: Message,
            message: String,
            alsoSendToChannel: Boolean,
            attachmentsFiles: List<File>,
        )

        public fun editMessage(oldMessage: Message, newMessageText: String)
        public fun dismissReply()
    }

    public fun interface OnMessageSendButtonClickListener {
        public fun onClick()
    }

    public interface TypingListener {
        public fun onKeystroke()
        public fun onStopTyping()
    }
}
