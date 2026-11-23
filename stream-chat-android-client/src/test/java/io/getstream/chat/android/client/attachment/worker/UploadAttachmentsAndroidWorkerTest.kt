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

package io.getstream.chat.android.client.attachment.worker

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.randomString
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doSuspendableAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.mockito.kotlin.wheneverBlocking

@RunWith(AndroidJUnit4::class)
internal class UploadAttachmentsAndroidWorkerTest {

    private val instanceField = ChatClient::class.java.getDeclaredField("instance").apply { isAccessible = true }
    private var previousInstance: Any? = null

    private val messageId = randomString()
    private val emptyRepositoryFacade: RepositoryFacade = mock()
    private val userRepositoryFacade: RepositoryFacade = mock()
    private val chatClient: ChatClient = mock()

    @Before
    fun setUp() {
        previousInstance = instanceField.get(null)
        instanceField.set(null, chatClient)
    }

    @After
    fun tearDown() {
        instanceField.set(null, previousInstance)
    }

    @Test
    fun `Given a process where no user is set When the work runs Should read the message of the stored user`() =
        runTest {
            var userSet = false
            wheneverBlocking { chatClient.setUserWithoutConnectingIfNeeded() } doSuspendableAnswer { userSet = true }
            whenever(chatClient.repositoryFacade) doAnswer {
                if (userSet) userRepositoryFacade else emptyRepositoryFacade
            }
            val worker = TestListenableWorkerBuilder<UploadAttachmentsAndroidWorker>(
                ApplicationProvider.getApplicationContext<Context>(),
            ).setInputData(
                workDataOf(
                    "channel_type" to "messaging",
                    "channel_id" to randomString(),
                    "message_id" to messageId,
                ),
            ).build()

            worker.doWork()

            verifyBlocking(userRepositoryFacade) { selectMessage(messageId) }
            verifyNoInteractions(emptyRepositoryFacade)
        }
}
