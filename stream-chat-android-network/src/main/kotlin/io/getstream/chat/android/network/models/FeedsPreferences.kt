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
 * 
 */

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class FeedsPreferences (
    @Json(name = "comment")
    val comment: Comment? = null,

    @Json(name = "comment_mention")
    val commentMention: CommentMention? = null,

    @Json(name = "comment_reaction")
    val commentReaction: CommentReaction? = null,

    @Json(name = "comment_reply")
    val commentReply: CommentReply? = null,

    @Json(name = "follow")
    val follow: Follow? = null,

    @Json(name = "mention")
    val mention: Mention? = null,

    @Json(name = "reaction")
    val reaction: Reaction? = null,

    @Json(name = "custom_activity_types")
    val customActivityTypes: kotlin.collections.Map<kotlin.String, kotlin.String>? = emptyMap()
)
{
    
    /**
    * Comment Enum
    */
    sealed class Comment(val value: kotlin.String) {
            override fun toString(): String = value

            companion object {
                fun fromString(s: kotlin.String): Comment = when (s) {
                    "all" -> All
                    "none" -> None
                    else -> Unknown(s)
                }
            }
            object All : Comment("all")
            object None : Comment("none")
            data class Unknown(val unknownValue: kotlin.String) : Comment(unknownValue)
        

        class CommentAdapter : JsonAdapter<Comment>() {
            @FromJson
            override fun fromJson(reader: JsonReader): Comment? {
                val s = reader.nextString() ?: return null
                return Comment.fromString(s)
            }

            @ToJson
            override fun toJson(writer: JsonWriter, value: Comment?) {
                writer.value(value?.value)
            }
        }
    }
    /**
    * CommentMention Enum
    */
    sealed class CommentMention(val value: kotlin.String) {
            override fun toString(): String = value

            companion object {
                fun fromString(s: kotlin.String): CommentMention = when (s) {
                    "all" -> All
                    "none" -> None
                    else -> Unknown(s)
                }
            }
            object All : CommentMention("all")
            object None : CommentMention("none")
            data class Unknown(val unknownValue: kotlin.String) : CommentMention(unknownValue)
        

        class CommentMentionAdapter : JsonAdapter<CommentMention>() {
            @FromJson
            override fun fromJson(reader: JsonReader): CommentMention? {
                val s = reader.nextString() ?: return null
                return CommentMention.fromString(s)
            }

            @ToJson
            override fun toJson(writer: JsonWriter, value: CommentMention?) {
                writer.value(value?.value)
            }
        }
    }
    /**
    * CommentReaction Enum
    */
    sealed class CommentReaction(val value: kotlin.String) {
            override fun toString(): String = value

            companion object {
                fun fromString(s: kotlin.String): CommentReaction = when (s) {
                    "all" -> All
                    "none" -> None
                    else -> Unknown(s)
                }
            }
            object All : CommentReaction("all")
            object None : CommentReaction("none")
            data class Unknown(val unknownValue: kotlin.String) : CommentReaction(unknownValue)
        

        class CommentReactionAdapter : JsonAdapter<CommentReaction>() {
            @FromJson
            override fun fromJson(reader: JsonReader): CommentReaction? {
                val s = reader.nextString() ?: return null
                return CommentReaction.fromString(s)
            }

            @ToJson
            override fun toJson(writer: JsonWriter, value: CommentReaction?) {
                writer.value(value?.value)
            }
        }
    }
    /**
    * CommentReply Enum
    */
    sealed class CommentReply(val value: kotlin.String) {
            override fun toString(): String = value

            companion object {
                fun fromString(s: kotlin.String): CommentReply = when (s) {
                    "all" -> All
                    "none" -> None
                    else -> Unknown(s)
                }
            }
            object All : CommentReply("all")
            object None : CommentReply("none")
            data class Unknown(val unknownValue: kotlin.String) : CommentReply(unknownValue)
        

        class CommentReplyAdapter : JsonAdapter<CommentReply>() {
            @FromJson
            override fun fromJson(reader: JsonReader): CommentReply? {
                val s = reader.nextString() ?: return null
                return CommentReply.fromString(s)
            }

            @ToJson
            override fun toJson(writer: JsonWriter, value: CommentReply?) {
                writer.value(value?.value)
            }
        }
    }
    /**
    * Follow Enum
    */
    sealed class Follow(val value: kotlin.String) {
            override fun toString(): String = value

            companion object {
                fun fromString(s: kotlin.String): Follow = when (s) {
                    "all" -> All
                    "none" -> None
                    else -> Unknown(s)
                }
            }
            object All : Follow("all")
            object None : Follow("none")
            data class Unknown(val unknownValue: kotlin.String) : Follow(unknownValue)
        

        class FollowAdapter : JsonAdapter<Follow>() {
            @FromJson
            override fun fromJson(reader: JsonReader): Follow? {
                val s = reader.nextString() ?: return null
                return Follow.fromString(s)
            }

            @ToJson
            override fun toJson(writer: JsonWriter, value: Follow?) {
                writer.value(value?.value)
            }
        }
    }
    /**
    * Mention Enum
    */
    sealed class Mention(val value: kotlin.String) {
            override fun toString(): String = value

            companion object {
                fun fromString(s: kotlin.String): Mention = when (s) {
                    "all" -> All
                    "none" -> None
                    else -> Unknown(s)
                }
            }
            object All : Mention("all")
            object None : Mention("none")
            data class Unknown(val unknownValue: kotlin.String) : Mention(unknownValue)
        

        class MentionAdapter : JsonAdapter<Mention>() {
            @FromJson
            override fun fromJson(reader: JsonReader): Mention? {
                val s = reader.nextString() ?: return null
                return Mention.fromString(s)
            }

            @ToJson
            override fun toJson(writer: JsonWriter, value: Mention?) {
                writer.value(value?.value)
            }
        }
    }
    /**
    * Reaction Enum
    */
    sealed class Reaction(val value: kotlin.String) {
            override fun toString(): String = value

            companion object {
                fun fromString(s: kotlin.String): Reaction = when (s) {
                    "all" -> All
                    "none" -> None
                    else -> Unknown(s)
                }
            }
            object All : Reaction("all")
            object None : Reaction("none")
            data class Unknown(val unknownValue: kotlin.String) : Reaction(unknownValue)
        

        class ReactionAdapter : JsonAdapter<Reaction>() {
            @FromJson
            override fun fromJson(reader: JsonReader): Reaction? {
                val s = reader.nextString() ?: return null
                return Reaction.fromString(s)
            }

            @ToJson
            override fun toJson(writer: JsonWriter, value: Reaction?) {
                writer.value(value?.value)
            }
        }
    }    
}
