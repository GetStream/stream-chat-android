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

package io.getstream.chat.android.client.benchmark

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.SystemClock
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.ChatClientConfig
import io.getstream.chat.android.client.api.MessageBufferConfig
import io.getstream.chat.android.client.api.MessageLimitConfig
import io.getstream.chat.android.client.api.state.state
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.User
import io.getstream.result.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONArray
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestName
import org.junit.runner.RunWith
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Sends new message events to a real client, with the state and offline plugins and the real database, and
 * measures how long the SDK takes to show and to store them, and to show a message and a reaction sent meanwhile.
 * The numbers are only comparable between runs on the same device: run it on the baseline commit and on the
 * change, one after the other.
 *
 * Each scenario logs one line under the [RESULT_TAG] tag. The `iterations`, `warmups`, `burstSize`, `actions`,
 * `streamSeconds`, `streamWarmupSeconds`, `eventsPerSecond` and `channels` instrumentation arguments override the
 * defaults.
 */
@RunWith(AndroidJUnit4::class)
internal class EventBurstBenchmark {

    @get:Rule
    val testName = TestName()

    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context: Context = instrumentation.targetContext
    private val arguments = InstrumentationRegistry.getArguments()
    private val iterations = argument("iterations", default = 10)
    private val warmups = argument("warmups", default = 3)
    private val burstSize = argument("burstSize", default = 200)
    private val actions = argument("actions", default = 5)
    private val streamSeconds = argument("streamSeconds", default = 20)
    private val streamWarmupSeconds = argument("streamWarmupSeconds", default = 3)
    private val eventsPerSecond = argument("eventsPerSecond", default = 5)
    private val channels = argument("channels", default = 50)
    private val boundedBurstSize = argument("boundedBurstSize", default = 1000)
    private val bufferCapacity = argument("bufferCapacity", default = 50)
    private val boundedBurstMillis = argument("boundedBurstMillis", default = 2000)

    private val server = MockChatServer()
    private val recorder = BatchTimeRecorder(eventType = "message.new")
    private val observerScope = CoroutineScope(Dispatchers.Default)
    private lateinit var client: ChatClient

    /** When each message was first in its channel's state, in `elapsedRealtimeNanos`, by message id. */
    private val messageSeenAt: MutableMap<String, Long> = ConcurrentHashMap()

    /** When each message first had a reaction of the current user in the state, by message id. */
    private val reactionSeenAt: MutableMap<String, Long> = ConcurrentHashMap()
    private val failedActions = CopyOnWriteArrayList<String>()

    @Before
    fun setUp() {
        BenchmarkJson.currentUserId = "benchmark-user-${testName.methodName}"
        // The data of an earlier run would make this one start with a bigger database.
        context.deleteDatabase("stream_chat_database_${BenchmarkJson.currentUserId}")
        server.start()
        recorder.install()
        instrumentation.runOnMainSync {
            client = ChatClient.Builder("benchmark-api-key", context)
                .forceHttpUrl(server.httpUrl)
                .forceWsUrl(server.wsUrl)
                .apply { if (testName.methodName == BOUNDED_BUFFER_SCENARIO) config(boundedBufferConfig()) }
                .build()
        }
        runBlocking {
            val user = User(id = BenchmarkJson.currentUserId)
            val connection = withTimeoutOrNull(CONNECT_TIMEOUT_MILLIS) {
                client.connectUser(user, client.devToken(user.id)).await()
            }
            assertTrue(
                "The client did not connect: $connection, errors: ${recorder.errors}, " +
                    "unmocked: ${server.unmockedPaths}",
                connection is Result.Success,
            )
        }
        assertTrue("The socket did not open", server.awaitSocket(timeoutSeconds = 10))
        // The app settings are not mocked, and their failed load is logged once, some time after the connection.
        assertTrue("The app settings were not requested", server.awaitUnmocked(APP_SETTINGS_REQUEST, timeoutSeconds = 10))
        Thread.sleep(SETTLE_MILLIS)
        recorder.errors.clear()
    }

    @After
    fun tearDown() {
        observerScope.cancel()
        if (::client.isInitialized) runBlocking { client.disconnect(flushPersistence = true).await() }
        recorder.uninstall()
        server.shutdown()
    }

