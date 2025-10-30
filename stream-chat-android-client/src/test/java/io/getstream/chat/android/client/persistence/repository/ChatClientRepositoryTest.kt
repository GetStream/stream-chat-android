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

package io.getstream.chat.android.client.persistence.repository

import io.getstream.chat.android.client.persistence.db.ChatClientDatabase
import io.getstream.chat.android.client.persistence.db.dao.MessageReceiptDao
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertNotNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyBlocking

internal class ChatClientRepositoryTest {

    @Test
    fun `should instantiate from database`() {
        val mockDatabase = mock<ChatClientDatabase> {
            on { messageReceiptDao() } doReturn mock<MessageReceiptDao>()
        }

        val actual = ChatClientRepository.from(mockDatabase)

        assertNotNull(actual)
    }

    @Test
    fun `should clear repositories`() = runTest {
        val fixture = Fixture()
        val sut = fixture.get()

        sut.clear()

        fixture.verifyRepositoriesCleared()
    }

    private class Fixture {
        private val mockMessageReceiptRepository = mock<MessageReceiptRepository>()

        fun verifyRepositoriesCleared() {
            verifyBlocking(mockMessageReceiptRepository) { clearMessageReceipts() }
        }

        fun get() = ChatClientRepository(
            messageReceiptRepository = mockMessageReceiptRepository,
        )
    }
}
