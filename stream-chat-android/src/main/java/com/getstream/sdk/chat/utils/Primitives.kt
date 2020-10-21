package com.getstream.sdk.chat.utils

internal inline fun Boolean.whenTrue(crossinline f: () -> Unit): Boolean = also { if (this) f() }
internal inline fun Boolean.whenFalse(crossinline f: () -> Unit): Boolean = also { if (!this) f() }

internal inline val <T> T.exhaustive: T
    get() = this

internal fun String?.initials(): String =
    this
        ?.trim()
        ?.split("\\s+".toRegex())
        ?.take(2)?.joinToString(separator = "") { it.take(1).toUpperCase() }
        ?: ""