    @Test
    fun burstOfNewMessagesInOneChannel() = runBlocking {
        val measured = runBursts(actions = 0)
        report("burstOfNewMessagesInOneChannel", measured) { put("burstSize", burstSize) }
    }

    @Test
    fun burstOfNewMessagesWithSendsAndReactions() = runBlocking {
        val measured = runBursts(actions)
        report("burstOfNewMessagesWithSendsAndReactions", measured) {
            put("burstSize", burstSize).put("actions", actions)
        }
    }

    @Test
    fun streamOfNewMessagesAcrossChannels() = runBlocking {
        val measured = runStream()
        report("streamOfNewMessagesAcrossChannels", listOf(measured)) {
            put("channels", channels).put("eventsPerSecond", eventsPerSecond).put("streamSeconds", streamSeconds)
        }
    }

    /**
     * The only scenario where the SDK drops events: by default it queues every socket event without a limit, so a
     * slower SDK only shows as events that take longer. Here the new messages of the channel type go through a
     * buffer of [bufferCapacity], and the burst is much bigger than it. The events are spread over
     * [boundedBurstMillis]: sent all at once, all but the first batch and the last [bufferCapacity] are dropped
     * however fast the SDK is.
     *
     * The drop count is a weak signal of speed: it changes little when the batches get slower.
     */
    @Test
    fun burstIntoBoundedBuffer() = runBlocking {
        check(testName.methodName == BOUNDED_BUFFER_SCENARIO) { "The client was built without the bounded buffer" }
        val measured = (0 until warmups + iterations).map { runBoundedBurst(channelId = "bounded-$it") }.drop(warmups)
        report(BOUNDED_BUFFER_SCENARIO, measured, allowDrops = true) {
            put("burstSize", boundedBurstSize).put("bufferCapacity", bufferCapacity)
                .put("burstMillis", boundedBurstMillis)
        }
    }

    private fun boundedBufferConfig() = ChatClientConfig(
        messageLimitConfig = MessageLimitConfig(
            messageBufferConfig = MessageBufferConfig(
                channelTypes = setOf(BenchmarkJson.CHANNEL_TYPE),
                capacity = bufferCapacity,
            ),
        ),
    )

    private suspend fun runBoundedBurst(channelId: String): Measurement {
        watch(channelId)
        recorder.reset()
        val events = List(boundedBurstSize) { MockMessage(channelId, "$channelId-message-$it", "Message $it") }
        val eventSentAt = HashMap<String, Long>()
        val startedAt = SystemClock.elapsedRealtimeNanos()
        events.chunked(BOUNDED_CHUNK_SIZE).forEachIndexed { index, chunk ->
            val chunkAt = startedAt + index.toLong() * BOUNDED_CHUNK_SIZE * boundedBurstMillis *
                NANOS_PER_MILLI.toLong() / boundedBurstSize
            val waitNanos = chunkAt - SystemClock.elapsedRealtimeNanos()
            if (waitNanos > 0) delay(waitNanos / NANOS_PER_MILLI.toLong())
            server.sendNewMessages(chunk) { eventSentAt[it.id] = SystemClock.elapsedRealtimeNanos() }
        }
        awaitQuiet(eventSentAt.keys)
        delay(SETTLE_MILLIS)
        return measurement(eventSentAt.values.min(), eventSentAt, emptyMap(), emptyMap())
    }

    /**
     * Waits until no more of the events reach the state: the dropped ones never do. A batch still being handled
     * is not quiet, however long it takes.
     */
    private suspend fun awaitQuiet(eventIds: Set<String>) = withTimeoutOrNull(BURST_TIMEOUT_MILLIS) {
        var seen = -1
        var quietSince = SystemClock.elapsedRealtime()
        while (seen < eventIds.size && SystemClock.elapsedRealtime() - quietSince < QUIET_MILLIS) {
            delay(QUIET_POLL_MILLIS)
            val nowSeen = eventIds.count { it in messageSeenAt }
            if (nowSeen != seen || recorder.hasBatchInFlight) {
                seen = nowSeen
                quietSince = SystemClock.elapsedRealtime()
            }
        }
    }

    private suspend fun runBursts(actions: Int): List<Measurement> =
        (0 until warmups + iterations).map { index ->
            runBurst(channelId = "burst-$index", actions)
        }.drop(warmups)

