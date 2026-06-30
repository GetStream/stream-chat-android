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

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.cache.CacheDataSource
import io.getstream.chat.android.client.cdn.internal.CDNDataSourceFactory

/**
 * A [DataSource.Factory] that serves video bytes from a [VideoMediaCache] on hit and delegates
 * to [upstreamFactory] on miss, writing the fetched bytes back into the cache.
 *
 * Cache entries are keyed by the URI with its query stripped, so rotating signature/expiry
 * parameters on the same path resolve to the same cache entry. The full [DataSpec] still flows
 * to [upstreamFactory] on a miss, so a custom CDN sees the original URL and can re-sign or
 * rewrite it. A caller-supplied [DataSpec.key] takes precedence over the URI-derived key.
 *
 * @param videoCache The cache that holds the cached video spans.
 * @param upstreamFactory Factory invoked on cache miss (typically the [CDNDataSourceFactory] when
 * a custom CDN is configured, or the base data source otherwise).
 */
@OptIn(UnstableApi::class)
internal class VideoCacheDataSourceFactory(
    videoCache: VideoMediaCache,
    upstreamFactory: DataSource.Factory,
) : DataSource.Factory {

    private val delegate: DataSource.Factory = CacheDataSource.Factory()
        .setCache(videoCache.cache)
        .setUpstreamDataSourceFactory(upstreamFactory)
        .setCacheKeyFactory(::cacheKeyFor)
        .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

    override fun createDataSource(): DataSource = delegate.createDataSource()
}

/**
 * Returns the cache key for [dataSpec]. Strips the URI's query so rotating signature or expiry
 * parameters on the same path land on the same cache entry; a caller-supplied [DataSpec.key] is
 * honoured when present.
 */
@OptIn(UnstableApi::class)
private fun cacheKeyFor(dataSpec: DataSpec): String =
    dataSpec.key ?: dataSpec.uri.buildUpon().clearQuery().build().toString()
