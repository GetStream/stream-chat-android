package java.util

internal fun Date.isInLastMinute(): Boolean = (Date().time - 60000 < time)
