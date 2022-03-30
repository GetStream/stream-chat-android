package io.getstream.logging.helper

import io.getstream.logging.Priority
import io.getstream.logging.StreamLogger
import java.io.PrintWriter
import java.io.StringWriter

public fun Thread.stringify(): String {
    return "$name:$id"
}

public fun Priority.stringify(): String = when (this) {
    StreamLogger.VERBOSE -> "V"
    StreamLogger.DEBUG -> "D"
    StreamLogger.INFO -> "I"
    StreamLogger.WARN -> "W"
    StreamLogger.ERROR -> "E"
    StreamLogger.ASSERT -> "E"
    else -> "?"
}

public fun Throwable.stringify(): String {
    // Don't replace this with Log.getStackTraceString() - it hides
    // UnknownHostException, which is not what we want.
    val sw = StringWriter(256)
    val pw = PrintWriter(sw, false)
    printStackTrace(pw)
    pw.flush()
    return sw.toString()
}
