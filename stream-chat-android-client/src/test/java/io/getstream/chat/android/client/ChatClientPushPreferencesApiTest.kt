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
import io.getstream.chat.android.client.utils.RetroError
import io.getstream.chat.android.client.utils.RetroSuccess
import io.getstream.chat.android.client.utils.verifyNetworkError
import io.getstream.chat.android.client.utils.verifySuccess
import io.getstream.chat.android.models.PushPreference
import io.getstream.chat.android.models.PushPreferenceLevel
import io.getstream.chat.android.models.User
import io.getstream.chat.android.positiveRandomInt
import io.getstream.chat.android.randomDate
import io.getstream.chat.android.randomString
import io.getstream.result.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

/**
 * Test class for the push preferences functionality of the [ChatClient].
 */
internal class ChatClientPushPreferencesApiTest : BaseChatClientTest() {

    @Test
    fun `setUserPushPreference success should return push preference and update user state`() = runTest {
        // given
        val level = PushPreferenceLevel.mentions
        val pushPreference = PushPreference(level = level, disabledUntil = null)
        val currentUser = User(id = "user1", name = "Test User")
        val userStateFlow = MutableStateFlow(currentUser)

        whenever(api.setUserPushPreference(level))
            .doReturn(RetroSuccess(pushPreference).toRetrofitCall())
        whenever(mutableClientState.user).doReturn(userStateFlow)

        // when
        val result = chatClient.setUserPushPreference(level).await()

        // then
        verifySuccess(result, pushPreference)
        verify(mutableClientState).setUser(currentUser.copy(pushPreference = pushPreference))
    }

    @Test
    fun `setUserPushPreference error should return error`() = runTest {
        // given
        val level = PushPreferenceLevel.none
        val errorCode = positiveRandomInt()

        whenever(api.setUserPushPreference(level))
            .doReturn(RetroError<PushPreference>(errorCode).toRetrofitCall())

        // when
        val result = chatClient.setUserPushPreference(level).await()

        // then
        verifyNetworkError(result, errorCode)
        verifyNoInteractions(mutableClientState)
    }

    @Test
    fun `snoozeUserPushNotifications success should return push preference and update user state`() = runTest {
        // given
        val until = randomDate()
        val pushPreference = PushPreference(level = null, disabledUntil = until)
        val currentUser = User(id = "user1", name = "Test User")
        val userStateFlow = MutableStateFlow(currentUser)

        whenever(api.snoozeUserPushNotifications(until))
            .doReturn(RetroSuccess(pushPreference).toRetrofitCall())
        whenever(mutableClientState.user).doReturn(userStateFlow)

        // when
        val result = chatClient.snoozeUserPushNotifications(until).await()

        // then
        verifySuccess(result, pushPreference)
        verify(mutableClientState).setUser(currentUser.copy(pushPreference = pushPreference))
    }

    @Test
    fun `snoozeUserPushNotifications error should return error`() = runTest {
        // given
        val until = randomDate()
        val errorCode = positiveRandomInt()

        whenever(api.snoozeUserPushNotifications(until))
            .doReturn(RetroError<PushPreference>(errorCode).toRetrofitCall())

        // when
        val result = chatClient.snoozeUserPushNotifications(until).await()

        // then
        verifyNetworkError(result, errorCode)
        verifyNoInteractions(mutableClientState)
    }

    @Test
    fun `setChannelPushPreference success should return push preference and notify plugins`() = runTest {
        // given
        val cid = "messaging:${randomString()}"
        val level = PushPreferenceLevel.mentions
        val pushPreference = PushPreference(level = level, disabledUntil = null)
        val mockPlugin = mock<Plugin>()
        plugins.add(mockPlugin)

        whenever(api.setChannelPushPreference(cid, level))
            .doReturn(RetroSuccess(pushPreference).toRetrofitCall())

        // when
        val result = chatClient.setChannelPushPreference(cid, level).await()

        // then
        verifySuccess(result, pushPreference)
        verify(mockPlugin).onChannelPushPreferenceSet(cid, level, Result.Success(pushPreference))
    }

    @Test
    fun `setChannelPushPreference error should return error and notify plugins`() = runTest {
        // given
        val cid = "messaging:${randomString()}"
        val level = PushPreferenceLevel.all
        val errorCode = positiveRandomInt()
        val mockPlugin = mock<Plugin>()
        plugins.add(mockPlugin)

        whenever(api.setChannelPushPreference(cid, level))
            .doReturn(RetroError<PushPreference>(errorCode).toRetrofitCall())

        // when
        val result = chatClient.setChannelPushPreference(cid, level).await()

        // then
        verifyNetworkError(result, errorCode)
        verify(mockPlugin).onChannelPushPreferenceSet(cid, level, result)
    }

    @Test
    fun `snoozeChannelPushNotifications success should return push preference and notify plugins`() = runTest {
        // given
        val cid = "messaging:${randomString()}"
        val until = randomDate()
        val pushPreference = PushPreference(level = null, disabledUntil = until)
        val mockPlugin = mock<Plugin>()
        plugins.add(mockPlugin)

        whenever(api.snoozeChannelPushNotifications(cid, until))
            .doReturn(RetroSuccess(pushPreference).toRetrofitCall())

        // when
        val result = chatClient.snoozeChannelPushNotifications(cid, until).await()

        // then
        verifySuccess(result, pushPreference)
        verify(mockPlugin).onChannelPushNotificationsSnoozed(cid, until, Result.Success(pushPreference))
    }

    @Test
    fun `snoozeChannelPushNotifications error should return error and notify plugins`() = runTest {
        // given
        val cid = "messaging:${randomString()}"
        val until = randomDate()
        val errorCode = positiveRandomInt()
        val mockPlugin = mock<Plugin>()
        plugins.add(mockPlugin)

        whenever(api.snoozeChannelPushNotifications(cid, until))
            .doReturn(RetroError<PushPreference>(errorCode).toRetrofitCall())

        // when
        val result = chatClient.snoozeChannelPushNotifications(cid, until).await()

        // then
        verifyNetworkError(result, errorCode)
        verify(mockPlugin).onChannelPushNotificationsSnoozed(cid, until, result)
    }
}
