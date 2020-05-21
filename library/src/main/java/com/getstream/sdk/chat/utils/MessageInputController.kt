package com.getstream.sdk.chat.utils

import android.app.Activity
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Environment
import android.text.TextUtils
import android.view.View
import android.widget.AdapterView
import com.getstream.sdk.chat.R
import com.getstream.sdk.chat.adapter.AttachmentListAdapter
import com.getstream.sdk.chat.adapter.CommandMentionListItemAdapter
import com.getstream.sdk.chat.adapter.MediaAttachmentAdapter
import com.getstream.sdk.chat.adapter.MediaAttachmentSelectedAdapter
import com.getstream.sdk.chat.databinding.StreamViewMessageInputBinding
import com.getstream.sdk.chat.enums.InputType
import com.getstream.sdk.chat.enums.MessageInputType
import com.getstream.sdk.chat.model.AttachmentMetaData
import com.getstream.sdk.chat.model.ModelType
import com.getstream.sdk.chat.view.MessageInputStyle
import com.getstream.sdk.chat.view.MessageInputView.AttachmentListener
import com.getstream.sdk.chat.viewmodel.MessageInputViewModel
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.logger.ChatLogger.Companion.get
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Command
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.ProgressCallback
import java.io.File
import java.util.ArrayList

class MessageInputController(private val context: Context,
                             private val binding: StreamViewMessageInputBinding,
                             private val viewModel: MessageInputViewModel,
                             style: MessageInputStyle,
                             attachmentListener: AttachmentListener?) {
	private val logger = get(MessageInputController::class.java.simpleName)
	private val channel: Channel = viewModel.channel
	private val style: MessageInputStyle = style
	private var mediaAttachmentAdapter: MediaAttachmentAdapter? = null
	private var selectedMediaAttachmentAdapter: MediaAttachmentSelectedAdapter? = null
	private var fileAttachmentAdapter: AttachmentListAdapter? = null
	private var selectedFileAttachmentAdapter: AttachmentListAdapter? = null
	private var commandMentionListItemAdapter: CommandMentionListItemAdapter<MessageInputStyle>? = null
	private var commands: MutableList<Any>? = null
	private var messageInputType: MessageInputType? = null
	private var selectedAttachments: MutableList<AttachmentMetaData> = ArrayList()
	private val attachmentListener: AttachmentListener? = attachmentListener
	private var attachmentData: List<AttachmentMetaData>? = null
	private val uploadManager: UploadManager
	fun getSelectedAttachments(): List<AttachmentMetaData> {
		return selectedAttachments
	}

	val isUploadingFile: Boolean
		get() = uploadManager.isUploadingFile

	fun setSelectedAttachments(selectedAttachments: MutableList<AttachmentMetaData>) {
		this.selectedAttachments = selectedAttachments
	}

	fun onClickOpenBackGroundView(type: MessageInputType) {
		binding.root.setBackgroundResource(R.drawable.stream_round_thread_toolbar)
		binding.clTitle.visibility = View.VISIBLE
		binding.btnClose.visibility = View.VISIBLE
		binding.clAddFile.visibility = View.GONE
		binding.clCommand.visibility = View.GONE
		binding.clSelectPhoto.visibility = View.GONE
		when (type) {
			MessageInputType.EDIT_MESSAGE -> {
			}
			MessageInputType.ADD_FILE -> {
				if (selectedAttachments != null && ! selectedAttachments !!.isEmpty()) return
				binding.clAddFile.visibility = View.VISIBLE
			}
			MessageInputType.UPLOAD_MEDIA, MessageInputType.UPLOAD_FILE -> {
				binding.clSelectPhoto.visibility = View.VISIBLE
				configAttachmentButtonVisible(false)
			}
			MessageInputType.COMMAND, MessageInputType.MENTION -> {
				binding.btnClose.visibility = View.GONE
				binding.clCommand.visibility = View.VISIBLE
			}
		}
		binding.tvTitle.text = type.getLabel(context)
		messageInputType = type
		configPermissions()
	}

	fun configPermissions() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
			binding.ivMediaPermission.visibility = View.GONE
			binding.ivCameraPermission.visibility = View.GONE
			binding.ivFilePermission.visibility = View.GONE
			return
		}
		if (PermissionChecker.isGrantedCameraPermissions(context)) {
			binding.ivMediaPermission.visibility = View.GONE
			binding.ivCameraPermission.visibility = View.GONE
			binding.ivFilePermission.visibility = View.GONE
		} else if (PermissionChecker.isGrantedStoragePermissions(context)) {
			binding.ivMediaPermission.visibility = View.GONE
			binding.ivCameraPermission.visibility = View.VISIBLE
			binding.ivFilePermission.visibility = View.GONE
		} else {
			binding.ivMediaPermission.visibility = View.VISIBLE
			binding.ivCameraPermission.visibility = View.VISIBLE
			binding.ivFilePermission.visibility = View.VISIBLE
		}
	}

	fun onClickCloseBackGroundView() {
		binding.clTitle.visibility = View.GONE
		binding.clAddFile.visibility = View.GONE
		binding.clSelectPhoto.visibility = View.GONE
		binding.clCommand.visibility = View.GONE
		binding.root.setBackgroundResource(0)
		messageInputType = null
		commandMentionListItemAdapter = null
		configAttachmentButtonVisible(true)
	}

	// endregion
	// region Upload Attachment File
	private fun configSelectAttachView(isMedia: Boolean) {
		binding.isAttachFile = ! isMedia
		getAttachmentsFromLocal(isMedia)
		(context as Activity).runOnUiThread {
			if (selectedAttachments !!.isEmpty()) {
				setAttachmentAdapters(isMedia)
				if (attachmentData !!.isEmpty()) {
					Utils.showMessage(context, context.getResources().getString(R.string.stream_no_media_error))
					onClickCloseBackGroundView()
				}
				binding.progressBarFileLoader.visibility = View.GONE
			} else {
				showHideComposerAttachmentGalleryView(true, isMedia)
				setSelectedAttachmentAdapter(false, isMedia)
			}
		}
	}

	private fun getAttachmentsFromLocal(isMedia: Boolean) {
		if (isMedia) {
			attachmentData = Utils.getMediaAttachments(context)
			return
		}
		Utils.attachments = ArrayList()
		attachmentData = Utils.getFileAttachments(Environment.getExternalStorageDirectory())
	}

	private fun setAttachmentAdapters(isMedia: Boolean) {
		if (isMedia) {
			mediaAttachmentAdapter = MediaAttachmentAdapter(context, attachmentData, MediaAttachmentAdapter.OnItemClickListener { position: Int -> uploadOrCancelAttachment(attachmentData !![position], isMedia) }
			)
			binding.rvMedia.adapter = mediaAttachmentAdapter
		} else {
			fileAttachmentAdapter = AttachmentListAdapter(context, attachmentData, true, true)
			binding.lvFile.adapter = fileAttachmentAdapter
			binding.lvFile.onItemClickListener = AdapterView.OnItemClickListener { parent: AdapterView<*>?, view: View?, position: Int, id: Long -> uploadOrCancelAttachment(attachmentData !![position], isMedia) }
		}
	}

	private fun setSelectedAttachmentAdapter(fromGallery: Boolean, isMedia: Boolean) {
		if (isMedia) {
			selectedMediaAttachmentAdapter = MediaAttachmentSelectedAdapter(context, selectedAttachments, MediaAttachmentSelectedAdapter.OnAttachmentCancelListener { attachment: AttachmentMetaData -> cancelAttachment(attachment, fromGallery, isMedia) })
			binding.rvComposer.adapter = selectedMediaAttachmentAdapter
		} else {
			selectedFileAttachmentAdapter = AttachmentListAdapter(context, selectedAttachments, true, false, AttachmentListAdapter.OnAttachmentCancelListener { attachment: AttachmentMetaData -> cancelAttachment(attachment, fromGallery, isMedia) })
			binding.lvComposer.adapter = selectedFileAttachmentAdapter
		}
	}

	private fun uploadOrCancelAttachment(attachment: AttachmentMetaData,
	                                     isMedia: Boolean) {
		if (! attachment.isSelected) {
			uploadAttachment(attachment, true, isMedia)
		} else {
			cancelAttachment(attachment, true, isMedia)
		}
	}

	private fun isOverMaxUploadFileSize(file: File): Boolean {
		if (file.length() > Constant.MAX_UPLOAD_FILE_SIZE) {
			Utils.showMessage(context, R.string.stream_large_size_file_error)
			return true
		}
		return false
	}

	private fun uploadAttachment(attachment: AttachmentMetaData, fromGallery: Boolean, isMedia: Boolean) {
		val file = File(attachment.file.path)
		if (isOverMaxUploadFileSize(file)) return
		attachment.isSelected = true
		selectedAttachments !!.add(attachment)
		if (attachment.isUploaded) uploadedFileProgress(attachment) else uploadFile(attachment, fromGallery, isMedia)
		showHideComposerAttachmentGalleryView(true, isMedia)
		if (fromGallery) totalAttachmentAdapterChanged(attachment, isMedia)
		selectedAttachmentAdapterChanged(attachment, fromGallery, isMedia)
		configSendButtonEnableState()
	}

	private fun uploadFile(attachment: AttachmentMetaData, fromGallery: Boolean, isMedia: Boolean) {
		uploadManager.uploadFile(attachment, object : ProgressCallback {
			override fun onSuccess(s: String) {
				selectedAttachmentAdapterChanged(attachment, fromGallery, isMedia)
				uploadedFileProgress(attachment)
			}

			override fun onError(chatError: ChatError) {
				logger.logE(chatError)
				Utils.showMessage(context, R.string.stream_attachment_uploading_error)
				cancelAttachment(attachment, fromGallery, isMedia)
			}

			override fun onProgress(l: Long) {
				if (! attachment.isSelected) return
				selectedAttachmentAdapterChanged(attachment, fromGallery, isMedia)
				configSendButtonEnableState()
			}
		})
	}

	private fun uploadedFileProgress(attachment: AttachmentMetaData) {
		attachmentListener?.onAddAttachment(attachment)
		configSendButtonEnableState()
	}

	private fun cancelAttachment(attachment: AttachmentMetaData, fromGallery: Boolean, isMedia: Boolean) {
		attachment.isSelected = false
		selectedAttachments !!.remove(attachment)
		uploadManager.removeFromQueue(attachment)
		if (fromGallery) totalAttachmentAdapterChanged(null, isMedia)
		selectedAttachmentAdapterChanged(null, fromGallery, isMedia)
		configSendButtonEnableState()
		if (selectedAttachments !!.isEmpty() && messageInputType == MessageInputType.EDIT_MESSAGE) configAttachmentButtonVisible(true)
	}

	private fun configAttachmentButtonVisible(visible: Boolean) {
		if (! style.isShowAttachmentButton) return
		binding.ivOpenAttach.visibility = if (visible) View.VISIBLE else View.GONE
	}

	private fun showHideComposerAttachmentGalleryView(show: Boolean, isMedia: Boolean) {
		if (isMedia) binding.rvComposer.visibility = if (show) View.VISIBLE else View.GONE else binding.lvComposer.visibility = if (show) View.VISIBLE else View.GONE
	}

	fun onClickOpenSelectView(editAttachments: MutableList<AttachmentMetaData>?, isMedia: Boolean) {
		if (! PermissionChecker.isGrantedStoragePermissions(context)) {
			PermissionChecker.showPermissionSettingDialog(context, context.getString(R.string.stream_storage_permission_message))
			return
		}
		initAdapter()
		if (editAttachments != null && ! editAttachments.isEmpty()) setSelectedAttachments(editAttachments)
		AsyncTask.execute { configSelectAttachView(isMedia) }
		if (selectedAttachments !!.isEmpty()) {
			binding.progressBarFileLoader.visibility = View.VISIBLE
			onClickOpenBackGroundView(if (isMedia) MessageInputType.UPLOAD_MEDIA else MessageInputType.UPLOAD_FILE)
		}
	}

	private fun totalAttachmentAdapterChanged(attachment: AttachmentMetaData?, isMedia: Boolean) {
		if (isMedia) {
			if (attachment == null) {
				mediaAttachmentAdapter !!.notifyDataSetChanged()
				return
			}
			val index = attachmentData !!.indexOf(attachment)
			if (index != - 1) mediaAttachmentAdapter !!.notifyItemChanged(index)
		} else fileAttachmentAdapter !!.notifyDataSetChanged()
	}

	private fun selectedAttachmentAdapterChanged(attachment: AttachmentMetaData?,
	                                             fromGallery: Boolean,
	                                             isMedia: Boolean) {
		if (isMedia) {
			if (selectedMediaAttachmentAdapter == null) setSelectedAttachmentAdapter(fromGallery, isMedia)
			if (attachment == null) {
				selectedMediaAttachmentAdapter !!.notifyDataSetChanged()
				return
			}
			val index = selectedAttachments !!.indexOf(attachment)
			if (index != - 1) selectedMediaAttachmentAdapter !!.notifyItemChanged(index)
		} else {
			if (selectedFileAttachmentAdapter == null) setSelectedAttachmentAdapter(fromGallery, isMedia)
			selectedFileAttachmentAdapter !!.notifyDataSetChanged()
		}
	}

	private fun configSendButtonEnableState() {
		if (! StringUtility.isEmptyTextMessage(binding.etMessage.text.toString())) {
			binding.activeMessageSend = true
		} else {
			if (uploadManager.isUploadingFile || selectedAttachments !!.isEmpty()) {
				viewModel.setInputType(InputType.DEFAULT)
				binding.activeMessageSend = false
			} else {
				binding.activeMessageSend = true
			}
		}
	}

	fun initSendMessage() {
		binding.etMessage.setText("")
		initAdapter()
		onClickCloseBackGroundView()
	}

	private fun initAdapter() {
		selectedAttachments !!.clear()
		uploadManager.resetQueue()
		binding.lvComposer.removeAllViewsInLayout()
		binding.rvComposer.removeAllViewsInLayout()
		binding.lvComposer.visibility = View.GONE
		binding.rvComposer.visibility = View.GONE
		mediaAttachmentAdapter = null
		selectedMediaAttachmentAdapter = null
		fileAttachmentAdapter = null
		selectedFileAttachmentAdapter = null
	}

	// endregion
	// region Camera
	fun progressCapturedMedia(file: File?, isImage: Boolean) {
		val attachment = AttachmentMetaData(file)
		attachment.file = file
		if (isImage) {
			attachment.type = ModelType.attach_image
		} else {
			val retriever = MediaMetadataRetriever()
			retriever.setDataSource(context, Uri.fromFile(file))
			val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
			val videolengh = time.toLong()
			attachment.videoLength = (videolengh / 1000).toInt()
			Utils.configFileAttachment(attachment, file, ModelType.attach_file, ModelType.attach_mime_mp4)
			retriever.release()
		}
		uploadAttachment(attachment, false, true)
	}

	// endregion
	// region Cammand
	private fun openCommandView() {
		onClickOpenBackGroundView(MessageInputType.COMMAND)
	}

	private fun closeCommandView() {
		if (isCommandOrMention) onClickCloseBackGroundView()
		commands = null
	}

	private val isCommandOrMention: Boolean
		private get() = messageInputType != null && (messageInputType == MessageInputType.COMMAND
				|| messageInputType == MessageInputType.MENTION)

	fun checkCommand(text: String) {
		if (TextUtils.isEmpty(text)
				|| ! text.startsWith("/") && ! text.contains("@")) {
			closeCommandView()
		} else if (text.length == 1) {
			onClickCommandViewOpen(text.startsWith("/"))
		} else if (text.endsWith("@")) {
			onClickCommandViewOpen(false)
		} else {
			setCommandsMentionUsers(text)
			if (! commands !!.isEmpty() && binding.clCommand.visibility != View.VISIBLE) openCommandView()
			setCommandMentionListItemAdapter(text.startsWith("/"))
		}
		if (commands == null || commands !!.isEmpty()) closeCommandView()
	}

	private fun onClickCommandViewOpen(isCommand: Boolean) {
		if (isCommand) {
			setCommands("")
		} else {
			setMentionUsers("")
		}
		val title = binding.tvTitle.context.resources.getString(if (isCommand) R.string.stream_input_type_command else R.string.stream_input_type_auto_mention)
		binding.tvTitle.text = title
		binding.tvCommand.text = ""
		setCommandMentionListItemAdapter(isCommand)
		openCommandView()
		binding.lvCommand.onItemClickListener = AdapterView.OnItemClickListener { adapterView: AdapterView<*>?, view: View?, position: Int, l: Long ->
			if (isCommand) binding.etMessage.setText("/" + (commands !![position] as Command).name + " ") else {
				val messageStr = binding.etMessage.text.toString()
				val userName = (commands !![position] as User).getExtraValue("name", "")
				val converted = StringUtility.convertMentionedText(messageStr, userName)
				binding.etMessage.setText(converted)
			}
			binding.etMessage.setSelection(binding.etMessage.text.length)
			closeCommandView()
		}
	}

	private fun setCommandMentionListItemAdapter(isCommand: Boolean) {
		if (commandMentionListItemAdapter == null) {
			commandMentionListItemAdapter = CommandMentionListItemAdapter<MessageInputStyle>(context, commands, style, isCommand)
			binding.lvCommand.adapter = commandMentionListItemAdapter
		} else {
			commandMentionListItemAdapter !!.setCommand(isCommand)
			commandMentionListItemAdapter !!.setCommands(commands)
			commandMentionListItemAdapter !!.notifyDataSetChanged()
		}
	}

	private fun setCommandsMentionUsers(string: String) {
		if (commands == null) commands = ArrayList()
		commands !!.clear()
		if (string.startsWith("/")) {
			val commands = channel.config.commands
			if (commands == null || commands.isEmpty()) return
			val commandStr = string.replace("/", "")
			setCommands(commandStr)
			binding.tvCommand.text = commandStr
		} else {
			val names = string.split("@").toTypedArray()
			if (names.size > 0) setMentionUsers(names[names.size - 1])
		}
	}

	private fun setCommands(string: String) {
		if (commands == null) commands = ArrayList()
		commands !!.clear()
		for (i in channel.config.commands.indices) {
			val command = channel.config.commands[i]
			if (command.name !!.contains(string)) commands !!.add(command)
		}
	}

	private fun setMentionUsers(string: String) {
		if (commands == null) commands = ArrayList()
		commands !!.clear()
		val members = channel.members
		for (i in members.indices) {
			val (user) = members[i]
			val name = user.getExtraValue("name", "")
			if (name.toLowerCase().contains(string.toLowerCase())) commands !!.add(user)
		}
	} // endregion

	// region Attachment
	init {
		uploadManager = UploadManager(channel)
	}
}