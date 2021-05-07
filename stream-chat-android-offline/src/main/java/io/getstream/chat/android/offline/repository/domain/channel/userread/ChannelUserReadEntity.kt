package io.getstream.chat.android.offline.repository.domain.channel.userread

import java.util.Date

/**
 * Efficiently store the channel user read info
 */
internal data class ChannelUserReadEntity(val userId: String, val lastRead: Date?, val unreadMessages: Int)
