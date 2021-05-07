package io.getstream.chat.android.livedata

import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import java.util.Date
import io.getstream.chat.android.offline.channel.ChannelData as ChannelDataOffline

/**
 * A class that only stores the channel data and not all the other channel state
 * Using this prevents code bugs and issues caused by confusing the channel data vs the full channel object
 */
@Deprecated(
    "Use ChannelData from the offline package",
    replaceWith = ReplaceWith("io.getstream.chat.android.offline.channel.ChannelData")
)
public data class ChannelData internal constructor(private val channelDataOffline: ChannelDataOffline) {

    var type: String
        get() = channelDataOffline.type
        set(value) {
            channelDataOffline.type = value
        }

    var channelId: String
        get() = channelDataOffline.channelId
        set(value) {
            channelDataOffline.channelId = value
        }

    var cid: String
        get() = channelDataOffline.cid
        set(value) {
            channelDataOffline.cid = value
        }

    var createdBy: User
        get() = channelDataOffline.createdBy
        set(value) {
            channelDataOffline.createdBy = value
        }

    var cooldown: Int
        get() = channelDataOffline.cooldown
        set(value) {
            channelDataOffline.cooldown = value
        }

    var frozen: Boolean
        get() = channelDataOffline.frozen
        set(value) {
            channelDataOffline.frozen = value
        }

    var createdAt: Date?
        get() = channelDataOffline.createdAt
        set(value) {
            channelDataOffline.createdAt = value
        }

    var updatedAt: Date?
        get() = channelDataOffline.updatedAt
        set(value) {
            channelDataOffline.updatedAt = value
        }

    var deletedAt: Date?
        get() = channelDataOffline.deletedAt
        set(value) {
            channelDataOffline.deletedAt = value
        }

    var memberCount: Int
        get() = channelDataOffline.memberCount
        set(value) {
            channelDataOffline.memberCount = value
        }

    var team: String
        get() = channelDataOffline.team
        set(value) {
            channelDataOffline.team = value
        }

    var extraData: MutableMap<String, Any>
        get() = channelDataOffline.extraData
        set(value) {
            channelDataOffline.extraData = value
        }

    public constructor(
        type: String,
        channelId: String,
        cid: String = "%s:%s".format(type, channelId),
        createdBy: User = User(),
        cooldown: Int = 0,
        frozen: Boolean = false,
        createdAt: Date? = null,
        updatedAt: Date? = null,
        deletedAt: Date? = null,
        memberCount: Int = 0,
        team: String = "",
        extraData: MutableMap<String, Any> = mutableMapOf(),
    ) : this(
        ChannelDataOffline(
            type,
            channelId,
            cid,
            createdBy,
            cooldown,
            frozen,
            createdAt,
            updatedAt,
            deletedAt,
            memberCount,
            team,
            extraData
        )
    )

    /** create a ChannelData object from a Channel object */
    public constructor(c: Channel) : this(ChannelDataOffline(c))

    /** convert a channelData into a channel object */
    internal fun toChannel(
        messages: List<Message>,
        members: List<Member>,
        reads: List<ChannelUserRead>,
        watchers: List<User>,
        watcherCount: Int,
    ): Channel = channelDataOffline.toChannel(messages, members, reads, watchers, watcherCount)
}
