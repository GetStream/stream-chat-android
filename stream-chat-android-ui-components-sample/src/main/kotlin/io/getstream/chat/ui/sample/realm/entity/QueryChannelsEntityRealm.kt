package io.getstream.chat.ui.sample.realm.entity

import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.querysort.QuerySorter
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.query.QueryChannelsSpec
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

internal class QueryChannelsEntityRealm : RealmObject {
  @PrimaryKey
  var id: String = ""
  var filter: FilterObject = Filters.neutral()
  var querySort: QuerySorter<Channel>? = null
  var cids: RealmList<String> = realmListOf()
}

internal fun QueryChannelsSpec.toRealm(): QueryChannelsEntityRealm {
  val thisQuery = this

  return QueryChannelsEntityRealm().apply {
    id = generateQuerySpecId(thisQuery.filter, thisQuery.querySort)
    filter = thisQuery.filter
    querySort = thisQuery.querySort
    cids = thisQuery.cids.toRealmList()
  }
}

internal fun QueryChannelsEntityRealm.toDomain(): QueryChannelsSpec {
  val entity = this

  return QueryChannelsSpec(
    filter = filter,
    querySort = querySort!!,
  ).apply {
    cids = entity.cids.toSet()
  }
}

internal fun generateQuerySpecId(filter: FilterObject, querySort: QuerySorter<Channel>): String {
  return "${filter.hashCode()}-${querySort.toDto().hashCode()}"
}
