package io.getstream.logging

public typealias Priority = Int

public interface StreamLogger {

    public companion object Level {
        public const val VERBOSE: Priority = 2
        public const val DEBUG: Priority = 3
        public const val INFO: Priority = 4
        public const val WARN: Priority = 5
        public const val ERROR: Priority = 6
        public const val ASSERT: Priority = 7
    }

    public fun log(priority: Priority, tag: String, throwable: Throwable?, message: String, args: Array<out Any?>?)
}

public object SilentStreamLogger : StreamLogger {

    override fun log(priority: Priority, tag: String, throwable: Throwable?, message: String, args: Array<out Any?>?) {}
}
