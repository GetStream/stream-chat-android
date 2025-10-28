/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.api

import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody
import okio.Buffer
import okio.BufferedSource
import java.nio.charset.Charset

internal data class FakeResponse(val statusCode: Int, val body: Body? = null) {
    class Body(data: String) : ResponseBody() {

        val buffer = Buffer().writeString(data, Charset.defaultCharset())

        override fun contentLength(): Long = buffer.size

        override fun contentType(): MediaType? = "application/json".toMediaType()

        override fun source(): BufferedSource = buffer
    }
}
