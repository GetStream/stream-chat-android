/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.models

import androidx.compose.runtime.Immutable
import io.getstream.chat.android.models.querysort.ComparableFieldProvider
import java.util.Date

/**
 * Model holding data about a message reminder.
 */
@Immutable
public data class MessageReminder(

    /**
     * The date when the user should be reminded about this message. If null, this is a bookmark type reminder without a
     * notification.
     */
    public val remindAt: Date?,

    /**
     * CID of the channel in which the message is.
     */
    public val cid: String,

    /**
     * The channel in which the message belongs to.
     */
    public val channel: Channel?,

    /**
     * ID of the message for which the reminder is set.
     */
    public val messageId: String,

    /**
     * The message that has been marked for reminder.
     */
    public val message: Message?,

    /**
     * Date when the reminder was created.
     */
    public val createdAt: Date,

    /**
     * Date when the reminder was last updated.
     */
    public val updatedAt: Date,
) : ComparableFieldProvider {

    override fun getComparableField(fieldName: String): Comparable<*>? =
        when (fieldName) {
            "remind_at", "remindAt" -> remindAt
            "created_at", "createdAt" -> createdAt
            "updated_at", "updatedAt" -> updatedAt
            else -> null
        }

    /**
     * Creates a [Builder] of this [MessageReminder] instance, allowing to modify some of its properties.
     */
    @SinceKotlin("99999.9")
    @Suppress("NEWER_VERSION_IN_SINCE_KOTLIN")
    public fun newBuilder(): Builder = Builder(this)

    /**
     * Builder for [MessageReminder].
     */
    public class Builder() {
        private var remindAt: Date? = null
        private var cid: String = ""
        private var channel: Channel? = null
        private var messageId: String = ""
        private var message: Message? = null
        private var createdAt: Date = Date()
        private var updatedAt: Date = Date()

        /**
         * Creates a [Builder] from the given [MessageReminder].
         */
        public constructor(reminder: MessageReminder) : this() {
            remindAt = reminder.remindAt
            message = reminder.message
            channel = reminder.channel
            createdAt = reminder.createdAt
            updatedAt = reminder.updatedAt
        }

        /** Sets the date when the user should be reminded about this message. */
        public fun withRemindAt(remindAt: Date?): Builder = apply { this.remindAt = remindAt }

        /** Sets the CID of the channel in which the message is. */
        public fun withCid(cid: String): Builder = apply { this.cid = cid }

        /** Sets the channel in which the message belongs to. */
        public fun withChannel(channel: Channel): Builder = apply { this.channel = channel }

        /** Sets the ID of the message for which the reminder is set. */
        public fun withMessageId(messageId: String): Builder = apply { this.messageId = messageId }

        /** Sets the message that has been marked for reminder. */
        public fun withMessage(message: Message): Builder = apply { this.message = message }

        /** Sets the date when the reminder was created. */
        public fun withCreatedAt(createdAt: Date): Builder = apply { this.createdAt = createdAt }

        /** Sets the date when the reminder was last updated. */
        public fun withUpdatedAt(updatedAt: Date): Builder = apply { this.updatedAt = updatedAt }

        /**
         * Builds a [MessageReminder] instance with the provided properties.
         *
         * @throws IllegalStateException if any required property is missing.
         */
        public fun build(): MessageReminder {
            return MessageReminder(
                remindAt = remindAt,
                cid = cid,
                channel = channel,
                messageId = messageId,
                message = message,
                createdAt = createdAt,
                updatedAt = updatedAt,
            )
        }
    }
}
