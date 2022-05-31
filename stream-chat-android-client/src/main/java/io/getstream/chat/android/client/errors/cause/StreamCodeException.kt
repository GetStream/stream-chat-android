package io.getstream.chat.android.client.errors.cause

public sealed class StreamCodeException : Exception {
    protected constructor() : super()
    protected constructor(message: String?) : super(message)
    protected constructor(message: String?, cause: Throwable?) : super(message, cause)
    protected constructor(cause: Throwable?) : super(cause)
}

public data class MessageModerationFailedException(
    val details: List<Detail>,
    override val message: String? = null
) : StreamCodeException(message) {

    public data class Detail(
        public val code: Int,
        public val messages: List<String>
    )

}