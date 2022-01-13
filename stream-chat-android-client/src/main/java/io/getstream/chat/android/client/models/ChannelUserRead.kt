package io.getstream.chat.android.client.models

import java.util.Date

/**
 * Information about how many messages are unread in the channel by a given user
 *
 */
public data class ChannelUserRead(
    /* The user which has read some of the message and may have some unread messages */
    override var user: User,
    /* The time of the last read message */
    var lastRead: Date? = null,
    /* How many messages are unread */
    var unreadMessages: Int = 0,
    /*
    * The time of the last message that the SDK is aware of. If new messages arrive the the createdAt newer than this
    * one, that means that the count of unread messages should be incremented.
    */
    var lastMessageSeenDate: Date? = null
) : UserEntity
