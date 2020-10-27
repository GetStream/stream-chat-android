package io.getstream.chat.android.client.utils

import android.util.Log

public object PerformanceUtils {
    private var tasks: Map<String, Long> = emptyMap()

    public fun startTask(taskName: String) {
        val startTime = System.currentTimeMillis()
        tasks = tasks + (taskName to startTime)

        log("Task \"$taskName\" started")
    }

    public fun stopTask(taskName: String) {
        val startTime = tasks[taskName]
        if (startTime == null) {
            log("There is no such task \"$taskName\"")
            return
        } else {
            tasks = tasks - taskName
            measure(taskName, startTime)
        }
    }

    private fun measure(taskName: String, startTime: Long) {
        val duration = (System.currentTimeMillis() - startTime) / 1000.0
        log("Task \"$taskName\" completed for $duration seconds")
    }

    public fun <T> task(taskName: String, task: () -> T): T {
        val startTime = System.currentTimeMillis()
        return task().also {
            measure(taskName, startTime)
        }
    }

    public suspend fun <T> suspendTask(taskName: String, task: suspend () -> T): T {
        val startTime = System.currentTimeMillis()
        return task().also {
            measure(taskName, startTime)
        }
    }

    public fun log(message: String) {
        Log.d("Performance", message)
    }
}
