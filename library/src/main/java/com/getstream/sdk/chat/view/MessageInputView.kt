package com.getstream.sdk.chat.view

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
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
import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.Chat.Companion.getInstance
import com.getstream.sdk.chat.R
import com.getstream.sdk.chat.databinding.StreamViewMessageInputBinding
import com.getstream.sdk.chat.enums.InputType
import com.getstream.sdk.chat.enums.MessageInputType
import com.getstream.sdk.chat.interfaces.MessageSendListener
import com.getstream.sdk.chat.model.AttachmentMetaData
import com.getstream.sdk.chat.model.ModelType
import com.getstream.sdk.chat.navigation.destinations.CameraDestination
import com.getstream.sdk.chat.utils.CaptureController
import com.getstream.sdk.chat.utils.Constant
import com.getstream.sdk.chat.utils.GridSpacingItemDecoration
import com.getstream.sdk.chat.utils.LlcMigrationUtils
import com.getstream.sdk.chat.utils.MessageInputController
import com.getstream.sdk.chat.utils.PermissionChecker
import com.getstream.sdk.chat.utils.StringUtility
import com.getstream.sdk.chat.utils.TextViewUtils
import com.getstream.sdk.chat.utils.Utils
import com.getstream.sdk.chat.viewmodel.MessageInputViewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import java.io.File
import java.util.Date
import java.util.UUID

class MessageInputView(context: Context, attrs: AttributeSet?) : RelativeLayout(context, attrs) {
	/**
	 * Tag for logging purposes
	 */
	val TAG = MessageInputView::class.java.simpleName
	private val binding: StreamViewMessageInputBinding

	/**
	 * Styling class for the MessageInput
	 */
	private var style: MessageInputStyle? = null

	/**
	 * Fired when a message is sent
	 */
	private var messageSendListener: MessageSendListener? = null

	/**
	 * Permission Request listener
	 */
	private var permissionRequestListener: PermissionRequestListener? = null

	/**
	 * The viewModel for handling typing etc.
	 */
	private lateinit var viewModel: MessageInputViewModel
	private var messageInputController: MessageInputController? = null

	// endregion
	// region init
	private fun initBinding(context: Context): StreamViewMessageInputBinding {
		val inflater = LayoutInflater.from(context)
		return StreamViewMessageInputBinding.inflate(inflater, this, true)
	}

	private fun parseAttr(context: Context, attrs: AttributeSet?) {
		style = MessageInputStyle(context, attrs)
	}

	fun setViewModel(viewModel: MessageInputViewModel, lifecycleOwner: LifecycleOwner?) {
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
		binding.ivOpenAttach.visibility = if (style !!.isShowAttachmentButton) View.VISIBLE else View.GONE
		binding.ivOpenAttach.setImageDrawable(style !!.getAttachmentButtonIcon(false))
		binding.ivOpenAttach.layoutParams.width = style !!.attachmentButtonWidth
		binding.ivOpenAttach.layoutParams.height = style !!.attachmentButtonHeight
		// Send Button
		binding.ivSend.setImageDrawable(style !!.getInputButtonIcon(false))
		binding.ivSend.layoutParams.width = style !!.inputButtonWidth
		binding.ivSend.layoutParams.height = style !!.inputButtonHeight
		// Input Background
		binding.llComposer.background = style !!.inputBackground
		// Input Text
		style !!.inputText.apply(binding.etMessage)
		style !!.inputBackgroundText.apply(binding.tvTitle)
		style !!.inputBackgroundText.apply(binding.tvCommand)
		style !!.inputBackgroundText.apply(binding.tvUploadPhotoVideo)
		style !!.inputBackgroundText.apply(binding.tvUploadFile)
		style !!.inputBackgroundText.apply(binding.tvUploadCamera)
	}

	private fun configOnClickListener() {
		binding.ivSend.setOnClickListener { view: View? -> onSendMessage() }
		binding.ivOpenAttach.setOnClickListener { view: View? ->
			binding.isAttachFile = true
			messageInputController !!.onClickOpenBackGroundView(MessageInputType.ADD_FILE)
			if (! PermissionChecker.isGrantedCameraPermissions(context)
					&& permissionRequestListener != null && ! style !!.passedPermissionCheck()) permissionRequestListener !!.openPermissionRequest()
		}
	}

