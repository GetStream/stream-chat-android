package io.getstream.chat.android.models

/**
 * A transformer that can be used to transform a message object before third parties can access it.
 * This is useful for adding extra data to the message object and/or encrypting/decrypting it.
 */
public fun interface MessageTransformer {

    /**
     * Transforms the [message] before returning it to the caller.
     * This can be used to add extra data to the message object and/or encrypt/decrypt it.
     *
     * @return The transformed message.
     */
    public fun transform(message: Message): Message
}

/**
 * A no-op implementation of [MessageTransformer].
 */
public object NoOpMessageTransformer : MessageTransformer {

    /**
     * Returns the [message] as is.
     */
    override fun transform(message: Message): Message = message
}