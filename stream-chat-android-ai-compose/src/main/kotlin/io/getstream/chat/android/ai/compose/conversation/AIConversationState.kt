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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import io.getstream.chat.android.ai.compose.generativeui.AIUIAction
import io.getstream.chat.android.ai.compose.ui.component.AIToolApprover
import io.getstream.chat.android.ai.compose.ui.component.MessageData
import io.getstream.chat.android.ai.compose.util.internal.simpleLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * The state behind a conversation with an AI agent, and what the person does in it.
 *
 * It holds the conversation's messages, where the latest exchange with the agent is and why it
 * failed, and it sends what the person asks through its [AIConversationTransport]. Give it to
 * `AIConversation(state)` and `ChatComposer(state)`, which show it and act on it:
 *
 * ```
 * class ChatViewModel(transport: AgentTransport) : ViewModel() {
 *     val conversation = AIConversationState(transport, viewModelScope)
 * }
 *
 * Column {
 *     AIConversation(
 *         state = viewModel.conversation,
 *         modifier = Modifier.weight(1f),
 *         footer = {
 *             if (status == AIConversationStatus.Error) {
 *                 Button(onClick = viewModel.conversation::retry) { Text("Try again") }
 *             }
 *         },
 *     )
 *     ChatComposer(state = viewModel.conversation)
 * }
 * ```
 *
 * What the person sends shows straight away as [pendingMessage], and the agent thinks until its
 * reply starts. Requests and stops are numbered, so the outcome of one that a newer one replaced
 * is ignored. After a stop, the reply counts as finished even while the transport still marks it
 * as generating, and one that showed nothing is left out of [messages].
 *
 * It is confined to the thread of [scope]: pass one that runs on the main thread, such as a
 * `viewModelScope`, and call it and its feed there. It lives as long as [scope].
 *
 * @param transport How the state reaches the agent and learns what the conversation holds. The
 * state connects to it straight away.
 * @param scope Where requests, stops and the reply timeout run.
 * @param approver Who answers tool calls' questions and sign-ins on this device, which the
 * conversation asks them.
 * @param replyTimeout How long the state waits, once the agent has a request, for the agent to
 * start its reply or say what it is doing, before it shows an [AIAgentError]. While the agent says
 * it is working, such as thinking, it waits as long as that takes. Null or [Duration.INFINITE]
 * waits as long as it takes.
 */