	private fun configInputEditText() {
		binding.etMessage.onFocusChangeListener = OnFocusChangeListener { view: View?, hasFocus: Boolean ->
			viewModel !!.setInputType(if (hasFocus) InputType.SELECT else InputType.DEFAULT)
			if (hasFocus) {
				Utils.showSoftKeyboard(context as Activity)
			} else Utils.hideSoftKeyboard(context as Activity)
		}
		TextViewUtils.afterTextChanged(binding.etMessage) { editable: Editable -> keyStroke(editable) }
		binding.etMessage.setCallback { inputContentInfo: InputContentInfoCompat, flags: Int, opts: Bundle -> sendGiphyFromKeyboard(inputContentInfo, flags, opts) }
	}

	private fun keyStroke(editable: Editable) {
		if (editable.toString().length > 0) viewModel !!.keystroke()
		val messageText = messageText !!
		// detect commands
		messageInputController !!.checkCommand(messageText)
		val s_ = messageText.replace("\\s+".toRegex(), "")
		if (TextUtils.isEmpty(s_)) binding.activeMessageSend = false else binding.activeMessageSend = messageText.length != 0
		configSendButtonEnableState()
	}

	private fun configMessageInputBackground(lifecycleOwner: LifecycleOwner?) {
		viewModel !!.getInputType().observe(lifecycleOwner !!, Observer { inputType: InputType? ->
			when (inputType) {
				InputType.DEFAULT -> {
					binding.llComposer.background = style !!.inputBackground
					binding.ivOpenAttach.setImageDrawable(style !!.getAttachmentButtonIcon(false))
					binding.ivSend.setImageDrawable(style !!.getInputButtonIcon(viewModel !!.isEditing))
				}
				InputType.SELECT -> {
					binding.llComposer.background = style !!.inputSelectedBackground
					binding.ivOpenAttach.setImageDrawable(style !!.getAttachmentButtonIcon(true))
					binding.ivSend.setImageDrawable(style !!.getInputButtonIcon(false))
				}
				InputType.EDIT -> {
					binding.llComposer.background = style !!.inputEditBackground
					binding.ivOpenAttach.setImageDrawable(style !!.getAttachmentButtonIcon(true))
					binding.ivSend.setImageDrawable(style !!.getInputButtonIcon(true))
					messageInputController !!.onClickOpenBackGroundView(MessageInputType.EDIT_MESSAGE)
				}
			}
		})
	}

	private fun configSendButtonEnableState() {
		val attachments = messageInputController !!.getSelectedAttachments()
		val hasAttachment = attachments != null && ! attachments.isEmpty()
		val notEmptyMessage = ! StringUtility.isEmptyTextMessage(messageText) || ! messageInputController !!.isUploadingFile && hasAttachment
		binding.activeMessageSend = notEmptyMessage
	}

	private fun configAttachmentUI() {
		// TODO: make the attachment UI into it's own view and allow you to change it.
		messageInputController = MessageInputController(context, binding, viewModel !!, style !!, label@ object : AttachmentListener {
			override fun onAddAttachment(attachment: AttachmentMetaData?) {
				if (binding.ivSend.isEnabled) return@label
				for (attachment_ in messageInputController !!.getSelectedAttachments()) if (! attachment_.isUploaded) return@label
				onSendMessage()
			}
		})
		binding.rvMedia.layoutManager = GridLayoutManager(context, 4, RecyclerView.VERTICAL, false)
		binding.rvMedia.hasFixedSize()
		binding.rvComposer.layoutManager = GridLayoutManager(context, 1, RecyclerView.HORIZONTAL, false)
		val spanCount = 4 // 4 columns
		val spacing = 2 // 1 px
		val includeEdge = false
		binding.rvMedia.addItemDecoration(GridSpacingItemDecoration(spanCount, spacing, includeEdge))
		binding.btnClose.setOnClickListener { v: View? ->
			messageInputController !!.onClickCloseBackGroundView()
			Utils.hideSoftKeyboard(context as Activity)
			if (viewModel !!.isEditing) {
				initSendMessage()
				clearFocus()
			}
		}
		binding.llMedia.setOnClickListener { v: View? -> messageInputController !!.onClickOpenSelectView(viewModel.channel, null, true) }
		binding.llCamera.setOnClickListener { v: View? ->
			if (! PermissionChecker.isGrantedCameraPermissions(context)) {
				PermissionChecker.showPermissionSettingDialog(context, context.getString(R.string.stream_camera_permission_message))
				return@setOnClickListener
			}
			Utils.setButtonDelayEnable(v)
			messageInputController !!.onClickCloseBackGroundView()
			val builder = VmPolicy.Builder()
			StrictMode.setVmPolicy(builder.build())
			Utils.hideSoftKeyboard(context as Activity)
			getInstance().navigator.navigate(CameraDestination(context as Activity))
		}
		binding.llFile.setOnClickListener { v: View? -> messageInputController !!.onClickOpenSelectView(viewModel.channel, null, false) }
	}

