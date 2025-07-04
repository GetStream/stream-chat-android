/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client

import io.getstream.chat.android.client.chatclient.BaseChatClientTest
import io.getstream.chat.android.client.plugin.Plugin
import io.getstream.chat.android.client.utils.verifySuccess
import io.getstream.chat.android.models.Location
import io.getstream.chat.android.randomDate
import io.getstream.chat.android.randomDouble
import io.getstream.chat.android.randomLocation
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomString
import io.getstream.chat.android.test.asCall
import io.getstream.result.Result
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class ChatClientLocationApiTest : BaseChatClientTest() {

    private val plugin: Plugin = mock()

    @BeforeEach
    fun setUp() {
        plugins.add(plugin)
    }

    @Test
    fun `sendStaticLocation should call api with correct parameters and return result`() = runTest {
        val channelType = randomString()
        val channelId = randomString()
        val cid = "$channelType:$channelId"
        val latitude = randomDouble()
        val longitude = randomDouble()
        val deviceId = randomString()
        val location = randomLocation(
            cid = cid,
            latitude = latitude,
            longitude = longitude,
            deviceId = deviceId,
        )
        val message = randomMessage(sharedLocation = location)

        whenever(api.sendMessage(channelType, channelId, message)) doReturn message.asCall()
        whenever(
            attachmentsSender.sendAttachments(
                message = any(),
                channelType = eq(channelType),
                channelId = eq(channelId),
                isRetrying = eq(false),
            ),
        ) doReturn Result.Success(message)

        val actual = chatClient.sendStaticLocation(
            cid = cid,
            latitude = latitude,
            longitude = longitude,
            deviceId = deviceId,
        ).await()

        verifySuccess(actual, location)
    }

    @Test
    fun `startLiveLocationSharing should call api with correct parameters and return result`() = runTest {
        val channelType = randomString()
        val channelId = randomString()
        val cid = "$channelType:$channelId"
        val latitude = randomDouble()
        val longitude = randomDouble()
        val deviceId = randomString()
        val endAt = randomDate()
        val location = Location(
            cid = cid,
            latitude = latitude,
            longitude = longitude,
            deviceId = deviceId,
            endAt = endAt,
        )
        val message = randomMessage(sharedLocation = location)

        whenever(api.sendMessage(channelType, channelId, message)) doReturn message.asCall()
        whenever(
            attachmentsSender.sendAttachments(
                message = any(),
                channelType = eq(channelType),
                channelId = eq(channelId),
                isRetrying = eq(false),
            ),
        ) doReturn Result.Success(message)

        val actual = chatClient.startLiveLocationSharing(
            cid = cid,
            latitude = latitude,
            longitude = longitude,
            deviceId = deviceId,
            endAt = endAt,
        ).await()

        verify(plugin).onStartLiveLocationSharingResult(location, Result.Success(location))
        verifySuccess(actual, location)
    }

    @Test
    fun `updateLiveLocation should call api with correct parameters and return result`() = runTest {
        val messageId = randomString()
        val latitude = randomDouble()
        val longitude = randomDouble()
        val deviceId = randomString()
        val location = Location(
            messageId = messageId,
            latitude = latitude,
            longitude = longitude,
            deviceId = deviceId,
        )

        whenever(api.updateLiveLocation(location)) doReturn location.asCall()

        val actual = chatClient.updateLiveLocation(
            messageId = messageId,
            latitude = latitude,
            longitude = longitude,
            deviceId = deviceId,
        ).await()

        verify(plugin).onUpdateLiveLocationPrecondition(location)
        verify(plugin).onUpdateLiveLocationResult(location, Result.Success(location))
        verifySuccess(actual, location)
    }

    @Test
    fun `stopLiveLocationSharing should call api with correct parameters and return result`() = runTest {
        val messageId = randomString()
        val location = Location(messageId = messageId, endAt = now)

        whenever(api.updateLiveLocation(location)) doReturn location.asCall()

        val actual = chatClient.stopLiveLocationSharing(messageId).await()

        verify(plugin).onStopLiveLocationSharingResult(location, Result.Success(location))
        verifySuccess(actual, location)
    }
}
