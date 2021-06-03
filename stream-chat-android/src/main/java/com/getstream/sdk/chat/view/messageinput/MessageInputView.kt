package com.getstream.sdk.chat.view.messageinput

import android.content.Context
import android.os.Build
import android.text.Editable
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.core.view.inputmethod.InputConnectionCompat
import androidx.core.view.inputmethod.InputContentInfoCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.CaptureMediaContract
import com.getstream.sdk.chat.R
import com.getstream.sdk.chat.SelectFilesContract
import com.getstream.sdk.chat.adapter.CommandsAdapter
import com.getstream.sdk.chat.adapter.FileAttachmentSelectedAdapter
import com.getstream.sdk.chat.adapter.MediaAttachmentAdapter
import com.getstream.sdk.chat.adapter.MediaAttachmentSelectedAdapter
import com.getstream.sdk.chat.adapter.MentionsAdapter
import com.getstream.sdk.chat.databinding.StreamViewMessageInputBinding
import com.getstream.sdk.chat.enums.MessageInputType
import com.getstream.sdk.chat.model.AttachmentMetaData
import com.getstream.sdk.chat.model.ModelType
import com.getstream.sdk.chat.utils.GridSpacingItemDecoration
import com.getstream.sdk.chat.utils.Utils
import com.getstream.sdk.chat.utils.extensions.activity
import com.getstream.sdk.chat.utils.extensions.whenFalse
import com.getstream.sdk.chat.utils.extensions.whenTrue
import io.getstream.chat.android.client.models.Command
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import java.io.File

public class MessageInputView(context: Context, attrs: AttributeSet?) : RelativeLayout(context, attrs) {
    private val binding: StreamViewMessageInputBinding =
        StreamViewMessageInputBinding.inflate(LayoutInflater.from(context), this, true)

    /**
     * Styling class for the MessageInput
     */
    private val style: MessageInputStyle = MessageInputStyle(context, attrs)
    private val gridLayoutManager = GridLayoutManager(context, 4, RecyclerView.VERTICAL, false)
    private val gridSpacingItemDecoration = GridSpacingItemDecoration(4, 2, false)

    private var isKeyboardEventListenerInitialized = false

    public var messageText: String
        get() = binding.messageTextInput.text.toString()
        set(text) {
            if (TextUtils.isEmpty(text)) return
            binding.messageTextInput.requestFocus()
            binding.messageTextInput.setText(text)
            binding.messageTextInput.setSelection(binding.messageTextInput.text.length)
        }

    public var maxMessageLength: Int = Integer.MAX_VALUE

    public var messageSendHandler: MessageSendHandler = object : MessageSendHandler {
        override fun sendMessage(messageText: String) {
            throw IllegalStateException("MessageInputView#messageSendHandler needs to be configured to send messages")
        }

        override fun sendMessageWithAttachments(message: String, attachmentsFiles: List<Pair<File, String?>>) {
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
    }

    private object LauncherRequestsKeys {
        const val CAPTURE_MEDIA = "capture_media_request_key"
        const val SELECT_FILES = "select_files_request_key"
    }

    private var activityResultLauncher: ActivityResultLauncher<Unit>? = null
    private var selectFilesResultLauncher: ActivityResultLauncher<Unit>? = null

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        val activityResultRegistry = activity?.activityResultRegistry

        activityResultLauncher = activityResultRegistry
            ?.register(LauncherRequestsKeys.CAPTURE_MEDIA, CaptureMediaContract()) { file: File? ->
                file?.let { messageInputController.onFileCaptured(context, it) }
            }
        selectFilesResultLauncher = activityResultRegistry
            ?.register(LauncherRequestsKeys.SELECT_FILES, SelectFilesContract()) {
                messageInputController.onFilesSelected(it)
            }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        activityResultLauncher?.unregister()
        selectFilesResultLauncher?.unregister()
    }

    private val commandsAdapter =
        CommandsAdapter(style) { messageInputController.onCommandSelected(it) }
    private val mentionsAdapter = MentionsAdapter(style) {
        messageInputController.onUserSelected(messageText, it)
    }

