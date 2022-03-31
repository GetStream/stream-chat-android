package io.getstream.logging

public class CompositeStreamLogger(
    private val children: List<StreamLogger>,
) : StreamLogger {

    public constructor(vararg children: StreamLogger) : this(children.toList())

    override fun log(
        priority: Priority,
        tag: String,
        message: () -> String,
        throwable: Throwable?,
    ) {
        children.forEach { childLogger ->
            childLogger.log(priority, tag, message, throwable)
        }
    }
}
