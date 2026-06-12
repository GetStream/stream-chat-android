/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-video-android/blob/main/LICENSE
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
    "UnusedImport"
)

package io.getstream.chat.android.network.models

import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.*
import kotlin.io.*
import com.squareup.moshi.FromJson
import com.squareup.moshi.Json
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
sealed class ChannelOwnCapability(val value: kotlin.String) {
    override fun toString(): String = value

    companion object {
        fun fromString(s: kotlin.String): ChannelOwnCapability = when (s) {
            "ban-channel-members" -> BanChannelMembers
            "cast-poll-vote" -> CastPollVote
            "connect-events" -> ConnectEvents
            "create-attachment" -> CreateAttachment
            "delete-any-message" -> DeleteAnyMessage
            "delete-channel" -> DeleteChannel
            "delete-own-message" -> DeleteOwnMessage
            "delivery-events" -> DeliveryEvents
            "flag-message" -> FlagMessage
            "freeze-channel" -> FreezeChannel
            "join-channel" -> JoinChannel
            "leave-channel" -> LeaveChannel
            "mute-channel" -> MuteChannel
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
    object BanChannelMembers : ChannelOwnCapability("ban-channel-members")
    object CastPollVote : ChannelOwnCapability("cast-poll-vote")
    object ConnectEvents : ChannelOwnCapability("connect-events")
    object CreateAttachment : ChannelOwnCapability("create-attachment")
    object DeleteAnyMessage : ChannelOwnCapability("delete-any-message")
    object DeleteChannel : ChannelOwnCapability("delete-channel")
    object DeleteOwnMessage : ChannelOwnCapability("delete-own-message")
    object DeliveryEvents : ChannelOwnCapability("delivery-events")
    object FlagMessage : ChannelOwnCapability("flag-message")
    object FreezeChannel : ChannelOwnCapability("freeze-channel")
    object JoinChannel : ChannelOwnCapability("join-channel")
    object LeaveChannel : ChannelOwnCapability("leave-channel")
    object MuteChannel : ChannelOwnCapability("mute-channel")
    object PinMessage : ChannelOwnCapability("pin-message")
    object QueryPollVotes : ChannelOwnCapability("query-poll-votes")
    object QuoteMessage : ChannelOwnCapability("quote-message")
    object ReadEvents : ChannelOwnCapability("read-events")
    object SearchMessages : ChannelOwnCapability("search-messages")
    object SendCustomEvents : ChannelOwnCapability("send-custom-events")
    object SendLinks : ChannelOwnCapability("send-links")
    object SendMessage : ChannelOwnCapability("send-message")
    object SendPoll : ChannelOwnCapability("send-poll")
    object SendReaction : ChannelOwnCapability("send-reaction")
    object SendReply : ChannelOwnCapability("send-reply")
    object SendRestrictedVisibilityMessage : ChannelOwnCapability("send-restricted-visibility-message")
    object SendTypingEvents : ChannelOwnCapability("send-typing-events")
    object SetChannelCooldown : ChannelOwnCapability("set-channel-cooldown")
    object ShareLocation : ChannelOwnCapability("share-location")
    object SkipSlowMode : ChannelOwnCapability("skip-slow-mode")
    object SlowMode : ChannelOwnCapability("slow-mode")
    object TypingEvents : ChannelOwnCapability("typing-events")
    object UpdateAnyMessage : ChannelOwnCapability("update-any-message")
    object UpdateChannel : ChannelOwnCapability("update-channel")
    object UpdateChannelMembers : ChannelOwnCapability("update-channel-members")
    object UpdateOwnMessage : ChannelOwnCapability("update-own-message")
    object UpdateThread : ChannelOwnCapability("update-thread")
    object UploadFile : ChannelOwnCapability("upload-file")
    data class Unknown(val unknownValue: kotlin.String) : ChannelOwnCapability(unknownValue)


    class ChannelOwnCapabilityAdapter : JsonAdapter<ChannelOwnCapability>() {
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
