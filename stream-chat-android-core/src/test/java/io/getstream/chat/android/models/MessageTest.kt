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

package io.getstream.chat.android.models

import io.getstream.chat.android.randomAttachment
import io.getstream.chat.android.randomBoolean
import io.getstream.chat.android.randomCID
import io.getstream.chat.android.randomChannelInfo
import io.getstream.chat.android.randomDate
import io.getstream.chat.android.randomInt
import io.getstream.chat.android.randomLocation
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomMessageModerationDetails
import io.getstream.chat.android.randomMessageReminderInfo
import io.getstream.chat.android.randomModeration
import io.getstream.chat.android.randomPoll
import io.getstream.chat.android.randomReaction
import io.getstream.chat.android.randomReactionGroup
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomSyncStatus
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.randomUserGroup
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class MessageTest {

    @Test
    @Suppress("LongMethod")
    fun `builder should set every field`() {
        val expected = Message(
            id = randomString(),
            cid = randomCID(),
            text = randomString(),
            html = randomString(),
            parentId = randomString(),
            command = randomString(),
            attachments = listOf(randomAttachment()),
            mentionedUsersIds = listOf(randomString()),
            mentionedUsers = listOf(randomUser()),
            replyCount = randomInt(),
            deletedReplyCount = randomInt(),
            reactionCounts = mapOf(randomString() to randomInt()),
            reactionScores = mapOf(randomString() to randomInt()),
            reactionGroups = mapOf(randomString() to randomReactionGroup()),
            syncStatus = randomSyncStatus(),
            type = randomString(),
            latestReactions = listOf(randomReaction()),
            ownReactions = listOf(randomReaction()),
            createdAt = randomDate(),
            updatedAt = randomDate(),
            deletedAt = randomDate(),
            updatedLocallyAt = randomDate(),
            createdLocallyAt = randomDate(),
            user = randomUser(),
            extraData = mapOf(randomString() to randomString()),
            silent = randomBoolean(),
            shadowed = randomBoolean(),
            i18n = mapOf(randomString() to randomString()),
            showInChannel = randomBoolean(),
            channelInfo = randomChannelInfo(),
            replyTo = randomMessage(),
            replyMessageId = randomString(),
            pinned = randomBoolean(),
            pinnedAt = randomDate(),
            pinExpires = randomDate(),
            pinnedBy = randomUser(),
            threadParticipants = listOf(randomUser()),
            skipPushNotification = randomBoolean(),
            skipEnrichUrl = randomBoolean(),
            moderationDetails = randomMessageModerationDetails(),
            moderation = randomModeration(),
            messageTextUpdatedAt = randomDate(),
            poll = randomPoll(),
            restrictedVisibility = listOf(randomString()),
            reminder = randomMessageReminderInfo(),
            sharedLocation = randomLocation(),
            channelRole = randomString(),
            deletedForMe = randomBoolean(),
            mentionedHere = randomBoolean(),
            mentionedChannel = randomBoolean(),
            mentionedGroups = listOf(randomUserGroup()),
            mentionedRoles = listOf(randomString()),
        )

        val built = Message.Builder()
            .withId(expected.id)
            .withCid(expected.cid)
            .withText(expected.text)
            .withHtml(expected.html)
            .withParentId(expected.parentId)
            .withCommand(expected.command)
            .withAttachments(expected.attachments)
            .withMentionedUsersIds(expected.mentionedUsersIds)
            .withMentionedUsers(expected.mentionedUsers)
            .withReplyCount(expected.replyCount)
            .withDeletedReplyCount(expected.deletedReplyCount)
            .withReactionCounts(expected.reactionCounts)
            .withReactionScores(expected.reactionScores)
            .withReactionGroups(expected.reactionGroups)
            .withSyncStatus(expected.syncStatus)
            .withType(expected.type)
            .withLatestReactions(expected.latestReactions)
            .withOwnReactions(expected.ownReactions)
            .withCreatedAt(expected.createdAt)
            .withUpdatedAt(expected.updatedAt)
            .withDeletedAt(expected.deletedAt)
            .withUpdatedLocallyAt(expected.updatedLocallyAt)
            .withCreatedLocallyAt(expected.createdLocallyAt)
            .withUser(expected.user)
            .withExtraData(expected.extraData)
            .withSilent(expected.silent)
            .withShadowed(expected.shadowed)
            .withI18n(expected.i18n)
            .withShowInChannel(expected.showInChannel)
            .withChannelInfo(expected.channelInfo)
            .withReplyTo(expected.replyTo)
            .withReplyMessageId(expected.replyMessageId)
            .withPinned(expected.pinned)
            .withPinnedAt(expected.pinnedAt)
            .withPinExpires(expected.pinExpires)
            .withPinnedBy(expected.pinnedBy)
            .withThreadParticipants(expected.threadParticipants)
            .withSkipPushNotification(expected.skipPushNotification)
            .withSkipEnrichUrl(expected.skipEnrichUrl)
            .withModerationDetails(requireNotNull(expected.moderationDetails))
            .withModeration(requireNotNull(expected.moderation))
            .withMessageTextUpdatedAt(expected.messageTextUpdatedAt)
            .withPoll(expected.poll)
            .withRestrictedVisibility(expected.restrictedVisibility)
            .withReminder(expected.reminder)
            .withSharedLocation(expected.sharedLocation)
            .withChannelRole(expected.channelRole)
            .withDeletedForMe(expected.deletedForMe)
            .withMentionedHere(expected.mentionedHere)
            .withMentionedChannel(expected.mentionedChannel)
            .withMentionedGroups(expected.mentionedGroups)
            .withMentionedRoles(expected.mentionedRoles)
            .build()

        assertEquals(expected, built)
    }

    @Test
    fun `builder copy constructor should copy every field`() {
        val message = randomMessage(
            replyTo = randomMessage(),
            poll = randomPoll(),
            moderationDetails = randomMessageModerationDetails(),
            moderation = randomModeration(),
            channelRole = randomString(),
            threadParticipants = listOf(randomUser()),
            mentionedGroups = listOf(randomUserGroup()),
            mentionedRoles = listOf(randomString()),
        )

        val built = Message.Builder(message).build()

        assertEquals(message, built)
    }

    @Test
    fun `getComparableField should return string fields`() {
        val message = randomMessage()
        assertEquals(message.id, message.getComparableField("id"))
        assertEquals(message.cid, message.getComparableField("cid"))
        assertEquals(message.text, message.getComparableField("text"))
        assertEquals(message.html, message.getComparableField("html"))
        assertEquals(message.command, message.getComparableField("command"))
        assertEquals(message.type, message.getComparableField("type"))
    }

    @Test
    fun `getComparableField should return parentId for snake_case and camelCase field names`() {
        val message = randomMessage()
        assertEquals(message.parentId, message.getComparableField("parent_id"))
        assertEquals(message.parentId, message.getComparableField("parentId"))
    }

    @Test
    fun `getComparableField should return count fields for snake_case and camelCase field names`() {
        val message = randomMessage()
        assertEquals(message.replyCount, message.getComparableField("reply_count"))
        assertEquals(message.replyCount, message.getComparableField("replyCount"))
        assertEquals(message.deletedReplyCount, message.getComparableField("deleted_reply_count"))
        assertEquals(message.deletedReplyCount, message.getComparableField("deletedReplyCount"))
    }

    @Test
    fun `getComparableField should return date fields for snake_case and camelCase field names`() {
        val message = randomMessage(deletedAt = randomDate())
        assertEquals(message.createdAt, message.getComparableField("created_at"))
        assertEquals(message.createdAt, message.getComparableField("createdAt"))
        assertEquals(message.updatedAt, message.getComparableField("updated_at"))
        assertEquals(message.updatedAt, message.getComparableField("updatedAt"))
        assertEquals(message.deletedAt, message.getComparableField("deleted_at"))
        assertEquals(message.deletedAt, message.getComparableField("deletedAt"))
        assertEquals(message.updatedLocallyAt, message.getComparableField("updated_locally_at"))
        assertEquals(message.updatedLocallyAt, message.getComparableField("updatedLocallyAt"))
        assertEquals(message.createdLocallyAt, message.getComparableField("created_locally_at"))
        assertEquals(message.createdLocallyAt, message.getComparableField("createdLocallyAt"))
        assertEquals(message.pinnedAt, message.getComparableField("pinned_at"))
        assertEquals(message.pinnedAt, message.getComparableField("pinnedAt"))
        assertEquals(message.pinExpires, message.getComparableField("pin_expires"))
        assertEquals(message.pinExpires, message.getComparableField("pinExpires"))
    }

    @Test
    fun `getComparableField should return boolean fields`() {
        val message = randomMessage()
        assertEquals(message.silent, message.getComparableField("silent"))
        assertEquals(message.shadowed, message.getComparableField("shadowed"))
        assertEquals(message.pinned, message.getComparableField("pinned"))
    }

    @Test
    fun `getComparableField should return extraData value for custom field`() {
        val message = randomMessage(extraData = mapOf("customField" to "customValue"))
        assertEquals("customValue", message.getComparableField("customField"))
    }

    @Test
    fun `getComparableField should return null for unknown field`() {
        val message = randomMessage(extraData = emptyMap())
        assertNull(message.getComparableField("unknownField"))
    }

    @Test
    fun `getTranslation should return the translated text for the given language`() {
        val message = randomMessage(i18n = mapOf("fr_text" to "Bonjour"))
        assertEquals("Bonjour", message.getTranslation("fr"))
    }

    @Test
    fun `getTranslation should return empty string when the translation is missing`() {
        val message = randomMessage(i18n = emptyMap())
        assertEquals("", message.getTranslation("fr"))
    }

    @Test
    fun `originalLanguage should return the language entry from i18n`() {
        val message = randomMessage(i18n = mapOf("language" to "en"))
        assertEquals("en", message.originalLanguage)
    }

    @Test
    fun `originalLanguage should return empty string when i18n has no language entry`() {
        val message = randomMessage(i18n = emptyMap())
        assertEquals("", message.originalLanguage)
    }

    @Test
    fun `identifierHash should be based on the message id when there is no quoted message`() {
        val message = randomMessage(id = "message1", replyTo = null)
        assertEquals("message1".hashCode().toLong(), message.identifierHash())
    }

    @Test
    fun `identifierHash should change when the quoted message id changes`() {
        val message = randomMessage(id = "message1", replyTo = null)
        val quotingReply1 = message.copy(replyTo = randomMessage(id = "reply1"))
        val quotingReply2 = message.copy(replyTo = randomMessage(id = "reply2"))
        assertNotEquals(message.identifierHash(), quotingReply1.identifierHash())
        assertNotEquals(quotingReply1.identifierHash(), quotingReply2.identifierHash())
    }

    @Test
    fun `toString should contain the core message fields`() {
        val message = randomMessage(id = "message1", text = "Hello", type = "regular")
        val string = message.toString()
        assertTrue(string.contains("id=\"message1\""))
        assertTrue(string.contains("text=\"Hello\""))
        assertTrue(string.contains("type=\"regular\""))
    }
}
