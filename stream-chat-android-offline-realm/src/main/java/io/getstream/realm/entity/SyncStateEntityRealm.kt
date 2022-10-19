package io.getstream.realm.entity

import io.getstream.chat.android.client.sync.SyncState
import io.getstream.realm.utils.toDate
import io.getstream.realm.utils.toRealmInstant
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

@Suppress("VariableNaming")
internal class SyncStateEntityRealm : RealmObject {
    @PrimaryKey
    var user_id: String = ""
    var active_channel_ids: RealmList<String> = realmListOf()
    var last_synced_at: RealmInstant? = null
    var raw_last_synced_at: String? = null
    var marked_all_read_at: RealmInstant? = null
}

internal fun SyncStateEntityRealm.toDomain(): SyncState =
    SyncState(
        userId = user_id,
        activeChannelIds = active_channel_ids,
        lastSyncedAt = last_synced_at?.toDate(),
        rawLastSyncedAt = raw_last_synced_at,
        markedAllReadAt = marked_all_read_at?.toDate(),
    )

internal fun SyncState.toRealm(): SyncStateEntityRealm {
    val thisSyncState = this

    return SyncStateEntityRealm().apply {
        user_id = thisSyncState.userId
        active_channel_ids = thisSyncState.activeChannelIds.toRealmList()
        last_synced_at = thisSyncState.lastSyncedAt?.toRealmInstant()
        raw_last_synced_at = thisSyncState.userId
        marked_all_read_at = thisSyncState.markedAllReadAt?.toRealmInstant()
    }
}
