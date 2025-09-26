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
import io.getstream.chat.android.offline.repository.domain.channel.userread.internal.ChannelUserReadEntity
import io.getstream.chat.android.offline.repository.domain.message.attachment.internal.AttachmentEntity
import io.getstream.chat.android.offline.repository.domain.message.internal.LocationEntity
import io.getstream.chat.android.offline.repository.domain.message.internal.MessageEntity
import io.getstream.chat.android.offline.repository.domain.message.internal.MessageInnerEntity
import io.getstream.chat.android.offline.repository.domain.message.internal.ReactionGroupEntity
import io.getstream.chat.android.offline.repository.domain.message.internal.ReminderInfoEntity
import io.getstream.chat.android.offline.repository.domain.queryChannels.internal.QueryChannelsEntity
import io.getstream.chat.android.offline.repository.domain.reaction.internal.ReactionEntity
import io.getstream.chat.android.offline.repository.domain.threads.internal.ThreadEntity
import io.getstream.chat.android.offline.repository.domain.user.internal.PrivacySettingsEntity
import io.getstream.chat.android.offline.repository.domain.user.internal.UserEntity
import io.getstream.chat.android.randomBoolean
import io.getstream.chat.android.randomCID
import io.getstream.chat.android.randomDate
import io.getstream.chat.android.randomDateOrNull
import io.getstream.chat.android.randomDouble
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
    privacySettings: PrivacySettingsEntity? = null,
    banned: Boolean = randomBoolean(),
    mutes: List<String> = emptyList(),
    teams: List<String> = emptyList(),
    teamsRole: Map<String, String> = emptyMap(),
    extraData: Map<String, Any> = emptyMap(),
): UserEntity = UserEntity(
    id = id,
    originalId = originalId,
    name = name,
    role = role,
    image = image,
    createdAt = createdAt,
    updatedAt = updatedAt,
    lastActive = lastActive,
    invisible = invisible,
    privacySettings = privacySettings,
    banned = banned,
    mutes = mutes,
    teams = teams,
    teamsRole = teamsRole,
    extraData = extraData,
)

internal fun randomMessageEntity(
    id: String = randomString(),
    cid: String = randomCID(),
    userId: String = randomString(),
    text: String = randomString(),
    attachments: List<AttachmentEntity> = emptyList(),
    type: String = randomString(),
    syncStatus: SyncStatus = SyncStatus.COMPLETED,
    replyCount: Int = randomInt(),
    deletedReplyCount: Int = randomInt(),
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
    reactionGroups: Map<String, ReactionGroupEntity> = emptyMap(),
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
    pollId: String? = null,
    reminder: ReminderInfoEntity = randomReminderInfoEntity(),
    sharedLocation: LocationEntity? = randomLocationEntity(),
    channelRole: String? = null,
) = MessageEntity(
    messageInnerEntity = MessageInnerEntity(
        id = id,
        cid = cid,
        userId = userId,
        text = text,
        type = type,
        syncStatus = syncStatus,
        replyCount = replyCount,
        deletedReplyCount = deletedReplyCount,
        createdAt = createdAt,
        createdLocallyAt = createdLocallyAt,
        updatedAt = updatedAt,
        updatedLocallyAt = updatedLocallyAt,
        deletedAt = deletedAt,
        mentionedUsersId = mentionedUsersId,
        reactionCounts = reactionCounts,
        reactionScores = reactionScores,
        reactionGroups = reactionGroups,
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
        pollId = pollId,
        reminder = reminder,
        sharedLocation = sharedLocation,
        channelRole = channelRole,
    ),
    attachments = attachments,
    latestReactions = latestReactions,
    ownReactions = ownReactions,
)

internal fun randomReminderInfoEntity(
    remindAt: Date? = randomDateOrNull(),
    createdAt: Date = randomDate(),
    updatedAt: Date = randomDate(),
): ReminderInfoEntity = ReminderInfoEntity(
    remindAt = remindAt,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

internal fun randomReactionGroupEntity(
    type: String = randomString(),
    count: Int = randomInt(),
    sumScore: Int = randomInt(),
    firstReactionAt: Date = randomDate(),
    lastReactionAt: Date = randomDate(),
): ReactionGroupEntity {
    return ReactionGroupEntity(
        type = type,
        count = count,
        sumScore = sumScore,
        firstReactionAt = firstReactionAt,
        lastReactionAt = lastReactionAt,
    )
}

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

internal fun randomLocationEntity(): LocationEntity =
    LocationEntity(
        cid = randomCID(),
        messageId = randomString(),
        userId = randomString(),
        endAt = randomDate(),
        latitude = randomDouble(),
        longitude = randomDouble(),
        deviceId = randomString(),
    )

internal fun randomThreadEntity(
    parentMessageId: String = randomString(),
    cid: String = randomCID(),
    createdByUserId: String = randomString(),
    activeParticipantCount: Int = randomInt(),
    participantCount: Int = randomInt(),
    threadParticipantIds: List<String> = emptyList(),
    lastMessageAt: Date = randomDate(),
    createdAt: Date = randomDate(),
    updatedAt: Date = randomDate(),
    deletedAt: Date? = randomDateOrNull(),
    title: String = randomString(),
    read: List<ChannelUserReadEntity> = emptyList(),
    latestReplyIds: List<String> = emptyList(),
    extraData: Map<String, Any> = emptyMap(),
): ThreadEntity = ThreadEntity(
    parentMessageId = parentMessageId,
    cid = cid,
    createdByUserId = createdByUserId,
    activeParticipantCount = activeParticipantCount,
    participantCount = participantCount,
    threadParticipantIds = threadParticipantIds,
    lastMessageAt = lastMessageAt,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
    title = title,
    read = read,
    latestReplyIds = latestReplyIds,
    extraData = extraData,
)
