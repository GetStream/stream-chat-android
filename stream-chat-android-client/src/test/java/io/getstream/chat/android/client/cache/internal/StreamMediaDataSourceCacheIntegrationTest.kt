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

package io.getstream.chat.android.client.cache.internal

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.TransferListener
import androidx.test.core.app.ApplicationProvider
import io.getstream.chat.android.client.cache.VideoCacheConfig
import io.getstream.chat.android.client.cdn.CDN
import io.getstream.chat.android.client.cdn.CDNRequest
import io.getstream.chat.android.client.cdn.internal.CDNDataSourceFactory
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

/**
 * Verifies the cache + CDN composition end-to-end across the four cells of:
 * `{custom CDN, no CDN} × {cache hit, cache miss}`.
 *
 * The composition under test mirrors what `StreamMediaDataSource.factory()` produces for video
 * playback when a `VideoMediaCache` is supplied: `CacheDataSource` (outer) → `CDNDataSourceFactory`
 * (inner, when CDN is configured) → upstream `DataSource`. The test drives `DataSpec`s through that
 * composed factory directly — no ExoPlayer is involved — and asserts on a recording upstream and a
 * fake CDN.
 */
@OptIn(UnstableApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
internal class StreamMediaDataSourceCacheIntegrationTest {

    private val context: Context get() = ApplicationProvider.getApplicationContext()
    private val cacheDir: File get() = File(context.cacheDir, SUB_DIR)

    private lateinit var cache: VideoMediaCache

    @Before
    fun setUp() {
        cacheDir.deleteRecursively()
        cache = VideoMediaCache.create(context, cacheDir, VideoCacheConfig())
    }

    @After
    fun tearDown() {
        cache.release()
        cacheDir.deleteRecursively()
    }

    @Test
    fun `no CDN - first open misses cache and reads upstream, second open serves from cache`() {
        val upstream = RecordingDataSourceFactory()
        val factory = VideoCacheDataSourceFactory(cache, upstream)

        readFully(factory.createDataSource(), DataSpec(Uri.parse(VIDEO_URL)))
        readFully(factory.createDataSource(), DataSpec(Uri.parse(VIDEO_URL)))

        assertEquals(1, upstream.openCount)
        assertEquals(listOf(VIDEO_URL), upstream.openedUris)
    }

    @Test
    fun `custom CDN - first open invokes CDN with raw URL and upstream sees the signed URL`() {
        val cdn = FakeCDN()
        val upstream = RecordingDataSourceFactory()
        val factory = VideoCacheDataSourceFactory(cache, CDNDataSourceFactory(cdn) { upstream.createDataSource() })

        readFully(factory.createDataSource(), DataSpec(Uri.parse(VIDEO_URL)))

        assertEquals(listOf(VIDEO_URL), cdn.invocations)
        assertEquals(listOf("$VIDEO_URL?sig=0"), upstream.openedUris)
        assertEquals("0", upstream.lastOpenedHeaders["X-Sig"])
    }

    /**
     * The load-bearing case: a cache hit must NOT consult the customer's CDN. If the cache were
     * keyed by the signed URL, [FakeCDN] would have rotated the signature on the second open and
     * the cache would have missed.
     */
    @Test
    fun `custom CDN - cache hit does not invoke CDN and does not hit upstream`() {
        val cdn = FakeCDN()
        val upstream = RecordingDataSourceFactory()
        val factory = VideoCacheDataSourceFactory(cache, CDNDataSourceFactory(cdn) { upstream.createDataSource() })

        readFully(factory.createDataSource(), DataSpec(Uri.parse(VIDEO_URL)))
        readFully(factory.createDataSource(), DataSpec(Uri.parse(VIDEO_URL)))

        assertEquals(
            "CDN should be consulted exactly once across the miss + hit",
            1,
            cdn.invocations.size,
        )
        assertEquals(
            "Upstream should be opened exactly once across the miss + hit",
            1,
            upstream.openCount,
        )
    }

    private fun readFully(source: DataSource, spec: DataSpec) {
        source.open(spec)
        try {
            val buffer = ByteArray(BUFFER_SIZE)
            while (source.read(buffer, 0, buffer.size) != C.RESULT_END_OF_INPUT) {
                /* drain */
            }
        } finally {
            source.close()
        }
    }

    /**
     * A [DataSource.Factory] that produces [RecordingDataSource]s sharing a single open-log.
     */
    @OptIn(UnstableApi::class)
    private class RecordingDataSourceFactory(
        private val totalLength: Long = TOTAL_LENGTH,
    ) : DataSource.Factory {

        private val openedSpecs = mutableListOf<DataSpec>()

        val openCount: Int get() = openedSpecs.size
        val openedUris: List<String> get() = openedSpecs.map { it.uri.toString() }
        val lastOpenedHeaders: Map<String, String>
            get() = openedSpecs.lastOrNull()?.httpRequestHeaders.orEmpty()

        override fun createDataSource(): DataSource =
            RecordingDataSource(totalLength) { openedSpecs += it }
    }

    @OptIn(UnstableApi::class)
    private class RecordingDataSource(
        private val totalLength: Long,
        private val onOpen: (DataSpec) -> Unit,
    ) : DataSource {

        private var position = 0L
        private var endPosition = 0L
        private var lastUri: Uri? = null

        override fun open(dataSpec: DataSpec): Long {
            onOpen(dataSpec)
            lastUri = dataSpec.uri
            position = dataSpec.position
            endPosition = if (dataSpec.length == C.LENGTH_UNSET.toLong()) {
                totalLength
            } else {
                position + dataSpec.length
            }
            return endPosition - position
        }

        override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
            if (position >= endPosition) return C.RESULT_END_OF_INPUT
            val remaining = (endPosition - position).toInt()
            val toRead = minOf(length, remaining)
            repeat(toRead) { buffer[offset + it] = FILL_BYTE }
            position += toRead
            return toRead
        }

        override fun close() { /* nothing to release */ }

        override fun getUri(): Uri? = lastUri

        override fun getResponseHeaders(): Map<String, List<String>> = emptyMap()

        override fun addTransferListener(transferListener: TransferListener) { /* unused */ }
    }

    /**
     * Returns a fresh signature on every call so a cache miss after the first open would be visible
     * via a rotated `?sig=N` query parameter in the upstream's recorded URL.
     */
    private class FakeCDN : CDN {
        private val _invocations = mutableListOf<String>()
        val invocations: List<String> get() = _invocations.toList()

        private var counter = 0

        override suspend fun fileRequest(url: String): CDNRequest {
            _invocations += url
            val sig = counter++
            return CDNRequest("$url?sig=$sig", mapOf("X-Sig" to sig.toString()))
        }
    }

    private companion object {
        private const val SUB_DIR = "video_cache_integration_test"
        private const val VIDEO_URL = "https://stream.io/v.mp4"
        private const val TOTAL_LENGTH = 4096L
        private const val BUFFER_SIZE = 512
        private const val FILL_BYTE: Byte = 0xAB.toByte()
    }
}
