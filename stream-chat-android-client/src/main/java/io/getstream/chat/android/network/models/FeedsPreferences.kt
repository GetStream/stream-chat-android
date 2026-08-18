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
import com.squareup.moshi.Json
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson

/**
 *
 */
@com.squareup.moshi.JsonClass(generateAdapter = true)
internal data class FeedsPreferences(
    @Json(name = "comment")
    internal val comment: Comment? = null,

    @Json(name = "comment_mention")
    internal val commentMention: CommentMention? = null,

    @Json(name = "comment_reaction")
    internal val commentReaction: CommentReaction? = null,

    @Json(name = "comment_reply")
    internal val commentReply: CommentReply? = null,

    @Json(name = "follow")
    internal val follow: Follow? = null,

    @Json(name = "mention")
    internal val mention: Mention? = null,

    @Json(name = "reaction")
    internal val reaction: Reaction? = null,

    @Json(name = "custom_activity_types")
    internal val customActivityTypes: Map<String, String>? = emptyMap(),
) {

    /**
     * Comment Enum
     */
    internal sealed class Comment(internal val value: String) {
        override fun toString(): String = value

        internal companion object {
            internal fun fromString(s: String): Comment = when (s) {
                "all" -> All
                "none" -> None
                else -> Unknown(s)
            }
        }
        internal object All : Comment("all")
        internal object None : Comment("none")
        internal data class Unknown(val unknownValue: String) : Comment(unknownValue)

        internal class CommentAdapter : JsonAdapter<Comment>() {
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
    internal sealed class CommentMention(internal val value: String) {
        override fun toString(): String = value

        internal companion object {
            internal fun fromString(s: String): CommentMention = when (s) {
                "all" -> All
                "none" -> None
                else -> Unknown(s)
            }
        }
        internal object All : CommentMention("all")
        internal object None : CommentMention("none")
        internal data class Unknown(val unknownValue: String) : CommentMention(unknownValue)

        internal class CommentMentionAdapter : JsonAdapter<CommentMention>() {
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
    internal sealed class CommentReaction(internal val value: String) {
        override fun toString(): String = value

        internal companion object {
            internal fun fromString(s: String): CommentReaction = when (s) {
                "all" -> All
                "none" -> None
                else -> Unknown(s)
            }
        }
        internal object All : CommentReaction("all")
        internal object None : CommentReaction("none")
        internal data class Unknown(val unknownValue: String) : CommentReaction(unknownValue)

        internal class CommentReactionAdapter : JsonAdapter<CommentReaction>() {
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
    internal sealed class CommentReply(internal val value: String) {
        override fun toString(): String = value

        internal companion object {
            internal fun fromString(s: String): CommentReply = when (s) {
                "all" -> All
                "none" -> None
                else -> Unknown(s)
            }
        }
        internal object All : CommentReply("all")
        internal object None : CommentReply("none")
        internal data class Unknown(val unknownValue: String) : CommentReply(unknownValue)

        internal class CommentReplyAdapter : JsonAdapter<CommentReply>() {
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
    internal sealed class Follow(internal val value: String) {
        override fun toString(): String = value

        internal companion object {
            internal fun fromString(s: String): Follow = when (s) {
                "all" -> All
                "none" -> None
                else -> Unknown(s)
            }
        }
        internal object All : Follow("all")
        internal object None : Follow("none")
        internal data class Unknown(val unknownValue: String) : Follow(unknownValue)

        internal class FollowAdapter : JsonAdapter<Follow>() {
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
    internal sealed class Mention(internal val value: String) {
        override fun toString(): String = value

        internal companion object {
            internal fun fromString(s: String): Mention = when (s) {
                "all" -> All
                "none" -> None
                else -> Unknown(s)
            }
        }
        internal object All : Mention("all")
        internal object None : Mention("none")
        internal data class Unknown(val unknownValue: String) : Mention(unknownValue)

        internal class MentionAdapter : JsonAdapter<Mention>() {
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
    internal sealed class Reaction(internal val value: String) {
        override fun toString(): String = value

        internal companion object {
            internal fun fromString(s: String): Reaction = when (s) {
                "all" -> All
                "none" -> None
                else -> Unknown(s)
            }
        }
        internal object All : Reaction("all")
        internal object None : Reaction("none")
        internal data class Unknown(val unknownValue: String) : Reaction(unknownValue)

        internal class ReactionAdapter : JsonAdapter<Reaction>() {
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
