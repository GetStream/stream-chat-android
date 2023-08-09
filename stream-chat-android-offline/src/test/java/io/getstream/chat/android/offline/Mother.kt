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

package io.getstream.chat.android.offline

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.NeutralFilterObject
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.models.querysort.QuerySorter
import io.getstream.chat.android.offline.repository.database.internal.ChatDatabase
import io.getstream.chat.android.offline.repository.domain.message.attachment.internal.AttachmentEntity
import io.getstream.chat.android.offline.repository.domain.message.internal.MessageEntity
import io.getstream.chat.android.offline.repository.domain.message.internal.MessageInnerEntity
import io.getstream.chat.android.offline.repository.domain.queryChannels.internal.QueryChannelsEntity
import io.getstream.chat.android.offline.repository.domain.reaction.internal.ReactionEntity
import io.getstream.chat.android.offline.repository.domain.user.internal.UserEntity
import io.getstream.chat.android.randomBoolean
import io.getstream.chat.android.randomCID
import io.getstream.chat.android.randomDate
import io.getstream.chat.android.randomInt
import io.getstream.chat.android.randomString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import java.util.Date
import java.util.concurrent.Executors

internal fun randomUserEntity(
    id: String = randomString(),
    originalId: String = randomString(),
    name: String = randomString(),
    image: String = randomString(),
    role: String = randomString(),
    createdAt: Date? = null,
    updatedAt: Date? = null,
    lastActive: Date? = null,
    invisible: Boolean = randomBoolean(),
    banned: Boolean = randomBoolean(),
    mutes: List<String> = emptyList(),
    extraData: Map<String, Any> = emptyMap(),
): UserEntity =
    UserEntity(id, originalId, name, role, image, createdAt, updatedAt, lastActive, invisible, banned, mutes, extraData)

internal fun randomMessageEntity(
    id: String = randomString(),
    cid: String = randomCID(),
    userId: String = randomString(),
    text: String = randomString(),
    attachments: List<AttachmentEntity> = emptyList(),
    type: String = randomString(),
    syncStatus: SyncStatus = SyncStatus.COMPLETED,
    replyCount: Int = randomInt(),
    createdAt: Date? = randomDate(),
    createdLocallyAt: Date? = randomDate(),
    updatedAt: Date? = randomDate(),
    updatedLocallyAt: Date? = randomDate(),
    deletedAt: Date? = randomDate(),
    latestReactions: List<ReactionEntity> = emptyList(),
    ownReactions: List<ReactionEntity> = emptyList(),
    mentionedUsersId: List<String> = emptyList(),
    reactionCounts: Map<String, Int> = emptyMap(),
    reactionScores: Map<String, Int> = emptyMap(),
    parentId: String? = randomString(),
    command: String? = randomString(),
    shadowed: Boolean = randomBoolean(),
    extraData: Map<String, Any> = emptyMap(),
    replyToId: String? = randomString(),
    pinned: Boolean = randomBoolean(),
    pinnedAt: Date? = randomDate(),
    pinExpires: Date? = randomDate(),
    pinnedByUserId: String? = randomString(),
    threadParticipantsIds: List<String> = emptyList(),
) = MessageEntity(
    messageInnerEntity = MessageInnerEntity(
        id = id,
        cid = cid,
        userId = userId,
        text = text,
        type = type,
        syncStatus = syncStatus,
        replyCount = replyCount,
        createdAt = createdAt,
        createdLocallyAt = createdLocallyAt,
        updatedAt = updatedAt,
        updatedLocallyAt = updatedLocallyAt,
        deletedAt = deletedAt,
        mentionedUsersId = mentionedUsersId,
        reactionCounts = reactionCounts,
        reactionScores = reactionScores,
        parentId = parentId,
        command = command,
        shadowed = shadowed,
        extraData = extraData,
        replyToId = replyToId,
        pinned = pinned,
        pinnedAt = pinnedAt,
        pinExpires = pinExpires,
        pinnedByUserId = pinnedByUserId,
        threadParticipantsIds = threadParticipantsIds,
    ),
    attachments = attachments,
    latestReactions = latestReactions,
    ownReactions = ownReactions,
)

internal fun randomQueryChannelsEntity(
    id: String = randomString(),
    filter: FilterObject = NeutralFilterObject,
    querySort: QuerySorter<Channel> = QuerySortByField(),
    cids: List<String> = emptyList(),
): QueryChannelsEntity = QueryChannelsEntity(id, filter, querySort, cids)

internal fun createRoomDB(): ChatDatabase =
    Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), ChatDatabase::class.java)
        .allowMainThreadQueries()
        // Use a separate thread for Room transactions to avoid deadlocks. This means that tests that run Room
        // transactions can't use testCoroutines.scope.runBlockingTest, and have to simply use runBlocking instead.
        .setTransactionExecutor(Executors.newSingleThreadExecutor())
        .setQueryExecutor(Dispatchers.IO.asExecutor())
        .build()
