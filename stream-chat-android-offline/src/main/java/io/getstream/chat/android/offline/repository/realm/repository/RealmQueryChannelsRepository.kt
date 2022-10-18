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

package io.getstream.chat.android.offline.repository.realm.repository

import android.util.Log
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.querysort.QuerySorter
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.persistance.repository.QueryChannelsRepository
import io.getstream.chat.android.client.query.QueryChannelsSpec
import io.getstream.chat.android.offline.repository.realm.entity.QueryChannelsEntityRealm
import io.getstream.chat.android.offline.repository.realm.entity.generateQuerySpecId
import io.getstream.chat.android.offline.repository.realm.entity.toDomain
import io.getstream.chat.android.offline.repository.realm.entity.toRealm
import io.realm.kotlin.Realm
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query

private const val TAG = "ChannelsRepository"

public class RealmQueryChannelsRepository(private val realm: Realm) : QueryChannelsRepository {

    override suspend fun clear() {
        val queries = realm.query<QueryChannelsEntityRealm>().find()

        realm.writeBlocking {
            delete(queries)
        }
    }

    override suspend fun insertQueryChannels(queryChannelsSpec: QueryChannelsSpec) {
        val realmSpec = queryChannelsSpec.toRealm()
        realm.writeBlocking {
            copyToRealm(realmSpec, updatePolicy = UpdatePolicy.ALL)
        }

        realm.query<QueryChannelsEntityRealm>("id == '${realmSpec.id}'")
            .find()
            .onEach { querySpec ->
                Log.d(TAG, "inserted spec with id: ${querySpec.id}")
                Log.d(TAG, "inserted spec with cids: ${querySpec.cids.joinToString()}")
            }
    }

    override suspend fun selectBy(
        filter: FilterObject,
        querySort: QuerySorter<Channel>,
    ): QueryChannelsSpec? {
        val id = generateQuerySpecId(filter, querySort)

        val query = "id == '$id'"

        return realm.query<QueryChannelsEntityRealm>(query)
            .first()
            .find()
            .also { querySpec ->
                Log.d(TAG, "Query: $query")
                Log.d(TAG, "Found spec with id: ${querySpec?.id}")
                Log.d(TAG, "Found spec with cids: ${querySpec?.cids?.joinToString()}")
            }
            ?.toDomain()
    }
}
