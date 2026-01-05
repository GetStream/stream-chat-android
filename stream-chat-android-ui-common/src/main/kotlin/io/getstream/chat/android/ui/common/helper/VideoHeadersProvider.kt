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

package io.getstream.chat.android.ui.common.helper

import io.getstream.chat.android.core.internal.InternalStreamChatApi

/**
 * Provides HTTP headers for video loading requests.
 */
public interface VideoHeadersProvider {

    /**
     * Returns a map of headers to be used for the video loading request.
     *
     * @param url The URL of the video to load.
     * @return A map of headers to be used for the video loading request.
     */
    public fun getVideoRequestHeaders(url: String): Map<String, String>
}

@InternalStreamChatApi
public object DefaultVideoHeadersProvider : VideoHeadersProvider {
    override fun getVideoRequestHeaders(url: String): Map<String, String> = emptyMap<String, String>()
}
