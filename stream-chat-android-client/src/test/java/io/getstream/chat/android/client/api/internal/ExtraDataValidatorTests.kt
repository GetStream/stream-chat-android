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

package io.getstream.chat.android.client.api.internal

import io.getstream.chat.android.client.api.ChatApi
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.test.TestCall
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.shouldBeFalse
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class ExtraDataValidatorTests {

    private val chatApi: ChatApi = mock()
    private val validator = ExtraDataValidator(chatApi)

    @Test
    fun testUpdateChannel() {
        /* Given */
        val channel: Channel = mock()
        val channelId = "channel-id"
        val channelType = "channel-type"
        val extraData = mapOf("id" to "another-id")
        val updateMessage = null

        whenever(chatApi.updateChannel(channelType, channelId, extraData, updateMessage)) doReturn channel.toTestCall()

        /* When */
        val result: Result<Channel> = validator.updateChannel(
            channelId = channelId,
            channelType = channelType,
            extraData = extraData,
            updateMessage = updateMessage
        ).execute()

        /* Then */
        println("[testUpdateChannel] error.message: \"${result.error().message}\"")
        result.isSuccess.shouldBeFalse()
        result.error().message?.contains("id") `should be equal to` true
    }

    @Test
    fun testUpdateChannelPartial() {
        /* Given */
        val channel: Channel = mock()
        val channelId = "channel-id"
        val channelType = "channel-type"
        val set = mapOf("type" to "another-type")
        val unset = emptyList<String>()

        whenever(chatApi.updateChannelPartial(channelType, channelId, set, unset)) doReturn channel.toTestCall()

        /* When */
        val result: Result<Channel> = validator.updateChannelPartial(
            channelId = channelId,
            channelType = channelType,
            set = set,
            unset = unset
        ).execute()

        /* Then */
        println("[testUpdateChannelPartial] error.message: \"${result.error().message}\"")
        result.isSuccess.shouldBeFalse()
        result.error().message?.contains("type") `should be equal to` true
    }

    @Test
    fun testUpdateMessage() {
        /* Given */
        val message: Message = mock()
        val extraData: MutableMap<String, Any> = mutableMapOf("cid" to "another-cid")

        whenever(message.extraData) doReturn extraData
        whenever(chatApi.updateMessage(message)) doReturn message.toTestCall()

        /* When */
        val result: Result<Message> = validator.updateMessage(message).execute()

        /* Then */
        println("[testUpdateMessage] error.message: \"${result.error().message}\"")
        result.isSuccess.shouldBeFalse()
        result.error().message?.contains("cid") `should be equal to` true
    }

    @Test
    fun testPartialUpdateMessage() {
        /* Given */
        val messageId = "message-id"
        val message: Message = mock()
        val set: MutableMap<String, Any> = mutableMapOf("created_at" to "another-date")
        val unset = emptyList<String>()

        whenever(chatApi.partialUpdateMessage(messageId, set, unset)) doReturn message.toTestCall()

        /* When */
        val result: Result<Message> = validator.partialUpdateMessage(messageId, set, unset).execute()

        /* Then */
        println("[testPartialUpdateMessage] error.message: \"${result.error().message}\"")
        result.isSuccess.shouldBeFalse()
        result.error().message?.contains("created_at") `should be equal to` true
    }

    @Test
    fun testUpdateUsers() {
        /* Given */
        val user: User = mock()
        val users = listOf(user)
        val extraData: MutableMap<String, Any> = mutableMapOf(
            "cid" to "another-cid",
            "updated_at" to "another-date"
        )
        whenever(user.extraData) doReturn extraData
        whenever(chatApi.updateUsers(users)) doReturn users.toTestCall()

        /* When */
        val result: Result<List<User>> = validator.updateUsers(users).execute()

        /* Then */
        println("[testUpdateUsers] error.message: \"${result.error().message}\"")
        result.isSuccess.shouldBeFalse()
        result.error().message?.contains("cid") `should be equal to` true
        result.error().message?.contains("updated_at") `should be equal to` true
    }

    @Test
    fun testPartialUpdateUser() {
        /* Given */
        val userId = "user-id"
        val user: User = mock()
        val set: MutableMap<String, Any> = mutableMapOf(
            "updated_at" to "another-date",
            "created_at" to "another-date"
        )
        val unset = emptyList<String>()
        whenever(chatApi.partialUpdateUser(userId, set, unset)) doReturn user.toTestCall()

        /* When */
        val result: Result<User> = validator.partialUpdateUser(userId, set, unset).execute()

        /* Then */
        println("[testPartialUpdateUser] error.message: \"${result.error().message}\"")
        result.isSuccess.shouldBeFalse()
        result.error().message?.contains("updated_at") `should be equal to` true
        result.error().message?.contains("created_at") `should be equal to` true
        println(result.error().message)
    }

    private fun <T : Any> T.toTestCall(): TestCall<T> {
        return TestCall(Result.success(this))
    }
}
