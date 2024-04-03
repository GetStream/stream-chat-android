package io.getstream.chat.android.client.errors.cause

/**
 * Identifies that channel with given cid not found.
 */
public class StreamChannelNotFoundException(
    public val cid: String,
    public override val message: String = "Channel with cid \"$cid\" not found",
) : StreamException(message)