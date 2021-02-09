package io.getstream.chat.android.livedata.repository.domain.channel.userread

import java.util.Date

/**
 * Efficiently store the channel user read info
 *
 */
internal data class ChannelUserReadEntity(var userId: String, var lastRead: Date? = null)
