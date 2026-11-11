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

import kotlinx.coroutines.CompletableDeferred

internal data class ChatMessage(
    override val id: String,
    override val role: AIMessageRole,
    override val text: String,
    override val isGenerating: Boolean = false,
) : AIMessage

internal fun user(id: String, text: String = "Plan a weekend in Lisbon") = ChatMessage(id, AIMessageRole.User, text)

internal fun reply(id: String, text: String = "Here's a plan", generating: Boolean = false) =
    ChatMessage(id, AIMessageRole.Assistant, text, isGenerating = generating)

internal class TransportError : Exception("The transport failed")

/**
 * Records what the state asks of it, and lets a test say what the conversation holds and when
 * each request finishes.
 */
internal open class FakeTransport(private val initialMessages: List<ChatMessage> = emptyList()) :
    AIConversationTransport<ChatMessage> {

    var feed: AIConversationFeed<ChatMessage>? = null
    val requests = mutableListOf<AIConversationRequest<ChatMessage>>()
    var stops = 0
    var loads = 0
    val ratings = mutableListOf<Pair<String, AIMessageRating?>>()

    var sendError: Exception? = null
    var stopError: Exception? = null
    var loadError: Exception? = null
    var failsRating: (AIMessageRating?) -> Boolean = { false }

    // Holds each send, stop, load and rating until the test finishes it.
    var holdsSends = false
    var holdsStops = false
    var holdsLoads = false
    var holdsRatings = false
    private val heldSends = mutableListOf<CompletableDeferred<Unit>>()
    private val heldStops = mutableListOf<CompletableDeferred<Unit>>()
    private val heldLoads = mutableListOf<CompletableDeferred<Unit>>()
    private val heldRatings = mutableListOf<CompletableDeferred<Unit>>()

    val lastRequest: AIConversationRequest<ChatMessage>? get() = requests.lastOrNull()

    override fun connect(feed: AIConversationFeed<ChatMessage>) {
        this.feed = feed
        feed.update(messages = initialMessages)
    }

    override suspend fun send(request: AIConversationRequest<ChatMessage>) {
        requests += request
        if (holdsSends) CompletableDeferred<Unit>().also { heldSends += it }.await()
        sendError?.let { throw it }
    }

    override suspend fun stop() {
        stops += 1
        if (holdsStops) CompletableDeferred<Unit>().also { heldStops += it }.await()
        stopError?.let { throw it }
    }

    override suspend fun loadOlderMessages() {
        loads += 1
        if (holdsLoads) CompletableDeferred<Unit>().also { heldLoads += it }.await()
        loadError?.let { throw it }
    }

    override suspend fun rate(reply: ChatMessage, rating: AIMessageRating?) {
        ratings += reply.id to rating
        if (holdsRatings) CompletableDeferred<Unit>().also { heldRatings += it }.await()
        if (failsRating(rating)) throw TransportError()
    }

    fun finishSend(index: Int, error: Throwable? = null) = heldSends[index].finish(error)

    fun finishStop(index: Int) = heldStops[index].finish(null)

    fun finishRating(index: Int, error: Throwable? = null) = heldRatings[index].finish(error)

    fun finishLoads() {
        heldLoads.forEach { it.complete(Unit) }
        heldLoads.clear()
    }

    fun update(vararg messages: ChatMessage) {
        feed?.update(messages = messages.toList())
    }

    fun update(activity: AIAgentActivity?) {
        feed?.update(activity = activity)
    }

    private fun CompletableDeferred<Unit>.finish(error: Throwable?) {
        if (error == null) complete(Unit) else completeExceptionally(error)
    }
}

/** A transport that relies on what the interface does by default. */
internal class MinimalTransport : AIConversationTransport<ChatMessage> {
    override fun connect(feed: AIConversationFeed<ChatMessage>) = Unit
    override suspend fun send(request: AIConversationRequest<ChatMessage>) = Unit
    override suspend fun stop() = Unit
}

internal val List<AIMessage>.ids: List<String> get() = map { it.id }
