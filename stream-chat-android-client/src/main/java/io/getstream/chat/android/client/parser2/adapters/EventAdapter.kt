package io.getstream.chat.android.client.parser2.adapters

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.rawType
import io.getstream.chat.android.client.api2.model.dto.ChannelCreatedEventDto
import io.getstream.chat.android.client.api2.model.dto.ChannelDeletedEventDto
import io.getstream.chat.android.client.api2.model.dto.ChannelHiddenEventDto
import io.getstream.chat.android.client.api2.model.dto.ChannelMuteEventDto
import io.getstream.chat.android.client.api2.model.dto.ChannelTruncatedEventDto
import io.getstream.chat.android.client.api2.model.dto.ChannelUnmuteEventDto
import io.getstream.chat.android.client.api2.model.dto.ChannelUpdatedByUserEventDto
import io.getstream.chat.android.client.api2.model.dto.ChannelUpdatedEventDto
import io.getstream.chat.android.client.api2.model.dto.ChannelUserBannedEventDto
import io.getstream.chat.android.client.api2.model.dto.ChannelUserUnbannedEventDto
import io.getstream.chat.android.client.api2.model.dto.ChannelVisibleEventDto
import io.getstream.chat.android.client.api2.model.dto.ChannelsMuteEventDto
import io.getstream.chat.android.client.api2.model.dto.ChannelsUnmuteEventDto
import io.getstream.chat.android.client.api2.model.dto.ChatEventDto
import io.getstream.chat.android.client.api2.model.dto.ConnectedEventDto
import io.getstream.chat.android.client.api2.model.dto.GlobalUserBannedEventDto
import io.getstream.chat.android.client.api2.model.dto.GlobalUserUnbannedEventDto
import io.getstream.chat.android.client.api2.model.dto.HealthEventDto
import io.getstream.chat.android.client.api2.model.dto.MarkAllReadEventDto
import io.getstream.chat.android.client.api2.model.dto.MemberAddedEventDto
import io.getstream.chat.android.client.api2.model.dto.MemberRemovedEventDto
import io.getstream.chat.android.client.api2.model.dto.MemberUpdatedEventDto
import io.getstream.chat.android.client.api2.model.dto.MessageDeletedEventDto
import io.getstream.chat.android.client.api2.model.dto.MessageReadEventDto
import io.getstream.chat.android.client.api2.model.dto.MessageUpdatedEventDto
import io.getstream.chat.android.client.api2.model.dto.NewMessageEventDto
import io.getstream.chat.android.client.api2.model.dto.NotificationAddedToChannelEventDto
import io.getstream.chat.android.client.api2.model.dto.NotificationChannelDeletedEventDto
import io.getstream.chat.android.client.api2.model.dto.NotificationChannelMutesUpdatedEventDto
import io.getstream.chat.android.client.api2.model.dto.NotificationChannelTruncatedEventDto
import io.getstream.chat.android.client.api2.model.dto.NotificationInviteAcceptedEventDto
import io.getstream.chat.android.client.api2.model.dto.NotificationInvitedEventDto
import io.getstream.chat.android.client.api2.model.dto.NotificationMarkReadEventDto
import io.getstream.chat.android.client.api2.model.dto.NotificationMessageNewEventDto
import io.getstream.chat.android.client.api2.model.dto.NotificationMutesUpdatedEventDto
import io.getstream.chat.android.client.api2.model.dto.NotificationRemovedFromChannelEventDto
import io.getstream.chat.android.client.api2.model.dto.ReactionDeletedEventDto
import io.getstream.chat.android.client.api2.model.dto.ReactionNewEventDto
import io.getstream.chat.android.client.api2.model.dto.ReactionUpdateEventDto
import io.getstream.chat.android.client.api2.model.dto.TypingStartEventDto
import io.getstream.chat.android.client.api2.model.dto.TypingStopEventDto
import io.getstream.chat.android.client.api2.model.dto.UnknownEventDto
import io.getstream.chat.android.client.api2.model.dto.UserDeletedEventDto
import io.getstream.chat.android.client.api2.model.dto.UserMutedEventDto
import io.getstream.chat.android.client.api2.model.dto.UserPresenceChangedEventDto
import io.getstream.chat.android.client.api2.model.dto.UserStartWatchingEventDto
import io.getstream.chat.android.client.api2.model.dto.UserStopWatchingEventDto
import io.getstream.chat.android.client.api2.model.dto.UserUnmutedEventDto
import io.getstream.chat.android.client.api2.model.dto.UserUpdatedEventDto
import io.getstream.chat.android.client.api2.model.dto.UsersMutedEventDto
import io.getstream.chat.android.client.api2.model.dto.UsersUnmutedEventDto
import io.getstream.chat.android.client.models.EventType
import java.lang.reflect.Type
import java.util.Date

