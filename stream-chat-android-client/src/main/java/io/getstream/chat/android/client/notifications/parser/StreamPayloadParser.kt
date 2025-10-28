/*
 * Copyright (c) 2014-2023 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.notifications.parser

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.log.StreamLog

/**
 * Helper class for parsing the payload of a push message.
 */
@InternalStreamChatApi
public object StreamPayloadParser {

    private const val TAG = "StreamPayloadParser"

    private val mapAdapter: JsonAdapter<MutableMap<String, Any?>> by lazy {
        Moshi.Builder()
            .build()
            .adapter(Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java))
    }

    /**
     * Parses the [json] string into a [Map] of [String] to [Any].
     */
    public fun parse(json: String?): Map<String, Any?> = try {
        json?.takeIf { it.isNotBlank() }?.let { mapAdapter.fromJson(it) } ?: emptyMap()
    } catch (e: Throwable) {
        StreamLog.e(TAG, e) { "[parse] failed: $json" }
        emptyMap()
    }
}
