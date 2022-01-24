package io.getstream.chat.android.client.models

/**
 * Predefined list of channel capabilities constants.
 * Channel capabilities provide you information on which features are available for the current user.
 * Channel capabilities are affected by a number of factors including:
 * * User Permissions
 * * Channel Type settings
 * * Channel-level settings
 *
 * Capabilities are the preferred way of deciding which actions should be available in the user interface.
 */
public object ChannelCapabilities {
    /** Ability to ban channel members. */
    public const val BanChannelMembers: String = "ban-channel-members"
    /** Ability to receive connect events. */
    public const val ConnectEvents: String = "connect-events"
    /** Ability to delete any message from the channel. */
    public const val DeleteAnyMessage: String = "delete-any-message"
    /** Ability to delete the channel. */
    public const val DeleteChannel: String = "delete-channel"
    /** Ability to delete own messages from the channel. */
    public const val DeleteOwnMessage: String = "delete-own-message"
    /** Ability to flag a message. */
    public const val FlagMessage: String = "flag-message"
    /** Ability to freeze or unfreeze the channel. */
    public const val FreezeChannel: String = "freeze-channel"
    /** Ability to leave the channel (remove own membership). */
    public const val LeaveChannel: String = "leave-channel"
    /** Ability to mute the channel. */
    public const val MuteChannel: String = "mute-channel"
    /** Ability to pin a message. */
    public const val PinMessage: String = "pin-message"
    /** Ability to quote a message. */
    public const val QuoteMessage: String = "quote-message"
    /** Ability to receive read events. */
    public const val ReadEvents: String = "read-events"
    /** Ability to use message search. */
    public const val SearchMessages: String = "search-messages"
    /** Ability to send custom events. */
    public const val SendCustomEvents: String = "send-custom-events"
    /** Ability to attach links to messages. */
    public const val SendLinks: String = "send-links"
    /** Ability to send a message. */
    public const val SendMessage: String = "send-message"
    /** Ability to send reactions. */
    public const val SendReaction: String = "send-reaction"
    /** Ability to thread reply to a message. */
    public const val SendReply: String = "send-reply"
    /** Ability to enable or disable slow mode. */
    public const val SetChannelCooldown: String = "set-channel-cooldown"
    /** Ability to send and receive typing events. */
    public const val TypingEvents: String = "typing-events"
    /** Ability to update any message in the channel. */
    public const val UpdateAnyMessage: String = "update-any-message"
    /** Ability to update channel data. */
    public const val UpdateChannel: String = "update-channel"
    /** Ability to update channel members. */
    public const val UpdateChannelMembers: String = "update-channel-members"
    /** Ability to update own messages in the channel. */
    public const val UpdateOwnMessage: String = "update-own-message"
    /** Ability to upload message attachments. */
    public const val UploadFile: String = "upload-file"
}