    /** One burst into a new channel, with [actions] messages and reactions sent by the client right after it. */
    private suspend fun runBurst(channelId: String, actions: Int): Measurement {
        watch(channelId)
        val reactedTo = seedMessages(List(actions) { MockMessage(channelId, "$channelId-seed-$it", "Seed $it") })
        recorder.reset()

        val events = List(burstSize) { MockMessage(channelId, "$channelId-message-$it", "Message $it") }
        val sends = List(actions) { MockMessage(channelId, "$channelId-sent-$it", "Sent $it") }
        val eventSentAt = HashMap<String, Long>()
        val sendCalledAt = HashMap<String, Long>()
        val reactionCalledAt = HashMap<String, Long>()
        server.sendNewMessages(events) { eventSentAt[it.id] = SystemClock.elapsedRealtimeNanos() }
        // Spread over the time the burst takes to handle: called all at once, they would be done before it starts.
        sends.zip(reactedTo).forEach { (send, seed) ->
            delay(ACTION_INTERVAL_MILLIS)
            sendCalledAt[send.id] = sendMessage(send)
            reactionCalledAt[seed.id] = sendReaction(seed)
        }

        awaitHandled(eventSentAt.keys, sendCalledAt.keys, reactionCalledAt.keys, BURST_TIMEOUT_MILLIS)
        // The batch ends after the state is updated, once its database write is done.
        awaitUntil(BATCH_TIMEOUT_MILLIS) { recorder.batches.sumOf { it.size } >= burstSize }
        // The batches of the events of the sent messages may still be running.
        delay(SETTLE_MILLIS)
        return measurement(eventSentAt.values.min(), eventSentAt, sendCalledAt, reactionCalledAt)
    }

    /**
     * A steady stream of events, one channel after the other, with a message and a reaction sent by the client
     * every second. What is sent in the first [streamWarmupSeconds] is not measured.
     */
    private suspend fun runStream(): Measurement {
        val channelIds = List(channels) { "stream-$it" }
        channelIds.forEach { watch(it) }
        val seeds = seedMessages(channelIds.map { MockMessage(it, "$it-seed", "Seed") })
        recorder.reset()

        val eventSentAt = HashMap<String, Long>()
        val sendCalledAt = HashMap<String, Long>()
        val reactionCalledAt = HashMap<String, Long>()
        val intervalNanos = NANOS_PER_SECOND / eventsPerSecond
        val warmupEvents = streamWarmupSeconds * eventsPerSecond
        var measuredFrom = 0L
        val streamStartedAt = SystemClock.elapsedRealtimeNanos()
        repeat(streamSeconds * eventsPerSecond) { index ->
            val waitNanos = streamStartedAt + index * intervalNanos - SystemClock.elapsedRealtimeNanos()
            if (waitNanos > 0) delay(waitNanos / NANOS_PER_MILLI.toLong())
            val isMeasured = index >= warmupEvents
            if (index == warmupEvents) {
                recorder.reset()
                measuredFrom = SystemClock.elapsedRealtimeNanos()
            }
            val channelId = channelIds[index % channels]
            val event = MockMessage(channelId, "$channelId-message-$index", "Message $index")
            server.sendNewMessages(listOf(event)) {
                if (isMeasured) eventSentAt[it.id] = SystemClock.elapsedRealtimeNanos()
            }

            // Once a second, in a channel other than the one that just got an event. Each seed is reacted to once.
            val second = index / eventsPerSecond
            if (index % eventsPerSecond == 0 && second < channels) {
                val seed = seeds[channels - 1 - second]
                val sent = MockMessage(seed.channelId, "${seed.channelId}-sent-$second", "Sent $second")
                val sendAt = sendMessage(sent)
                val reactionAt = sendReaction(seed)
                if (isMeasured) {
                    sendCalledAt[sent.id] = sendAt
                    reactionCalledAt[seed.id] = reactionAt
                }
            }
        }
        awaitHandled(eventSentAt.keys, sendCalledAt.keys, reactionCalledAt.keys, STREAM_TIMEOUT_MILLIS)
        delay(SETTLE_MILLIS)
        return measurement(measuredFrom, eventSentAt, sendCalledAt, reactionCalledAt)
    }