	private fun onBackPressed() {
		isFocusableInTouchMode = true
		requestFocus()
		setOnKeyListener { v: View?, keyCode: Int, event: KeyEvent ->
			if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
				if (viewModel !!.isThread) {
					viewModel !!.resetThread()
					initSendMessage()
					return@setOnKeyListener true
				}
				if (viewModel !!.isEditing) {
					messageInputController !!.onClickCloseBackGroundView()
					initSendMessage()
					return@setOnKeyListener true
				}
				if (! TextUtils.isEmpty(messageText)) {
					initSendMessage()
					return@setOnKeyListener true
				}
				if (binding.clTitle.visibility == View.VISIBLE) {
					messageInputController !!.onClickCloseBackGroundView()
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

	var messageText: String?
		get() = binding.etMessage.text.toString()
		set(text) {
			if (TextUtils.isEmpty(text)) return
			binding.etMessage.requestFocus()
			binding.etMessage.setText(text)
			binding.etMessage.setSelection(binding.etMessage.text.length)
		}

	// endregion
	// region observe
	private fun observeUIs(lifecycleOwner: LifecycleOwner?) {
		configMessageInputBackground(lifecycleOwner)
		viewModel !!.getEditMessage().observe(lifecycleOwner !!, Observer { message: Message? -> editMessage(message) })
		viewModel !!.getMessageListScrollUp().observe(lifecycleOwner, Observer { messageListScrollup: Boolean -> if (messageListScrollup) Utils.hideSoftKeyboard(context as Activity) })
		viewModel !!.getActiveThread().observe(lifecycleOwner, Observer { threadParentMessage: Message? ->
			if (threadParentMessage == null) {
				initSendMessage()
				Utils.hideSoftKeyboard(context as Activity)
			}
		})
	}
	// endregion
	// region send message
	/**
	 * Prepare message takes the message input string and returns a message object
	 * You can overwrite this method in case you want to attach more custom properties to the message
	 */
	private fun onSendMessage(message: Message?) {
		binding.ivSend.isEnabled = false
		if (isEdit) viewModel !!.editMessage(message) else viewModel !!.sendMessage(message !!)
		handleSentMessage()
	}

	private val newMessage: Message
		private get() {
			val message = Message()
			message.text = messageText !!
			return message
		}

	protected fun onSendMessage() {
		// TODO: fix the horrible naming on the send message flows
		val message = if (isEdit) editMessage else newMessage
		onSendMessage(message)
		handleSentMessage()
		if (isEdit) Utils.hideSoftKeyboard(context as Activity)
	}

	private fun handleSentMessage() {
		initSendMessage()
		if (isEdit) clearFocus()
	}

	private fun initSendMessage() {
		messageInputController !!.initSendMessage()
		viewModel !!.setEditMessage(null)
		binding.etMessage.setText("")
		binding.ivSend.isEnabled = true
	}

	/**
	 * Prepare message takes the message input string and returns a message object
	 * You can overwrite this method in case you want to attach more custom properties to the message
	 */
	protected fun prepareNewMessage(message: Message): Message {
		// Check file uploading
		if (messageInputController !!.isUploadingFile) {
			// message.user = ChatDomain.instance().getCurrentUser();
			val clientSideID = generateMessageID()
			message.id = clientSideID
			message.createdAt = Date()
			//TODO: llc check sync
			//message.setSyncStatus(Sync.LOCAL_UPDATE_PENDING);
		} else {
			message.attachments.addAll(LlcMigrationUtils.getAttachments(messageInputController !!.getSelectedAttachments()))
		}
		return message
	}

	protected fun prepareEditMessage(message: Message): Message {
		message.text = messageText !!
		val newAttachments = messageInputController !!.getSelectedAttachments()
		message.attachments.addAll(LlcMigrationUtils.getAttachments(newAttachments))
		return message
	}

	protected val editMessage: Message?
		protected get() {
			val message = viewModel !!.getEditMessage().value
			message !!.text = messageText !!
			return message
		}

	// endregion
	// region send giphy from keyboard
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
		messageInputController !!.setSelectedAttachments(mutableListOf(AttachmentMetaData(attachment)))
		binding.etMessage.setText("")
		onSendMessage()
		return true
	}

	// endregion
	protected val isEdit: Boolean
		protected get() = viewModel !!.isEditing

	// region edit message
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
				messageInputController !!.onClickOpenSelectView(viewModel.channel, attachments, true)
			} else {
				messageInputController !!.onClickOpenSelectView(viewModel.channel, attachments, false)
			}
		} else {
			messageInputController !!.onClickOpenSelectView(viewModel.channel, attachments, true)
		}
	}

	// endregion
	// region permission check
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
				messageInputController !!.progressCapturedMedia(viewModel.channel, imageFile, true)
				updateGallery(imageFile)
			} else if (vieoFile != null && vieoFile.length() > 0) {
				messageInputController !!.progressCapturedMedia(viewModel.channel, vieoFile, false)
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

	/*Used for handling requestPermissionsResult*/
	fun permissionResult(requestCode: Int,
	                     permissions: Array<String>,
	                     grantResults: IntArray) {
		if (requestCode == Constant.PERMISSIONS_REQUEST) {
			var storageGranted = true
			var cameraGranted = true
			var permission: String
			var grantResult: Int
			for (i in permissions.indices) {
				permission = permissions[i]
				grantResult = grantResults[i]
				if (permission == Manifest.permission.CAMERA) {
					cameraGranted = grantResult == PackageManager.PERMISSION_GRANTED
				} else if (grantResult != PackageManager.PERMISSION_GRANTED) {
					storageGranted = false
				}
			}
			if (storageGranted && cameraGranted) {
				messageInputController !!.onClickOpenBackGroundView(MessageInputType.ADD_FILE)
				style !!.setCheckPermissions(true)
			} else {
				val message: String
				message = if (! storageGranted && ! cameraGranted) {
					context.getString(R.string.stream_both_permissions_message)
				} else if (! cameraGranted) {
					style !!.setCheckPermissions(true)
					context.getString(R.string.stream_camera_permission_message)
				} else {
					style !!.setCheckPermissions(true)
					context.getString(R.string.stream_storage_permission_message)
				}
				PermissionChecker.showPermissionSettingDialog(context, message)
			}
			messageInputController !!.configPermissions()
		}
	}

	// endregion
	// region listeners
	protected fun setMessageSendListener(manager: MessageSendListener?) {
		messageSendListener = manager
	}

	fun setPermissionRequestListener(l: PermissionRequestListener?) {
		permissionRequestListener = l
	}

	private fun generateMessageID(): String {
		val currentUser = ChatClient.instance().getCurrentUser()
		val id = currentUser !!.id
		return id + "-" + UUID.randomUUID().toString()
	}

	/**
	 * This interface is called when you add an attachment
	 */
	interface AttachmentListener {
		fun onAddAttachment(attachment: AttachmentMetaData?)
	}

	/**
	 * Interface for Permission request
	 */
	interface PermissionRequestListener {
		fun openPermissionRequest()
	} // endregion

	// region constructor
	init {
		parseAttr(context, attrs)
		binding = initBinding(context)
		applyStyle()
	}
}