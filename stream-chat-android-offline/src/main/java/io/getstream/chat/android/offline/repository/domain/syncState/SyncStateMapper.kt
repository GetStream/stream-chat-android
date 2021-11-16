package io.getstream.chat.android.offline.repository.domain.syncState

import io.getstream.chat.android.offline.model.SyncState

internal fun SyncStateEntity.toModel() =
    SyncState(userId, activeChannelIds, lastSyncedAt, markedAllReadAt)

internal fun SyncState.toEntity() =
    SyncStateEntity(userId, activeChannelIds, lastSyncedAt, markedAllReadAt)
