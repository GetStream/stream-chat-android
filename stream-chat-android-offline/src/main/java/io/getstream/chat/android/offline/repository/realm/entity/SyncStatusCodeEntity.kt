package io.getstream.chat.ui.sample.realm.entity

import io.getstream.chat.android.client.utils.SyncStatus

public fun Int.toDomain(): SyncStatus =
    when (this) {
        SyncStatus.SYNC_NEEDED.status -> SyncStatus.SYNC_NEEDED
        SyncStatus.COMPLETED.status -> SyncStatus.COMPLETED
        SyncStatus.FAILED_PERMANENTLY.status -> SyncStatus.FAILED_PERMANENTLY
        SyncStatus.IN_PROGRESS.status -> SyncStatus.IN_PROGRESS
        SyncStatus.AWAITING_ATTACHMENTS.status -> SyncStatus.AWAITING_ATTACHMENTS
        else -> throw IllegalStateException("The status code: $this is not supported")
    }

public fun SyncStatus.toRealm(): Int = status
