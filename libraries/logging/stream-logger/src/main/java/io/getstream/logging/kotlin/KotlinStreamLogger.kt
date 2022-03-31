package io.getstream.logging.kotlin

import io.getstream.logging.Priority
import io.getstream.logging.StreamLogger
import io.getstream.logging.StreamLogger.Level.ASSERT
import io.getstream.logging.StreamLogger.Level.ERROR
import io.getstream.logging.helper.stringify
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * The [StreamLogger] implementation for kotlin projects. Mainly used in Unit Tests.
 */
public class KotlinStreamLogger(
    private val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss''SSS", Locale.ENGLISH),
) : StreamLogger {

    override fun log(priority: Priority, tag: String, message: () -> String, throwable: Throwable?) {
        val now = dateFormat.format(now())
        val thread = Thread.currentThread().run { "$name:$id" }
        val composed = "$now ($thread) [${priority.stringify()}/$tag]: ${message()}"
        val finalMessage = throwable?.let {
            "$composed\n${it.stringify()}"
        } ?: composed
        when (priority) {
            ERROR, ASSERT -> System.err.println(finalMessage)
            else -> println(finalMessage)
        }
    }

    private fun now() = System.currentTimeMillis()
}
