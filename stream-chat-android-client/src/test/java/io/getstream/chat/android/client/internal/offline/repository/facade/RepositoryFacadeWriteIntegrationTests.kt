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

package io.getstream.chat.android.client.internal.offline.repository.facade

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.client.internal.offline.integration.BaseRepositoryFacadeIntegrationTest
import io.getstream.chat.android.client.internal.offline.repository.domain.threads.internal.toEntity
import io.getstream.chat.android.client.persistance.repository.write
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomThread
import io.getstream.chat.android.randomUser
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldNotBeNull
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class RepositoryFacadeWriteIntegrationTests : BaseRepositoryFacadeIntegrationTest() {

    private val user = randomUser()
    private val channel = randomChannel(createdBy = user)
    private val message = randomMessage(cid = channel.cid, user = user, deletedAt = null, deletedForMe = false)
    private val thread = randomThread(
        cid = channel.cid,
        channel = channel,
        parentMessageId = message.id,
        parentMessage = message,
        createdBy = user,
        latestReplies = emptyList(),
        draftMessage = null,
    )

    @Test
    fun `Given a write of users, channels and messages When it ends Should store all of them`() = runWriteTest {
        repositoryFacade.write {
            repositoryFacade.insertUsers(listOf(user))
            repositoryFacade.insertChannels(listOf(channel))
            repositoryFacade.insertMessages(listOf(message))
        }

        chatDatabase.userDao().select(user.id).shouldNotBeNull()
        chatDatabase.channelStateDao().select(channel.cid).shouldNotBeNull()
        chatDatabase.messageDao().select(message.id).shouldNotBeNull()
    }

    @Test
    fun `Given a write of users, channels and messages When it fails at its end Should store none of them`() =
        runWriteTest {
            assertThrows(IllegalStateException::class.java) {
                runBlocking {
                    repositoryFacade.write {
                        repositoryFacade.insertUsers(listOf(user))
                        repositoryFacade.insertChannels(listOf(channel))
                        repositoryFacade.insertMessages(listOf(message))
                        error("failed")
                    }
                }
            }

            chatDatabase.userDao().select(user.id) shouldBeEqualTo null
            chatDatabase.channelStateDao().select(channel.cid) shouldBeEqualTo null
            chatDatabase.messageDao().select(message.id) shouldBeEqualTo null
        }

    @Test
    fun `Given a failed write When the same data is written again Should store it`() = runWriteTest {
        runCatching {
            repositoryFacade.write {
                repositoryFacade.insertUsers(listOf(user))
                repositoryFacade.insertChannels(listOf(channel))
                repositoryFacade.insertMessages(listOf(message))
                error("failed")
            }
        }

        repositoryFacade.write {
            repositoryFacade.insertUsers(listOf(user))
            repositoryFacade.insertChannels(listOf(channel))
            repositoryFacade.insertMessages(listOf(message))
        }

        chatDatabase.userDao().select(user.id).shouldNotBeNull()
        chatDatabase.channelStateDao().select(channel.cid).shouldNotBeNull()
        chatDatabase.messageDao().select(message.id).shouldNotBeNull()
    }

    @Test
    fun `Given a write When it reads channels with their messages Should not wait for itself`() = runWriteTest {
        val cids = repositoryFacade.write {
            repositoryFacade.insertChannels(listOf(channel))
            repositoryFacade.insertMessages(listOf(message))
            repositoryFacade.selectChannels(listOf(channel.cid)).map { it.cid }
        }

        cids shouldBeEqualTo listOf(channel.cid)
    }

    @Test
    fun `Given a stored thread When a reply is stored Should add the reply to the stored thread`() =
        runWriteTest {
            // Stored without the repository, so the write reads the thread from the database
            chatDatabase.threadDao().insertThread(thread.toEntity())
            val reply = randomMessage(
                cid = channel.cid,
                user = user,
                parentId = thread.parentMessageId,
                deletedAt = null,
                deletedForMe = false,
            )

            repositoryFacade.insertMessage(reply)

            chatDatabase.threadDao().selectThread(thread.parentMessageId)?.latestReplyIds shouldBeEqualTo
                listOf(reply.id)
            repositoryFacade.selectThread(thread.parentMessageId)?.latestReplies?.map { it.id } shouldBeEqualTo
                listOf(reply.id)
        }

    @Test
    fun `Given a write of a thread When it fails at its end Should neither store nor cache the thread`() =
        runWriteTest {
            val result = runCatching {
                repositoryFacade.write {
                    repositoryFacade.insertThreads(listOf(thread))
                    error("failed")
                }
            }

            result.exceptionOrNull()?.message shouldBeEqualTo "failed"
            chatDatabase.threadDao().selectThread(thread.parentMessageId) shouldBeEqualTo null
            repositoryFacade.selectThread(thread.parentMessageId) shouldBeEqualTo null
        }

    private fun runWriteTest(testBody: suspend () -> Unit) {
        // A deadlock fails the test instead of hanging it.
        runBlocking { withTimeout(TIMEOUT_MILLIS) { testBody() } }
    }

    private companion object {
        private const val TIMEOUT_MILLIS = 10_000L
    }
}
