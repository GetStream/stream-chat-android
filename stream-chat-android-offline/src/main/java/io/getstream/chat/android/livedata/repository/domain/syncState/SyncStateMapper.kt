package io.getstream.chat.android.livedata.repository.domain.syncState

import io.getstream.chat.android.livedata.model.SyncState

internal fun SyncStateEntity.toModel() =
    SyncState(userId, activeChannelIds, activeQueryIds, lastSyncedAt, markedAllReadAt)

internal fun SyncState.toEntity() =
    SyncStateEntity(userId, activeChannelIds, activeQueryIds, lastSyncedAt, markedAllReadAt)
