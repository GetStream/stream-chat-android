package com.getstream.sdk.chat.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.RelativeLayout
import androidx.core.os.BuildCompat
import androidx.core.view.inputmethod.InputConnectionCompat
import androidx.core.view.inputmethod.InputContentInfoCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.MergeAdapter
import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.Chat.Companion.getInstance
import com.getstream.sdk.chat.R
import com.getstream.sdk.chat.adapter.CommandsAdapter
import com.getstream.sdk.chat.adapter.MentionsAdapter
import com.getstream.sdk.chat.databinding.StreamViewMessageInputBinding
import com.getstream.sdk.chat.enums.MessageInputType
import com.getstream.sdk.chat.model.AttachmentMetaData
import com.getstream.sdk.chat.model.ModelType
import com.getstream.sdk.chat.navigation.destinations.CameraDestination
import com.getstream.sdk.chat.utils.CaptureController
import com.getstream.sdk.chat.utils.Constant
import com.getstream.sdk.chat.utils.LlcMigrationUtils
import com.getstream.sdk.chat.utils.MessageInputController
import com.getstream.sdk.chat.utils.PermissionChecker
import com.getstream.sdk.chat.utils.StringUtility
import com.getstream.sdk.chat.utils.TextViewUtils
import com.getstream.sdk.chat.utils.Utils
import com.getstream.sdk.chat.viewmodel.MessageInputViewModel
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Command
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import whenFalse
import whenTrue
import java.io.File

class MessageInputView(context: Context, attrs: AttributeSet?) : RelativeLayout(context, attrs) {
	private val binding: StreamViewMessageInputBinding = StreamViewMessageInputBinding.inflate(LayoutInflater.from(context), this, true)

	/**
	 * Styling class for the MessageInput
	 */
	private val style: MessageInputStyle = MessageInputStyle(context, attrs)

	/**
	 * Permission Request listener
	 */
	private var permissionRequestListener: PermissionRequestListener? = null

	var messageSendHandler: MessageSendHandler = object : MessageSendHandler {
		override fun sendMessage(messageText: String) {
			throw IllegalStateException("MessageInputView#messageSendHandler needs to be configured to send messages")
		}

		override fun sendMessageWithAttachments(message: String, attachmentsFiles: List<File>) {
			throw IllegalStateException("MessageInputView#messageSendHandler needs to be configured to send messages")
		}
	}

	private val commandsAdapter = CommandsAdapter(style) { messageInputController.onCommandSelected(it) }
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

	/**
	 * The viewModel for handling typing etc.
	 */
	private lateinit var viewModel: MessageInputViewModel
	private val messageInputController: MessageInputController by lazy {
		MessageInputController(binding, this, style)
	}

	fun setViewModel(viewModel: MessageInputViewModel, lifecycleOwner: LifecycleOwner) {
		this.viewModel = viewModel
		binding.lifecycleOwner = lifecycleOwner
		init()
		observeUIs(lifecycleOwner)
	}

	private fun init() {
		binding.activeMessageSend = false
		configOnClickListener()
		configInputEditText()
		configAttachmentUI()
		onBackPressed()
		setKeyboardEventListener()
	}

	private fun applyStyle() {
		// Attachment Button
		binding.ivOpenAttach.visibility = if (style.isShowAttachmentButton) View.VISIBLE else View.GONE
		binding.ivOpenAttach.setImageDrawable(style.getAttachmentButtonIcon(false))
		binding.ivOpenAttach.layoutParams.width = style.attachmentButtonWidth
		binding.ivOpenAttach.layoutParams.height = style.attachmentButtonHeight
		// Send Button
		binding.sendButton.setImageDrawable(style.getInputButtonIcon(false))
		binding.sendButton.layoutParams.width = style.inputButtonWidth
		binding.sendButton.layoutParams.height = style.inputButtonHeight
		// Input Background
		binding.llComposer.background = style.inputBackground
		// Input Text
		style.inputText.apply(binding.etMessage)
		style.inputBackgroundText.apply(binding.tvTitle)
		style.inputBackgroundText.apply(binding.tvCommand)
		style.inputBackgroundText.apply(binding.tvUploadPhotoVideo)
		style.inputBackgroundText.apply(binding.tvUploadFile)
		style.inputBackgroundText.apply(binding.tvUploadCamera)
	}

	private fun configOnClickListener() {
		binding.sendButton.setOnClickListener { onSendMessage() }
		binding.ivOpenAttach.setOnClickListener { view: View? ->
			messageInputController.onClickOpenBackGroundView(MessageInputType.ADD_FILE)
			if (! PermissionChecker.isGrantedCameraPermissions(context)
					&& permissionRequestListener != null && ! style.passedPermissionCheck()) permissionRequestListener !!.openPermissionRequest()
		}
	}