    private var typeListeners: List<TypeListener> = listOf()

    public fun addTypeListener(typeListener: TypeListener) {
        typeListeners = typeListeners + typeListener
    }

    public fun removeTypeListener(typeListener: TypeListener) {
        typeListeners = typeListeners - typeListener
    }

    private val messageInputController: MessageInputController by lazy {
        MessageInputController(binding, this, style)
    }

    private fun applyStyle() {
        ActivityResultContracts.GetContent()
        // Attachment Button
        binding.ivOpenAttach.isVisible = style.isShowAttachmentButton
        binding.ivOpenAttach.setImageDrawable(style.getAttachmentButtonIcon(false))
        binding.ivOpenAttach.layoutParams.width = style.attachmentButtonWidth
        binding.ivOpenAttach.layoutParams.height = style.attachmentButtonHeight
        binding.btnClose.background = style.attachmentCloseButtonBackground
        // Send Button
        binding.sendButton.setImageDrawable(style.getInputButtonIcon(false))
        binding.sendButton.layoutParams.width = style.inputButtonWidth
        binding.sendButton.layoutParams.height = style.inputButtonHeight
        binding.cbSendAlsoToChannel.setTextColor(style.inputSendAlsoToChannelTextColor)
        // Input Background
        binding.llComposer.background = style.inputBackground
        // Input Text
        style.inputText.apply(binding.messageTextInput)
        binding.messageTextInput.hint = style.getInputHint()
        binding.messageTextInput.isVerticalScrollBarEnabled = style.inputScrollbarEnabled
        binding.messageTextInput.isScrollbarFadingEnabled = style.inputScrollbarFadingEnabled
        style.inputBackgroundText.apply(binding.tvTitle)
        style.inputBackgroundText.apply(binding.tvCommand)
        style.inputBackgroundText.apply(binding.tvUploadPhotoVideo)
        style.inputBackgroundText.apply(binding.tvUploadFile)
        style.inputBackgroundText.apply(binding.tvUploadCamera)
        binding.rvMedia.layoutManager = gridLayoutManager
        binding.rvMedia.addItemDecoration(gridSpacingItemDecoration)
        binding.rvSuggestions.background = style.suggestionsBackground
    }

    private fun configOnClickListener() {
        binding.sendButton.setOnClickListener { onSendMessage() }
        binding.ivOpenAttach.setOnClickListener {
            messageInputController.onClickOpenAttachmentSelectionMenu(MessageInputType.ADD_FILE)
        }
    }

    private fun configInputEditText() {
        binding.messageTextInput.onFocusChangeListener =
            OnFocusChangeListener { _: View?, hasFocus: Boolean ->
                if (hasFocus) {
                    Utils.showSoftKeyboard(this)
                } else {
                    Utils.hideSoftKeyboard(this)
                }
                if (!isKeyboardEventListenerInitialized) {
                    isKeyboardEventListenerInitialized = true
                    setKeyboardEventListener()
                }
            }
        binding.messageTextInput.doAfterTextChanged { editable: Editable? ->
            keyStroke(
                editable?.toString() ?: ""
            )
        }
        binding.messageTextInput.setCallback { inputContentInfo, flags, _ ->
            sendGifFromKeyboard(inputContentInfo, flags)
        }
    }

    private fun keyStroke(inputMessage: String) {
        messageInputController.checkCommandsOrMentions(messageText)
        binding.sendButton.isVisible = inputMessage.isNotBlank()
            .whenTrue { typeListeners.forEach(TypeListener::onKeystroke) }
            .whenFalse { typeListeners.forEach(TypeListener::onStopTyping) }
        configSendButtonEnableState()
        configInputEditTextError()
    }

    private fun configInputEditTextError() {
        if (isMessageTooLong()) {
            binding.messageTextInput.error =
                String.format(context.getString(R.string.stream_message_length_exceeded_error), maxMessageLength)
        } else {
            binding.messageTextInput.error = null
        }
    }

