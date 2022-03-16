package io.getstream.chat.android.offline.repository.domain.syncState.internal

import io.getstream.chat.android.offline.sync.internal.SyncState

internal fun SyncStateEntity.toModel() =
    SyncState(userId, activeChannelIds, lastSyncedAt, markedAllReadAt)

internal fun SyncState.toEntity() =
    SyncStateEntity(userId, activeChannelIds, lastSyncedAt, markedAllReadAt)