	private fun configInputEditText() {
		binding.etMessage.onFocusChangeListener = OnFocusChangeListener { view: View?, hasFocus: Boolean ->
			if (hasFocus) {
				Utils.showSoftKeyboard(context as Activity)
			} else Utils.hideSoftKeyboard(context as Activity)
		}
		TextViewUtils.afterTextChanged(binding.etMessage) { editable: Editable -> keyStroke(editable.toString()) }
		binding.etMessage.setCallback { inputContentInfo: InputContentInfoCompat, flags: Int, opts: Bundle -> sendGiphyFromKeyboard(inputContentInfo, flags, opts) }
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
		val hasAttachment = attachments != null && ! attachments.isEmpty()
		val notEmptyMessage = ! StringUtility.isEmptyTextMessage(messageText) || ! messageInputController.isUploadingFile && hasAttachment
		binding.activeMessageSend = notEmptyMessage
	}

	private fun configAttachmentUI() {
		// TODO: make the attachment UI into it's own view and allow you to change it.
		binding.rvComposer.layoutManager = GridLayoutManager(context, 1, RecyclerView.HORIZONTAL, false)
		binding.btnClose.setOnClickListener { v: View? ->
			messageInputController.onClickCloseBackGroundView()
			Utils.hideSoftKeyboard(context as Activity)
			if (viewModel.isEditing) {
				initSendMessage()
				clearFocus()
			}
		}
		binding.llMedia.setOnClickListener { messageInputController.onClickOpenSelectView(null, true) }
		binding.llCamera.setOnClickListener { v: View ->
			if (! PermissionChecker.isGrantedCameraPermissions(context)) {
				PermissionChecker.checkCameraPermissions(v) { navigateToCamera(v) }
				return@setOnClickListener
			}
			navigateToCamera(v)
		}
		binding.llFile.setOnClickListener { messageInputController.onClickOpenSelectView( null, false) }
	}

	private fun navigateToCamera(v: View) {
		Utils.setButtonDelayEnable(v)
		messageInputController.onClickCloseBackGroundView()
		Utils.hideSoftKeyboard(context as Activity)
		getInstance().navigator.navigate(CameraDestination(context as Activity))
	}

