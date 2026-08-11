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

package io.getstream.chat.android.client.internal.offline.repository.domain.message.internal

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.client.internal.offline.createRoomDB
import io.getstream.chat.android.client.internal.offline.repository.database.internal.ChatDatabase
import io.getstream.chat.android.models.MemberInfo
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomUser
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Covers that the member snapshot attached to a message (`message.member`) survives a real database round trip, since
 * it is the only place the author's projected member custom data is kept.
 */
@RunWith(AndroidJUnit4::class)
internal class MessageMemberInfoDaoTest {

    private lateinit var database: ChatDatabase
    private lateinit var messageDao: MessageDao
    private lateinit var replyMessageDao: ReplyMessageDao

    @Before
    fun setUp() {
        database = createRoomDB()
        messageDao = database.messageDao()
        replyMessageDao = database.replyMessageDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `member custom survives the round trip`(): Unit = runBlocking {
        val member = MemberInfo(
            channelRole = "channel_moderator",
            notificationsMuted = true,
            extraData = mapOf("flair" to mapOf("tier" to "gold", "badge" to "whale")),
        )
        val message = randomMessage(member = member, replyTo = null, poll = null)

        messageDao.insert(message.toEntity())

        messageDao.select(message.id)?.messageInnerEntity?.member shouldBeEqualTo member.toEntity()
    }

    @Test
    fun `a member without custom data survives the round trip`(): Unit = runBlocking {
        val member = MemberInfo(channelRole = "channel_member", notificationsMuted = false, extraData = emptyMap())
        val message = randomMessage(member = member, replyTo = null, poll = null)

        messageDao.insert(message.toEntity())

        messageDao.select(message.id)?.messageInnerEntity?.member shouldBeEqualTo member.toEntity()
    }

    @Test
    fun `an absent member stays absent across the round trip`(): Unit = runBlocking {
        val message = randomMessage(member = null, replyTo = null, poll = null)

        messageDao.insert(message.toEntity())

        messageDao.select(message.id)?.messageInnerEntity?.member.shouldBeNull()
    }

    @Test
    fun `member custom survives the round trip for a quoted message`(): Unit = runBlocking {
        val member = MemberInfo(
            channelRole = "channel_moderator",
            notificationsMuted = true,
            extraData = mapOf("flair" to "gold"),
        )
        val reply = randomMessage(user = randomUser(), member = member, replyTo = null, poll = null)

        replyMessageDao.insert(listOf(reply.toReplyEntity()))

        replyMessageDao.selectById(reply.id)?.replyMessageInnerEntity?.member shouldBeEqualTo member.toEntity()
    }

    @Test
    fun `a message carrying only the deprecated channelRole keeps it across the round trip`(): Unit = runBlocking {
        @Suppress("DEPRECATION")
        val message = randomMessage(channelRole = "channel_moderator", member = null, replyTo = null, poll = null)

        messageDao.insert(message.toEntity())

        val stored = messageDao.select(message.id)?.messageInnerEntity?.member
        stored shouldBeEqualTo MemberInfoEntity(channelRole = "channel_moderator")
    }

    @Test
    fun `updating the member touches only the matching channel and author`(): Unit = runBlocking {
        val author = randomUser(id = "author")
        val other = randomUser(id = "other")
        val mine = randomMessage(cid = CID, user = author, member = null, replyTo = null, poll = null)
        val theirs = randomMessage(cid = CID, user = other, member = null, replyTo = null, poll = null)
        val elsewhere = randomMessage(cid = OTHER_CID, user = author, member = null, replyTo = null, poll = null)
        messageDao.insert(listOf(mine, theirs, elsewhere).map { it.toEntity() })
        val member = MemberInfoEntity(channelRole = "channel_moderator", extraData = mapOf("flair" to "gold"))

        messageDao.updateMemberByCidAndUserId(CID, author.id, member)

        messageDao.select(mine.id)?.messageInnerEntity?.member shouldBeEqualTo member
        messageDao.select(theirs.id)?.messageInnerEntity?.member.shouldBeNull()
        messageDao.select(elsewhere.id)?.messageInnerEntity?.member.shouldBeNull()
    }

    @Test
    fun `updating the member leaves the rest of the message untouched`(): Unit = runBlocking {
        // This is the point of the targeted update: a concurrent edit elsewhere must not be reverted by the refresh.
        val author = randomUser(id = "author")
        val message = randomMessage(cid = CID, user = author, member = null, replyTo = null, poll = null)
        messageDao.insert(message.toEntity())

        messageDao.updateMemberByCidAndUserId(CID, author.id, MemberInfoEntity(channelRole = "channel_moderator"))

        val stored = messageDao.select(message.id)?.messageInnerEntity
        stored?.text shouldBeEqualTo message.text
        stored?.extraData shouldBeEqualTo message.extraData
        stored?.member shouldBeEqualTo MemberInfoEntity(channelRole = "channel_moderator")
    }

    @Test
    fun `updating the member clears it when null is stored`(): Unit = runBlocking {
        val author = randomUser(id = "author")
        val member = MemberInfo(channelRole = "channel_member", extraData = mapOf("flair" to "gold"))
        val message = randomMessage(cid = CID, user = author, member = member, replyTo = null, poll = null)
        messageDao.insert(message.toEntity())

        messageDao.updateMemberByCidAndUserId(CID, author.id, null)

        messageDao.select(message.id)?.messageInnerEntity?.member.shouldBeNull()
    }

    private companion object {
        const val CID = "messaging:probe"
        const val OTHER_CID = "messaging:other"
    }
}
