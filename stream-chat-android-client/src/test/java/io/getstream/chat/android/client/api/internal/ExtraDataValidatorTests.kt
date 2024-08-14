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
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.chat.android.test.asCall
import io.getstream.result.Result
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class ExtraDataValidatorTests {

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }
    private val chatApi: ChatApi = mock()
    private lateinit var validator: ExtraDataValidator

    @BeforeEach
    fun setup() {
        validator = ExtraDataValidator(testCoroutines.scope, chatApi)
    }

    @Test
    fun testUpdateChannel() = runTest {
        /* Given */
        val channel: Channel = mock()
        val channelId = "channel-id"
        val channelType = "channel-type"
        val extraData = mapOf("id" to "another-id")
        val updateMessage = null

        whenever(chatApi.updateChannel(channelType, channelId, extraData, updateMessage)) doReturn channel.asCall()

        /* When */
        val result: Result<Channel> = validator.updateChannel(
            channelId = channelId,
            channelType = channelType,
            extraData = extraData,
            updateMessage = updateMessage,
        ).await()

        /* Then */
        result.shouldBeInstanceOf(Result.Failure::class)
        (result as Result.Failure).value.message?.contains("id") `should be equal to` true
    }

    @Test
    fun testUpdateChannelPartial() = runTest {
        /* Given */
        val channel: Channel = mock()
        val channelId = "channel-id"
        val channelType = "channel-type"
        val set = mapOf("type" to "another-type")
        val unset = emptyList<String>()

        whenever(chatApi.updateChannelPartial(channelType, channelId, set, unset)) doReturn channel.asCall()

        /* When */
        val result: Result<Channel> = validator.updateChannelPartial(
            channelId = channelId,
            channelType = channelType,
            set = set,
            unset = unset,
        ).await()

        /* Then */
        result.shouldBeInstanceOf(Result.Failure::class)
        (result as Result.Failure).value.message?.contains("type") `should be equal to` true
    }

    @Test
    fun testUpdateMessage() = runTest {
        /* Given */
        val message: Message = mock()
        val extraData: MutableMap<String, Any> = mutableMapOf("cid" to "another-cid")

        whenever(message.extraData) doReturn extraData
        whenever(chatApi.updateMessage(message)) doReturn message.asCall()

        /* When */
        val result: Result<Message> = validator.updateMessage(message).await()

        /* Then */
        result.shouldBeInstanceOf(Result.Failure::class)
        (result as Result.Failure).value.message?.contains("cid") `should be equal to` true
    }

    @Test
    fun testPartialUpdateMessage() = runTest {
        /* Given */
        val messageId = "message-id"
        val message: Message = mock()
        val set: MutableMap<String, Any> = mutableMapOf("created_at" to "another-date")
        val unset = emptyList<String>()

        whenever(chatApi.partialUpdateMessage(messageId, set, unset)) doReturn message.asCall()

        /* When */
        val result: Result<Message> = validator.partialUpdateMessage(messageId, set, unset).await()

        /* Then */
        result.shouldBeInstanceOf(Result.Failure::class)
        (result as Result.Failure).value.message?.contains("created_at") `should be equal to` true
    }

    @Test
    fun testUpdateUsers() = runTest {
        /* Given */
        val user: User = mock()
        val users = listOf(user)
        val extraData: MutableMap<String, Any> = mutableMapOf(
            "cid" to "another-cid",
            "updated_at" to "another-date",
        )
        whenever(user.extraData) doReturn extraData
        whenever(chatApi.updateUsers(users)) doReturn users.asCall()

        /* When */
        val result: Result<List<User>> = validator.updateUsers(users).await()

        /* Then */
        result.shouldBeInstanceOf(Result.Failure::class)
        (result as Result.Failure).value.message?.contains("cid") `should be equal to` true
        result.value.message?.contains("updated_at") `should be equal to` true
    }

    @Test
    fun testPartialUpdateUser() = runTest {
        /* Given */
        val userId = "user-id"
        val user: User = mock()
        val set: MutableMap<String, Any> = mutableMapOf(
            "updated_at" to "another-date",
            "created_at" to "another-date",
        )
        val unset = emptyList<String>()
        whenever(chatApi.partialUpdateUser(userId, set, unset)) doReturn user.asCall()

        /* When */
        val result: Result<User> = validator.partialUpdateUser(userId, set, unset).await()

        /* Then */
        result.shouldBeInstanceOf(Result.Failure::class)
        (result as Result.Failure).value.message?.contains("updated_at") `should be equal to` true
        result.value.message?.contains("created_at") `should be equal to` true
        println(result.value.message)
    }
}
