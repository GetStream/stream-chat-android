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
 * How an [AIConversationState] reaches the agent and learns what it holds: it sends what the
 * person asks, stops the agent, and tells the state its messages as they change.
 *
 * With Stream Chat, the conversation is a channel: the transport follows its messages and AI
 * indicator events, and sends each request through the channel or your agent's own API.
 *
 * ```
 * class AgentTransport(
 *     private val channel: ChannelState,
 *     private val agent: AgentApi,
 *     private val scope: CoroutineScope,
 * ) : AIConversationTransport<AgentMessage> {
 *
 *     override fun connect(feed: AIConversationFeed<AgentMessage>) {
 *         scope.launch {
 *             channel.messages.collect { messages -> feed.update(messages = messages.map(::AgentMessage)) }
 *         }
 *     }
 *
 *     override suspend fun send(request: AIConversationRequest<AgentMessage>) {
 *         agent.respond(request.text, files = request.attachments)
 *     }
 *
 *     override suspend fun stop() {
 *         agent.interrupt()
 *     }
 * }
 * ```
 *
 * The state calls it on the thread of its scope, the main thread for a `viewModelScope`.
 */
public interface AIConversationTransport<M : AIMessage> {

    /**
     * Starts telling [feed] what the conversation holds: its messages each time they change,
     * whether there are older ones to load, and what the agent says it is doing. The state calls
     * it once, as it is made. Call the feed on the state's thread.
     *
     * A transport is connected once per state: a new state, such as one `rememberAIConversationState`
     * makes when its keys change, calls it again with a new feed. Follow the latest feed, and stop
     * following an old one once its [AIConversationFeed.isConnected] is false.
     *
     * @param feed Where to tell the state what the conversation holds.
     */
    public fun connect(feed: AIConversationFeed<M>)

    /**
     * Sends what the person asks. Return once the agent has the request: its reply arrives through
     * the feed. A thrown exception shows as the state's error, and the person can retry, which
     * sends the same request again with the same [AIConversationRequest.messageId]: if the first
     * attempt reached your backend, or you keep a copy of the failed message, use the id so the
     * message isn't added twice.
     *
     * The state sends only [AIConversationRequestKind.Message] requests today. Once it asks for
     * replies written again, it leaves out the messages a [AIConversationRequestKind.Regenerate]
     * replaces only until the new reply starts. From then on it shows the messages as the feed
     * has them, so update the conversation before the new reply arrives: remove the request's
     * `message` and the messages after it, or write the new reply in place of `message`, under
     * its id.
     *
     * @param request What the person asks.
     */
    public suspend fun send(request: AIConversationRequest<M>)

    /** Stops the reply the agent is writing. What it wrote so far stays. */
    public suspend fun stop()

    /**
     * Loads the messages before the first one, which arrive through the feed. Does nothing by
     * default.
     */
    public suspend fun loadOlderMessages() {
        // No older messages to load.
    }

    /**
     * Tells your backend how the person rated a reply, or that they took their rating back with
     * null. Does nothing by default: the state keeps the ratings on this device either way.
     *
     * @param reply The reply the person rated.
     * @param rating Their rating, or null when they took it back.
     */
    public suspend fun rate(reply: M, rating: AIMessageRating?) {
        // Kept on this device.
    }
}

/**
 * Where an [AIConversationTransport] tells its [AIConversationState] what the conversation holds.
 * Call it on the state's thread, the main thread for a `viewModelScope`.
 *
 * Once the state's scope is cancelled, its updates go nowhere: stop following what the
 * conversation holds when [isConnected] is false.
 */
public class AIConversationFeed<M : AIMessage> internal constructor(
    private val state: AIConversationState<M>,
) {

    /**
     * Whether the state it updates is still there. Stop following what the conversation holds once
     * it isn't.
     */
    public val isConnected: Boolean
        get() = state.isActive

    /**
     * The conversation's messages, oldest first, each time they change.
     *
     * @param messages The messages, oldest first.
     */
    public fun update(messages: List<M>) {
        if (isConnected) state.receiveMessages(messages)
    }

    /**
     * Whether there are messages before the first one, which the transport's
     * [AIConversationTransport.loadOlderMessages] loads.
     *
     * @param hasOlderMessages Whether there are older messages.
     */
    public fun update(hasOlderMessages: Boolean) {
        if (isConnected) state.receiveHasOlderMessages(hasOlderMessages)
    }

    /**
     * What the agent says it is doing, or null once it stops saying. An activity in the
     * [AIAgentActivity.State.Error] state ends the latest request with an [AIAgentError].
     *
     * @param activity What the agent is doing, or null.
     */
    public fun update(activity: AIAgentActivity?) {
        if (isConnected) state.receiveActivity(activity)
    }
}
