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
import io.getstream.chat.android.client.api.state.state
import io.getstream.chat.android.models.User
import io.getstream.result.Result
import kotlinx.coroutines.CompletableDeferred
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
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.ConcurrentHashMap

/**
 * Sends bursts of new message events to a real client, with the state and offline plugins and the real database,
 * and measures how long the SDK takes to show and to store them. The numbers are only comparable between runs
 * on the same device: run it on the baseline commit and on the change, one after the other.
 *
 * The result is one logcat line under the [RESULT_TAG] tag. The `iterations`, `warmups` and `burstSize`
 * instrumentation arguments override the defaults.
 */
@RunWith(AndroidJUnit4::class)
internal class EventBurstBenchmark {

    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context: Context = instrumentation.targetContext
    private val arguments = InstrumentationRegistry.getArguments()
    private val iterations = arguments.getString("iterations")?.toInt() ?: DEFAULT_ITERATIONS
    private val warmups = arguments.getString("warmups")?.toInt() ?: DEFAULT_WARMUPS
    private val burstSize = arguments.getString("burstSize")?.toInt() ?: DEFAULT_BURST_SIZE

    private val server = MockChatServer()
    private val recorder = BatchTimeRecorder(eventType = "message.new")
    private val observerScope = CoroutineScope(Dispatchers.Default)
    private lateinit var client: ChatClient

    @Before
    fun setUp() {
        // The data of an earlier run would make this one start with a bigger database.
        context.deleteDatabase("stream_chat_database_${BenchmarkJson.CURRENT_USER_ID}")
        server.start()
        recorder.install()
        instrumentation.runOnMainSync {
            client = ChatClient.Builder("benchmark-api-key", context)
                .forceHttpUrl(server.httpUrl)
                .forceWsUrl(server.wsUrl)
                .build()
        }
        runBlocking {
            val user = User(id = BenchmarkJson.CURRENT_USER_ID)
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
        val measured = (0 until warmups + iterations).map { index ->
            val iteration = runBurst(channelId = "burst-$index")
            delay(SETTLE_MILLIS)
            iteration
        }.drop(warmups)

        report("burstOfNewMessagesInOneChannel", measured)
        assertTrue("The SDK logged errors: ${recorder.errors}", recorder.errors.isEmpty())
        assertTrue("Events did not reach the state: $measured", measured.all { it.dropped == 0 })
    }

    private suspend fun runBurst(channelId: String): Iteration {
        val watched = client.channel(BenchmarkJson.CHANNEL_TYPE, channelId).watch().await()
        check(watched is Result.Success) { "The channel was not watched: $watched, unmocked: ${server.unmockedPaths}" }
        recorder.reset()

        val messageIds = List(burstSize) { "$channelId-message-$it" }
        val burstIds = messageIds.toSet()
        val sentAt = ConcurrentHashMap<String, Long>()
        val seenAt = ConcurrentHashMap<String, Long>()
        val allSeen = CompletableDeferred<Unit>()
        val observer = observerScope.launch {
            client.state.channel(BenchmarkJson.CHANNEL_TYPE, channelId).messages.collect { messages ->
                val now = SystemClock.elapsedRealtimeNanos()
                messages.forEach { if (it.id in burstIds) seenAt.putIfAbsent(it.id, now) }
                if (seenAt.size >= burstSize) allSeen.complete(Unit)
            }
        }

        val createdAt = System.currentTimeMillis()
        val events = messageIds.mapIndexed { index, id ->
            id to BenchmarkJson.newMessage(channelId, id, text = "Message $index", now = createdAt + index)
        }
        val startedAt = SystemClock.elapsedRealtimeNanos()
        events.forEach { (id, event) ->
            sentAt[id] = SystemClock.elapsedRealtimeNanos()
            server.send(event)
        }
        withTimeoutOrNull(BURST_TIMEOUT_MILLIS) { allSeen.await() }
        val endedAt = SystemClock.elapsedRealtimeNanos()
        // The batch ends after the state is updated, once its database write is done.
        withTimeoutOrNull(BATCH_TIMEOUT_MILLIS) {
            while (recorder.batches.sumOf { it.size } < burstSize) delay(POLL_MILLIS)
        }
        observer.cancel()

        return Iteration(
            eventToStateMillis = messageIds.mapNotNull { id -> seenAt[id]?.let { (it - sentAt.getValue(id)).toMillis() } },
            burstMillis = (endedAt - startedAt).toMillis(),
            batchMillis = recorder.batches.map { it.nanos.toMillis() },
            batchSizes = recorder.batches.map { it.size },
            dropped = burstSize - seenAt.size,
        )
    }

    private fun report(scenario: String, measured: List<Iteration>) {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val result = JSONObject()
            .put("scenario", scenario)
            .put("burstSize", burstSize)
            .put("iterations", measured.size)
            .put("warmups", warmups)
            .put("device", "${Build.MODEL} API ${Build.VERSION.SDK_INT}")
            // Room uses the TRUNCATE journal on a low-RAM device and WAL otherwise.
            .put("lowRamDevice", activityManager.isLowRamDevice)
            .put("eventToStateP50Millis", median(measured.map { percentile(it.eventToStateMillis, P50) }))
            .put("eventToStateP95Millis", median(measured.map { percentile(it.eventToStateMillis, P95) }))
            .put("burstMillis", median(measured.map { it.burstMillis }))
            .put("batchP95Millis", percentile(measured.flatMap { it.batchMillis }, P95))
            .put("batchTotalMillis", median(measured.map { it.batchMillis.sum() }))
            .put("batchesPerBurst", median(measured.map { it.batchSizes.size.toDouble() }))
            .put("dropped", measured.sumOf { it.dropped })
            .put("burstMillisPerIteration", JSONArray(measured.map { it.burstMillis }))
            .put("errors", JSONArray(recorder.errors))
            .put("unmockedPaths", JSONArray(server.unmockedPaths.distinct()))
        Log.i(RESULT_TAG, result.toString())
    }

    private data class Iteration(
        val eventToStateMillis: List<Double>,
        val burstMillis: Double,
        val batchMillis: List<Double>,
        val batchSizes: List<Int>,
        val dropped: Int,
    )

    private companion object {
        private const val RESULT_TAG = "BURST_BENCH"
        private const val APP_SETTINGS_REQUEST = "GET /api/v2/app"
        private const val DEFAULT_ITERATIONS = 10
        private const val DEFAULT_WARMUPS = 3
        private const val DEFAULT_BURST_SIZE = 200
        private const val CONNECT_TIMEOUT_MILLIS = 15_000L
        private const val BURST_TIMEOUT_MILLIS = 60_000L
        private const val BATCH_TIMEOUT_MILLIS = 10_000L
        private const val SETTLE_MILLIS = 500L
        private const val POLL_MILLIS = 10L
        private const val NANOS_PER_MILLI = 1_000_000.0
        private const val P50 = 0.50
        private const val P95 = 0.95

        private fun Long.toMillis(): Double = this / NANOS_PER_MILLI

        private fun percentile(values: List<Double>, fraction: Double): Double? {
            // JSON has no NaN: with nothing measured, no value is reported.
            if (values.isEmpty()) return null
            val sorted = values.sorted()
            return sorted[((sorted.size - 1) * fraction).toInt()]
        }

        private fun median(values: List<Double?>): Double? = percentile(values.filterNotNull(), P50)
    }
}
