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

package io.getstream.chat.android.offline.repository.integration

import android.database.sqlite.SQLiteException
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.offline.integration.BaseDomainTest2
import io.getstream.chat.android.offline.randomAttachment
import io.getstream.chat.android.offline.randomMessage
import io.getstream.chat.android.test.randomString
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.coInvoking
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.shouldNotThrow
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class MessageRepositoryTest : BaseDomainTest2() {

    @Test
    fun `Given message with 3 attachments When update it in DB Should keep only 3 newer attachments`(): Unit =
        runBlocking {
            val attachment1 = randomAttachment { url = "url1" }
            val attachment2 = randomAttachment { url = "url2" }
            val attachment3 = randomAttachment { url = "url3" }
            val message = randomMessage(attachments = mutableListOf(attachment1, attachment2, attachment3))
            repos.insertMessage(message)

            val newAttachment1 = attachment1.copy(url = "newUrl1")
            val newAttachment2 = attachment2.copy(url = "newUrl2")
            val newAttachment3 = attachment3.copy(url = "newUrl3")
            message.attachments = mutableListOf(newAttachment1, newAttachment2, newAttachment3)
            repos.insertMessage(message)

            val messageFromDb = requireNotNull(repos.selectMessage(message.id))

            messageFromDb.attachments.size `should be equal to` 3
            messageFromDb.attachments[0].url `should be equal to` "newUrl1"
            messageFromDb.attachments[1].url `should be equal to` "newUrl2"
            messageFromDb.attachments[2].url `should be equal to` "newUrl3"
        }

    @Test
    fun `When selecting more than 999 messages Should not throw SQLiteException`(): Unit = runBlocking {
        coInvoking { repos.selectMessages(List(1000) { randomString() }) } shouldNotThrow (SQLiteException::class)
    }

    @Test
    fun `When inserting more than 999 messages Should not throw SQLiteException`(): Unit = runBlocking {
        coInvoking { repos.insertMessages(List(1000) { randomMessage() }) } shouldNotThrow (SQLiteException::class)
    }
}
