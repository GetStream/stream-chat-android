package io.getstream.chat.android.ui.utils.extensions

internal inline fun <reified AnyT> Any.cast() = this as AnyT

internal inline fun <reified AnyT> Any.safeCast() = this as? AnyT

internal val <AnyT> AnyT.exhaustive: AnyT
    get() = this

// Just a way to abstract the elvis operator into a method for chaining
internal fun <AnyT> AnyT?.getOrDefault(default: AnyT): AnyT = this ?: default
