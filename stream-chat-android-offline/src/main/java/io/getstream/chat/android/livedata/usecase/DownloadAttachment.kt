package io.getstream.chat.android.livedata.usecase

import androidx.annotation.CheckResult
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.offline.usecase.DownloadAttachment as OfflineDownloadAttachment

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

internal class DownloadAttachmentImpl(private val offlineDownloadAttachment: OfflineDownloadAttachment) :
    DownloadAttachment {
    override operator fun invoke(attachment: Attachment): Call<Unit> = offlineDownloadAttachment.invoke(attachment)
}
