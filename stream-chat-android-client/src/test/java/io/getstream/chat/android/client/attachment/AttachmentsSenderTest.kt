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

package io.getstream.chat.android.client.attachment

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.UploadAttachmentsNetworkType
import io.getstream.chat.android.randomString
import io.getstream.result.Error
import io.getstream.result.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.plus
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import org.amshove.kluent.shouldBeEmpty
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.robolectric.annotation.Config
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
internal class AttachmentsSenderTest {

    @Before
    fun setUp() {
        WorkManagerTestInitHelper.initializeTestWorkManager(ApplicationProvider.getApplicationContext())
    }

    @After
    fun tearDown() {
        AttachmentsUploadStates.clearStates()
    }

    private val channelId = randomString()

    private fun TestScope.sender() = AttachmentsSender(
        context = ApplicationProvider.getApplicationContext(),
        networkType = UploadAttachmentsNetworkType.CONNECTED,
        clientState = mock<ClientState> { on { isNetworkAvailable } doReturn true },
        scope = backgroundScope + UnconfinedTestDispatcher(testScheduler),
    )

    private fun message(uploadState: Attachment.UploadState, assetUrl: String? = null) = Message(
        id = randomString(),
        attachments = listOf(Attachment(upload = File(randomString()), uploadState = uploadState, assetUrl = assetUrl)),
    )

    private fun uploadWork(message: Message) = WorkManager.getInstance(ApplicationProvider.getApplicationContext())
        .getWorkInfosForUniqueWork("$channelId${message.id}")
        .get()

    @Test
    fun `retrying a message whose attachments are already uploaded sends it without uploading`() = runTest {
        val message = message(Attachment.UploadState.Success, assetUrl = "https://cdn/${randomString()}")

        val result = withTimeout(10_000) {
            sender().sendAttachments(message, "messaging", channelId, isRetrying = true)
        }

        result shouldBeEqualTo Result.Success(message)
        uploadWork(message).shouldBeEmpty()
    }

    @Test
    fun `a pending attachment is sent once its upload succeeds`() = runTest {
        val message = message(Attachment.UploadState.Idle)
        val uploaded = message.attachments.map {
            it.copy(uploadState = Attachment.UploadState.Success, assetUrl = "https://cdn/${randomString()}")
        }

        val result = async { sender().sendAttachments(message, "messaging", channelId, isRetrying = false) }
        runCurrent()
        uploadWork(message).size shouldBeEqualTo 1
        AttachmentsUploadStates.updateMessageAttachments(message.copy(attachments = uploaded))

        result.await() shouldBeEqualTo
            Result.Success(message.copy(attachments = uploaded, type = Message.TYPE_REGULAR))
    }

    @Test
    fun `a pending attachment whose upload fails isn't sent`() = runTest {
        val message = message(Attachment.UploadState.Idle)
        val failed = message.attachments.map {
            it.copy(uploadState = Attachment.UploadState.Failed(Error.GenericError(randomString())))
        }

        val result = async { sender().sendAttachments(message, "messaging", channelId, isRetrying = false) }
        runCurrent()
        AttachmentsUploadStates.updateMessageAttachments(message.copy(attachments = failed))

        result.await() shouldBeInstanceOf Result.Failure::class
    }
}
