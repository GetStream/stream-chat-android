package io.getstream.realm.entity

import io.getstream.chat.android.client.api.models.querysort.ComparableFieldProvider
import io.getstream.chat.android.client.api.models.querysort.QuerySorter
import io.getstream.chat.android.client.api.models.querysort.SortDirection
import io.getstream.chat.android.client.api.models.querysort.internal.SortAttribute
import io.getstream.chat.android.client.api.models.querysort.internal.SortSpecification
import io.getstream.chat.android.offline.repository.database.converter.internal.QuerySortParser
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

internal class QuerySorterInfoEntityRealm : RealmObject {
    var querySpecs: RealmList<SortSpecificationEntityRealm> = realmListOf()

    @PrimaryKey
    var id: Int = querySpecs.hashCode()
}

internal class SortSpecificationEntityRealm : RealmObject {
    var sort_attribute_name: String = ""
    var sort_direction: Int = 0
}

internal fun <T: ComparableFieldProvider> QuerySorterInfoEntityRealm.toDomain(): QuerySorter<T> {
    return QuerySortParser<T>().fromSpecifications(this.querySpecs.map { it.toDomain() })
}

internal fun <T: ComparableFieldProvider> QuerySorter<T>.toRealm(): QuerySorterInfoEntityRealm {
    val thisQuerySorter = this

    return QuerySorterInfoEntityRealm().apply {
        querySpecs = thisQuerySorter.sortSpecifications
            .map { spec -> spec.toRealm() }
            .toRealmList()

        id = querySpecs.hashCode()
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
