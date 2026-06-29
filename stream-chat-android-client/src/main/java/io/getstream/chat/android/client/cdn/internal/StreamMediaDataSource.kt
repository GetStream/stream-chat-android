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

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import io.getstream.chat.android.client.cache.internal.VideoCacheDataSourceFactory
import io.getstream.chat.android.client.cache.internal.VideoMediaCache
import io.getstream.chat.android.client.cdn.CDN
import io.getstream.chat.android.core.internal.InternalStreamChatApi

/**
 * Centralized provider for Media3 [DataSource.Factory] instances.
 *
 * Wraps the base [DefaultDataSource.Factory] with [CDNDataSourceFactory] when a custom [CDN] is configured,
 * enabling URL rewriting and header injection for media playback (video, audio, voice recordings).
 * When a [VideoMediaCache] is provided, an on-disk cache layer is composed *outside* the CDN factory
 * so cached bytes are served without re-signing the URL; misses still go through the CDN.
 */
@InternalStreamChatApi
public object StreamMediaDataSource {

    /**
     * Creates a [DataSource.Factory] that handles both local and network media URIs.
     *
     * When a [CDN] is provided, HTTP/HTTPS requests are transformed through [CDN.fileRequest]
     * for URL rewriting and header injection. Local URIs (file://, content://) pass through unchanged.
     *
     * When a [videoCache] is provided, it wraps the resulting factory so video byte ranges are
     * served from disk on subsequent reads. The cache layer is the outermost wrapper, keyed by the
     * raw `dataSpec.uri`, so cache lookups are not affected by CDN URL rewriting.
     *
     * @param context The context used to create the base data source.
     * @param cdn Optional custom CDN for transforming network requests.
     * @param videoCache Optional disk cache for video playback. Callers should pass `null` for
     * non-video content (e.g. audio attachments, voice recordings) so audio bytes do not occupy
     * disk space in the video cache.
     */
    @OptIn(UnstableApi::class)
    public fun factory(
        context: Context,
        cdn: CDN?,
        videoCache: VideoMediaCache? = null,
    ): DataSource.Factory {
        val base = DefaultDataSource.Factory(context)
        val cdnWrapped = cdn?.let { CDNDataSourceFactory(it, base) } ?: base
        return videoCache?.let { VideoCacheDataSourceFactory(it, cdnWrapped) } ?: cdnWrapped
    }
}
