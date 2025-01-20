package io.getstream.chat.android.models

/**
 * A transformer that can be used to transform a channel object before third parties can access it.
 * This is useful for adding extra data to the channel object and/or encrypting/decrypting it.
 */
public fun interface ChannelTransformer {

    /**
     * Transforms the [channel] before returning it to the caller.
     * This can be used to add extra data to the channel object and/or encrypt/decrypt it.
     *
     * @return The transformed channel.
     */
    public fun transform(channel: Channel): Channel
}

/**
 * A no-op implementation of [ChannelTransformer].
 */
public object NoOpChannelTransformer : ChannelTransformer {

    /**
     * Returns the [channel] as is.
     */
    override fun transform(channel: Channel): Channel = channel
}
