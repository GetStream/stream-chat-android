package io.getstream.chat.android.offline.internal.repository.domain.syncState

import io.getstream.chat.android.offline.internal.sync.SyncState

internal fun SyncStateEntity.toModel() =
    SyncState(userId, activeChannelIds, lastSyncedAt, markedAllReadAt)

internal fun SyncState.toEntity() =
    SyncStateEntity(userId, activeChannelIds, lastSyncedAt, markedAllReadAt)
