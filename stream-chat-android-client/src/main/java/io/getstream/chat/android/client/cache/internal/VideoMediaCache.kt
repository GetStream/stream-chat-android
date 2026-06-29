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
import androidx.annotation.OptIn
import androidx.annotation.VisibleForTesting
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import io.getstream.chat.android.client.cache.VideoCacheConfig
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.log.taggedLogger
import java.io.File

/**
 * Owns a [SimpleCache] that caches video bytes streamed through ExoPlayer.
 *
 * [SimpleCache] requires one instance per directory per process. Use [create] to obtain an
 * instance; the factory guarantees that subsequent calls for the same directory return the
 * existing cache instead of attempting to construct a second [SimpleCache], which would throw.
 *
 * The cache layer is composed *outside* any CDN URL-rewriting layer (see
 * [VideoCacheDataSourceFactory]), so entries are keyed by the raw `dataSpec.uri` (= the unsigned
 * `attachment.assetUrl` from the `MediaItem`). On a cache miss the customer's
 * [io.getstream.chat.android.client.cdn.CDN] still runs and signs the URL just in time; on a
 * cache hit the bytes are served from disk and no CDN call is made.
 */
@OptIn(UnstableApi::class)
@InternalStreamChatApi
public class VideoMediaCache private constructor(
    /**
     * The underlying [SimpleCache]. Exposed so [VideoCacheDataSourceFactory] can plug it into the
     * Media3 [androidx.media3.datasource.cache.CacheDataSource.Factory]; not intended for direct
     * use by callers outside the `cache.internal` package.
     */
    public val cache: SimpleCache,
    private val databaseProvider: StandaloneDatabaseProvider,
    private val dirPath: String,
) {

    private val logger by taggedLogger(TAG)

    /**
     * Tears down the underlying [SimpleCache] and the [StandaloneDatabaseProvider] it owns, and
     * removes this instance from the process-wide [instances] registry so that a fresh
     * [VideoMediaCache] can be constructed for the same directory.
     *
     * Production code does not need to call this — the cache is designed to live for the process
     * lifetime, and the OS reclaims its resources on process death. Tests call this between
     * cases so the next [create] does not collide with Media3's per-directory `SimpleCache` lock.
     */
    @VisibleForTesting
    internal fun release() {
        synchronized(instances) {
            instances.remove(dirPath)
            try {
                cache.release()
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                logger.e(e) { "[release] failed to release SimpleCache" }
            }
            try {
                databaseProvider.close()
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                logger.e(e) { "[release] failed to close StandaloneDatabaseProvider" }
            }
        }
    }

    @InternalStreamChatApi
    public companion object {
        private const val TAG = "Chat:VideoMediaCache"
        private val instances: MutableMap<String, VideoMediaCache> = mutableMapOf()
        private val logger by taggedLogger(TAG)

        /**
         * Returns a [VideoMediaCache] backed by the [SimpleCache] at [cacheDir]. If an instance
         * for that absolute directory path already exists in this process, that instance is
         * returned and [config] is ignored beyond the first call; this prevents a second
         * [SimpleCache] from being constructed against the same directory (which would throw).
         *
         * @param appContext Application context used to construct the [StandaloneDatabaseProvider].
         * @param cacheDir Directory that backs the [SimpleCache]. Created if it does not exist.
         * @param config Cache configuration. Honored only on the first call for [cacheDir].
         */
        @JvmStatic
        public fun create(appContext: Context, cacheDir: File, config: VideoCacheConfig): VideoMediaCache =
            synchronized(instances) {
                cacheDir.mkdirs()
                val key = cacheDir.absolutePath
                instances[key]?.let { existing ->
                    logger.w {
                        "[create] Reusing existing VideoMediaCache for '$key'; " +
                            "additional VideoCacheConfig values are ignored."
                    }
                    return@synchronized existing
                }
                val dbProvider = StandaloneDatabaseProvider(appContext)
                val simpleCache = SimpleCache(
                    cacheDir,
                    LeastRecentlyUsedCacheEvictor(config.maxSizeBytes),
                    dbProvider,
                )
                VideoMediaCache(simpleCache, dbProvider, key).also { instances[key] = it }
            }
    }
}
