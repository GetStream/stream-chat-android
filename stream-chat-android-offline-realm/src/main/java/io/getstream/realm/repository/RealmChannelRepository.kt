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

package io.getstream.realm.repository

import android.util.Log
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.persistance.repository.ChannelRepository
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.realm.entity.ChannelEntityRealm
import io.getstream.realm.entity.toDomain
import io.getstream.realm.entity.toRealm
import io.getstream.realm.utils.toRealmInstant
import io.realm.kotlin.Realm
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import java.util.*

private const val LOG_TAG: String = "RealmChannelRepository"
private const val NO_LIMIT: Int = -1

@Suppress("TooManyFunctions")
public class RealmChannelRepository(private val realm: Realm) : ChannelRepository {

    override suspend fun clear() {
        realm.writeBlocking {
            query<ChannelEntityRealm>().find().let(this::delete)
        }
    }

    override fun clearChannelCache() {
        // Nothing do to. There's no cache
    }

    override suspend fun deleteChannel(cid: String) {
        realm.writeBlocking {
            realm.query<ChannelEntityRealm>("cid == '$cid'")
                .first()
                .find()
                ?.let(this::delete)
        }
    }

    override suspend fun evictChannel(cid: String) {
        // Nothing to do
    }

    override suspend fun insertChannel(channel: Channel) {
        realm.writeBlocking {
            this.copyToRealm(channel.toRealm(), updatePolicy = UpdatePolicy.ALL)
        }
    }

    override suspend fun insertChannels(channels: Collection<Channel>) {
        Log.d(LOG_TAG, "inserting channels: ${channels.names()}")
        channels.forEach { channel -> insertChannel(channel) }
    }

    override suspend fun selectAllCids(): List<String> =
        realm.query<ChannelEntityRealm>().find().map { entity -> entity.cid }

    override suspend fun selectChannelByCid(cid: String): Channel? =
        selectChannelByCidRealm(cid)?.toDomain()

    override suspend fun selectChannelCidsBySyncNeeded(limit: Int): List<String> =
        realm.query<ChannelEntityRealm>("sync_status == $0", SyncStatus.SYNC_NEEDED.status)
            .apply {
                if (limit != NO_LIMIT) limit(limit)
            }
            .find()
            .map { entity -> entity.cid }

    override suspend fun selectChannelWithoutMessages(cid: String): Channel? =
        selectChannelByCidRealm(cid)?.toDomain()

    override suspend fun selectChannels(
        channelCIDs: List<String>,
        forceCache: Boolean,
    ): List<Channel> {
        val channelsString = channelCIDs.joinToString(
            prefix = "{ ",
            postfix = " }",
            separator = ", "
        ) { cid ->
            "'$cid'"
        }

        val query = "cid IN $channelsString"

        return realm.query<ChannelEntityRealm>(query)
            .find()
            .map { entity -> entity.toDomain() }
    }

    override suspend fun selectChannelsByCids(cids: List<String>): List<Channel> =
        selectChannels(cids, false)

    override suspend fun selectChannelsSyncNeeded(limit: Int): List<Channel> {
        return realm.query<ChannelEntityRealm>("sync_status == $0", SyncStatus.SYNC_NEEDED.status)
            .apply {
                if (limit != NO_LIMIT) limit(limit)
            }
            .find()
            .map { entity -> entity.toDomain() }
    }

    override suspend fun selectMembersForChannel(cid: String): List<Member> = emptyList()

    override suspend fun setChannelDeletedAt(cid: String, deletedAt: Date) {
        val channel = selectChannelByCidRealm(cid)
        realm.writeBlocking {
            channel.apply { this?.deleted_at = deletedAt.toRealmInstant() }?.let { entity ->
                this.copyToRealm(entity, UpdatePolicy.ALL)
            }
        }
    }

    override suspend fun setHiddenForChannel(cid: String, hidden: Boolean) {
        val channel = selectChannelByCidRealm(cid)
        realm.writeBlocking {
            channel.apply { this?.hidden = hidden }
                ?.let { entity -> copyToRealm(entity, updatePolicy = UpdatePolicy.ALL) }
        }
    }

    override suspend fun setHiddenForChannel(cid: String, hidden: Boolean, hideMessagesBefore: Date) {
        // Nothing to do here
    }

    override suspend fun updateLastMessageForChannel(cid: String, lastMessage: Message) {
        // Nothing to do here
    }

    override suspend fun updateMembersForChannel(cid: String, members: List<Member>) {
        // Nothing to do here
    }

    private fun selectChannelByCidRealm(cid: String): ChannelEntityRealm? =
        realm.query<ChannelEntityRealm>("cid == '$cid'")
            .first()
            .find()
}

private fun Iterable<Channel>.names(): String = joinToString { channel -> channel.name }