    private fun configSendButtonEnableState() {
        val attachments = messageInputController.getSelectedAttachments()
        val notEmptyMessage = messageText.isNotBlank() || attachments.isNotEmpty()
        binding.sendButton.isVisible = notEmptyMessage && !isMessageTooLong()
    }

    private fun isMessageTooLong() = messageText.length > maxMessageLength

    private fun configAttachmentUI() {
        // TODO: make the attachment UI into it's own view and allow you to change it.
        binding.mediaComposer.layoutManager =
            GridLayoutManager(context, 1, RecyclerView.HORIZONTAL, false)
        binding.btnClose.setOnClickListener {
            messageInputController.onClickCloseAttachmentSelectionMenu()
            Utils.hideSoftKeyboard(this)
        }
        binding.selectMedia.setOnClickListener { messageInputController.onClickOpenMediaSelectView() }
        binding.selectCamera.setOnClickListener { messageInputController.onCameraClick() }
        binding.selectFile.setOnClickListener {
            selectFilesResultLauncher?.launch(Unit)
        }
    }

    internal fun showCameraOptions() {
        activityResultLauncher?.launch(Unit)
    }

    private fun setKeyboardEventListener() {
        KeyboardVisibilityEvent.setEventListener(activity) { isOpen: Boolean ->
            if (!isOpen) {
                binding.messageTextInput.clearFocus()
            }
        }
    }

    override fun setEnabled(enabled: Boolean) {
        binding.messageTextInput.isEnabled = true
    }

    override fun clearFocus() {
        binding.messageTextInput.clearFocus()
    }

    public fun configureMembers(members: List<Member>) {
        messageInputController.members = members
    }

    public fun configureCommands(commands: List<Command>) {
        messageInputController.channelCommands = commands
    }

    private fun onSendMessage() {
        messageInputController.onSendMessageClick(messageText)
        handleSentMessage()
    }

    internal fun sendTextMessage(message: String) {
        messageSendHandler.sendMessage(message)
    }

    internal fun sendAttachments(message: String, attachmentFiles: List<Pair<File, String?>>) {
        messageSendHandler.sendMessageWithAttachments(message, attachmentFiles)
    }

    internal fun sendToThread(parentMessage: Message, message: String, alsoSendToChannel: Boolean) {
        messageSendHandler.sendToThread(parentMessage, message, alsoSendToChannel)
    }

    internal fun sendToThreadWithAttachments(
        parentMessage: Message,
        message: String,
        alsoSendToChannel: Boolean,
        attachmentFiles: List<File>,
    ) {
        messageSendHandler.sendToThreadWithAttachments(
            parentMessage,
            message,
            alsoSendToChannel,
            attachmentFiles
        )
    }

    internal fun editMessage(oldMessage: Message, newMessageText: String) {
        messageSendHandler.editMessage(oldMessage, newMessageText)
    }

    private fun handleSentMessage() {
        typeListeners.forEach(TypeListener::onStopTyping)
        initSendMessage()
    }

    private fun initSendMessage() {
        messageInputController.initSendMessage()
        binding.messageTextInput.setText("")
        binding.sendButton.isEnabled = true
    }

