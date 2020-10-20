internal inline fun Boolean.whenTrue(crossinline f: () -> Unit): Boolean = also { if (this) f() }
internal inline fun Boolean.whenFalse(crossinline f: () -> Unit): Boolean = also { if (!this) f() }

internal val <T> T.exhaustive: T
    get() = this
