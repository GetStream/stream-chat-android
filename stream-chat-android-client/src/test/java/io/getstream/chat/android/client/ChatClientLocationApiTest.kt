package io.getstream.chat.android.client

import io.getstream.chat.android.client.chatclient.BaseChatClientTest
import io.getstream.chat.android.client.utils.verifySuccess
import io.getstream.chat.android.randomDate
import io.getstream.chat.android.randomDouble
import io.getstream.chat.android.randomLocation
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomString
import io.getstream.chat.android.test.asCall
import io.getstream.result.Result
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever

internal class ChatClientLocationApiTest : BaseChatClientTest() {

    @Test
    fun `sendStaticLocation should call api with correct parameters and return result`() = runTest {
        val channelType = randomString()
        val channelId = randomString()
        val cid = "$channelType:$channelId"
        val latitude = randomDouble()
        val longitude = randomDouble()
        val deviceId = randomString()
        val message = randomMessage(
            sharedLocation = randomLocation(
                cid = cid,
                latitude = latitude,
                longitude = longitude,
                deviceId = deviceId
            )
        )

        whenever(api.sendMessage(channelType, channelId, message)) doReturn message.asCall()
        whenever(
            attachmentsSender.sendAttachments(
                message = any(),
                channelType = eq(channelType),
                channelId = eq(channelId),
                isRetrying = eq(false),
            )
        ) doReturn Result.Success(message)

        val actual = chatClient.sendStaticLocation(cid, latitude, longitude, deviceId).await()

        verifySuccess(actual, message.sharedLocation!!)
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
        val message = randomMessage(
            sharedLocation = randomLocation(
                cid = cid,
                latitude = latitude,
                longitude = longitude,
                deviceId = deviceId,
                endAt = endAt,
            )
        )

        whenever(api.sendMessage(channelType, channelId, message)) doReturn message.asCall()
        whenever(
            attachmentsSender.sendAttachments(
                message = any(),
                channelType = eq(channelType),
                channelId = eq(channelId),
                isRetrying = eq(false),
            )
        ) doReturn Result.Success(message)

        val actual = chatClient.startLiveLocationSharing(cid, latitude, longitude, deviceId, endAt).await()

        verifySuccess(actual, message.sharedLocation!!)
    }
}
