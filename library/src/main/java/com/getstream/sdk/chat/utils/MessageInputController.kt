package com.getstream.sdk.chat.utils

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.R
import com.getstream.sdk.chat.adapter.AttachmentListAdapter
import com.getstream.sdk.chat.adapter.FileAttachmentListAdapter
import com.getstream.sdk.chat.adapter.MediaAttachmentAdapter
import com.getstream.sdk.chat.adapter.MediaAttachmentSelectedAdapter
import com.getstream.sdk.chat.databinding.StreamViewMessageInputBinding
import com.getstream.sdk.chat.enums.MessageInputType
import com.getstream.sdk.chat.model.AttachmentMetaData
import com.getstream.sdk.chat.model.ModelType
import com.getstream.sdk.chat.view.MessageInputStyle
import com.getstream.sdk.chat.view.MessageInputView
import com.getstream.sdk.chat.view.MessageInputView.AttachmentListener
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.logger.ChatLogger.Companion.get
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Command
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.client.utils.ProgressCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.ArrayList
import java.util.regex.Pattern

private val COMMAND_PATTERN = Pattern.compile("^/[a-z]*$")
private val MENTION_PATTERN = Pattern.compile("^(.* )?@([a-zA-Z]+[0-9]*)*$")
class MessageInputController(private val context: Context,
                             private val binding: StreamViewMessageInputBinding,
                             private val view: MessageInputView,
                             private val style: MessageInputStyle,
                             private val attachmentListener: AttachmentListener?) {
	private val logger = get(MessageInputController::class.java.simpleName)
	private var mediaAttachmentAdapter: MediaAttachmentAdapter? = null
	private var selectedMediaAttachmentAdapter: MediaAttachmentSelectedAdapter? = null
	private var fileAttachmentAdapter: FileAttachmentListAdapter? = null
	private var selectedFileAttachmentAdapter: AttachmentListAdapter? = null
	private var messageInputType: MessageInputType? = null
	private var selectedAttachments: MutableList<AttachmentMetaData> = ArrayList()
	private var attachmentData: List<AttachmentMetaData> = emptyList()
	private val uploadManager: UploadManager = UploadManager()
	val gridLayoutManager = GridLayoutManager(context, 4, RecyclerView.VERTICAL, false)
	val gridSpacingItemDecoration = GridSpacingItemDecoration(4, 2, false)
	var members: List<Member> = listOf()
	var channelCommands: List<Command> = listOf()
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
		binding.clSelectPhoto.visibility = View.GONE
		when (type) {
			MessageInputType.EDIT_MESSAGE -> {
			}
			MessageInputType.ADD_FILE -> {
				if (selectedAttachments.isNotEmpty()) return
				binding.clAddFile.visibility = View.VISIBLE
			}
			MessageInputType.UPLOAD_MEDIA, MessageInputType.UPLOAD_FILE -> {
				binding.clSelectPhoto.visibility = View.VISIBLE
				configAttachmentButtonVisible(false)
			}
			MessageInputType.COMMAND, MessageInputType.MENTION -> {
				binding.btnClose.visibility = View.GONE
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
		binding.root.setBackgroundResource(0)
		messageInputType = null
		configAttachmentButtonVisible(true)
	}

	private fun configSelectAttachView(channel: Channel, isMedia: Boolean) {
		GlobalScope.launch(Dispatchers.Main) {
			attachmentData = getAttachmentsFromLocal(isMedia)
			if (selectedAttachments.isEmpty()) {
				setAttachmentAdapters(channel, isMedia)
				if (attachmentData.isEmpty()) {
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

	private suspend fun getAttachmentsFromLocal(isMedia: Boolean): List<AttachmentMetaData> =
			withContext(Dispatchers.IO) {
				when (isMedia) {
					true -> Utils.getMediaAttachments(context)
					false -> Utils.getFileAttachments(Environment.getExternalStorageDirectory())
				}
			}

	private fun setAttachmentAdapters(channel: Channel, isMedia: Boolean) {
		if (isMedia) {
			gridSpacingItemDecoration.setSpanCount(4)
			gridLayoutManager.spanCount = 4
			mediaAttachmentAdapter = MediaAttachmentAdapter(attachmentData) { uploadOrCancelAttachment(channel, it, isMedia) }
			binding.rvMedia.adapter = mediaAttachmentAdapter
		} else {
			gridSpacingItemDecoration.setSpanCount(1)
			gridLayoutManager.spanCount = 1
			fileAttachmentAdapter = FileAttachmentListAdapter(attachmentData) { uploadOrCancelAttachment(channel, it, isMedia) }
			binding.rvMedia.adapter = fileAttachmentAdapter
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

	private fun uploadOrCancelAttachment(channel: Channel,
	                                     attachment: AttachmentMetaData,
	                                     isMedia: Boolean) {
		if (! attachment.isSelected) {
			uploadAttachment(channel, attachment, true, isMedia)
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

	private fun uploadAttachment(channel: Channel, attachment: AttachmentMetaData, fromGallery: Boolean, isMedia: Boolean) {
		val file = File(attachment.file.path)
		if (isOverMaxUploadFileSize(file)) return
		attachment.isSelected = true
		selectedAttachments.add(attachment)
		if (attachment.isUploaded) uploadedFileProgress(attachment) else uploadFile(channel, attachment, fromGallery, isMedia)
		showHideComposerAttachmentGalleryView(true, isMedia)
		if (fromGallery) totalAttachmentAdapterChanged(attachment, isMedia)
		selectedAttachmentAdapterChanged(attachment, fromGallery, isMedia)
		configSendButtonEnableState()
	}

	private fun uploadFile(channel: Channel, attachment: AttachmentMetaData, fromGallery: Boolean, isMedia: Boolean) {
		uploadManager.uploadFile(channel.id, channel.type, attachment, object : ProgressCallback {
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
		selectedAttachments.remove(attachment)
		uploadManager.removeFromQueue(attachment)
		if (fromGallery) totalAttachmentAdapterChanged(null, isMedia)
		selectedAttachmentAdapterChanged(null, fromGallery, isMedia)
		configSendButtonEnableState()
		if (selectedAttachments.isEmpty() && messageInputType == MessageInputType.EDIT_MESSAGE) configAttachmentButtonVisible(true)
	}

	private fun configAttachmentButtonVisible(visible: Boolean) {
		if (! style.isShowAttachmentButton) return
		binding.ivOpenAttach.visibility = if (visible) View.VISIBLE else View.GONE
	}

	private fun showHideComposerAttachmentGalleryView(show: Boolean, isMedia: Boolean) {
		if (isMedia) binding.rvComposer.visibility = if (show) View.VISIBLE else View.GONE else binding.lvComposer.visibility = if (show) View.VISIBLE else View.GONE
	}

	fun onClickOpenSelectView(channel: Channel, editAttachments: MutableList<AttachmentMetaData>?, isMedia: Boolean) {
		if (! PermissionChecker.isGrantedStoragePermissions(context)) {
			PermissionChecker.checkStoragePermissions(view) { onClickOpenSelectView(channel, editAttachments, isMedia) }
			return
		}
		initAdapter()
		if (editAttachments != null && editAttachments.isNotEmpty()) setSelectedAttachments(editAttachments)
		configSelectAttachView(channel, isMedia)
		if (selectedAttachments.isEmpty()) {
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
			val index = selectedAttachments.indexOf(attachment)
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
			binding.activeMessageSend = !(uploadManager.isUploadingFile || selectedAttachments.isEmpty())
		}
	}

	fun initSendMessage() {
		binding.etMessage.setText("")
		initAdapter()
		onClickCloseBackGroundView()
	}

	private fun initAdapter() {
		selectedAttachments.clear()
		uploadManager.resetQueue()
		binding.lvComposer.removeAllViewsInLayout()
		binding.rvComposer.removeAllViewsInLayout()
		binding.lvComposer.visibility = View.GONE
		binding.rvComposer.visibility = View.GONE
		binding.rvMedia.layoutManager = gridLayoutManager
		binding.rvMedia.addItemDecoration(gridSpacingItemDecoration)
		mediaAttachmentAdapter?.clear()
		fileAttachmentAdapter?.clear()
		mediaAttachmentAdapter = null
		selectedMediaAttachmentAdapter = null
		fileAttachmentAdapter = null
		selectedFileAttachmentAdapter = null
	}

	fun progressCapturedMedia(channel: Channel, file: File?, isImage: Boolean) {
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
		uploadAttachment(channel, attachment, false, true)
	}

	fun checkCommandsOrMentions(inputMessage: String) {
		when {
			inputMessage.isCommandMessage() -> { view.showSuggestedCommand(channelCommands.matchName(inputMessage.removePrefix("/"))) }
			inputMessage.isMentionMessage() -> { view.showSuggestedMentions(members.matchUserName(inputMessage.substringAfterLast("@"))) }
			else -> { cleanSuggestion() }
		}
	}

	private fun cleanSuggestion() {
		view.showSuggestedMentions(listOf())
		view.showSuggestedCommand(listOf())
	}

	fun onCommandSelected(command: Command) {
		view.messageText = "/${command.name} "
	}

	fun onUserSelected(currentMessage: String, user: User) {
		view.messageText = "${currentMessage.substringBeforeLast("@")}@${user.name} "
	}
}

private fun String.isCommandMessage() = COMMAND_PATTERN.matcher(this).find()
private fun String.isMentionMessage() = MENTION_PATTERN.matcher(this).find()
private fun List<Command>.matchName(namePattern: String) = filter { it.name.startsWith(namePattern) }
private fun List<Member>.matchUserName(namePattern: String): List<User> = map { it.user }
				.filter{ it.name.contains(namePattern, true) }