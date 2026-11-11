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

/**
 * A request the person made, from when they make it until its reply starts or another request
 * takes its place.
 *
 * @param id Tells it apart from the requests before and after it.
 * @param request What the person asked.
 * @param conversation The messages there were when it was made, none of which is its reply.
 */
internal class AIConversationExchange<M : AIMessage>(
    val id: Int,
    val request: AIConversationRequest<M>,
    conversation: List<M>,
) {
    /**
     * Where the request is: waiting for its reply, stopped by the person before it started, not
     * sent because the transport failed, or not answered by the agent.
     */
    enum class State { Waiting, Stopped, Failed, Unanswered }

    var state: State = State.Waiting

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
            attachments = request.attachments.map { AIMessageAttachment.image(it) },
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
        return last.takeIf { it.id !in known || isRewritten }
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
