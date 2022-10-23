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

import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.querysort.QuerySorter
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.persistance.repository.QueryChannelsRepository
import io.getstream.chat.android.client.query.QueryChannelsSpec
import io.getstream.logging.StreamLog
import io.getstream.realm.entity.QueryChannelsEntityRealm
import io.getstream.realm.entity.generateQuerySpecId
import io.getstream.realm.entity.toDomain
import io.getstream.realm.entity.toRealm
import io.realm.kotlin.Realm
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query

public class RealmQueryChannelsRepository(private val realm: Realm) : QueryChannelsRepository {

    override suspend fun clear() {
        val queries = realm.query<QueryChannelsEntityRealm>().find()

        realm.writeBlocking { delete(queries) }
    }

    override suspend fun insertQueryChannels(queryChannelsSpec: QueryChannelsSpec) {
        val realmSpec = queryChannelsSpec.toRealm()

        realm.writeBlocking {
            copyToRealm(realmSpec, updatePolicy = UpdatePolicy.ALL)
            StreamLog.d("RealmQueryChannelsRepo") { "Saving realm spec -------" }
            realmSpec.log()
            StreamLog.d("RealmQueryChannelsRepo") { "END of Saving realm spec -------" }
        }
    }

    override suspend fun selectBy(
        filter: FilterObject,
        querySort: QuerySorter<Channel>,
    ): QueryChannelsSpec? {
        val id = generateQuerySpecId(filter, querySort)

        realm.query<QueryChannelsEntityRealm>()
            .find()
            .also { channelsRealm ->
                StreamLog.d("RealmQueryChannelsRepo") {
                    "Querying all QueryChannelsEntityRealm. Count: ${channelsRealm.size}"
                }
                channelsRealm.forEachIndexed { i, channelRealm ->
                    channelRealm.log(i.toString())
                }
            }

        val query = "id == '$id'"

        return realm.query<QueryChannelsEntityRealm>(query)
            .first()
            .find()
            .also { entity ->
                StreamLog.d("RealmQueryChannelsRepo") { "Result of query: $query ----------" }
                entity?.log("result!")
                StreamLog.d("RealmQueryChannelsRepo") { "END of query: $query ----------" }
            }
            ?.toDomain()
            .also { entity ->
                StreamLog.d("RealmQueryChannelsRepo") { "Result of query: $query - TO DOMAIN ----------" }
                entity?.log("result!")
            }
    }

    private fun QueryChannelsEntityRealm.log(index: String = "") {
        StreamLog.d("RealmQueryChannelsRepo") {
            "[QueryChannelsEntityRealm-$index]. id: ${this.id}. " +
                "filter: ${this.filterAsString} " +
                "query sort: ${this.query_sort} " +
                "ids: ${this.cids.joinToString()}"
        }
    }

    private fun QueryChannelsSpec.log(index: String = "") {
        StreamLog.d("RealmQueryChannelsRepo") {
            "[QueryChannelsSpec] $this"
        }
    }
}
