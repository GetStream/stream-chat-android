package io.getstream.chat.android.offline.repository.domain.channel.userread

import com.squareup.moshi.JsonClass
import java.util.Date

/**
 * Efficiently store the channel user read info
 */
@JsonClass(generateAdapter = true)
internal data class ChannelUserReadEntity(val userId: String, val lastRead: Date?, val unreadMessages: Int)
