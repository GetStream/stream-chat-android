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

package io.getstream.realm.entity

import com.squareup.moshi.Moshi
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.querysort.QuerySortByField
import io.getstream.chat.android.client.api.models.querysort.QuerySorter
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.query.QueryChannelsSpec
import io.getstream.realm.filter.toFilterNode
import io.getstream.realm.filter.toFilterObject
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

@Suppress("VariableNaming")
internal class QueryChannelsEntityRealm : RealmObject {
    @PrimaryKey
    var id: String = ""
    var filterAsString: String? = null
    var query_sort: QuerySorterInfoEntityRealm? = null
    var cids: RealmList<String> = realmListOf()
}

@OptIn(ExperimentalStdlibApi::class)
internal fun QueryChannelsSpec.toRealm(): QueryChannelsEntityRealm {
    val thisQuery = this
    val moshi = Moshi.Builder()
        // .addAdapter(FilterNodeAdapter())
        .build()

    return QueryChannelsEntityRealm().apply {
        id = generateQuerySpecId(thisQuery.filter, thisQuery.querySort)
        filterAsString =
            moshi.adapter(FilterNode::class.java).toJson(thisQuery.filter.toFilterNode())
        query_sort = thisQuery.querySort.toRealm()
        cids = thisQuery.cids.toRealmList()
    }
}

@OptIn(ExperimentalStdlibApi::class)
internal fun QueryChannelsEntityRealm.toDomain(): QueryChannelsSpec {
    val thisEntity = this

    val moshi = Moshi.Builder()
        // .addAdapter(FilterNodeAdapter())
        .build()

    return QueryChannelsSpec(
        filter = filterAsString
            ?.let(moshi.adapter(FilterNode::class.java)::fromJson)?.toFilterObject()
            ?: Filters.neutral(),
        querySort = query_sort?.toDomain() ?: QuerySortByField.ascByName("name"),
    ).apply {
        cids = thisEntity.cids.toSet()
    }
}

internal fun generateQuerySpecId(filter: FilterObject, querySort: QuerySorter<out Channel>): String {
    return "${filter.hashCode()}-${querySort.toDto().hashCode()}"
}
