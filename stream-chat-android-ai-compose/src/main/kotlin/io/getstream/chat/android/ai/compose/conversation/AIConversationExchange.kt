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

package io.getstream.chat.android.ai.compose.conversation

import android.net.Uri
import io.getstream.chat.android.ai.compose.util.internal.pathExtension

/**
 * A request the person made, from when they make it until its reply starts or another request
 * takes its place.
 *
 * @param id Tells it apart from the requests before and after it.
 * @param request What the person asked.
 * @param conversation The messages there were when it was made, none of which is its reply.
 * @param lateTurns How many of the person's messages, of requests stopped before it, may still
 * arrive with their replies after the [conversation]: its reply comes after those.
 */
internal class AIConversationExchange<M : AIMessage>(
    val id: Int,
    val request: AIConversationRequest<M>,
    conversation: List<M>,
    private val lateTurns: Int = 0,
) {
    /**
     * Where the request is: waiting for its reply, stopped by the person before it started, not
     * sent because the transport failed, or not answered by the agent.
     */
    enum class State { Waiting, Stopped, Failed, Unanswered }

    var state: State = State.Waiting

    /** Whether the transport has the request: its send returned. */
    var isSent: Boolean = false

    /** Whether the request went to the transport, whatever came of it. */
    var isSending: Boolean = false

    private val known: Set<String> = conversation.mapTo(HashSet()) { it.id }

    /**
     * The messages it replaces, as they were, which the state leaves out while it waits: the
     * reply written again and those after it.
     */
    val replaced: Map<String, M> = run {
        val target = request.message?.let { message -> conversation.indexOfFirst { it.id == message.id } } ?: -1
        if (target >= 0 && request.kind == AIConversationRequestKind.Regenerate) {
            conversation.subList(target, conversation.size).associateBy { it.id }
        } else {
            emptyMap()
        }
    }

    /** The person's message on its way. A reply written again has none. */
    val pending: AIPendingMessage? = if (request.kind == AIConversationRequestKind.Regenerate) {
        null
    } else {
        AIPendingMessage(
            id = request.messageId,
            text = request.text,
            attachments = request.attachments.map(::attachmentOnItsWay),
        )
    }

    val hasFailed: Boolean
        get() = state == State.Failed || state == State.Unanswered

    /**
     * The person's message while the conversation shows it: until its reply starts, and for a new
     * message also once it is stopped or failed, since the agent may have it, or a retry sends it
     * again.
     */
    val shownPending: AIPendingMessage?
        get() = if (state == State.Waiting || request.kind == AIConversationRequestKind.Message) pending else null

    /**
     * The latest message when it is the request's reply: a reply that wasn't there, or one the
     * request replaces, once it is written again.
     */
    fun reply(conversation: List<M>): M? {
        val last = conversation.lastOrNull()?.takeIf { it.role != AIMessageRole.User } ?: return null
        val isRewritten = replaced[last.id]?.let { last.isRewrittenFrom(it) } ?: false
        if (last.id in known && !isRewritten) return null
        return last.takeIf { lateTurns == 0 || followsOwnMessage(conversation) }
    }

    /**
     * The latest message when it is the late reply of a request stopped before this one: a new
     * reply that came before this request's own message.
     */
    fun lateReplyOfStoppedRequest(conversation: List<M>): M? {
        if (lateTurns == 0) return null
        val last = conversation.lastOrNull()?.takeIf { it.role != AIMessageRole.User && it.id !in known } ?: return null
        return last.takeUnless { followsOwnMessage(conversation) }
    }

    /**
     * Whether the person's messages still to come, of the requests stopped before it, arrived
     * before its reply: this request's own message is the one after them, or the one with its id.
     */
    private fun followsOwnMessage(conversation: List<M>): Boolean {
        if (conversation.any { it.id == request.messageId }) return true
        val own = if (request.kind == AIConversationRequestKind.Regenerate) 0 else 1
        return newMessagesOfThePerson(conversation) >= lateTurns + own
    }

    /** How many of the person's messages arrived after those there were when it was made. */
    private fun newMessagesOfThePerson(conversation: List<M>): Int {
        val lastKnown = conversation.indexOfLast { it.id in known }
        return conversation.drop(lastKnown + 1).count { it.role == AIMessageRole.User }
    }

    /**
     * How many of the person's messages, of this request and those stopped before it, may still
     * arrive with their replies after [conversation], once it is stopped before its reply came.
     */
    fun lateTurnsAfterStop(conversation: List<M>): Int {
        if (state != State.Stopped || !isSending) return 0
        val own = if (request.kind == AIConversationRequestKind.Regenerate) 0 else 1
        return (lateTurns + own - newMessagesOfThePerson(conversation)).coerceAtLeast(0)
    }

    /** Whether its reply has started: it shows something, or it is already finished. */
    fun hasReplyStarted(conversation: List<M>): Boolean =
        reply(conversation)?.let { !it.hasEmptyContent || !it.isGenerating } ?: false

    /**
     * What a retry sends: the request again, with the same message id, when it never reached the
     * transport. Null once the transport had it, since the agent can't write a reply again and
     * asking again would add the person's message twice: the person asks again instead.
     */
    fun retryRequest(): AIConversationRequest<M>? = request.takeIf { state == State.Failed }
}

/**
 * A file the person picked, on its way: by its extension when its name has one, else an image,
 * as the composer's photo picker gives, such as an extensionless `content://` URI.
 */
private fun attachmentOnItsWay(uri: Uri): AIMessageAttachment =
    if (pathExtension(null, uri) != null) AIMessageAttachment.from(uri) else AIMessageAttachment.image(uri)

/** Whether a message shows nothing: no text, steps, attachments or generated UI. */
internal val AIMessage.hasEmptyContent: Boolean
    get() = text.isEmpty() && parts.isEmpty() && attachments.isEmpty() && uiDocument == null

/** Whether a reply is written again since it was [original]: it started anew, or it shows something else. */
private fun AIMessage.isRewrittenFrom(original: AIMessage): Boolean =
    (isGenerating && !original.isGenerating) ||
        text != original.text ||
        parts != original.parts ||
        attachments != original.attachments ||
        uiDocument != original.uiDocument
