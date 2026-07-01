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

package io.getstream.chat.android.client.cache

/**
 * Bundles the per-cache configurations exposed by the Stream Chat SDK.
 *
 * Pass an instance to [io.getstream.chat.android.client.ChatClient.Builder.cacheConfig] to configure
 * the on-disk caches.
 *
 * @param video Configuration for the video playback cache used by SDK.
 */
public data class StreamCacheConfig(
    public val video: VideoCacheConfig? = null,
)

/**
 * Configuration for the on-disk cache used when streaming video attachments.
 *
 * Wrap an instance in [StreamCacheConfig] and pass it to
 * [io.getstream.chat.android.client.ChatClient.Builder.cacheConfig] to opt in. When the cache is
 * enabled, replaying or seeking within a previously watched video reuses cached byte ranges
 * instead of re-downloading from the CDN.
 *
 * @param maxSizeBytes Soft cap on cache size; LRU eviction kicks in once exceeded. Files larger
 * than this cap are not effectively cached. Size [maxSizeBytes] to comfortably exceed the
 * largest expected video.
 */
public data class VideoCacheConfig(
    public val maxSizeBytes: Long = DEFAULT_MAX_SIZE_BYTES,
) {
    init {
        require(maxSizeBytes > 0) { "maxSizeBytes must be > 0, got $maxSizeBytes" }
    }

    public companion object {
        /** Default cap of 150 MB. */
        public const val DEFAULT_MAX_SIZE_BYTES: Long = 150L * 1024 * 1024
    }
}
