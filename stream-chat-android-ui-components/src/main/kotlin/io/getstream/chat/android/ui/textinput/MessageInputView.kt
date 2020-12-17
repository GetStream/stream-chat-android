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
import com.getstream.sdk.chat.model.AttachmentMetaData
import com.getstream.sdk.chat.utils.StorageHelper
import io.getstream.chat.android.client.models.Command
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.attachments.AttachmentDialogFragment
import io.getstream.chat.android.ui.attachments.AttachmentSelectionListener
import io.getstream.chat.android.ui.attachments.AttachmentSource
import io.getstream.chat.android.ui.databinding.StreamUiMessageInputBinding
import io.getstream.chat.android.ui.suggestions.SuggestionListController
import io.getstream.chat.android.ui.suggestions.SuggestionListView
import io.getstream.chat.android.ui.utils.extensions.getColorCompat
import io.getstream.chat.android.ui.utils.extensions.getFragmentManager
import io.getstream.chat.android.ui.utils.getBackgroundColor
import io.getstream.chat.android.ui.utils.getColorList
import io.getstream.chat.android.ui.utils.getTextColor
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    private var iconDisabledSendButtonDrawable: Drawable? = null
    private var iconEnabledSendButtonDrawable: Drawable? = null
    private var sendAlsoToChannelCheckBoxEnabled: Boolean = true

    public var inputMode: InputMode by Delegates.observable(InputMode.Normal) { _, _, _ ->
        configSendAlsoToChannelCheckbox()
    }

    public var chatMode: ChatMode by Delegates.observable(ChatMode.GroupChat) { _, _, _ ->
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
                    AttachmentSource.CAMERA -> {
                        binding.messageInputFieldView.mode =
                            MessageInputFieldView.Mode.MediaAttachmentMode(attachments.toList())
                    }
                    AttachmentSource.FILE -> {
                        GlobalScope.launch(DispatcherProvider.Main) {
                            val attachments = withContext(DispatcherProvider.IO) {
                                val uris = attachments.mapNotNull(AttachmentMetaData::uri)
                                StorageHelper().getAttachmentsFromUriList(context, uris).toMutableList()
                            }

                            binding.messageInputFieldView.mode =
                                MessageInputFieldView.Mode.FileAttachmentMode(attachments)
                        }
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

    public fun configureMembers(members: List<Member>) {
        suggestionListController?.users = members.map { it.user }
    }

    public fun configureCommands(commands: List<Command>) {
        suggestionListController?.commands = commands
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
        configColours()
        configSendAlsoToChannelCheckbox()
        configSendButtonListener()
    }

    private fun configColours() {
        binding.root.setBackgroundColor(ContextCompat.getColor(context, getBackgroundColor(context)))
    }

    private fun configSendButtonListener() {
        binding.sendMessageButtonEnabled.setOnClickListener {
            onSendButtonClickListener?.onClick()
            inputMode.let {
                when (it) {
                    is InputMode.Normal -> sendMessage()
                    is InputMode.Thread -> sendThreadMessage(it.parentMessage)
                    is InputMode.Edit -> editMessage(it.oldMessage)
                }
            }
            binding.messageInputFieldView.clearContent()
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
        binding.attachmentsButton.run {
            isVisible = typedArray.getBoolean(R.styleable.StreamUiMessageInputView_streamUiAttachButtonEnabled, true)

            typedArray.getDrawable(R.styleable.StreamUiMessageInputView_streamUiAttachButtonIcon)
                ?.let(this::setImageDrawable)

            DrawableCompat.setTintList(
                drawable,
                getColorList(
                    normalColor = typedArray.getColor(
                        R.styleable.StreamUiMessageInputView_streamUiAttachButtonIconColor,
                        context.getColorCompat(R.color.stream_ui_grey)
                    ),
                    selectedColor = typedArray.getColor(
                        R.styleable.StreamUiMessageInputView_streamUiAttachButtonIconPressedColor,
                        context.getColorCompat(R.color.stream_ui_blue)
                    ),
                    disabledColor = typedArray.getColor(
                        R.styleable.StreamUiMessageInputView_streamUiAttachButtonIconPressedColor,
                        context.getColorCompat(R.color.stream_ui_grey_medium_light)
                    )
                )
            )

            layoutParams.width = typedArray.getDimensionPixelSize(
                R.styleable.StreamUiMessageInputView_streamUiAttachButtonWidth,
                context.resources.getDimensionPixelSize(R.dimen.stream_ui_attachment_button_width)
            )

            layoutParams.height = typedArray.getDimensionPixelSize(
                R.styleable.StreamUiMessageInputView_streamUiAttachButtonHeight,
                context.resources.getDimensionPixelSize(R.dimen.stream_ui_attachment_button_height)
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
                typedArray.getBoolean(R.styleable.StreamUiMessageInputView_streamUiLightningButtonEnabled, true)

            typedArray.getDrawable(R.styleable.StreamUiMessageInputView_streamUiLightningButtonIcon)
                ?.let(this::setImageDrawable)

            DrawableCompat.setTintList(
                drawable,
                getColorList(
                    normalColor = typedArray.getColor(
                        R.styleable.StreamUiMessageInputView_streamUiLightningButtonIconColor,
                        context.getColorCompat(R.color.stream_ui_grey)
                    ),
                    selectedColor = typedArray.getColor(
                        R.styleable.StreamUiMessageInputView_streamUiLightningButtonIconPressedColor,
                        context.getColorCompat(R.color.stream_ui_blue)
                    ),
                    disabledColor = typedArray.getColor(
                        R.styleable.StreamUiMessageInputView_streamUiLightningButtonIconPressedColor,
                        context.getColorCompat(R.color.stream_ui_grey_medium_light)
                    )
                )
            )

            layoutParams.width = typedArray.getDimensionPixelSize(
                R.styleable.StreamUiMessageInputView_streamUiLightningButtonWidth,
                context.resources.getDimensionPixelSize(R.dimen.stream_ui_attachment_button_width)
            )

            layoutParams.height = typedArray.getDimensionPixelSize(
                R.styleable.StreamUiMessageInputView_streamUiLightningButtonHeight,
                context.resources.getDimensionPixelSize(R.dimen.stream_ui_attachment_button_height)
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

    private fun showSendMessageEnabled() {
        val fadeAnimator = binding.sendMessageButtonDisabled.run {
            ObjectAnimator.ofFloat(this, "alpha", alpha, 0F).setDuration(ANIMATION_DURATION)
        }

        val appearAnimator = binding.sendMessageButtonEnabled.run {
            ObjectAnimator.ofFloat(this, "alpha", alpha, 1F).setDuration(ANIMATION_DURATION)
        }

        currentAnimatorSet?.cancel()

        currentAnimatorSet = AnimatorSet().apply {
            playSequentially(fadeAnimator, appearAnimator)
            start()
        }

        binding.sendMessageButtonEnabled.isEnabled = true
    }

    private fun showSendMessageDisabled() {
        val fadeAnimator = binding.sendMessageButtonEnabled.run {
            ObjectAnimator.ofFloat(this, "alpha", alpha, 0F).setDuration(ANIMATION_DURATION)
        }

        val appearAnimator = binding.sendMessageButtonDisabled.run {
            ObjectAnimator.ofFloat(this, "alpha", alpha, 1F).setDuration(ANIMATION_DURATION)
        }

        currentAnimatorSet?.cancel()

        currentAnimatorSet = AnimatorSet().apply {
            playSequentially(fadeAnimator, appearAnimator)
            start()
        }

        binding.sendMessageButtonEnabled.isEnabled = false
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
                    R.styleable.StreamUiMessageInputView_streamUiMessageInputTextColor,
                    ContextCompat.getColor(context, getTextColor(context))
                )
            )

            setHintTextColor(
                typedArray.getColor(
                    R.styleable.StreamUiMessageInputView_streamUiMessageInputHintTextColor,
                    ContextCompat.getColor(context, R.color.stream_ui_gray_dark)
                )
            )

            setTextSizePx(
                typedArray.getDimensionPixelSize(
                    R.styleable.StreamUiMessageInputView_streamUiMessageInputTextSize,
                    context.resources.getDimensionPixelSize(R.dimen.stream_ui_text_size_input)
                ).toFloat()
            )

            setHint(typedArray.getText(R.styleable.StreamUiMessageInputView_streamUiMessageInputHint))

            setInputFieldScrollBarEnabled(
                typedArray.getBoolean(
                    R.styleable.StreamUiMessageInputView_streamUiMessageInputScrollbarEnabled,
                    false
                )
            )

            setInputFieldScrollbarFadingEnabled(
                typedArray.getBoolean(
                    R.styleable.StreamUiMessageInputView_streamUiMessageInputScrollbarFadingEnabled,
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
            typedArray.getDrawable(R.styleable.StreamUiMessageInputView_streamUiSendButtonDisabledIcon)
            ?: ContextCompat.getDrawable(context, R.drawable.stream_ui_ic_filled_right_arrow)
            ?: throw IllegalStateException(NO_ICON_MESSAGE_DISABLED_STATE)

        iconEnabledSendButtonDrawable =
            typedArray.getDrawable(R.styleable.StreamUiMessageInputView_streamUiSendButtonEnabledIcon)
            ?: ContextCompat.getDrawable(context, R.drawable.stream_ui_ic_filled_up_arrow)
            ?: throw IllegalStateException(NO_ICON_MESSAGE_ENABLED_STATE)

        DrawableCompat.setTintList(
            iconEnabledSendButtonDrawable!!,
            getColorList(
                normalColor = typedArray.getColor(
                    R.styleable.StreamUiMessageInputView_streamUiSendButtonEnabledIconColor,
                    context.getColorCompat(R.color.stream_ui_blue)
                ),
                selectedColor = typedArray.getColor(
                    R.styleable.StreamUiMessageInputView_streamUiSendButtonPressedIconColor,
                    context.getColorCompat(R.color.stream_ui_blue)
                ),
                disabledColor = typedArray.getColor(
                    R.styleable.StreamUiMessageInputView_streamUiSendButtonDisabledIconColor,
                    context.getColorCompat(R.color.stream_ui_grey_medium_light)
                )
            )
        )

        DrawableCompat.setTintList(
            iconDisabledSendButtonDrawable!!,
            getColorList(
                normalColor = typedArray.getColor(
                    R.styleable.StreamUiMessageInputView_streamUiSendButtonDisabledIconColor,
                    ContextCompat.getColor(context, getDisabledSendButtonIconColor())
                ),
                selectedColor = typedArray.getColor(
                    R.styleable.StreamUiMessageInputView_streamUiSendButtonPressedIconColor,
                    ContextCompat.getColor(context, R.color.stream_ui_blue)
                ),
                disabledColor = typedArray.getColor(
                    R.styleable.StreamUiMessageInputView_streamUiSendButtonDisabledIconColor,
                    ContextCompat.getColor(context, R.color.stream_ui_grey_medium_light)
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

    private fun getDisabledSendButtonIconColor(): Int {
        return when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_NO -> R.color.stream_ui_grey_medium_light
            Configuration.UI_MODE_NIGHT_YES -> R.color.stream_ui_disabled_send_message_dark_theme
            else -> R.color.stream_ui_grey_medium_light
        }
    }

    private fun refreshControlsState() {
        val isCommandMode = binding.messageInputFieldView.mode is MessageInputFieldView.Mode.CommandMode
        val hasContent = binding.messageInputFieldView.hasContent()

        binding.attachmentsButton.isVisible = !isCommandMode
        binding.commandsButton.isVisible = !isCommandMode
        binding.commandsButton.isEnabled = !hasContent
        if (hasContent) {
            showSendMessageEnabled()
        } else {
            showSendMessageDisabled()
        }
    }

    private fun sendMessage() {
        if (binding.messageInputFieldView.hasAttachments()) {
            sendMessageHandler.sendMessageWithAttachments(
                binding.messageInputFieldView.messageText,
                binding.messageInputFieldView.getAttachedFiles()
            )
        } else {
            sendMessageHandler.sendMessage(binding.messageInputFieldView.messageText)
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
