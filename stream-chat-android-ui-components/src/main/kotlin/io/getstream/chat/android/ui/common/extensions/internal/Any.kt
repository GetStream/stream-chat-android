package io.getstream.chat.android.ui.common.extensions.internal

internal inline fun <reified AnyT> Any.cast() = this as AnyT

internal inline fun <reified AnyT> Any.safeCast() = this as? AnyT

// Just a way to abstract the elvis operator into a method for chaining
internal fun <AnyT> AnyT?.getOrDefault(default: AnyT): AnyT = this ?: default

internal fun <AnyT> AnyT?.isNotNull() = this != null

internal fun <AnyT> AnyT.singletonList() = listOf(this)
