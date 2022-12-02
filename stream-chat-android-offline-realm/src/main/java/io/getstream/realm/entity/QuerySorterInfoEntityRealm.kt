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

import io.getstream.chat.android.models.querysort.ComparableFieldProvider
import io.getstream.chat.android.models.querysort.QuerySorter
import io.getstream.chat.android.models.querysort.SortDirection
import io.getstream.chat.android.models.querysort.internal.SortAttribute
import io.getstream.chat.android.models.querysort.internal.SortSpecification
import io.getstream.chat.android.offline.repository.database.converter.internal.QuerySortParser
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

@Suppress("VariableNaming")
internal class QuerySorterInfoEntityRealm : RealmObject {
    var query_specs: RealmList<SortSpecificationEntityRealm> = realmListOf()

    @PrimaryKey
    var id: Int = query_specs.hashCode()

    override fun toString(): String {
        return "[QuerySorterInfoEntityRealm] id: $id " +
            "${query_specs.joinToString { "name: ${it.sort_attribute_name} direction: ${it.sort_direction}" }}}"
    }
}

@Suppress("VariableNaming")
internal class SortSpecificationEntityRealm : RealmObject {
    var sort_attribute_name: String = ""
    var sort_direction: Int = 0
}

internal fun <T : ComparableFieldProvider> QuerySorterInfoEntityRealm.toDomain(): QuerySorter<T> {
    return QuerySortParser<T>().fromSpecifications(this.query_specs.map { it.toDomain() })
}

internal fun <T : ComparableFieldProvider> QuerySorter<T>.toRealm(): QuerySorterInfoEntityRealm {
    val thisQuerySorter = this

    return QuerySorterInfoEntityRealm().apply {
        query_specs = thisQuerySorter.sortSpecifications
            .map { spec -> spec.toRealm() }
            .toRealmList()

        id = query_specs.hashCode()
    }
}

internal fun <T> SortSpecificationEntityRealm.toDomain(): SortSpecification<T> {
    return SortSpecification(
        sortAttribute = SortAttribute.FieldNameSortAttribute(this.sort_attribute_name),
        sortDirection = SortDirection.fromNumber(sort_direction),
    )
}

private fun SortSpecification<*>.toRealm(): SortSpecificationEntityRealm {
    val thisSortSpec = this

    return SortSpecificationEntityRealm().apply {
        sort_attribute_name = thisSortSpec.sortAttribute.name
        sort_direction = thisSortSpec.sortDirection.value
    }
}
