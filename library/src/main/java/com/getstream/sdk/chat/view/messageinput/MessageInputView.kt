package com.getstream.sdk.chat.view.messageinput

import android.app.Activity
import android.content.Context
import android.os.Build
import android.text.Editable
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.RelativeLayout
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.core.view.inputmethod.InputConnectionCompat
import androidx.core.view.inputmethod.InputContentInfoCompat
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
import com.getstream.sdk.chat.utils.StringUtility
import com.getstream.sdk.chat.utils.TextViewUtils
import com.getstream.sdk.chat.utils.Utils
import com.getstream.sdk.chat.utils.whenFalse
import com.getstream.sdk.chat.utils.whenTrue
import com.getstream.sdk.chat.view.common.visible
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Command
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import java.io.File

class MessageInputView(context: Context, attrs: AttributeSet?) : RelativeLayout(context, attrs) {
    private val binding: StreamViewMessageInputBinding =
        StreamViewMessageInputBinding.inflate(LayoutInflater.from(context), this, true)

    /**
     * Styling class for the MessageInput
     */
    private val style: MessageInputStyle = MessageInputStyle(context, attrs)
    private val gridLayoutManager = GridLayoutManager(context, 4, RecyclerView.VERTICAL, false)
    private val gridSpacingItemDecoration = GridSpacingItemDecoration(4, 2, false)

    private var isKeyboardEventListenerInitialized = false

    var messageText: String
        get() = binding.messageTextInput.text.toString()
        set(text) {
            if (TextUtils.isEmpty(text)) return
            binding.messageTextInput.requestFocus()
            binding.messageTextInput.setText(text)
            binding.messageTextInput.setSelection(binding.messageTextInput.text.length)
        }

