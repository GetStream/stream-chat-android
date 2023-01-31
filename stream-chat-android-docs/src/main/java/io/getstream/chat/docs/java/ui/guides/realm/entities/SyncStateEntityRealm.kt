/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.docs.java.ui.guides.realm.entities

import io.getstream.chat.android.client.sync.SyncState
import io.getstream.chat.docs.java.ui.guides.realm.utils.toDate
import io.getstream.chat.docs.java.ui.guides.realm.utils.toRealmInstant
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
