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

import com.squareup.moshi.Moshi
import com.squareup.moshi.addAdapter
import io.getstream.chat.android.client.query.QueryChannelsSpec
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.models.querysort.QuerySorter
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

@Suppress("VariableNaming")
internal class QueryChannelsEntityRealm : RealmObject {
    @PrimaryKey
    var id: String = ""
    var filter_as_string: String? = null
    var query_sort: QuerySorterInfoEntityRealm? = null
    var cids: RealmList<String> = realmListOf()
}

internal fun QueryChannelsSpec.toRealm(): QueryChannelsEntityRealm {
    val thisQuery = this
    val adapter = filterNodeAdapter()

    return QueryChannelsEntityRealm().apply {
        id = generateQuerySpecId(thisQuery.filter, thisQuery.querySort)
        filter_as_string = adapter.toJson(thisQuery.filter.toFilterNode())
        query_sort = thisQuery.querySort.toRealm()
        cids = thisQuery.cids.toRealmList()
    }
}

@OptIn(ExperimentalStdlibApi::class)
private fun filterNodeAdapter() =
    Moshi.Builder()
        .addAdapter(FilterNodeAdapter())
        .build()
        .adapter(FilterNode::class.java)

internal fun generateQuerySpecId(filter: FilterObject, querySort: QuerySorter<out Channel>): String {
    return "${filter.hashCode()}-${querySort.toDto().hashCode()}"
}

internal fun QueryChannelsEntityRealm.toDomain(): QueryChannelsSpec {
    val thisEntity = this

    val adapter = filterNodeAdapter()
    val querySort: QuerySorter<Channel> = query_sort?.toDomain() ?: QuerySortByField.ascByName("name")

    val filterAsString = filter_as_string
        ?.let(adapter::fromJson)
        ?.toFilterObject()
        ?: Filters.neutral()

    return QueryChannelsSpec(
        querySort = querySort,
        filter = filterAsString,
    ).apply {
        cids = thisEntity.cids.toSet()
    }
}
