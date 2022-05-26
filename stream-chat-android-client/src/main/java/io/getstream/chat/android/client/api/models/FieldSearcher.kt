package io.getstream.chat.android.client.api.models

import io.getstream.chat.android.client.extensions.lowerCamelCaseToGetter
import io.getstream.chat.android.client.extensions.snakeToLowerCamelCase
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

public class FieldSearcher {

    public fun <T : Any> findProperty(
        fieldName: String,
        kClass: KClass<T>,
    ): KProperty1<T, Comparable<*>?>? {
        val members = kClass.members.filterIsInstance<KProperty1<T, Comparable<*>?>>()
        val camelCaseName = fieldName.snakeToLowerCamelCase()
        val getField = camelCaseName.lowerCamelCaseToGetter()

        return members.firstOrNull { property ->
            property.name == camelCaseName || property.name == getField
        }
    }

    public fun <T : Any> findMemberProperty(
        fieldName: String,
        kClass: KClass<T>,
    ): KProperty1<T, Comparable<*>?>? {
        val members = kClass.memberProperties.filterIsInstance<KProperty1<T, Comparable<*>?>>()
        val camelCaseName = fieldName.snakeToLowerCamelCase()

        return members.firstOrNull { property -> property.name == camelCaseName }
    }
}
