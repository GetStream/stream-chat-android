/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress(
    "ArrayInDataClass",
    "EnumEntryName",
    "RemoveRedundantQualifierName",
    "UnusedImport",
)

package io.getstream.chat.android.network.models

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson

/**
 * [All possibility of string to use]
 */
/**
 * ChannelOwnCapability Enum
 */
internal sealed class ChannelOwnCapability(internal val value: String) {
    override fun toString(): String = value

    internal companion object {
        internal fun fromString(s: String): ChannelOwnCapability = when (s) {
            "ban-channel-members" -> BanChannelMembers
            "cast-poll-vote" -> CastPollVote
            "connect-events" -> ConnectEvents
            "create-attachment" -> CreateAttachment
            "create-mention" -> CreateMention
            "delete-any-message" -> DeleteAnyMessage
            "delete-channel" -> DeleteChannel
            "delete-own-message" -> DeleteOwnMessage
            "delivery-events" -> DeliveryEvents
            "flag-message" -> FlagMessage
            "freeze-channel" -> FreezeChannel
            "join-channel" -> JoinChannel
            "leave-channel" -> LeaveChannel
            "mute-channel" -> MuteChannel
            "notify-channel" -> NotifyChannel
            "notify-group" -> NotifyGroup
            "notify-here" -> NotifyHere
            "notify-role" -> NotifyRole
            "pin-message" -> PinMessage
            "query-poll-votes" -> QueryPollVotes
            "quote-message" -> QuoteMessage
            "read-events" -> ReadEvents
            "search-messages" -> SearchMessages
            "send-custom-events" -> SendCustomEvents
            "send-links" -> SendLinks
            "send-message" -> SendMessage
            "send-poll" -> SendPoll
            "send-reaction" -> SendReaction
            "send-reply" -> SendReply
            "send-restricted-visibility-message" -> SendRestrictedVisibilityMessage
            "send-typing-events" -> SendTypingEvents
            "set-channel-cooldown" -> SetChannelCooldown
            "share-location" -> ShareLocation
            "skip-slow-mode" -> SkipSlowMode
            "slow-mode" -> SlowMode
            "typing-events" -> TypingEvents
            "update-any-message" -> UpdateAnyMessage
            "update-channel" -> UpdateChannel
            "update-channel-members" -> UpdateChannelMembers
            "update-own-message" -> UpdateOwnMessage
            "update-thread" -> UpdateThread
            "upload-file" -> UploadFile
            else -> Unknown(s)
        }
    }
    internal object BanChannelMembers : ChannelOwnCapability("ban-channel-members")
    internal object CastPollVote : ChannelOwnCapability("cast-poll-vote")
    internal object ConnectEvents : ChannelOwnCapability("connect-events")
    internal object CreateAttachment : ChannelOwnCapability("create-attachment")
    internal object CreateMention : ChannelOwnCapability("create-mention")
    internal object DeleteAnyMessage : ChannelOwnCapability("delete-any-message")
    internal object DeleteChannel : ChannelOwnCapability("delete-channel")
    internal object DeleteOwnMessage : ChannelOwnCapability("delete-own-message")
    internal object DeliveryEvents : ChannelOwnCapability("delivery-events")
    internal object FlagMessage : ChannelOwnCapability("flag-message")
    internal object FreezeChannel : ChannelOwnCapability("freeze-channel")
    internal object JoinChannel : ChannelOwnCapability("join-channel")
    internal object LeaveChannel : ChannelOwnCapability("leave-channel")
    internal object MuteChannel : ChannelOwnCapability("mute-channel")
    internal object NotifyChannel : ChannelOwnCapability("notify-channel")
    internal object NotifyGroup : ChannelOwnCapability("notify-group")
    internal object NotifyHere : ChannelOwnCapability("notify-here")
    internal object NotifyRole : ChannelOwnCapability("notify-role")
    internal object PinMessage : ChannelOwnCapability("pin-message")
    internal object QueryPollVotes : ChannelOwnCapability("query-poll-votes")
    internal object QuoteMessage : ChannelOwnCapability("quote-message")
    internal object ReadEvents : ChannelOwnCapability("read-events")
    internal object SearchMessages : ChannelOwnCapability("search-messages")
    internal object SendCustomEvents : ChannelOwnCapability("send-custom-events")
    internal object SendLinks : ChannelOwnCapability("send-links")
    internal object SendMessage : ChannelOwnCapability("send-message")
    internal object SendPoll : ChannelOwnCapability("send-poll")
    internal object SendReaction : ChannelOwnCapability("send-reaction")
    internal object SendReply : ChannelOwnCapability("send-reply")
    internal object SendRestrictedVisibilityMessage : ChannelOwnCapability("send-restricted-visibility-message")
    internal object SendTypingEvents : ChannelOwnCapability("send-typing-events")
    internal object SetChannelCooldown : ChannelOwnCapability("set-channel-cooldown")
    internal object ShareLocation : ChannelOwnCapability("share-location")
    internal object SkipSlowMode : ChannelOwnCapability("skip-slow-mode")
    internal object SlowMode : ChannelOwnCapability("slow-mode")
    internal object TypingEvents : ChannelOwnCapability("typing-events")
    internal object UpdateAnyMessage : ChannelOwnCapability("update-any-message")
    internal object UpdateChannel : ChannelOwnCapability("update-channel")
    internal object UpdateChannelMembers : ChannelOwnCapability("update-channel-members")
    internal object UpdateOwnMessage : ChannelOwnCapability("update-own-message")
    internal object UpdateThread : ChannelOwnCapability("update-thread")
    internal object UploadFile : ChannelOwnCapability("upload-file")
    internal data class Unknown(val unknownValue: String) : ChannelOwnCapability(unknownValue)

    internal class ChannelOwnCapabilityAdapter : JsonAdapter<ChannelOwnCapability>() {
        @FromJson
        override fun fromJson(reader: JsonReader): ChannelOwnCapability? {
            val s = reader.nextString() ?: return null
            return ChannelOwnCapability.fromString(s)
        }

        @ToJson
        override fun toJson(writer: JsonWriter, value: ChannelOwnCapability?) {
            writer.value(value?.value)
        }
    }
}
