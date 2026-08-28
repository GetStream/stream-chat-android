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

package io.getstream.chat.android.client.uploader

import io.getstream.chat.android.randomString
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldNotBeEqualTo
import org.junit.jupiter.api.Test

internal class FileUploadContextTest {

    private val channelType = randomString()
    private val channelId = randomString()
    private val userId = randomString()
    private val messageId = randomString()
    private val context = FileUploadContext(channelType, channelId, userId, messageId)

    @Test
    fun `contexts with the same values are equal with the same hash code`() {
        val other = FileUploadContext(channelType, channelId, userId, messageId)

        context shouldBeEqualTo context
        context shouldBeEqualTo other
        context.hashCode() shouldBeEqualTo other.hashCode()
    }

    @Test
    fun `contexts differing in any value are not equal`() {
        context shouldNotBeEqualTo FileUploadContext(randomString(), channelId, userId, messageId)
        context shouldNotBeEqualTo FileUploadContext(channelType, randomString(), userId, messageId)
        context shouldNotBeEqualTo FileUploadContext(channelType, channelId, randomString(), messageId)
        context shouldNotBeEqualTo FileUploadContext(channelType, channelId, userId, randomString())
        context shouldNotBeEqualTo FileUploadContext(channelType, channelId, userId, messageId = null)
    }

    @Test
    fun `a context is not equal to a different type or null`() {
        val differentType: Any = randomString()
        val nullValue: Any? = null

        (context == differentType) shouldBeEqualTo false
        (context == nullValue) shouldBeEqualTo false
    }

    @Test
    fun `the message id defaults to null`() {
        FileUploadContext(channelType, channelId, userId).messageId shouldBeEqualTo null
    }

    @Test
    fun `toString contains all values`() {
        val string = context.toString()

        string shouldBeEqualTo
            "FileUploadContext(channelType='$channelType', channelId='$channelId', " +
            "userId='$userId', messageId=$messageId)"
    }
}
