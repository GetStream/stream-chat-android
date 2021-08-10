package com.getstream.sdk.chat.adapter

import com.getstream.sdk.chat.createAttachment
import com.getstream.sdk.chat.createChannelUserRead
import com.getstream.sdk.chat.createChannelUserReads
import com.getstream.sdk.chat.createMessage
import com.getstream.sdk.chat.createMessageItem
import com.getstream.sdk.chat.createPositions
import com.getstream.sdk.chat.randomSyncStatus
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.test.createDate
import io.getstream.chat.android.test.positiveRandomInt
import io.getstream.chat.android.test.randomString
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class MessageListItemDiffCallbackTest {

    private val channelUserRead = createChannelUserRead()
    private val msg = createMessageItem()
    private val msgWithUserRead = createMessageItem(messageReadBy = mutableListOf(channelUserRead))
    private val msgWithModifiedUserRead = createMessageItem(
        message = msgWithUserRead.message,
        positions = msgWithUserRead.positions,
        isMine = msgWithUserRead.isMine,
        messageReadBy = mutableListOf(
            channelUserRead.copy(
                user = User("other")
            )
        )
    )

    @Test
    fun `Should properly check if items are the same when messages have the same id`() {
        val result = MessageListItemDiffCallback.areItemsTheSame(msg, msg)

        result shouldBe true
    }

    @Test
    fun `Should properly check if items are the same when messages have different id`() {
        val result = MessageListItemDiffCallback.areItemsTheSame(msg, msgWithUserRead)

        result shouldBe false
    }

    @Test
    fun `Should properly check if contents are the same when messages are the same`() {
        val result = MessageListItemDiffCallback.areContentsTheSame(msgWithUserRead, msgWithUserRead)

        result shouldBe true
    }

    @Test
    fun `Should properly check if contents are the same when messages are different`() {
        val result = MessageListItemDiffCallback.areContentsTheSame(msg, msgWithUserRead)

        result shouldBe false
    }

    @Test
    fun `Should properly check if contents are the same when messages are the same but with different user read`() {
        val result = MessageListItemDiffCallback.areContentsTheSame(msgWithUserRead, msgWithModifiedUserRead)

        result shouldBe false
    }

    /** [messagePayloadDiffArguments] */
    @ParameterizedTest
    @MethodSource("messagePayloadDiffArguments")
    fun `Should generate a proper MessageListItemPayloadDiff`(
        oldItem: MessageListItem.MessageItem,
        newItem: MessageListItem.MessageItem,
        expectedDiff: MessageListItemPayloadDiff?
    ) {
        val result: MessageListItemPayloadDiff? = MessageListItemDiffCallback.getChangePayload(oldItem, newItem) as? MessageListItemPayloadDiff

        result `should be equal to` expectedDiff
    }

    companion object {
        private val EMPTY_MESSAGE_LIST_ITEM_PAYLOAD_DIFF = MessageListItemPayloadDiff(
            text = false,
            reactions = false,
            attachments = false,
            replies = false,
            syncStatus = false,
            deleted = false,
            positions = false,
            readBy = false
        )

        @JvmStatic
        fun messagePayloadDiffArguments() = listOf(
            createMessageItem().let { mi: MessageListItem.MessageItem ->
                Arguments.of(mi, mi.copy(message = mi.message.copy(text = randomString())), EMPTY_MESSAGE_LIST_ITEM_PAYLOAD_DIFF.copy(text = true))
            },
            createMessageItem(message = createMessage(reactionCounts = mutableMapOf(randomString() to positiveRandomInt()))).let { mi: MessageListItem.MessageItem ->
                Arguments.of(mi, mi.copy(message = mi.message.copy(reactionCounts = mutableMapOf(randomString() to positiveRandomInt()))), EMPTY_MESSAGE_LIST_ITEM_PAYLOAD_DIFF.copy(reactions = true))
            },
            createMessageItem(message = createMessage(reactionScores = mutableMapOf(randomString() to positiveRandomInt()))).let { mi: MessageListItem.MessageItem ->
                Arguments.of(mi, mi.copy(message = mi.message.copy(reactionScores = mutableMapOf(randomString() to positiveRandomInt()))), EMPTY_MESSAGE_LIST_ITEM_PAYLOAD_DIFF.copy(reactions = true))
            },
            createMessageItem().let { mi: MessageListItem.MessageItem ->
                Arguments.of(mi, mi.copy(message = mi.message.copy(attachments = mutableListOf(createAttachment()))), EMPTY_MESSAGE_LIST_ITEM_PAYLOAD_DIFF.copy(attachments = true))
            },
            createMessageItem(message = createMessage(replyCount = positiveRandomInt())).let { mi: MessageListItem.MessageItem ->
                Arguments.of(mi, mi.copy(message = mi.message.copy(replyCount = positiveRandomInt())), EMPTY_MESSAGE_LIST_ITEM_PAYLOAD_DIFF.copy(replies = true))
            },
            createMessageItem().let { mi: MessageListItem.MessageItem ->
                Arguments.of(mi, mi.copy(message = mi.message.copy(syncStatus = randomSyncStatus { it == mi.message.syncStatus })), EMPTY_MESSAGE_LIST_ITEM_PAYLOAD_DIFF.copy(syncStatus = true))
            },
            createMessageItem(message = createMessage(deletedAt = null)).let { mi: MessageListItem.MessageItem ->
                Arguments.of(mi, mi.copy(message = mi.message.copy(deletedAt = createDate())), EMPTY_MESSAGE_LIST_ITEM_PAYLOAD_DIFF.copy(deleted = true))
            },
            createMessageItem().let { mi: MessageListItem.MessageItem ->
                Arguments.of(mi, mi.copy(positions = createPositions(10).toMutableList()), EMPTY_MESSAGE_LIST_ITEM_PAYLOAD_DIFF.copy(positions = true))
            },
            createMessageItem().let { mi: MessageListItem.MessageItem ->
                Arguments.of(mi, mi.copy(messageReadBy = createChannelUserReads()), EMPTY_MESSAGE_LIST_ITEM_PAYLOAD_DIFF.copy(readBy = true))
            },
        )
    }
}
