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

package io.getstream.chat.android.client

import io.getstream.chat.android.client.chatclient.BaseChatClientTest
import io.getstream.chat.android.client.utils.RetroError
import io.getstream.chat.android.client.utils.RetroSuccess
import io.getstream.chat.android.client.utils.verifyNetworkError
import io.getstream.chat.android.client.utils.verifySuccess
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.positiveRandomInt
import io.getstream.chat.android.randomAttachment
import io.getstream.chat.android.randomString
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

/**
 * Covers the 'enrich URL' functionality of the [ChatClient].
 */
internal class ChatClientEnrichUrlApiTests : BaseChatClientTest() {

    @Test
    fun enrichUrlSuccess() = runTest {
        // given
        val url = randomString()
        val enrichment = randomAttachment()
        whenever(api.og(any()))
            .thenReturn(RetroSuccess(enrichment).toRetrofitCall())
        // when
        val result = chatClient.enrichUrl(url).await()
        // then
        verifySuccess(result, enrichment)
    }

    @Test
    fun enrichUrlError() = runTest {
        // given
        val url = randomString()
        val errorCode = positiveRandomInt()
        whenever(api.og(any()))
            .thenReturn(RetroError<Attachment>(errorCode).toRetrofitCall())
        // when
        val result = chatClient.enrichUrl(url).await()
        // then
        verifyNetworkError(result, errorCode)
    }
}
