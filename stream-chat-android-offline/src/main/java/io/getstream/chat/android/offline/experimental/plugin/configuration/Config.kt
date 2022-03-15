package io.getstream.chat.android.offline.experimental.plugin.configuration

import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.offline.message.attachment.UploadAttachmentsNetworkType

@InternalStreamChatApi
public data class Config(
    public val backgroundSyncEnabled: Boolean = true,
    public val userPresence: Boolean = true,
    public val persistenceEnabled: Boolean = true,
    public val uploadAttachmentsNetworkType: UploadAttachmentsNetworkType = UploadAttachmentsNetworkType.NOT_ROAMING
)