    var messageSendHandler: MessageSendHandler = object : MessageSendHandler {
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

    private val activityResultLauncher: ActivityResultLauncher<Unit>? =
        (context as? ComponentActivity)
            ?.registerForActivityResult(CaptureMediaContract()) { file: File? ->
                file?.let { messageInputController.onFileCaptured(it) }
            }

    private val selectFilesResultLauncher: ActivityResultLauncher<Unit>? =
        (context as? ComponentActivity)?.registerForActivityResult(SelectFilesContract()) {
            messageInputController.onFilesSelected(it)
        }

    private val commandsAdapter =
        CommandsAdapter(style) { messageInputController.onCommandSelected(it) }
    private val mentionsAdapter = MentionsAdapter(style) {
        messageInputController.onUserSelected(messageText, it)
    }
    private var typeListeners: List<TypeListener> = listOf()
    fun addTypeListener(typeListener: TypeListener) {
        typeListeners = typeListeners + typeListener
    }

    fun removeTypeListener(typeListener: TypeListener) {
        typeListeners = typeListeners - typeListener
    }

    private val messageInputController: MessageInputController by lazy {
        MessageInputController(binding, this, style)
    }

    private fun applyStyle() {
        ActivityResultContracts.GetContent()
        // Attachment Button
        binding.ivOpenAttach.visible(style.isShowAttachmentButton)
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
        style.inputBackgroundText.apply(binding.tvTitle)
        style.inputBackgroundText.apply(binding.tvCommand)
        style.inputBackgroundText.apply(binding.tvUploadPhotoVideo)
        style.inputBackgroundText.apply(binding.tvUploadFile)
        style.inputBackgroundText.apply(binding.tvUploadCamera)
        binding.rvMedia.layoutManager = gridLayoutManager
        binding.rvMedia.addItemDecoration(gridSpacingItemDecoration)
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
        TextViewUtils.afterTextChanged(binding.messageTextInput) { editable: Editable ->
            keyStroke(
                editable.toString()
            )
        }
        binding.messageTextInput.setCallback { inputContentInfo, flags, _ ->
            sendGiphyFromKeyboard(inputContentInfo, flags)
        }
    }

    private fun keyStroke(inputMessage: String) {
        messageInputController.checkCommandsOrMentions(messageText)
        binding.activeMessageSend = inputMessage.isNotBlank()
            .whenTrue { typeListeners.forEach(TypeListener::onKeystroke) }
            .whenFalse { typeListeners.forEach(TypeListener::onStopTyping) }
        configSendButtonEnableState()
    }

    private fun configSendButtonEnableState() {
        val attachments = messageInputController.getSelectedAttachments()
        val notEmptyMessage =
            !StringUtility.isEmptyTextMessage(messageText) || attachments.isNotEmpty()
        binding.activeMessageSend = notEmptyMessage
    }

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
        KeyboardVisibilityEvent.setEventListener(
            context as Activity
        ) { isOpen: Boolean ->
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

    fun configureMembers(members: List<Member>) {
        messageInputController.members = members
    }

    fun configureCommands(commands: List<Command>) {
        messageInputController.channelCommands = commands
    }

    private fun onSendMessage() {
        messageInputController.onSendMessageClick(messageText)
        handleSentMessage()
    }

    internal fun sendTextMessage(message: String) {
        messageSendHandler.sendMessage(message)
    }

    internal fun sendAttachments(message: String, attachmentFiles: List<File>) {
        messageSendHandler.sendMessageWithAttachments(message, attachmentFiles)
    }

    internal fun sendToThread(parentMessage: Message, message: String, alsoSendToChannel: Boolean) {
        messageSendHandler.sendToThread(parentMessage, message, alsoSendToChannel)
    }

    internal fun sendToThreadWithAttachments(
        parentMessage: Message,
        message: String,
        alsoSendToChannel: Boolean,
        attachmentFiles: List<File>
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

    private fun sendGiphyFromKeyboard(
        inputContentInfo: InputContentInfoCompat,
        flags: Int
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
        if (inputContentInfo.linkUri == null) return false
        val url = inputContentInfo.linkUri.toString()
        val attachment = Attachment()
        attachment.thumbUrl = url
        attachment.titleLink = url
        attachment.title = inputContentInfo.description.label.toString()
        attachment.type = ModelType.attach_giphy
        messageInputController.setSelectedAttachments(setOf(AttachmentMetaData(attachment)))
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

    fun setNormalMode() {
        messageInputController.inputMode = InputMode.Normal
    }

    fun setThreadMode(parentMessage: Message) {
        messageInputController.inputMode = InputMode.Thread(parentMessage)
    }

    fun setEditMode(oldMessage: Message) {
        messageInputController.inputMode = InputMode.Edit(oldMessage)
    }

    internal fun showSelectedMediaAttachments(selectedMediaAttachmentAdapter: MediaAttachmentSelectedAdapter) {
        binding.mediaComposer.adapter = selectedMediaAttachmentAdapter
        binding.mediaComposer.visible(true)
        binding.fileComposer.visible(false)
        binding.fileComposer.adapter = null
    }

    internal fun showSelectedFileAttachments(selectedFileAttachmentAdapter: FileAttachmentSelectedAdapter) {
        binding.fileComposer.adapter = selectedFileAttachmentAdapter
        binding.fileComposer.visible(true)
        binding.mediaComposer.visible(false)
        binding.mediaComposer.adapter = null
    }

    internal fun showTotalMediaAttachments(totalMediaAttachmentAdapter: MediaAttachmentAdapter) {
        binding.rvMedia.adapter = totalMediaAttachmentAdapter
    }

    internal fun showMediaAttachments() {
        binding.mediaComposer.visible(true)
        binding.fileComposer.visible(false)
    }

    internal fun showFileAttachments() {
        binding.mediaComposer.visible(false)
        binding.fileComposer.visible(true)
    }

    internal fun showMediaPermissions(shouldBeVisible: Boolean) = binding.ivMediaPermission.visible(shouldBeVisible)
    internal fun showCameraPermissions(shouldBeVisible: Boolean) = binding.ivCameraPermission.visible(shouldBeVisible)

    internal fun hideAttachmentsMenu() {
        binding.clTitle.visible(false)
        binding.clAddFile.visible(false)
        binding.clSelectPhoto.visible(false)
        binding.root.setBackgroundResource(0)
    }

    internal fun showAttachmentsMenu() {
        binding.root.setBackgroundResource(R.drawable.stream_round_thread_toolbar)
        binding.clTitle.visibility = View.VISIBLE
        binding.btnClose.visibility = View.VISIBLE
        binding.clAddFile.visibility = View.GONE
        binding.clSelectPhoto.visibility = View.GONE
    }

    internal fun showLoadingTotalAttachments(shouldBeVisible: Boolean) =
        binding.progressBarFileLoader.visible(shouldBeVisible)

    internal fun showOpenAttachmentsMenuButton(shouldBeVisible: Boolean) = binding.ivOpenAttach.visible(shouldBeVisible)
    fun showMessage(@StringRes messageResId: Int) = Utils.showMessage(context, messageResId)

    interface TypeListener {
        fun onKeystroke()
        fun onStopTyping()
    }

    interface MessageSendHandler {
        fun sendMessage(messageText: String)
        fun sendMessageWithAttachments(message: String, attachmentsFiles: List<File>)
        fun sendToThread(parentMessage: Message, messageText: String, alsoSendToChannel: Boolean)
        fun sendToThreadWithAttachments(
            parentMessage: Message,
            message: String,
            alsoSendToChannel: Boolean,
            attachmentsFiles: List<File>
        )

        fun editMessage(oldMessage: Message, newMessageText: String)
    }

    init {
        applyStyle()
        binding.rvSuggestions.adapter = ConcatAdapter(commandsAdapter, mentionsAdapter)
        binding.activeMessageSend = false
        configOnClickListener()
        configInputEditText()
        configAttachmentUI()
    }
}
