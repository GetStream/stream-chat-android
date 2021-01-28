package io.getstream.chat.android.livedata.usecase

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.annotation.CheckResult
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.CoroutineCall
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.livedata.ChatDomainImpl

@InternalStreamChatApi
public interface DownloadAttachment {
    /**
     * Downloads the selected attachment to the "Download" folder in the public external storage directory.
     *
     * @param attachment the attachment to download
     */
    @CheckResult
    public operator fun invoke(attachment: Attachment): Call<Unit>
}

internal class DownloadAttachmentImpl(private val domainImpl: ChatDomainImpl) : DownloadAttachment {
    override operator fun invoke(attachment: Attachment): Call<Unit> {
        val result: Result<Unit> = try {
            val downloadManager = domainImpl.appContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val url = attachment.assetUrl ?: attachment.imageUrl
            downloadManager.enqueue(
                DownloadManager.Request(Uri.parse(url))
                    .setTitle(attachment.name)
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, attachment.name)
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            )
            Result(Unit)
        } catch (e: Exception) {
            Result(ChatError(cause = e))
        }
        return CoroutineCall(domainImpl.scope) { result }
    }
}
