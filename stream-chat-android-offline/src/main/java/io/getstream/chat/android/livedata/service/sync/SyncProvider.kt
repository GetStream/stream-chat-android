package io.getstream.chat.android.livedata.service.sync

import android.content.Context

internal class SyncProvider(context: Context) {

    internal val encryptedBackgroundSyncConfigStore by lazy {
        EncryptedBackgroundSyncConfigStore(context)
    }

    internal val notificationConfigStore by lazy {
        NotificationConfigStore(context)
    }
}
