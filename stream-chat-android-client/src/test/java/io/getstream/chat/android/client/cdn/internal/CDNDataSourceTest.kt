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

package io.getstream.chat.android.client.cdn.internal

import android.net.Uri
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.TransferListener
import io.getstream.chat.android.client.cdn.CDN
import io.getstream.chat.android.client.cdn.CDNRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@UnstableApi
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
internal class CDNDataSourceTest {

    @Test
    fun `open rewrites URI and headers when CDN returns new URL and headers`() {
        val cdn = object : CDN {
            override suspend fun fileRequest(url: String) =
                CDNRequest("https://cdn.example.com/video.mp4", mapOf("Auth" to "token"))
        }
        val upstream = FakeDataSource()
        val factory = CDNDataSourceFactory(cdn) { upstream }
        val dataSource = factory.createDataSource()
        val dataSpec = DataSpec(Uri.parse("https://original.com/video.mp4"))

        dataSource.open(dataSpec)

        val opened = upstream.lastOpenedDataSpec!!
        assertEquals("https://cdn.example.com/video.mp4", opened.uri.toString())
        assertEquals("token", opened.httpRequestHeaders["Auth"])
    }

    @Test
    fun `open merges CDN headers with existing DataSpec headers`() {
        val cdn = object : CDN {
            override suspend fun fileRequest(url: String) =
                CDNRequest(url, mapOf("X-CDN" to "cdn-value"))
        }
        val upstream = FakeDataSource()
        val factory = CDNDataSourceFactory(cdn) { upstream }
        val dataSource = factory.createDataSource()
        val dataSpec = DataSpec.Builder()
            .setUri("https://original.com/video.mp4")
            .setHttpRequestHeaders(mapOf("X-Existing" to "existing-value"))
            .build()

        dataSource.open(dataSpec)

        val opened = upstream.lastOpenedDataSpec!!
        assertEquals("existing-value", opened.httpRequestHeaders["X-Existing"])
        assertEquals("cdn-value", opened.httpRequestHeaders["X-CDN"])
    }

    @Test
    fun `open CDN headers override existing headers for same key`() {
        val cdn = object : CDN {
            override suspend fun fileRequest(url: String) =
                CDNRequest(url, mapOf("Auth" to "new-token"))
        }
        val upstream = FakeDataSource()
        val factory = CDNDataSourceFactory(cdn) { upstream }
        val dataSource = factory.createDataSource()
        val dataSpec = DataSpec.Builder()
            .setUri("https://original.com/video.mp4")
            .setHttpRequestHeaders(mapOf("Auth" to "old-token"))
            .build()

        dataSource.open(dataSpec)

        val opened = upstream.lastOpenedDataSpec!!
        assertEquals("new-token", opened.httpRequestHeaders["Auth"])
    }

    @Test
    @Suppress("TooGenericExceptionThrown")
    fun `open falls back to original DataSpec when CDN throws`() {
        val cdn = object : CDN {
            override suspend fun fileRequest(url: String): CDNRequest {
                throw RuntimeException("CDN error")
            }
        }
        val upstream = FakeDataSource()
        val factory = CDNDataSourceFactory(cdn) { upstream }
        val dataSource = factory.createDataSource()
        val originalUri = Uri.parse("https://original.com/video.mp4")
        val dataSpec = DataSpec(originalUri)

        dataSource.open(dataSpec)

        val opened = upstream.lastOpenedDataSpec!!
        assertEquals("https://original.com/video.mp4", opened.uri.toString())
    }

    @Test
    fun `open skips CDN for non-HTTP schemes`() {
        var cdnCalled = false
        val cdn = object : CDN {
            override suspend fun fileRequest(url: String): CDNRequest {
                cdnCalled = true
                return CDNRequest("https://should-not-be-used.com")
            }
        }
        val upstream = FakeDataSource()
        val factory = CDNDataSourceFactory(cdn) { upstream }
        val dataSource = factory.createDataSource()
        val dataSpec = DataSpec(Uri.parse("file:///local/video.mp4"))

        dataSource.open(dataSpec)

        val opened = upstream.lastOpenedDataSpec!!
        assertEquals("file:///local/video.mp4", opened.uri.toString())
        assertTrue("CDN should not be called for file:// URIs", !cdnCalled)
    }

    @Test
    fun `delegates read to upstream`() {
        val cdn = object : CDN {}
        val upstream: DataSource = mock()
        val factory = CDNDataSourceFactory(cdn) { upstream }
        val dataSource = factory.createDataSource()
        val buffer = ByteArray(1024)
        whenever(upstream.read(buffer, 0, 1024)).thenReturn(512)

        val result = dataSource.read(buffer, 0, 1024)

        assertEquals(512, result)
        verify(upstream).read(buffer, 0, 1024)
    }

    @Test
    fun `delegates close to upstream`() {
        val cdn = object : CDN {}
        val upstream: DataSource = mock()
        val factory = CDNDataSourceFactory(cdn) { upstream }
        val dataSource = factory.createDataSource()

        dataSource.close()

        verify(upstream).close()
    }

    @Test
    fun `delegates getUri to upstream`() {
        val cdn = object : CDN {}
        val expectedUri = Uri.parse("https://example.com")
        val upstream: DataSource = mock()
        whenever(upstream.uri).thenReturn(expectedUri)
        val factory = CDNDataSourceFactory(cdn) { upstream }
        val dataSource = factory.createDataSource()

        assertEquals(expectedUri, dataSource.uri)
    }

    @Test
    fun `delegates getResponseHeaders to upstream`() {
        val cdn = object : CDN {}
        val expectedHeaders = mapOf("Content-Type" to listOf("video/mp4"))
        val upstream: DataSource = mock()
        whenever(upstream.responseHeaders).thenReturn(expectedHeaders)
        val factory = CDNDataSourceFactory(cdn) { upstream }
        val dataSource = factory.createDataSource()

        assertEquals(expectedHeaders, dataSource.responseHeaders)
    }

    @Test
    fun `delegates addTransferListener to upstream`() {
        val cdn = object : CDN {}
        val upstream: DataSource = mock()
        val listener: TransferListener = mock()
        val factory = CDNDataSourceFactory(cdn) { upstream }
        val dataSource = factory.createDataSource()

        dataSource.addTransferListener(listener)

        verify(upstream).addTransferListener(listener)
    }

    /**
     * A simple fake [DataSource] that records the [DataSpec] passed to [open].
     */
    @UnstableApi
    private class FakeDataSource : DataSource {
        var lastOpenedDataSpec: DataSpec? = null

        override fun open(dataSpec: DataSpec): Long {
            lastOpenedDataSpec = dataSpec
            return 0
        }

        override fun read(buffer: ByteArray, offset: Int, length: Int): Int = 0
        override fun close() { /* empty on purpose */ }
        override fun getUri(): Uri? = null
        override fun getResponseHeaders(): Map<String, List<String>> = emptyMap()
        override fun addTransferListener(transferListener: TransferListener) { /* empty on purpose */ }
    }
}
