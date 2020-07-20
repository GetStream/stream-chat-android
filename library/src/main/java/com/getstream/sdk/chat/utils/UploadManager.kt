package com.getstream.sdk.chat.utils

import com.getstream.sdk.chat.model.AttachmentMetaData
import com.getstream.sdk.chat.model.ModelType
import exhaustive
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.utils.ProgressCallback
import java.util.ArrayList

class UploadManager(private val chatClient: ChatClient = ChatClient.instance()) {
    private val queue: MutableList<AttachmentMetaData> = ArrayList()
    fun uploadFile(
        channelType: String,
        channelId: String,
        data: AttachmentMetaData,
        fileListener: ProgressCallback
    ) {
        queue.add(data)
        val progressCallback: ProgressCallback = object : ProgressCallback {
            override fun onSuccess(path: String) {
                data.attachment = Attachment().apply {
                    mimeType = data.mimeType
                    fileSize = data.file.length().toInt()
                    name = data.file.name
                    type = data.type
                    url = path
                    if (data.type == ModelType.attach_image) {
                        imageUrl = path
                        fallback = data.file.name
                    } else {
                        assetUrl = path
                    }
                }
                queue.remove(data)
                fileListener.onSuccess(path)
            }

            override fun onError(error: ChatError) {
                queue.remove(data)
                fileListener.onError(error)
            }

            override fun onProgress(progress: Long) {
                fileListener.onProgress(progress)
            }
        }
        when (data.isImage) {
            true -> chatClient.sendImage(channelType, channelId, data.file, progressCallback)
            false -> chatClient.sendFile(channelType, channelId, data.file, progressCallback)
        }.exhaustive
    }

    fun removeFromQueue(file: AttachmentMetaData) {
        queue.remove(file)
    }

    val isUploadingFile: Boolean
        get() = queue.isNotEmpty()

    fun resetQueue() {
        queue.clear()
    }
}