@Suppress("TooManyFunctions")
public class AIConversationState<M : AIMessage>(
    private val transport: AIConversationTransport<M>,
    private val scope: CoroutineScope,
    public val approver: AIToolApprover? = null,
    private val replyTimeout: Duration? = DefaultReplyTimeout,
) {
    private val _messages = MutableStateFlow<List<M>>(emptyList())
    private val _pendingMessage = MutableStateFlow<AIPendingMessage?>(null)
    private val _status = MutableStateFlow(AIConversationStatus.Ready)
    private val _error = MutableStateFlow<Throwable?>(null)
    private val _activity = MutableStateFlow<AIAgentActivity?>(null)
    private val _hasOlderMessages = MutableStateFlow(false)
    private val _ratings = MutableStateFlow<Map<String, AIMessageRating>>(emptyMap())

    /**
     * The conversation as it shows, oldest message first: the transport's messages, minus those a
     * request replaces until its reply starts, and minus a stopped reply that showed nothing.
     */
    public val messages: StateFlow<List<M>> = _messages.asStateFlow()

    /**
     * The message the person sent last that is not in the conversation yet. It stays until its
     * reply starts, and after it was stopped or failed, while [retry] can send it again.
     */
    public val pendingMessage: StateFlow<AIPendingMessage?> = _pendingMessage.asStateFlow()

    /** Where the latest exchange with the agent is. */
    public val status: StateFlow<AIConversationStatus> = _status.asStateFlow()

    /**
     * Why the latest request failed: what the transport threw, or, as an [AIAgentError], what the
     * agent said when it couldn't answer or that it didn't answer in time. Also why loading older
     * messages failed. It stays until the next request, until [clearError] or [retry], or until a
     * reply starts after all. A failed load's error also goes once older messages load.
     */
    public val error: StateFlow<Throwable?> = _error.asStateFlow()

    /** What the agent says it is doing besides what its reply shows, such as checking sources. */
    public val activity: StateFlow<AIAgentActivity?> = _activity.asStateFlow()

    /** Whether the conversation has messages before the first one, which [loadOlderMessages] loads. */
    public val hasOlderMessages: StateFlow<Boolean> = _hasOlderMessages.asStateFlow()

    /** How the person rated replies on this device, by the reply's id. */
    public val ratings: StateFlow<Map<String, AIMessageRating>> = _ratings.asStateFlow()

    // The messages as the transport last told them, including those left out of `messages`.
    private var received: List<M> = emptyList()

    // The latest request, until its reply starts or another request takes its place.
    private var exchange: AIConversationExchange<M>? = null

    // Counts the requests and stops, so the outcome of one that was overtaken is ignored.
    private var generation = 0

    // Replies stopped while they were written, which are finished whatever they say. Each stays
    // stopped across requests until it stops generating or is no longer the latest message.
    private val stopped = mutableSetOf<String>()

    // Whether what the agent says it is doing is ignored, after it was stopped, until it stops
    // saying anything.
    private var ignoresActivity = false

    // The latest stop, which the next request waits for: the agent would stop that one too.
    private var stopping: Job? = null

    // Ends the wait for the latest request's reply when the agent says nothing for too long.
    private var replyTimer: Job? = null

    private val ratingChanges = mutableMapOf<String, RatingChange>()
    private var ratingAttempts = 0
    private var isLoadingOlderMessages = false

    // Why loading older messages failed, while it is the error shown.
    private var loadError: Throwable? = null
    private val logger = simpleLogger("AIConversationState")

    // A rating the person gave a reply, and what the transport has, which it goes back to when the
    // transport can't take the new one.
    private class RatingChange(var shown: AIMessageRating?, var sent: AIMessageRating?) {
        // Tells the latest rating of the reply apart from those before it.
        var attempt = 0

        // Sends the latest rating, after those before it.
        var sending: Job? = null
    }

    internal val isActive: Boolean
        get() = scope.isActive

    init {
        transport.connect(AIConversationFeed(this))
    }

    /**
     * Sends a message. A message with no text and no attachments is not sent.
     *
     * @param text What the person wrote.
     * @param attachments The files they picked, on this device.
     */
    public fun send(text: String, attachments: List<Uri> = emptyList()) {
        if (text.isBlank() && attachments.isEmpty()) return
        start(AIConversationRequest(kind = AIConversationRequestKind.Message, text = text, attachments = attachments))
    }

    /**
     * Sends what the person wrote in the composer.
     *
     * @param message The composer's message.
     */
    public fun send(message: MessageData) {
        send(message.text, message.attachments.toList())
    }

    /**
     * Sends what the person did in a reply's generated UI, as their next message: its label is the
     * text, and the request's [AIConversationRequest.uiAction] the action. Pass it as the action
     * callback of the conversation's `AIUIHost`.
     *
     * @param action What the person did.
     */
    public fun send(action: AIUIAction) {
        start(
            AIConversationRequest(
                kind = AIConversationRequestKind.Message,
                text = action.label,
                uiAction = action,
            ),
        )
    }

    /**
     * Stops the reply the agent is writing. What it wrote so far stays. When the reply hasn't
     * started yet, the person's message stays, and a reply that still arrives is finished whatever
     * it says. Does nothing while the agent isn't answering.
     */
    public fun stop() {
        if (!_status.value.isAnswering) return
        generation += 1
        val id = generation
        _activity.value = null
        ignoresActivity = true
        exchange?.takeIf { it.state == AIConversationExchange.State.Waiting }?.state =
            AIConversationExchange.State.Stopped
        val writing = writingReply
        writing?.let(stopped::add)
        refresh()
        stopping = scope.launch {
            try {
                transport.stop()
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (@Suppress("TooGenericExceptionCaught") _: Exception) {
                stopFailed(id, writing)
            }
        }
    }

    /**
     * Tries again after [error]. When the transport couldn't send the person's message, it sends
     * it again, with the same message id. Otherwise nothing is sent: the agent had the request but
     * couldn't answer it, stopped partway through its reply, or didn't answer in time, or older
     * messages couldn't load. Sending the message again would add it twice, so retrying forgets the
     * error, as [clearError] does, and the person asks again. Does nothing without an error.
     */
    public fun retry() {
        val request = exchange?.retryRequest()
        when {
            request != null -> start(request)
            _error.value != null -> clearError()
        }
    }

    /** Forgets [error], and the request that failed. */
    public fun clearError() {
        _error.value = null
        loadError = null
        if (exchange?.hasFailed == true) exchange = null
        refresh()
    }

    /**
     * Rates a reply, or takes the rating back with null. Ratings reach the transport in the order
     * the person gives them, and the reply's rating goes back to the one the transport has if it
     * can't take the new one.
     *
     * @param reply The reply to rate.
     * @param rating The rating, or null to take it back.
     */
    public fun rate(reply: M, rating: AIMessageRating?) {
        if (_ratings.value[reply.id] == rating) return
        ratingAttempts += 1
        val attempt = ratingAttempts
        val change = ratingChanges.getOrPut(reply.id) {
            val current = _ratings.value[reply.id]
            RatingChange(shown = current, sent = current)
        }
        val earlier = change.sending
        change.shown = rating
        change.attempt = attempt
        change.sending = scope.launch {
            earlier?.join()
            sendRating(rating, reply, attempt)
        }
        refresh()
    }

    /**
     * Loads the messages before the first one, one load at a time, while there are any. A load
     * that fails shows as [error] and can be made again; one that succeeds forgets it.
     */
    public suspend fun loadOlderMessages() {
        if (!_hasOlderMessages.value || isLoadingOlderMessages) return
        isLoadingOlderMessages = true
        try {
            transport.loadOlderMessages()
            if (loadError != null && _error.value === loadError) _error.value = null
            loadError = null
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            logger.e(e) { "Couldn't load older messages" }
            loadError = e
            _error.value = e
        } finally {
            isLoadingOlderMessages = false
        }
        refresh()
    }

    // What the conversation's views offer under a finished reply: rating it, with its rating.
    internal fun replyActions(reply: AIMessage, ratings: Map<String, AIMessageRating>): AIReplyActions {
        val message = received.lastOrNull { it.id == reply.id }
        return AIReplyActions(
            rating = ratings[reply.id],
            onRate = message?.let { { rating -> rate(it, rating) } },
        )
    }

    internal fun receiveMessages(messages: List<M>) {
        received = messages.toList()
        val exchange = exchange
        exchange?.reply(received)?.let { receiveReply(it, exchange) }
        // A stopped reply is done with once it finishes or another message follows it.
        val latest = received.lastOrNull()?.takeIf { it.isGenerating }?.id
        stopped.retainAll { it == latest }
        refresh()
    }

    internal fun receiveHasOlderMessages(hasOlderMessages: Boolean) {
        _hasOlderMessages.value = hasOlderMessages
    }

    internal fun receiveActivity(activity: AIAgentActivity?) {
        if (activity == null) {
            ignoresActivity = false
            _activity.value = null
            // The agent stopped saying it is working without starting its reply.
            exchange?.takeIf { it.state == AIConversationExchange.State.Waiting }?.let { waitForReply(it.id) }
            refresh()
            return
        }
        if (ignoresActivity) return
        _activity.value = activity
        if (activity.state == AIAgentActivity.State.Error) {
            agentCouldNotAnswer(AIAgentError(activity.text))
        } else {
            // The agent is working on it, however long it thinks.
            replyTimer?.cancel()
        }
        refresh()
    }

    private fun start(request: AIConversationRequest<M>) {
        generation += 1
        val id = generation
        _error.value = null
        loadError = null
        _activity.value = null
        ignoresActivity = false
        exchange = AIConversationExchange(id, request, received)
        refresh()
        val previousStop = stopping
        scope.launch {
            // The agent would stop a request that reaches it before the stop made before it.
            previousStop?.join()
            try {
                transport.send(request)
            } catch (cancellation: CancellationException) {
                requestCancelled(id)
                throw cancellation
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                requestFailed(id, e)
                return@launch
            }
            requestSent(id)
        }
    }

    // The reply to the latest request arrived: once it shows something, or as soon as it arrives
    // after the person stopped the request.
    private fun receiveReply(reply: M, exchange: AIConversationExchange<M>) {
        if (exchange.state == AIConversationExchange.State.Stopped) {
            if (reply.isGenerating) stopped += reply.id
            this.exchange = null
        } else if (!reply.hasEmptyContent || !reply.isGenerating) {
            this.exchange = null
            if (exchange.hasFailed) {
                // The agent answered after all.
                _error.value = null
                stopped -= reply.id
            }
        }
    }

    private fun requestSent(id: Int) {
        // The agent may already say it is working on it.
        exchange?.takeIf { it.id == id && it.state == AIConversationExchange.State.Waiting } ?: return
        if (_activity.value == null) waitForReply(id)
    }

    private fun requestFailed(id: Int, error: Throwable) {
        // Its reply started, so the agent had it, another request took its place, or it already
        // failed.
        val exchange = exchange ?: return
        if (exchange.id != id || exchange.hasFailed) return
        exchange.state = AIConversationExchange.State.Failed
        _error.value = error
        refresh()
    }

    private fun requestCancelled(id: Int) {
        val exchange = exchange ?: return
        if (exchange.id != id || exchange.hasFailed) return
        this.exchange = null
        refresh()
    }

    // The agent goes on answering, and the composer offers to stop it again.
    private fun stopFailed(id: Int, writing: String?) {
        if (id != generation) return
        writing?.let(stopped::remove)
        exchange?.takeIf { it.state == AIConversationExchange.State.Stopped }?.let {
            it.state = AIConversationExchange.State.Waiting
            waitForReply(it.id)
        }
        ignoresActivity = false
        refresh()
    }

    // The agent couldn't answer the latest request, or the latest message: what it was writing is
    // finished, and `retry()` asks it again.
    private fun agentCouldNotAnswer(error: AIAgentError) {
        writingReply?.let(stopped::add)
        if (exchange == null) {
            answerAgainRequest?.let { request ->
                generation += 1
                exchange = AIConversationExchange(generation, request, received)
            }
        }
        exchange?.state = AIConversationExchange.State.Unanswered
        _error.value = error
    }

    private fun waitForReply(id: Int) {
        replyTimer?.cancel()
        val timeout = replyTimeout?.takeIf { it.isFinite() } ?: return
        replyTimer = scope.launch {
            delay(timeout)
            replyTimedOut(id)
        }
    }

    private fun replyTimedOut(id: Int) {
        val exchange = exchange ?: return
        if (exchange.id != id || exchange.state != AIConversationExchange.State.Waiting) return
        agentCouldNotAnswer(AIAgentError())
        refresh()
    }

    // Sends the latest rating of a reply. Those it replaced while one was on its way aren't.
    private suspend fun sendRating(rating: AIMessageRating?, reply: M, attempt: Int) {
        if (ratingChanges[reply.id]?.attempt != attempt) return
        try {
            transport.rate(reply, rating)
            ratingChanges[reply.id]?.sent = rating
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (@Suppress("TooGenericExceptionCaught", "SwallowedException") _: Exception) {
            val change = ratingChanges[reply.id]?.takeIf { it.attempt == attempt } ?: return
            change.shown = change.sent
            refresh()
        }
    }

    // Asks for a new reply in place of the one at `index`, to the person's message before it.
    private fun regenerateRequest(index: Int): AIConversationRequest<M> {
        val prompt = received.subList(0, index).lastOrNull { it.role == AIMessageRole.User }
        return AIConversationRequest(
            kind = AIConversationRequestKind.Regenerate,
            text = prompt?.text.orEmpty(),
            message = received[index],
        )
    }

    // Asks again for the latest message's reply: the reply written again, or one to the person's
    // message, which has none.
    private val answerAgainRequest: AIConversationRequest<M>?
        get() {
            val last = received.lastOrNull() ?: return null
            if (last.role != AIMessageRole.User) return regenerateRequest(received.lastIndex)
            return AIConversationRequest(kind = AIConversationRequestKind.Regenerate, text = last.text)
        }

    // The latest reply, while the agent writes it.
    private val writingReply: String?
        get() = received.lastOrNull()?.takeIf { it.role != AIMessageRole.User && it.isGenerating }?.id

    // Works out what the conversation shows and where it is, after anything changes.
    private fun refresh() {
        val replaced = exchange?.takeIf { it.state == AIConversationExchange.State.Waiting }?.replaced.orEmpty()
        _messages.value = if (replaced.isEmpty() && stopped.isEmpty()) {
            received
        } else {
            // A reply stopped before it showed anything has nothing to show, and the conversation
            // would think it is still on its way.
            received.filter { it.id !in replaced && !(it.id in stopped && it.hasEmptyContent) }
        }
        val pending = exchange?.shownPending
        if (_pendingMessage.value?.id != pending?.id) _pendingMessage.value = pending
        _ratings.value = ratingChanges.mapNotNull { (id, change) -> change.shown?.let { id to it } }.toMap()
        _status.value = currentStatus
    }

    private val currentStatus: AIConversationStatus
        get() {
            if (exchange?.state == AIConversationExchange.State.Waiting) return AIConversationStatus.Submitted
            val last = _messages.value.lastOrNull()
            val writing = last?.takeIf { it.role != AIMessageRole.User && it.isGenerating }
            if (writing != null && writing.id !in stopped) {
                return if (writing.hasEmptyContent) AIConversationStatus.Submitted else AIConversationStatus.Streaming
            }
            // The agent is working on the person's latest message, whose reply isn't here yet.
            val isWorking = _activity.value?.let { it.state != AIAgentActivity.State.Error } ?: false
            if (isWorking && (last == null || last.role == AIMessageRole.User)) {
                return AIConversationStatus.Submitted
            }
            return if (_error.value == null) AIConversationStatus.Ready else AIConversationStatus.Error
        }

    public companion object {
        /** How long a state waits by default for the agent to start its reply: five minutes. */
        public val DefaultReplyTimeout: Duration = 5.minutes
    }
}

/**
 * An [AIConversationState] that lives as long as the composition, connected to [transport].
 *
 * It is lost when the activity is recreated, such as when the phone turns: make an
 * [AIConversationState] in a ViewModel, with its `viewModelScope`, to keep it.
 *
 * @param transport How the state reaches the agent and learns what the conversation holds.
 * @param approver Who answers tool calls' questions and sign-ins on this device.
 * @param replyTimeout How long the state waits for the agent to start its reply.
 * A transport is connected once per state. When [transport], [approver] or [replyTimeout] changes,
 * a new state is made and calls [AIConversationTransport.connect] again with a new feed, and the
 * old state's feed reports [AIConversationFeed.isConnected] as false.
 *
 * @return A state made anew, and connected anew, when any of them changes.
 */
@Composable
public fun <M : AIMessage> rememberAIConversationState(
    transport: AIConversationTransport<M>,
    approver: AIToolApprover? = null,
    replyTimeout: Duration? = AIConversationState.DefaultReplyTimeout,
): AIConversationState<M> {
    val parent = rememberCoroutineScope()
    return remember(transport, approver, replyTimeout, parent) {
        RememberedConversationState(parent) { scope ->
            AIConversationState(transport = transport, scope = scope, approver = approver, replyTimeout = replyTimeout)
        }
    }.state
}

/**
 * A remembered state with a scope of its own, a child of the composition's, which is cancelled
 * when the state is replaced or leaves the composition: its feed then goes nowhere.
 */
private class RememberedConversationState<M : AIMessage>(
    parent: CoroutineScope,
    create: (CoroutineScope) -> AIConversationState<M>,
) : RememberObserver {
    private val job = SupervisorJob(parent.coroutineContext[Job])
    val state: AIConversationState<M> = create(parent + job)

    override fun onRemembered() = Unit

    override fun onForgotten() {
        job.cancel()
    }

    override fun onAbandoned() {
        job.cancel()
    }
}
