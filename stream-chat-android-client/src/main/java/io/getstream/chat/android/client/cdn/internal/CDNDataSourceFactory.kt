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
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.TransferListener
import io.getstream.chat.android.client.cdn.CDN
import io.getstream.chat.android.client.cdn.CDNRequest
import io.getstream.log.taggedLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

/**
 * A [DataSource.Factory] that creates [CDNDataSource] instances which transform
 * media requests through the [CDN.fileRequest] method before delegating to an upstream data source.
 *
 * @param cdn The CDN used to transform file request URLs and headers.
 * @param upstreamFactory The factory for creating the upstream data source that performs the actual HTTP requests.
 */
@UnstableApi
internal class CDNDataSourceFactory(
    private val cdn: CDN,
    private val upstreamFactory: DataSource.Factory = DefaultHttpDataSource.Factory(),
) : DataSource.Factory {
    override fun createDataSource(): DataSource {
        return CDNDataSource(cdn, upstreamFactory.createDataSource())
    }
}

/**
 * A [DataSource] that transforms media requests through [CDN.fileRequest] before
 * delegating to an upstream data source. This allows custom CDN implementations
 * to rewrite URLs and inject headers for video/audio/voice recording playback via ExoPlayer.
 *
 * [CDN.fileRequest] is a suspend function and is called via [runBlocking] on [Dispatchers.IO].
 * This is safe because ExoPlayer always calls [open] from its loader thread, never the main thread.
 */
@UnstableApi
private class CDNDataSource(
    private val cdn: CDN,
    private val upstream: DataSource,
) : DataSource {

    private val logger by taggedLogger("Chat:CDNDataSource")

    override fun open(dataSpec: DataSpec): Long {
        val scheme = dataSpec.uri.scheme
        if (scheme != "http" && scheme != "https") {
            return upstream.open(dataSpec)
        }
        val url = dataSpec.uri.toString()
        val cdnRequest = try {
            runBlocking(Dispatchers.IO) {
                cdn.fileRequest(url)
            }
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            logger.e(e) { "[open] CDN.fileRequest() failed for url: $url. Falling back to original request." }
            CDNRequest(url)
        }
        val mergedHeaders = buildMap {
            putAll(dataSpec.httpRequestHeaders)
            cdnRequest.headers?.let { putAll(it) }
        }
        val transformedSpec = dataSpec.buildUpon()
            .setUri(Uri.parse(cdnRequest.url))
            .setHttpRequestHeaders(mergedHeaders)
            .build()
        return upstream.open(transformedSpec)
    }

    override fun read(buffer: ByteArray, offset: Int, length: Int): Int =
        upstream.read(buffer, offset, length)

    override fun close() {
        upstream.close()
    }

    override fun getUri(): Uri? = upstream.uri

    override fun getResponseHeaders(): Map<String, List<String>> = upstream.responseHeaders

    override fun addTransferListener(transferListener: TransferListener) {
        upstream.addTransferListener(transferListener)
    }
}
