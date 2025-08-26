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
import io.getstream.chat.android.client.utils.ProgressCallback
import io.getstream.chat.android.client.utils.RetroSuccess
import io.getstream.chat.android.client.utils.verifySuccess
import io.getstream.chat.android.randomFile
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomUploadedFile
import io.getstream.chat.android.randomUser
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever

internal class ChatClientStandaloneFileUploaderTests : BaseChatClientTest() {

    @Test
    fun `upload file with user and progress callback should return success result`() = runTest {
        val file = randomFile()
        val user = randomUser()
        val progressCallback = mock<ProgressCallback>()
        val uploadedFile = randomUploadedFile()
        whenever(api.uploadFile(file, user, progressCallback)) doReturn
            RetroSuccess(uploadedFile).toRetrofitCall()

        val result = chatClient.uploadFile(file, user, progressCallback).await()

        verifySuccess(result, equalsTo = uploadedFile)
    }

    @Test
    fun `upload file without user and progress callback should return success result`() = runTest {
        val file = randomFile()
        val uploadedFile = randomUploadedFile()
        whenever(api.uploadFile(file, user = null, progressCallback = null)) doReturn
            RetroSuccess(uploadedFile).toRetrofitCall()

        val result = chatClient.uploadFile(file).await()

        verifySuccess(result, equalsTo = uploadedFile)
    }

    @Test
    fun `upload image with user and progress callback should return success result`() = runTest {
        val file = randomFile()
        val user = randomUser()
        val progressCallback = mock<ProgressCallback>()
        val uploadedFile = randomUploadedFile()
        whenever(api.uploadImage(file, user, progressCallback)) doReturn
            RetroSuccess(uploadedFile).toRetrofitCall()

        val result = chatClient.uploadImage(file, user, progressCallback).await()

        verifySuccess(result, equalsTo = uploadedFile)
    }

    @Test
    fun `upload image without user and progress callback should return success result`() = runTest {
        val file = randomFile()
        val uploadedFile = randomUploadedFile()
        whenever(api.uploadImage(file, user = null, progressCallback = null)) doReturn
            RetroSuccess(uploadedFile).toRetrofitCall()

        val result = chatClient.uploadImage(file).await()

        verifySuccess(result, equalsTo = uploadedFile)
    }

    @Test
    fun `delete file with user ID should return success result`() = runTest {
        val url = randomString()
        val userId = randomString()
        whenever(api.deleteFile(url, userId)) doReturn
            RetroSuccess(Unit).toRetrofitCall()

        val result = chatClient.deleteFile(url, userId).await()

        verifySuccess(result, equalsTo = Unit)
    }

    @Test
    fun `delete file without user ID should return success result`() = runTest {
        val url = randomString()
        whenever(api.deleteFile(url, userId = null)) doReturn
            RetroSuccess(Unit).toRetrofitCall()

        val result = chatClient.deleteFile(url).await()

        verifySuccess(result, equalsTo = Unit)
    }

    @Test
    fun `delete image with user ID should return success result`() = runTest {
        val url = randomString()
        val userId = randomString()
        whenever(api.deleteImage(url, userId)) doReturn
            RetroSuccess(Unit).toRetrofitCall()

        val result = chatClient.deleteImage(url, userId).await()

        verifySuccess(result, equalsTo = Unit)
    }

    @Test
    fun `delete image without user ID should return success result`() = runTest {
        val url = randomString()
        whenever(api.deleteImage(url, userId = null)) doReturn
            RetroSuccess(Unit).toRetrofitCall()

        val result = chatClient.deleteImage(url).await()

        verifySuccess(result, equalsTo = Unit)
    }
}
