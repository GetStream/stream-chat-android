package io.getstream.chat.android.offline.utils

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
import io.getstream.chat.android.client.api.models.NorFilterObject
import io.getstream.chat.android.client.api.models.NotEqualsFilterObject
import io.getstream.chat.android.client.api.models.NotExistsFilterObject
import io.getstream.chat.android.client.api.models.NotInFilterObject
import io.getstream.chat.android.client.api.models.OrFilterObject
import io.getstream.chat.android.client.extensions.snakeToLowerCamelCase
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.CustomObject
import io.getstream.chat.android.client.models.Member
import java.lang.ClassCastException
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

private const val MEMBERS_FIELD_NAME = "members"

internal fun <T : CustomObject> Collection<T>.filter(filterObject: FilterObject): List<T> =
    filter { filterObject.filter(it) }

@Suppress("UNCHECKED_CAST")
internal fun <T : CustomObject> FilterObject.filter(t: T): Boolean = try {
    when (this) {
        is AndFilterObject -> filterObjects.all { it.filter(t) }
        is OrFilterObject -> filterObjects.any { it.filter(t) }
        is NorFilterObject -> filterObjects.none { it.filter(t) }
        is ContainsFilterObject -> when (fieldName) {
            MEMBERS_FIELD_NAME -> t.getMembersId().contains(value)
            else -> t.getMemberPropertyOrExtra(fieldName, List::class)?.contains(value) ?: false
        }
        is AutocompleteFilterObject -> t.getMemberPropertyOrExtra(fieldName, String::class)?.contains(value) ?: false
        is ExistsFilterObject -> t.getMemberPropertyOrExtra(fieldName, Any::class) != null
        is NotExistsFilterObject -> t.getMemberPropertyOrExtra(fieldName, Any::class) == null
        is EqualsFilterObject -> value == t.getMemberPropertyOrExtra(fieldName, value::class)
        is NotEqualsFilterObject -> value != t.getMemberPropertyOrExtra(fieldName, value::class)
        is GreaterThanFilterObject ->
            compare(t.getMemberPropertyOrExtra(fieldName, value::class) as? Comparable<Any>, value as? Comparable<Any>) { it > 0 }
        is GreaterThanOrEqualsFilterObject ->
            compare(t.getMemberPropertyOrExtra(fieldName, value::class) as? Comparable<Any>, value as? Comparable<Any>) { it >= 0 }
        is LessThanFilterObject ->
            compare(t.getMemberPropertyOrExtra(fieldName, value::class) as? Comparable<Any>, value as? Comparable<Any>) { it < 0 }
        is LessThanOrEqualsFilterObject ->
            compare(t.getMemberPropertyOrExtra(fieldName, value::class) as? Comparable<Any>, value as? Comparable<Any>) { it <= 0 }
        is InFilterObject -> when (fieldName) {
            MEMBERS_FIELD_NAME -> values.any(t.getMembersId()::contains)
            else -> {
                val fieldValue = t.getMemberPropertyOrExtra(fieldName, Any::class)
                if (fieldValue is List<*>) {
                    values.any(fieldValue::contains)
                } else {
                    values.contains(fieldValue)
                }
            }
        }
        is NotInFilterObject -> when (fieldName) {
            MEMBERS_FIELD_NAME -> values.none(t.getMembersId()::contains)
            else -> {
                val fieldValue = t.getMemberPropertyOrExtra(fieldName, Any::class)
                if (fieldValue is List<*>) {
                    values.none(fieldValue::contains)
                } else {
                    !values.contains(fieldValue)
                }
            }
        }
        is DistinctFilterObject -> (t as? Channel)?.let { channel ->
            channel.id.startsWith("!members") &&
                channel.members.size == memberIds.size &&
                channel.members.map { it.user.id }.containsAll(memberIds)
        } ?: false
        NeutralFilterObject -> true
    }
} catch (e: ClassCastException) {
    false
}

@Suppress("UNCHECKED_CAST")
private fun <T : Any> CustomObject.getMemberPropertyOrExtra(name: String, clazz: KClass<out T>): T? =
    name.snakeToLowerCamelCase().let { fieldName ->
        this::class.memberProperties.firstOrNull { it.name == fieldName }?.getter?.call(this)?.cast(clazz)
            ?: this.getExtra(name, clazz)
    }

private fun <T : Any> Any.cast(clazz: KClass<out T>): T = clazz.javaObjectType.cast(this)!!

private fun <T : Comparable<T>> compare(a: T?, b: T?, compareFun: (Int) -> Boolean): Boolean =
    a?.let { notNullA ->
        b?.let { notNullB ->
            compareFun(notNullA.compareTo(notNullB))
        }
    } ?: false

private fun CustomObject.getMembersId(): List<String> =
    getMemberPropertyOrExtra(MEMBERS_FIELD_NAME, List::class)?.mapNotNull { (it as? Member)?.getUserId() } ?: emptyList()

@Suppress("UNCHECKED_CAST")
private fun <T : Any> CustomObject.getExtra(name: String, clazz: KClass<out T>): T? =
    extraData[name]?.let {
        when (clazz) {
            Double::class -> (it as? Number)?.toDouble()
            Float::class -> (it as? Number)?.toFloat()
            Long::class -> (it as? Number)?.toLong()
            Int::class -> (it as? Number)?.toInt()
            Char::class -> (it as? Number)?.toChar()
            Short::class -> (it as? Number)?.toShort()
            Byte::class -> (it as? Number)?.toByte()
            else -> it
        }
    } as? T