    private fun sendGifFromKeyboard(
        inputContentInfo: InputContentInfoCompat,
        flags: Int,
    ): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            flags and InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION != 0
        ) {
            try {
                inputContentInfo.requestPermission()
            } catch (e: Exception) {
                return false
            }
        }
        val title = inputContentInfo.contentUri.pathSegments.lastOrNull() ?: AttachmentMetaData.DEFAULT_ATTACHMENT_TITLE
        messageInputController.setSelectedAttachments(
            setOf(
                AttachmentMetaData(
                    uri = inputContentInfo.contentUri,
                    type = ModelType.attach_image,
                    mimeType = ModelType.attach_mime_gif,
                    title = title,
                )
            )
        )
        binding.messageTextInput.setText("")
        onSendMessage()
        return true
    }

    internal fun showSuggestedMentions(users: List<User>) {
        mentionsAdapter.submitList(users)
    }

    internal fun showSuggestedCommand(commands: List<Command>) {
        commandsAdapter.submitList(commands)
    }

    public fun setNormalMode() {
        messageInputController.inputMode = InputMode.Normal
    }

    public fun setThreadMode(parentMessage: Message) {
        messageInputController.inputMode = InputMode.Thread(parentMessage)
    }

    public fun setEditMode(oldMessage: Message) {
        messageInputController.inputMode = InputMode.Edit(oldMessage)
    }

    internal fun showSelectedMediaAttachments(selectedMediaAttachmentAdapter: MediaAttachmentSelectedAdapter) {
        binding.mediaComposer.adapter = selectedMediaAttachmentAdapter
        binding.mediaComposer.isVisible = true
        binding.fileComposer.isVisible = false
        binding.fileComposer.adapter = null
    }

    internal fun showSelectedFileAttachments(selectedFileAttachmentAdapter: FileAttachmentSelectedAdapter) {
        binding.fileComposer.adapter = selectedFileAttachmentAdapter
        binding.fileComposer.isVisible = true
        binding.mediaComposer.isVisible = false
        binding.mediaComposer.adapter = null
    }

    internal fun showTotalMediaAttachments(totalMediaAttachmentAdapter: MediaAttachmentAdapter) {
        binding.rvMedia.adapter = totalMediaAttachmentAdapter
    }

    internal fun showMediaAttachments() {
        binding.mediaComposer.isVisible = true
        binding.fileComposer.isVisible = false
    }

    internal fun showFileAttachments() {
        binding.mediaComposer.isVisible = false
        binding.fileComposer.isVisible = true
    }

    internal fun showMediaPermissions(shouldBeVisible: Boolean) {
        binding.ivMediaPermission.isVisible = shouldBeVisible
    }

    internal fun showCameraPermissions(shouldBeVisible: Boolean) {
        binding.ivCameraPermission.isVisible = shouldBeVisible
    }

    internal fun hideAttachmentsMenu() {
        binding.clTitle.isVisible = false
        binding.clAddFile.isVisible = false
        binding.clSelectPhoto.isVisible = false
        binding.root.setBackgroundResource(0)
    }

    internal fun showAttachmentsMenu() {
        binding.root.background = style.attachmentsMenuBackground
        binding.clTitle.visibility = View.VISIBLE
        binding.btnClose.visibility = View.VISIBLE
        binding.clAddFile.visibility = View.GONE
        binding.clSelectPhoto.visibility = View.GONE
    }

    internal fun showLoadingTotalAttachments(shouldBeVisible: Boolean) {
        binding.progressBarFileLoader.isVisible = shouldBeVisible
    }

    internal fun showOpenAttachmentsMenuButton(shouldBeVisible: Boolean) {
        binding.ivOpenAttach.isVisible = shouldBeVisible
    }

    public fun showMessage(@StringRes messageResId: Int) {
        Toast.makeText(context, messageResId, Toast.LENGTH_SHORT).show()
    }

    public interface TypeListener {
        public fun onKeystroke()
        public fun onStopTyping()
    }

    public interface MessageSendHandler {
        public fun sendMessage(messageText: String)
        public fun sendMessageWithAttachments(message: String, attachmentsFiles: List<Pair<File, String?>>)
        public fun sendToThread(parentMessage: Message, messageText: String, alsoSendToChannel: Boolean)
        public fun sendToThreadWithAttachments(
            parentMessage: Message,
            message: String,
            alsoSendToChannel: Boolean,
            attachmentsFiles: List<File>,
        )

        public fun editMessage(oldMessage: Message, newMessageText: String)
    }

    init {
        applyStyle()
        binding.rvSuggestions.adapter = ConcatAdapter(commandsAdapter, mentionsAdapter)
        binding.sendButton.isVisible = false
        configOnClickListener()
        configInputEditText()
        configAttachmentUI()
    }
}