	private fun onBackPressed() {
		isFocusableInTouchMode = true
		requestFocus()
		setOnKeyListener { v: View?, keyCode: Int, event: KeyEvent ->
			if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
				if (viewModel.isThread) {
					viewModel.resetThread()
					initSendMessage()
					return@setOnKeyListener false
				}
				if (viewModel.isEditing) {
					messageInputController.onClickCloseBackGroundView()
					initSendMessage()
					return@setOnKeyListener true
				}
				if (! TextUtils.isEmpty(messageText)) {
					initSendMessage()
					return@setOnKeyListener true
				}
				if (binding.clTitle.visibility == View.VISIBLE) {
					messageInputController.onClickCloseBackGroundView()
					initSendMessage()
					return@setOnKeyListener true
				}
				return@setOnKeyListener false
			}
			false
		}
	}

	private fun setKeyboardEventListener() {
		KeyboardVisibilityEvent.setEventListener(
				context as Activity) { isOpen: Boolean ->
			if (! isOpen) {
				binding.etMessage.clearFocus()
				onBackPressed()
			}
		}
	}

	override fun setEnabled(enabled: Boolean) {
		binding.etMessage.isEnabled = true
	}

	override fun clearFocus() {
		binding.etMessage.clearFocus()
	}

	var messageText: String
		get() = binding.etMessage.text.toString()
		set(text) {
			if (TextUtils.isEmpty(text)) return
			binding.etMessage.requestFocus()
			binding.etMessage.setText(text)
			binding.etMessage.setSelection(binding.etMessage.text.length)
		}

	private fun observeUIs(lifecycleOwner: LifecycleOwner) {
		viewModel.getEditMessage().observe(lifecycleOwner , Observer { message: Message? -> editMessage(message) })
		viewModel.getMessageListScrollUp().observe(lifecycleOwner, Observer { messageListScrollup: Boolean -> if (messageListScrollup) Utils.hideSoftKeyboard(context as Activity) })
		viewModel.getActiveThread().observe(lifecycleOwner, Observer { threadParentMessage: Message? ->
			if (threadParentMessage == null) {
				initSendMessage()
				Utils.hideSoftKeyboard(context as Activity)
			}
		})
	}

	fun configureMembers(members: List<Member>) {
		messageInputController.members = members
	}

	fun configureCommands(commands: List<Command>) {
		messageInputController.channelCommands = commands
	}

	private fun onSendMessage() {
		when (isEdit) {
			true -> viewModel.editMessage(editMessage)
			false -> messageInputController.onSendMessageClick(messageText)
		}.also { handleSentMessage() }
	}

	internal fun sendTextMessage(message: String) {
		messageSendHandler.sendMessage(message)
	}

	internal fun sendAttachments(message: String, attachmentFiles: List<File>) {
		messageSendHandler.sendMessageWithAttachments(message, attachmentFiles)
	}

	private fun handleSentMessage() {
		typeListeners.forEach(TypeListener::onStopTyping)
		initSendMessage()
		if (isEdit) clearFocus()
	}

	private fun initSendMessage() {
		messageInputController.initSendMessage()
		viewModel.setEditMessage(null)
		binding.etMessage.setText("")
		binding.sendButton.isEnabled = true
	}

	protected val editMessage: Message?
		protected get() {
			val message = viewModel.getEditMessage().value
			message !!.text = messageText !!
			return message
		}

	private fun sendGiphyFromKeyboard(inputContentInfo: InputContentInfoCompat,
	                                  flags: Int, opts: Bundle): Boolean {
		if (BuildCompat.isAtLeastQ()
				&& flags and InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION != 0) {
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
		messageInputController.setSelectedAttachments(mutableListOf(AttachmentMetaData(attachment)))
		binding.etMessage.setText("")
		onSendMessage()
		return true
	}

	protected val isEdit: Boolean
		protected get() = viewModel.isEditing

	protected fun editMessage(message: Message?) {
		if (message == null) return

		// Set Text to Inputbox
		messageText = message.text
		binding.etMessage.requestFocus()
		val attachments = LlcMigrationUtils.getMetaAttachments(message)
		if (! attachments.isEmpty()) binding.ivOpenAttach.visibility = View.GONE
		// Set Attachments to Inputbox
		if (attachments.isEmpty()
				|| attachments[0].type == ModelType.attach_giphy || attachments[0].type == ModelType.attach_unknown) return

		//for (Attachment attachment : attachments)
		//attachment.setUploaded(true);
		val attachment = attachments[0]
		if (attachment.type == ModelType.attach_file) {
			val fileType = attachment.mimeType
			if (fileType == ModelType.attach_mime_mov || fileType == ModelType.attach_mime_mp4) {
				messageInputController.onClickOpenSelectView(attachments, true)
			} else {
				messageInputController.onClickOpenSelectView(attachments, false)
			}
		} else {
			messageInputController.onClickOpenSelectView(attachments, true)
		}
	}

	fun captureMedia(requestCode: Int, resultCode: Int, data: Intent?) {
		if (requestCode == Constant.CAPTURE_IMAGE_REQUEST_CODE
				&& resultCode == Activity.RESULT_OK) {
			val imageFile = CaptureController.getCaptureFile(true)
			val vieoFile = CaptureController.getCaptureFile(false)
			if (imageFile == null && vieoFile == null) {
				Utils.showMessage(context, context.getString(R.string.stream_take_photo_failed))
				return
			}
			if (imageFile != null && imageFile.length() > 0) {
				messageInputController.progressCapturedMedia(imageFile, true)
				updateGallery(imageFile)
			} else if (vieoFile != null && vieoFile.length() > 0) {
				messageInputController.progressCapturedMedia(vieoFile, false)
				updateGallery(vieoFile)
			} else Utils.showMessage(context, context.getString(R.string.stream_take_photo_failed))
		}
	}

	private fun updateGallery(outputFile: File) {
		val scanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
		val contentUri = Uri.fromFile(outputFile)
		scanIntent.data = contentUri
		context.sendBroadcast(scanIntent)
	}

	internal fun showSuggestedMentions(users: List<User>) {
		mentionsAdapter.submitList(users)
	}

	internal fun showSuggestedCommand(commands: List<Command>) {
		commandsAdapter.submitList(commands)
	}

	interface TypeListener {
		fun onKeystroke()
		fun onStopTyping()
	}

	interface PermissionRequestListener {
		fun openPermissionRequest()
	}

	interface MessageSendHandler {
		fun sendMessage(messageText: String)
		fun sendMessageWithAttachments(message: String, attachmentsFiles: List<File>)
	}

	init {
		applyStyle()
		binding.rvSuggestions.adapter = MergeAdapter(commandsAdapter, mentionsAdapter)
	}
}