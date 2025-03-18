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
import io.getstream.chat.android.client.utils.RetroError
import io.getstream.chat.android.client.utils.RetroSuccess
import io.getstream.chat.android.client.utils.verifyNetworkError
import io.getstream.chat.android.client.utils.verifySuccess
import io.getstream.chat.android.models.AppSettings
import io.getstream.chat.android.positiveRandomInt
import io.getstream.chat.android.randomAppSettings
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever

/**
 * Test class for the config functionality of the [ChatClient].
 */
internal class ChatClientConfigApiTest : BaseChatClientTest() {

    @Test
    fun appSettingsSuccess() = runTest {
        // given
        val appSettings = randomAppSettings()
        whenever(api.appSettings())
            .doReturn(RetroSuccess(appSettings).toRetrofitCall())
        // when
        val result = chatClient.appSettings().await()
        // then
        verifySuccess(result, appSettings)
    }

    @Test
    fun appSettingsError() = runTest {
        // given
        val errorCode = positiveRandomInt()
        whenever(api.appSettings())
            .doReturn(RetroError<AppSettings>(errorCode).toRetrofitCall())
        // when
        val result = chatClient.appSettings().await()
        // then
        verifyNetworkError(result, errorCode)
    }
}
