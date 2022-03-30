package io.getstream.logging

public class CompositeStreamLogger(
    public val children: List<StreamLogger>,
) : StreamLogger {

    public constructor(vararg children: StreamLogger) : this(children.toList())

    override fun log(
        priority: Priority,
        tag: String,
        throwable: Throwable?,
        message: String,
        args: Array<out Any?>?,
    ) {
        children.forEach { childLogger ->
            childLogger.log(priority, tag, throwable, message, args)
        }
    }
}
