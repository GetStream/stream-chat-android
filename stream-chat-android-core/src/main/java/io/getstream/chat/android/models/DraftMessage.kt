/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

/**
 * Model holding data about a draft message.
 */
@Immutable
public data class DraftMessage(
    /**
     * The unique string identifier of the message. This is either created by Stream
     * or set on the client side when the message is added.
     */
    val id: String = "",

    /**
     * Channel unique identifier in <type>:<id> format
     */
    val cid: String = "",

    /**
     * The text of this message
     */
    val text: String = "",

    /**
     * The ID of the parent message, if the message is a thread reply
     */
    val parentId: String? = null,

    /**
     * The list of message attachments
     */
    val attachments: List<Attachment> = listOf(),

    /**
     * The list of user mentioned in the message
     */
    val mentionedUsersIds: List<String> = listOf(),

    /**
     * All the custom data provided for this message
     */
    override val extraData: Map<String, Any> = mapOf(),

    /**
     * Whether message is silent or not
     */
    val silent: Boolean = false,

    /**
     * Whether thread reply should be shown in the channel as well
     */
    val showInChannel: Boolean = false,

    /**
     * The ID of the quoted message, if the message is a quoted reply.
     */
    val replyMessageId: String? = null,

) : CustomObject, ComparableFieldProvider {

    @Suppress("ComplexMethod")
    override fun getComparableField(fieldName: String): Comparable<*>? =
        when (fieldName) {
            "id" -> id
            "cid" -> cid
            "text" -> text
            "parent_id", "parentId" -> parentId
            "silent" -> silent
            else -> extraData[fieldName] as? Comparable<*>
        }

    private fun <A, B> Map<A, B>.get(key: A, default: B): B {
        return get(key) ?: default
    }

    /**
     * Identifier of message. The message can't be considered the same if the id of the message AND the id of a
     * quoted message are not the same.
     */
    @Suppress("MagicNumber")
    public fun identifierHash(): Long {
        var result = id.hashCode()

        replyMessageId.hashCode().takeIf { it != 0 }?.let { replyHash ->
            result = 31 * result + replyHash
        }

        return result.toLong()
    }

    override fun toString(): String = StringBuilder().apply {
        append("Message(")
        append("id=\"").append(id).append("\"")
        append(", text=\"").append(text).append("\"")
        append(", cid=\"").append(cid).append("\"")
        if (parentId != null) append(", parentId=").append(parentId)
        if (attachments.isNotEmpty()) append(", attachments=").append(attachments)
        if (mentionedUsersIds.isNotEmpty()) append(", mentionedUsersIds=").append(mentionedUsersIds)
        append(", silent=").append(silent)
        append(", showInChannel=").append(showInChannel)
        if (replyMessageId != null) append(", replyMessageId=").append(replyMessageId)
        if (extraData.isNotEmpty()) append(", extraData=").append(extraData)
        append(")")
    }.toString()

    @SinceKotlin("99999.9")
    @Suppress("NEWER_VERSION_IN_SINCE_KOTLIN")
    public fun newBuilder(): Builder = Builder(this)

    @Suppress("TooManyFunctions")
    public class Builder() {
        private var id: String = ""
        private var cid: String = ""
        private var text: String = ""
        private var parentId: String? = null
        private var attachments: List<Attachment> = listOf()
        private var mentionedUsersIds: List<String> = listOf()
        private var extraData: Map<String, Any> = mapOf()
        private var silent: Boolean = false
        private var showInChannel: Boolean = false
        private var replyMessageId: String? = null

        public constructor(message: DraftMessage) : this() {
            id = message.id
            cid = message.cid
            text = message.text
            parentId = message.parentId
            attachments = message.attachments
            mentionedUsersIds = message.mentionedUsersIds
            extraData = message.extraData
            silent = message.silent
            showInChannel = message.showInChannel
            replyMessageId = message.replyMessageId
        }

        public fun withId(id: String): Builder = apply { this.id = id }
        public fun withCid(cid: String): Builder = apply { this.cid = cid }
        public fun withText(text: String): Builder = apply { this.text = text }
        public fun withParentId(parentId: String?): Builder = apply { this.parentId = parentId }
        public fun withAttachments(attachments: List<Attachment>): Builder = apply { this.attachments = attachments }
        public fun withMentionedUsersIds(mentionedUsersIds: List<String>): Builder = apply {
            this.mentionedUsersIds = mentionedUsersIds
        }
        public fun withExtraData(extraData: Map<String, Any>): Builder = apply { this.extraData = extraData }
        public fun withSilent(silent: Boolean): Builder = apply { this.silent = silent }
        public fun withShowInChannel(showInChannel: Boolean): Builder = apply { this.showInChannel = showInChannel }
        public fun withReplyMessageId(replyMessageId: String?): Builder = apply { this.replyMessageId = replyMessageId }

        public fun build(): DraftMessage {
            return DraftMessage(
                id = id,
                cid = cid,
                text = text,
                parentId = parentId,
                attachments = attachments,
                mentionedUsersIds = mentionedUsersIds,
                extraData = extraData,
                silent = silent,
                showInChannel = showInChannel,
                replyMessageId = replyMessageId,
            )
        }
    }
}
