package io.getstream.chat.android.offline.usecase

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.annotation.CheckResult
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.CoroutineCall
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.offline.ChatDomainImpl

@InternalStreamChatApi
internal class DownloadAttachment(private val domainImpl: ChatDomainImpl) {

    private val logger = ChatLogger.get("DownloadAttachment")

    /**
     * Downloads the selected attachment to the "Download" folder in the public external storage directory.
     *
     * @param attachment The attachment to download.
     */
    @CheckResult
    operator fun invoke(attachment: Attachment): Call<Unit> {
        val result: Result<Unit> = try {
            val downloadManager = domainImpl.appContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val url = attachment.assetUrl ?: attachment.imageUrl
            val subPath = attachment.name ?: attachment.title

            logger.logD("Downloading attachment. Name: $subPath, Url: $url")

            downloadManager.enqueue(
                DownloadManager.Request(Uri.parse(url))
                    .setTitle(subPath)
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, subPath)
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            )
            Result(Unit)
        } catch (e: Exception) {
            Result(ChatError(cause = e))
        }
        return CoroutineCall(domainImpl.scope) { result }
    }
}
