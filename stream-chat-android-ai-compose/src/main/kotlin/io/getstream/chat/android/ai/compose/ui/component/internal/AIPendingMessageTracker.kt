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

package io.getstream.chat.android.ai.compose.ui.component.internal

import io.getstream.chat.android.ai.compose.conversation.AIMessage
import io.getstream.chat.android.ai.compose.conversation.AIMessageAttachment
import io.getstream.chat.android.ai.compose.conversation.AIMessageRole
import io.getstream.chat.android.ai.compose.conversation.AIPendingMessage

/**
 * A row of a conversation of [AIMessage]s: a message, or one the person sent that is on its way.
 *
 * @param id The message's id, or, for the copy of a message that was on its way, that message's,
 * so the row keeps its key when the copy takes it over.
 * @param message The message, or `null` for a message the person sent that is on its way.
 * @param pending The message the person sent that is on its way, until its copy arrives.
 * @param sentAttachments What the person sent with a message on its way, which its copy shows
 * rather than load it again.
 * @param isLatestReply Whether it is a reply that ends the conversation, which can be asked for
 * again.
 */
internal data class AIConversationEntry(
    val id: String,
    val message: AIMessage?,
    val pending: AIPendingMessage?,
    val sentAttachments: List<AIMessageAttachment> = emptyList(),
    val isLatestReply: Boolean = false,
) {
    /** Whether the person wrote it: a message on its way always is. */
    val isFromUser: Boolean get() = message?.let { it.role == AIMessageRole.User } ?: true

    companion object {
        /** The row of a message the person sent that is on its way. */
        fun pending(pending: AIPendingMessage): AIConversationEntry =
            AIConversationEntry(id = pending.id, message = null, pending = pending)
    }
}

/**
 * Matches the messages the person sent while they were on their way to the copies that arrive, so
 * each copy takes over its row.
 */
internal class AIPendingMessageTracker {

    // The person's messages each pending message's copy cannot be, by the pending message's id:
    // those there were in the update before it was first seen, and those that arrived since
    // without being its copy, such as older ones.
    private val seen = mutableMapOf<String, Set<String>>()

    // The person's messages in the last update, which a pending message first seen next to its
    // copy was sent after.
    private var lastSent: Set<String>? = null

    // The pending message each copy took over, by the copy's id.
    private val copies = linkedMapOf<String, AIPendingMessage>()

    // The pending message of the last update, whose copy can arrive in the update that stops
    // passing it.
    private var lastPending: AIPendingMessage? = null

    /**
     * The rows of a conversation: its [messages], apart from replies with nothing to show yet, and
     * the [pending] message until its copy arrives.
     */
    fun entries(messages: List<AIMessage>, pending: AIPendingMessage?): List<AIConversationEntry> {
        var showsPending = pending != null
        // A copy that arrives in the update that stops passing its pending message still takes
        // over its row. Only then: a message sent later is not the copy of one that failed.
        val awaited = pending ?: lastPending
        val sent = messages.filter { it.role == AIMessageRole.User }
        val ids = sent.mapTo(LinkedHashSet()) { it.id }
        if (awaited != null) {
            val known = seen[awaited.id] ?: lastSent ?: ids
            val matched = copies.entries.firstOrNull { it.value.id == awaited.id && it.key in ids }?.key
            val copy = matched ?: copyOf(awaited, sent, known)
            if (copy != null) {
                copies.getOrPut(copy) { awaited }
                showsPending = false
            } else {
                seen[awaited.id] = known + ids
            }
        }
        lastPending = pending
        lastSent = ids
        val entries = messages.filterNot { it.showsNothing }.mapTo(mutableListOf()) { message ->
            val taken = copies[message.id]
            AIConversationEntry(
                id = taken?.id ?: message.id,
                message = message,
                pending = null,
                sentAttachments = taken?.attachments.orEmpty(),
            )
        }
        if (showsPending && pending != null) {
            entries += AIConversationEntry.pending(pending)
        }
        val last = entries.lastIndex
        if (last >= 0 && !entries[last].isFromUser) {
            entries[last] = entries[last].copy(isLatestReply = true)
        }
        return entries
    }

    /**
     * Whether the latest message is surely waiting for a reply that shows nothing yet: it is on its
     * way, it is the copy of the one on its way, or its reply is being written but shows nothing.
     * Call it after [entries], which matches the copies.
     */
    fun isAwaitingReply(messages: List<AIMessage>, pending: AIPendingMessage?): Boolean {
        val last = messages.lastOrNull()
        if (last != null && last.isGenerating && last.showsNothing) return true
        if (pending == null) return false
        val copy = copies.entries.firstOrNull { it.value.id == pending.id }?.key ?: return true
        return last?.id == copy
    }

    // The copy of a message that was on its way: the person's message with its id, when the app
    // knew it. Otherwise their first after those known, which older messages loading above cannot
    // be. With none of theirs known, such as when older messages arrive with it, it is their
    // latest, when that is the only new one or says the same.
    private fun copyOf(pending: AIPendingMessage, sent: List<AIMessage>, known: Set<String>): String? {
        val ids = sent.map { it.id }
        if (pending.id in ids) return pending.id
        val lastKnown = ids.indexOfLast { it in known }
        if (lastKnown >= 0) return ids.getOrNull(lastKnown + 1)
        val new = sent.filter { it.id !in known }
        val latest = new.lastOrNull() ?: return null
        return latest.id.takeIf { new.size == 1 || latest.text == pending.text }
    }
}

// A reply the agent has started but that has nothing to show yet.
private val AIMessage.showsNothing: Boolean
    get() = role != AIMessageRole.User && text.isEmpty() && parts.isEmpty() && attachments.isEmpty()
