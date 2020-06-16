package com.getstream.sdk.chat.utils

import android.media.MediaMetadataRetriever
import android.net.Uri
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
import io.getstream.chat.android.client.models.Command
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.name
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import whenTrue
import java.io.File
import java.util.ArrayList
import java.util.regex.Pattern

private val COMMAND_PATTERN = Pattern.compile("^/[a-z]*$")
private val MENTION_PATTERN = Pattern.compile("^(.* )?@([a-zA-Z]+[0-9]*)*$")
private const val MEDIA_ITEMS_PER_ROW = 4
private const val FILE_ITEMS_PER_ROW = 1
class MessageInputController(private val binding: StreamViewMessageInputBinding,
                             private val view: MessageInputView,
                             private val style: MessageInputStyle) {
	private var mediaAttachmentAdapter: MediaAttachmentAdapter? = null
	private var selectedMediaAttachmentAdapter: MediaAttachmentSelectedAdapter? = null
	private var fileAttachmentAdapter: FileAttachmentListAdapter? = null
	private var selectedFileAttachmentAdapter: AttachmentListAdapter? = null
	private var messageInputType: MessageInputType? = null
	private var selectedAttachments: MutableList<AttachmentMetaData> = ArrayList()
	private var attachmentData: List<AttachmentMetaData> = emptyList()
	private val uploadManager: UploadManager = UploadManager()
	private val gridLayoutManager = GridLayoutManager(view.context, 4, RecyclerView.VERTICAL, false)
	private val gridSpacingItemDecoration = GridSpacingItemDecoration(4, 2, false)
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

	fun onSendMessageClick(message: String) = when (selectedAttachments.isEmpty()) {
		true -> view.sendTextMessage(message)
		false -> view.sendAttachments(message, selectedAttachments.map { it.file })
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
		binding.tvTitle.text = type.getLabel(view.context)
		messageInputType = type
		configPermissions()
	}

	private fun configPermissions() {
		when {
			PermissionChecker.isGrantedCameraPermissions(view.context) -> {
				binding.ivMediaPermission.visibility = View.GONE
				binding.ivCameraPermission.visibility = View.GONE
				binding.ivFilePermission.visibility = View.GONE
			}
			PermissionChecker.isGrantedStoragePermissions(view.context) -> {
				binding.ivMediaPermission.visibility = View.GONE
				binding.ivCameraPermission.visibility = View.VISIBLE
				binding.ivFilePermission.visibility = View.GONE
			}
			else -> {
				binding.ivMediaPermission.visibility = View.VISIBLE
				binding.ivCameraPermission.visibility = View.VISIBLE
				binding.ivFilePermission.visibility = View.VISIBLE
			}
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

	private fun configSelectAttachView(isMedia: Boolean) {
		GlobalScope.launch(Dispatchers.Main) {
			attachmentData = getAttachmentsFromLocal(isMedia)
			if (selectedAttachments.isEmpty()) {
				setAttachmentAdapters(isMedia)
				if (attachmentData.isEmpty()) {
					Utils.showMessage(view.context, view.context.getResources().getString(R.string.stream_no_media_error))
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
					true -> Utils.getMediaAttachments(view.context)
					false -> Utils.getFileAttachments(Environment.getExternalStorageDirectory())
				}
			}

	private fun setAttachmentAdapters(isMedia: Boolean) {
		if (isMedia) {
			gridSpacingItemDecoration.setSpanCount(MEDIA_ITEMS_PER_ROW)
			gridLayoutManager.spanCount = MEDIA_ITEMS_PER_ROW
			mediaAttachmentAdapter = MediaAttachmentAdapter(attachmentData) { updateAttachment(it, isMedia) }
			binding.rvMedia.adapter = mediaAttachmentAdapter
		} else {
			gridSpacingItemDecoration.setSpanCount(FILE_ITEMS_PER_ROW)
			gridLayoutManager.spanCount = FILE_ITEMS_PER_ROW
			fileAttachmentAdapter = FileAttachmentListAdapter(attachmentData) { updateAttachment(it, isMedia) }
			binding.rvMedia.adapter = fileAttachmentAdapter
		}
	}

	private fun setSelectedAttachmentAdapter(fromGallery: Boolean, isMedia: Boolean) {
		if (isMedia) {
			selectedMediaAttachmentAdapter = MediaAttachmentSelectedAdapter(view.context, selectedAttachments, MediaAttachmentSelectedAdapter.OnAttachmentCancelListener { attachment: AttachmentMetaData -> cancelAttachment(attachment, fromGallery, isMedia) })
			binding.rvComposer.adapter = selectedMediaAttachmentAdapter
		} else {
			selectedFileAttachmentAdapter = AttachmentListAdapter(view.context, selectedAttachments, true, false, AttachmentListAdapter.OnAttachmentCancelListener { attachment: AttachmentMetaData -> cancelAttachment(attachment, fromGallery, isMedia) })
			binding.lvComposer.adapter = selectedFileAttachmentAdapter
		}
	}

	private fun updateAttachment(attachment: AttachmentMetaData, isMedia: Boolean) = when(attachment.isSelected) {
		true -> unselectAttachment(attachment, isMedia)
		false -> selectAttachment(attachment, isMedia)
	}

	private fun unselectAttachment(attachment: AttachmentMetaData, isMedia: Boolean) {
		attachment.isSelected = false
		selectedAttachments.remove(attachment)
		selectedAttachmentAdapterChanged(null, true, isMedia)
		configSendButtonEnableState()
		if (selectedAttachments.isEmpty() && messageInputType == MessageInputType.EDIT_MESSAGE) configAttachmentButtonVisible(true)
	}

	private fun selectAttachment(attachment: AttachmentMetaData, isMedia: Boolean) {
		if (isOverMaxUploadFileSize(attachment.file)) {
			Utils.showMessage(view.context, R.string.stream_large_size_file_error)
		} else {
			attachment.isSelected = true
			selectedAttachments.add(attachment)
			showHideComposerAttachmentGalleryView(true, isMedia)
			configSendButtonEnableState()
			selectedAttachmentAdapterChanged(attachment, true, isMedia)
		}
	}

	private fun isOverMaxUploadFileSize(file: File): Boolean = file.length() > Constant.MAX_UPLOAD_FILE_SIZE

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

	fun onClickOpenSelectView(editAttachments: MutableList<AttachmentMetaData>?, isMedia: Boolean) {
		if (! PermissionChecker.isGrantedStoragePermissions(view.context)) {
			PermissionChecker.checkStoragePermissions(view) { onClickOpenSelectView(editAttachments, isMedia) }
			return
		}
		initAdapter()
		if (editAttachments != null && editAttachments.isNotEmpty()) setSelectedAttachments(editAttachments)
		configSelectAttachView(isMedia)
		if (selectedAttachments.isEmpty()) {
			binding.progressBarFileLoader.visibility = View.VISIBLE
			onClickOpenBackGroundView(if (isMedia) MessageInputType.UPLOAD_MEDIA else MessageInputType.UPLOAD_FILE)
		}
	}

	private fun totalAttachmentAdapterChanged(attachment: AttachmentMetaData?, isMedia: Boolean) {
		if (isMedia) {
			if (attachment == null) {
				mediaAttachmentAdapter?.notifyDataSetChanged()
				return
			}
			val index = attachmentData.indexOf(attachment)
			if (index != - 1) mediaAttachmentAdapter?.notifyItemChanged(index)
		} else fileAttachmentAdapter?.notifyDataSetChanged()
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

	fun progressCapturedMedia(file: File?, isImage: Boolean) {
		val attachment = AttachmentMetaData(file)
		attachment.file = file
		if (isImage) {
			attachment.type = ModelType.attach_image
		} else {
			val retriever = MediaMetadataRetriever()
			retriever.setDataSource(view.context, Uri.fromFile(file))
			val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
			val videolengh = time.toLong()
			attachment.videoLength = (videolengh / 1000).toInt()
			Utils.configFileAttachment(attachment, file, ModelType.attach_file, ModelType.attach_mime_mp4)
			retriever.release()
		}
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