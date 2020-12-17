package com.getstream.sdk.chat.utils.extensions

internal inline fun Boolean.whenTrue(crossinline f: () -> Unit): Boolean = also { if (this) f() }
internal inline fun Boolean.whenFalse(crossinline f: () -> Unit): Boolean = also { if (!this) f() }