internal class EventAdapterFactory : JsonAdapter.Factory {
    override fun create(type: Type, annotations: MutableSet<out Annotation>, moshi: Moshi): JsonAdapter<*>? {
        return when (type.rawType) {
            ChatEventDto::class.java -> EventDtoAdapter(moshi)
            else -> null
        }
    }
}

internal class EventDtoAdapter(
    private val moshi: Moshi,
) : JsonAdapter<ChatEventDto>() {

    private val mapAdapter: JsonAdapter<MutableMap<String, Any?>> =
        moshi.adapter(Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java))

    private val connectedEventAdapter = moshi.adapter(ConnectedEventDto::class.java)
    private val healthEventAdapter = moshi.adapter(HealthEventDto::class.java)
    private val newMessageEventAdapter = moshi.adapter(NewMessageEventDto::class.java)
    private val messageDeletedEventAdapter = moshi.adapter(MessageDeletedEventDto::class.java)
    private val messageUpdatedEventAdapter = moshi.adapter(MessageUpdatedEventDto::class.java)
    private val messageReadEventAdapter = moshi.adapter(MessageReadEventDto::class.java)
    private val typingStartEventAdapter = moshi.adapter(TypingStartEventDto::class.java)
    private val typingStopEventAdapter = moshi.adapter(TypingStopEventDto::class.java)
    private val reactionNewEventAdapter = moshi.adapter(ReactionNewEventDto::class.java)
    private val reactionUpdateEventAdapter = moshi.adapter(ReactionUpdateEventDto::class.java)
    private val reactionDeletedEventAdapter = moshi.adapter(ReactionDeletedEventDto::class.java)
    private val memberAddedEventAdapter = moshi.adapter(MemberAddedEventDto::class.java)
    private val memberRemovedEventAdapter = moshi.adapter(MemberRemovedEventDto::class.java)
    private val memberUpdatedEventAdapter = moshi.adapter(MemberUpdatedEventDto::class.java)
    private val channelCreatedEventAdapter = moshi.adapter(ChannelCreatedEventDto::class.java)
    private val channelUpdatedByUserEventAdapter = moshi.adapter(ChannelUpdatedByUserEventDto::class.java)
    private val channelUpdatedEventAdapter = moshi.adapter(ChannelUpdatedEventDto::class.java)
    private val channelHiddenEventAdapter = moshi.adapter(ChannelHiddenEventDto::class.java)
    private val channelMuteEventAdapter = moshi.adapter(ChannelMuteEventDto::class.java)
    private val channelsMuteEventAdapter = moshi.adapter(ChannelsMuteEventDto::class.java)
    private val channelUnmuteEventAdapter = moshi.adapter(ChannelUnmuteEventDto::class.java)
    private val channelsUnmuteEventAdapter = moshi.adapter(ChannelsUnmuteEventDto::class.java)
    private val channelDeletedEventAdapter = moshi.adapter(ChannelDeletedEventDto::class.java)
    private val channelVisibleEventAdapter = moshi.adapter(ChannelVisibleEventDto::class.java)
    private val channelTruncatedEventAdapter = moshi.adapter(ChannelTruncatedEventDto::class.java)
    private val userStartWatchingEventAdapter = moshi.adapter(UserStartWatchingEventDto::class.java)
    private val userStopWatchingEventAdapter = moshi.adapter(UserStopWatchingEventDto::class.java)
    private val notificationAddedToChannelEventAdapter = moshi.adapter(NotificationAddedToChannelEventDto::class.java)
    private val notificationMarkReadEventAdapter = moshi.adapter(NotificationMarkReadEventDto::class.java)
    private val markAllReadEventAdapter = moshi.adapter(MarkAllReadEventDto::class.java)
    private val notificationMessageNewEventAdapter = moshi.adapter(NotificationMessageNewEventDto::class.java)
    private val notificationInvitedEventAdapter = moshi.adapter(NotificationInvitedEventDto::class.java)
    private val notificationInviteAcceptedEventAdapter = moshi.adapter(NotificationInviteAcceptedEventDto::class.java)
    private val notificationRemovedFromChannelEventAdapter =
        moshi.adapter(NotificationRemovedFromChannelEventDto::class.java)
    private val notificationMutesUpdatedEventAdapter = moshi.adapter(NotificationMutesUpdatedEventDto::class.java)
    private val notificationChannelMutesUpdatedEventAdapter =
        moshi.adapter(NotificationChannelMutesUpdatedEventDto::class.java)
    private val notificationChannelDeletedEventAdapter = moshi.adapter(NotificationChannelDeletedEventDto::class.java)
    private val notificationChannelTruncatedEventAdapter =
        moshi.adapter(NotificationChannelTruncatedEventDto::class.java)
    private val userPresenceChangedEventAdapter = moshi.adapter(UserPresenceChangedEventDto::class.java)
    private val userUpdatedEventAdapter = moshi.adapter(UserUpdatedEventDto::class.java)
    private val userDeletedEventAdapter = moshi.adapter(UserDeletedEventDto::class.java)
    private val userMutedEventAdapter = moshi.adapter(UserMutedEventDto::class.java)
    private val usersMutedEventAdapter = moshi.adapter(UsersMutedEventDto::class.java)
    private val userUnmutedEventAdapter = moshi.adapter(UserUnmutedEventDto::class.java)
    private val usersUnmutedEventAdapter = moshi.adapter(UsersUnmutedEventDto::class.java)
    private val channelUserBannedEventAdapter = moshi.adapter(ChannelUserBannedEventDto::class.java)
    private val globalUserBannedEventAdapter = moshi.adapter(GlobalUserBannedEventDto::class.java)
    private val channelUserUnbannedEventAdapter = moshi.adapter(ChannelUserUnbannedEventDto::class.java)
    private val globalUserUnbannedEventAdapter = moshi.adapter(GlobalUserUnbannedEventDto::class.java)

    override fun fromJson(reader: JsonReader): ChatEventDto? {
        if (reader.peek() == JsonReader.Token.NULL) {
            reader.nextNull<Nothing?>()
            return null
        }

        val map: MutableMap<String, Any?> = mapAdapter.fromJson(reader)!!

        val adapter = when (val type = map["type"] as? String) {
            EventType.HEALTH_CHECK -> when {
                map.containsKey("me") -> connectedEventAdapter
                else -> healthEventAdapter
            }
            EventType.MESSAGE_NEW -> newMessageEventAdapter
            EventType.MESSAGE_DELETED -> messageDeletedEventAdapter
            EventType.MESSAGE_UPDATED -> messageUpdatedEventAdapter
            EventType.MESSAGE_READ -> when {
                map.containsKey("cid") -> messageReadEventAdapter
                else -> markAllReadEventAdapter
            }
            EventType.TYPING_START -> typingStartEventAdapter
            EventType.TYPING_STOP -> typingStopEventAdapter
            EventType.REACTION_NEW -> reactionNewEventAdapter
            EventType.REACTION_UPDATED -> reactionUpdateEventAdapter
            EventType.REACTION_DELETED -> reactionDeletedEventAdapter
            EventType.MEMBER_ADDED -> memberAddedEventAdapter
            EventType.MEMBER_REMOVED -> memberRemovedEventAdapter
            EventType.MEMBER_UPDATED -> memberUpdatedEventAdapter
            EventType.CHANNEL_CREATED -> channelCreatedEventAdapter
            EventType.CHANNEL_UPDATED -> when {
                map.containsKey("user") -> channelUpdatedByUserEventAdapter
                else -> channelUpdatedEventAdapter
            }
            EventType.CHANNEL_HIDDEN -> channelHiddenEventAdapter
            EventType.CHANNEL_MUTED -> when {
                map.containsKey("mute") -> channelMuteEventAdapter
                else -> channelsMuteEventAdapter
            }
            EventType.CHANNEL_UNMUTED -> when {
                map.containsKey("mute") -> channelUnmuteEventAdapter
                else -> channelsUnmuteEventAdapter
            }
            EventType.CHANNEL_DELETED -> channelDeletedEventAdapter
            EventType.CHANNEL_VISIBLE -> channelVisibleEventAdapter
            EventType.CHANNEL_TRUNCATED -> channelTruncatedEventAdapter
            EventType.USER_WATCHING_START -> userStartWatchingEventAdapter
            EventType.USER_WATCHING_STOP -> userStopWatchingEventAdapter
            EventType.NOTIFICATION_ADDED_TO_CHANNEL -> notificationAddedToChannelEventAdapter
            EventType.NOTIFICATION_MARK_READ -> when {
                map.containsKey("cid") -> notificationMarkReadEventAdapter
                else -> markAllReadEventAdapter
            }
            EventType.NOTIFICATION_MESSAGE_NEW -> notificationMessageNewEventAdapter
            EventType.NOTIFICATION_INVITED -> notificationInvitedEventAdapter
            EventType.NOTIFICATION_INVITE_ACCEPTED -> notificationInviteAcceptedEventAdapter
            EventType.NOTIFICATION_REMOVED_FROM_CHANNEL -> notificationRemovedFromChannelEventAdapter
            EventType.NOTIFICATION_MUTES_UPDATED -> notificationMutesUpdatedEventAdapter
            EventType.NOTIFICATION_CHANNEL_MUTES_UPDATED -> notificationChannelMutesUpdatedEventAdapter
            EventType.NOTIFICATION_CHANNEL_DELETED -> notificationChannelDeletedEventAdapter
            EventType.NOTIFICATION_CHANNEL_TRUNCATED -> notificationChannelTruncatedEventAdapter
            EventType.USER_PRESENCE_CHANGED -> userPresenceChangedEventAdapter
            EventType.USER_UPDATED -> userUpdatedEventAdapter
            EventType.USER_DELETED -> userDeletedEventAdapter
            EventType.USER_MUTED -> when {
                map.containsKey("target_user") -> userMutedEventAdapter
                else -> usersMutedEventAdapter
            }
            EventType.USER_UNMUTED -> when {
                map.containsKey("target_user") -> userUnmutedEventAdapter
                else -> usersUnmutedEventAdapter
            }
            EventType.USER_BANNED -> when {
                map.containsKey("cid") -> channelUserBannedEventAdapter
                else -> globalUserBannedEventAdapter
            }
            EventType.USER_UNBANNED -> when {
                map.containsKey("cid") -> channelUserUnbannedEventAdapter
                else -> globalUserUnbannedEventAdapter
            }
            else -> // Custom case, early return
                return UnknownEventDto(
                    type = type ?: EventType.UNKNOWN,
                    created_at = moshi.adapter(Date::class.java).fromJsonValue(map["created_at"])!!,
                    rawData = map
                )
        }

        return adapter.fromJsonValue(map)
    }

    override fun toJson(writer: JsonWriter, value: ChatEventDto?) {
        error("Can't convert this to Json")
    }
}