    private suspend fun watch(channelId: String) {
        val watched = client.channel(BenchmarkJson.CHANNEL_TYPE, channelId).watch().await()
        check(watched is Result.Success) { "The channel was not watched: $watched, unmocked: ${server.unmockedPaths}" }
        observerScope.launch {
            client.state.channel(BenchmarkJson.CHANNEL_TYPE, channelId).messages.collect { messages ->
                val now = SystemClock.elapsedRealtimeNanos()
                messages.forEach { message ->
                    messageSeenAt.putIfAbsent(message.id, now)
                    if (message.ownReactions.isNotEmpty()) reactionSeenAt.putIfAbsent(message.id, now)
                }
            }
        }
    }

    /** Messages that are in the state before the measured part, for the client to react to. */
    private suspend fun seedMessages(seeds: List<MockMessage>): List<MockMessage> {
        server.sendNewMessages(seeds)
        val seeded = awaitUntil(BURST_TIMEOUT_MILLIS) { seeds.all { it.id in messageSeenAt } }
        check(seeded) { "The seed messages did not reach the state, errors: ${recorder.errors}" }
        delay(SETTLE_MILLIS)
        return seeds
    }

    /** Sends [message] as the current user. Returns when it was called, in `elapsedRealtimeNanos`. */
    private fun sendMessage(message: MockMessage): Long {
        val calledAt = SystemClock.elapsedRealtimeNanos()
        client.channel(BenchmarkJson.CHANNEL_TYPE, message.channelId)
            .sendMessage(Message(id = message.id, text = message.text))
            .enqueue { if (it is Result.Failure) failedActions += "send ${message.id}: ${it.value}" }
        return calledAt
    }

    /** Sends a reaction of the current user to [message]. Returns when it was called. */
    private fun sendReaction(message: MockMessage): Long {
        val calledAt = SystemClock.elapsedRealtimeNanos()
        client.channel(BenchmarkJson.CHANNEL_TYPE, message.channelId)
            .sendReaction(Reaction(messageId = message.id, type = REACTION_TYPE, score = 1))
            .enqueue { if (it is Result.Failure) failedActions += "react to ${message.id}: ${it.value}" }
        return calledAt
    }

    /** Waits until the events and the sent messages are in the state, and the sends reached the server. */
    private suspend fun awaitHandled(
        eventIds: Set<String>,
        sentIds: Set<String>,
        reactedIds: Set<String>,
        timeoutMillis: Long,
    ) = awaitUntil(timeoutMillis) {
        eventIds.all { it in messageSeenAt } &&
            sentIds.all { it in messageSeenAt && it in server.messageRequestAt } &&
            reactedIds.all { it in reactionSeenAt && it in server.reactionRequestAt }
    }

    private suspend fun awaitUntil(timeoutMillis: Long, condition: () -> Boolean): Boolean =
        withTimeoutOrNull(timeoutMillis) {
            while (!condition()) delay(POLL_MILLIS)
            true
        } ?: false

    private fun measurement(
        startedAt: Long,
        eventSentAt: Map<String, Long>,
        sendCalledAt: Map<String, Long>,
        reactionCalledAt: Map<String, Long>,
    ) = Measurement(
        eventToStateMillis = elapsed(eventSentAt, messageSeenAt),
        totalMillis = eventSentAt.keys.mapNotNull { messageSeenAt[it] }.maxOrNull()?.let { (it - startedAt).toMillis() },
        batchMillis = recorder.batches.map { it.nanos.toMillis() },
        dropped = eventSentAt.keys.count { it !in messageSeenAt },
        sendToStateMillis = elapsed(sendCalledAt, messageSeenAt),
        sendToRequestMillis = elapsed(sendCalledAt, server.messageRequestAt),
        reactionToStateMillis = elapsed(reactionCalledAt, reactionSeenAt),
        reactionToRequestMillis = elapsed(reactionCalledAt, server.reactionRequestAt),
        unfinishedActions = sendCalledAt.keys.count { it !in messageSeenAt || it !in server.messageRequestAt } +
            reactionCalledAt.keys.count { it !in reactionSeenAt || it !in server.reactionRequestAt },
    )

