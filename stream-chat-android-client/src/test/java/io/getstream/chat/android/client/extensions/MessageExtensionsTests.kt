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

package io.getstream.chat.android.client.extensions

import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.randomDate
import io.getstream.chat.android.randomMessage
import io.getstream.result.Error
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test
import java.util.Date

internal class MessageExtensionsTests {

    @Test
    fun `enrichWithCid should set cid for message and replyTo`() {
        val replyMessage = randomMessage(id = "reply-id")
        val message = randomMessage(id = "message-id", replyTo = replyMessage)
        val enrichedMessage = message.enrichWithCid("channel:123")

        enrichedMessage.cid shouldBeEqualTo "channel:123"
        enrichedMessage.replyTo?.cid shouldBeEqualTo "channel:123"
    }

    @Test
    fun `updateFailedMessage should set syncStatus to FAILED_PERMANENTLY for permanent error`() {
        val message = randomMessage()
        val error = Error.NetworkError("Permanent error", statusCode = 404, serverErrorCode = 404)
        val updatedMessage = message.updateFailedMessage(error)

        updatedMessage.syncStatus shouldBeEqualTo SyncStatus.FAILED_PERMANENTLY
    }

    @Test
    fun `updateFailedMessage should set syncStatus to SYNC_NEEDED for non-permanent error`() {
        val message = randomMessage()
        val error = Error.GenericError("Generic error")
        val updatedMessage = message.updateFailedMessage(error)

        updatedMessage.syncStatus shouldBeEqualTo SyncStatus.SYNC_NEEDED
    }

    @Test
    fun `updateMessageOnlineState should set syncStatus to IN_PROGRESS when online`() {
        val message = randomMessage()
        val updatedMessage = message.updateMessageOnlineState(true)

        updatedMessage.syncStatus shouldBeEqualTo SyncStatus.IN_PROGRESS
    }

    @Test
    fun `updateMessageOnlineState should set syncStatus to SYNC_NEEDED when offline`() {
        val message = randomMessage()
        val updatedMessage = message.updateMessageOnlineState(false)

        updatedMessage.syncStatus shouldBeEqualTo SyncStatus.SYNC_NEEDED
    }

    @Test
    fun `getCreatedAtOrThrow should return createdAt if not null`() {
        val date = randomDate()
        val message = randomMessage(createdAt = date, createdLocallyAt = null)
        message.getCreatedAtOrThrow() shouldBeEqualTo date
    }

    @Test(expected = IllegalStateException::class)
    fun `getCreatedAtOrThrow should throw exception if createdAt and createdLocallyAt are null`() {
        val message = randomMessage(createdAt = null, createdLocallyAt = null)
        message.getCreatedAtOrThrow()
    }

    @Test
    fun `getCreatedAtOrNull should return createdAt if not null`() {
        val date = randomDate()
        val message = randomMessage(createdAt = date, createdLocallyAt = null)
        message.getCreatedAtOrNull() shouldBeEqualTo date
    }

    @Test
    fun `getCreatedAtOrNull should return createdLocallyAt if createdAt is null`() {
        val date = randomDate()
        val message = randomMessage(createdAt = null, createdLocallyAt = date)
        message.getCreatedAtOrNull() shouldBeEqualTo date
    }

    @Test
    fun `getCreatedAtOrNull should return createdLocallyAt if both createdAt and createdLocallyAt are not null`() {
        val createdAtDate = randomDate()
        val createdLocallyAtDate = randomDate()
        val message = randomMessage(createdAt = createdAtDate, createdLocallyAt = createdLocallyAtDate)
        message.getCreatedAtOrNull() shouldBeEqualTo createdLocallyAtDate
    }

    @Test
    fun `getCreatedAtOrNull should return null if createdAt and createdLocallyAt are null`() {
        val message = randomMessage(createdAt = null, createdLocallyAt = null)
        message.getCreatedAtOrNull() shouldBeEqualTo null
    }

    @Test
    fun `getCreatedAtOrDefault should return createdAt if not null`() {
        val date = randomDate()
        val message = randomMessage(createdAt = date, createdLocallyAt = null)
        message.getCreatedAtOrDefault(Date(0)) shouldBeEqualTo date
    }

    @Test
    fun `getCreatedAtOrDefault should return default if createdAt and createdLocallyAt are null`() {
        val defaultDate = randomDate()
        val message = randomMessage(createdAt = null, createdLocallyAt = null)
        message.getCreatedAtOrDefault(defaultDate) shouldBeEqualTo defaultDate
    }
}
