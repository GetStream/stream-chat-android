package io.getstream.chat.android.livedata.utils

import io.getstream.chat.android.client.api.models.AndFilterObject
import io.getstream.chat.android.client.api.models.AutocompleteFilterObject
import io.getstream.chat.android.client.api.models.ContainsFilterObject
import io.getstream.chat.android.client.api.models.DistinctFilterObject
import io.getstream.chat.android.client.api.models.EqualsFilterObject
import io.getstream.chat.android.client.api.models.ExistsFilterObject
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.GreaterThanFilterObject
import io.getstream.chat.android.client.api.models.GreaterThanOrEqualsFilterObject
import io.getstream.chat.android.client.api.models.InFilterObject
import io.getstream.chat.android.client.api.models.LessThanFilterObject
import io.getstream.chat.android.client.api.models.LessThanOrEqualsFilterObject
import io.getstream.chat.android.client.api.models.NeutralFilterObject
import io.getstream.chat.android.client.api.models.NonExistsFilterObject
import io.getstream.chat.android.client.api.models.NorFilterObject
import io.getstream.chat.android.client.api.models.NotEqualsFilterObject
import io.getstream.chat.android.client.api.models.NotInFilterObject
import io.getstream.chat.android.client.api.models.OrFilterObject
import io.getstream.chat.android.client.models.CustomObject
import java.lang.ClassCastException

internal fun <T : CustomObject> Collection<T>.filter(filterObject: FilterObject): List<T> =
    filter { filterObject.filter(it) }

@Suppress("UNCHECKED_CAST")
private fun <T : CustomObject> FilterObject.filter(t: T): Boolean = try {
    when (this) {
        is AndFilterObject -> TODO()
        is OrFilterObject -> TODO()
        is NorFilterObject -> TODO()
        is ContainsFilterObject -> TODO()
        is AutocompleteFilterObject -> TODO()
        is ExistsFilterObject -> TODO()
        is NonExistsFilterObject -> TODO()
        is EqualsFilterObject -> TODO()
        is NotEqualsFilterObject -> TODO()
        is GreaterThanFilterObject -> TODO()
        is GreaterThanOrEqualsFilterObject -> TODO()
        is LessThanFilterObject -> TODO()
        is LessThanOrEqualsFilterObject -> TODO()
        is InFilterObject -> TODO()
        is NotInFilterObject -> TODO()
        is DistinctFilterObject -> TODO()
        NeutralFilterObject -> TODO()
    }
} catch (e: ClassCastException) {
    false
}