    private fun report(
        scenario: String,
        measured: List<Measurement>,
        allowDrops: Boolean = false,
        parameters: JSONObject.() -> Unit,
    ) {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val result = JSONObject()
            .put("scenario", scenario)
            .apply(parameters)
            .put("iterations", measured.size)
            .put("device", "${Build.MODEL} API ${Build.VERSION.SDK_INT}")
            // Room uses the TRUNCATE journal on a low-RAM device and WAL otherwise.
            .put("lowRamDevice", activityManager.isLowRamDevice)
            .put("eventToStateP50Millis", median(measured.map { percentile(it.eventToStateMillis, P50) }))
            .put("eventToStateP95Millis", median(measured.map { percentile(it.eventToStateMillis, P95) }))
            .put("totalMillis", median(measured.map { it.totalMillis }))
            .put("batchP95Millis", percentile(measured.flatMap { it.batchMillis }, P95))
            .put("batchTotalMillis", median(measured.map { it.batchMillis.sum() }))
            .put("batches", median(measured.map { it.batchMillis.size.toDouble() }))
            .put("dropped", measured.sumOf { it.dropped })
            .put("droppedPerIteration", JSONArray(measured.map { it.dropped }))
            .put("sendToStateP95Millis", percentile(measured.flatMap { it.sendToStateMillis }, P95))
            .put("sendToRequestP95Millis", percentile(measured.flatMap { it.sendToRequestMillis }, P95))
            .put("reactionToStateP95Millis", percentile(measured.flatMap { it.reactionToStateMillis }, P95))
            .put("reactionToRequestP95Millis", percentile(measured.flatMap { it.reactionToRequestMillis }, P95))
            .put("unfinishedActions", measured.sumOf { it.unfinishedActions })
            .put("failedActions", JSONArray(failedActions))
            .put("errors", JSONArray(recorder.errors))
            .put("unmockedPaths", JSONArray(server.unmockedPaths.distinct()))
        Log.i(RESULT_TAG, result.toString())

        assertTrue("The SDK logged errors: ${recorder.errors}", recorder.errors.isEmpty())
        assertTrue("Sends or reactions failed: $failedActions", failedActions.isEmpty())
        assertTrue("Events did not reach the state: $measured", allowDrops || measured.all { it.dropped == 0 })
        assertTrue("Sends or reactions did not finish: $measured", measured.all { it.unfinishedActions == 0 })
    }

    private fun argument(name: String, default: Int): Int = arguments.getString(name)?.toInt() ?: default

    /** The times of one measured load, in milliseconds. */
    private data class Measurement(
        val eventToStateMillis: List<Double>,
        val totalMillis: Double?,
        val batchMillis: List<Double>,
        val dropped: Int,
        val sendToStateMillis: List<Double>,
        val sendToRequestMillis: List<Double>,
        val reactionToStateMillis: List<Double>,
        val reactionToRequestMillis: List<Double>,
        val unfinishedActions: Int,
    )

    private companion object {
        private const val RESULT_TAG = "BURST_BENCH"
        private const val APP_SETTINGS_REQUEST = "GET /api/v2/app"
        private const val REACTION_TYPE = "like"
        private const val BOUNDED_BUFFER_SCENARIO = "burstIntoBoundedBuffer"
        private const val BOUNDED_CHUNK_SIZE = 10
        private const val QUIET_MILLIS = 2_000L
        private const val QUIET_POLL_MILLIS = 50L
        private const val CONNECT_TIMEOUT_MILLIS = 15_000L
        private const val BURST_TIMEOUT_MILLIS = 60_000L
        private const val STREAM_TIMEOUT_MILLIS = 30_000L
        private const val BATCH_TIMEOUT_MILLIS = 10_000L
        private const val SETTLE_MILLIS = 500L
        private const val ACTION_INTERVAL_MILLIS = 20L
        private const val POLL_MILLIS = 5L
        private const val NANOS_PER_MILLI = 1_000_000.0
        private const val NANOS_PER_SECOND = 1_000_000_000L
        private const val P50 = 0.50
        private const val P95 = 0.95

        private fun Long.toMillis(): Double = this / NANOS_PER_MILLI

        /** The time from each start to its end, for the ids that have both. */
        private fun elapsed(startedAt: Map<String, Long>, endedAt: Map<String, Long>): List<Double> =
            startedAt.mapNotNull { (id, start) -> endedAt[id]?.let { (it - start).toMillis() } }

        private fun percentile(values: List<Double>, fraction: Double): Double? {
            // JSON has no NaN: with nothing measured, no value is reported.
            if (values.isEmpty()) return null
            val sorted = values.sorted()
            return sorted[((sorted.size - 1) * fraction).toInt()]
        }

        private fun median(values: List<Double?>): Double? = percentile(values.filterNotNull(), P50)
    }
}
