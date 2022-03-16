package io.getstream.chat.android.offline.experimental.plugin.configuration

import io.getstream.chat.android.offline.message.attachment.UploadAttachmentsNetworkType

/**
 * Provides a configuration for [io.getstream.chat.android.offline.experimental.plugin.OfflinePlugin].
 *
 * @param backgroundSyncEnabled Whether the SDK should perform background sync if some queries fail.
 * @param userPresence Whether the SDK should receive user presence changes.
 * @param persistenceEnabled Whether the data should be stored in the DB.
 * @param uploadAttachmentsNetworkType An enumeration of various network types used as a constraint inside upload attachments worker.
 */
public data class Config(
    public val backgroundSyncEnabled: Boolean = true,
    public val userPresence: Boolean = true,
    public val persistenceEnabled: Boolean = true,
    public val uploadAttachmentsNetworkType: UploadAttachmentsNetworkType = UploadAttachmentsNetworkType.NOT_ROAMING,
)
