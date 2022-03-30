package io.getstream.logging.android

import android.os.Build
import android.util.Log
import androidx.annotation.ChecksSdkIntAtLeast
import io.getstream.logging.Priority
import io.getstream.logging.StreamLogger
import io.getstream.logging.format.MessageFormatter
import io.getstream.logging.helper.stringify

private const val MAX_TAG_LEN = 23

public class AndroidStreamLogger : StreamLogger {

    override fun log(priority: Priority, tag: String, throwable: Throwable?, message: String, args: Array<out Any?>?) {

        val androidPriority = priority.toAndroidPriority()
        val androidTag = tag.takeIf { it.length > MAX_TAG_LEN && !isNougatOrHigher() }
            ?.substring(0, MAX_TAG_LEN)
            ?: tag

        val thread = Thread.currentThread().run { "$name:$id" }
        val formatted = MessageFormatter.formatMessage(message, args)
        val composed = "($thread) $formatted"
        val finalMessage = throwable?.let {
            "$composed\n${it.stringify()}"
        } ?: composed

        Log.println(androidPriority, androidTag, finalMessage)
    }

    private fun Priority.toAndroidPriority(): Int {
        return when (this) {
            StreamLogger.VERBOSE -> Log.VERBOSE
            StreamLogger.DEBUG -> Log.DEBUG
            StreamLogger.INFO -> Log.INFO
            StreamLogger.WARN -> Log.WARN
            StreamLogger.ERROR -> Log.ERROR
            StreamLogger.ASSERT -> Log.ASSERT
            else -> Log.ERROR
        }
    }

    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.N)
    private fun isNougatOrHigher() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
}
