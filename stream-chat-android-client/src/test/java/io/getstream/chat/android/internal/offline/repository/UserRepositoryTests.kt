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

package io.getstream.chat.android.internal.offline.repository

import app.cash.turbine.test
import io.getstream.chat.android.DeliveryReceipts
import io.getstream.chat.android.PrivacySettings
import io.getstream.chat.android.ReadReceipts
import io.getstream.chat.android.TypingIndicators
import io.getstream.chat.android.client.persistance.repository.UserRepository
import io.getstream.chat.android.internal.offline.randomPrivacySettingsEntity
import io.getstream.chat.android.internal.offline.randomUserEntity
import io.getstream.chat.android.internal.offline.randomUserMuteEntity
import io.getstream.chat.android.internal.offline.repository.domain.push.internal.toEntity
import io.getstream.chat.android.internal.offline.repository.domain.user.internal.DatabaseUserRepository
import io.getstream.chat.android.internal.offline.repository.domain.user.internal.DeliveryReceiptsEntity
import io.getstream.chat.android.internal.offline.repository.domain.user.internal.PrivacySettingsEntity
import io.getstream.chat.android.internal.offline.repository.domain.user.internal.ReadReceiptsEntity
import io.getstream.chat.android.internal.offline.repository.domain.user.internal.TypingIndicatorsEntity
import io.getstream.chat.android.internal.offline.repository.domain.user.internal.UserDao
import io.getstream.chat.android.internal.offline.repository.domain.user.internal.UserEntity
import io.getstream.chat.android.internal.offline.repository.domain.user.internal.UserMuteEntity
import io.getstream.chat.android.internal.offline.repository.domain.user.internal.toEntity
import io.getstream.chat.android.models.Mute
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomMute
import io.getstream.chat.android.randomPrivacySettings
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.test.TestCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Rule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
internal class UserRepositoryTests {
    @get:Rule
    val testCoroutines: TestCoroutineRule = TestCoroutineRule()

    private lateinit var sut: UserRepository

    private lateinit var userDao: UserDao

    @BeforeEach
    fun setup() {
        userDao = mock()
        sut = DatabaseUserRepository(testCoroutines.scope, userDao)
    }

    @Test
    fun `When insert users Should insert to dao`() = runTest {
        val users = listOf(
            randomUser(
                privacySettings = randomPrivacySettings(),
                mutes = List(10) { randomMute() },
            ),
        )

        sut.insertUsers(users)

        val expected = users.map { user ->
            UserEntity(
                id = user.id,
                originalId = user.id,
                name = user.name,
                image = user.image,
                role = user.role,
                createdAt = user.createdAt,
                updatedAt = user.updatedAt,
                lastActive = user.lastActive,
                invisible = user.isInvisible,
                privacySettings = PrivacySettingsEntity(
                    typingIndicators = TypingIndicatorsEntity(
                        enabled = user.privacySettings?.typingIndicators?.enabled ?: false,
                    ),
                    readReceipts = ReadReceiptsEntity(
                        enabled = user.privacySettings?.readReceipts?.enabled ?: false,
                    ),
                    deliveryReceipts = DeliveryReceiptsEntity(
                        enabled = user.privacySettings?.deliveryReceipts?.enabled ?: false,
                    ),
                ),
                banned = user.isBanned,
                mutes = user.mutes.map { mute ->
                    UserMuteEntity(
                        userId = mute.user?.id,
                        targetId = mute.target?.id,
                        createdAt = mute.createdAt,
                        updatedAt = mute.updatedAt,
                        expires = mute.expires,
                    )
                },
                teams = user.teams,
                teamsRole = user.teamsRole,
                extraData = user.extraData,
                avgResponseTime = user.avgResponseTime,
                pushPreference = user.pushPreference?.toEntity(),
            )
        }
        verify(userDao).insertMany(expected)
    }

    @Test
    fun `When insert users If users list is empty Should never insert to dao`() = runTest {
        sut.insertUsers(emptyList())

        verify(userDao, never()).insertMany(any())
    }

    @Test
    fun `When insert current user Should insert entity with me id to dao`() = runTest {
        val user = randomUser(
            privacySettings = randomPrivacySettings(),
            mutes = List(10) { randomMute() },
        )

        sut.insertCurrentUser(user)

        val expected = UserEntity(
            id = "me",
            originalId = user.id,
            name = user.name,
            image = user.image,
            role = user.role,
            createdAt = user.createdAt,
            updatedAt = user.updatedAt,
            lastActive = user.lastActive,
            invisible = user.isInvisible,
            privacySettings = PrivacySettingsEntity(
                typingIndicators = TypingIndicatorsEntity(
                    enabled = user.privacySettings?.typingIndicators?.enabled ?: false,
                ),
                readReceipts = ReadReceiptsEntity(
                    enabled = user.privacySettings?.readReceipts?.enabled ?: false,
                ),
                deliveryReceipts = DeliveryReceiptsEntity(
                    enabled = user.privacySettings?.deliveryReceipts?.enabled ?: false,
                ),
            ),
            banned = user.isBanned,
            mutes = user.mutes.map(Mute::toEntity),
            teams = user.teams,
            teamsRole = user.teamsRole,
            extraData = user.extraData,
            avgResponseTime = user.avgResponseTime,
            pushPreference = user.pushPreference?.toEntity(),
        )
        verify(userDao).insert(expected)
    }

    @Test
    fun `When insert user Should propagate updates to flow`() = runTest {
        val newUser = randomUser()
        val flow = sut.observeLatestUsers()

        sut.insertUser(newUser)

        val userMapFlow = flow.value
        userMapFlow.size shouldBeEqualTo 1
        userMapFlow[newUser.id] shouldBeEqualTo newUser
    }

    @Test
    fun `When insert users Should propagate updates to flow`() = runTest {
        val newUser1 = randomUser()
        val newUser2 = randomUser()
        val flow = sut.observeLatestUsers()

        sut.insertUsers(listOf(newUser1, newUser2))

        val userMapFlow = flow.value
        userMapFlow.size shouldBeEqualTo 2
        userMapFlow[newUser1.id] shouldBeEqualTo newUser1
        userMapFlow[newUser2.id] shouldBeEqualTo newUser2
    }

    @Test
    fun `Given users were inserted When insert a new user Should propagated new value to flow`() =
        runTest {
            val newUser1 = randomUser()
            val newUser2 = randomUser()
            val newUser3 = randomUser()
            var observedTimes = 0

            sut.observeLatestUsers()
                .drop(1) // empty initial value
                .onEach { observedTimes += 1 }
                .test {
                    sut.insertUsers(listOf(newUser1, newUser2))

                    sut.insertUser(newUser3)

                    observedTimes shouldBeEqualTo 2
                    cancelAndConsumeRemainingEvents()
                }
        }

    @Test
    fun `Given users were inserted already When insert a user Shouldn't be propagated value to flow again`() =
        runTest {
            val newUser1 = randomUser()
            val newUser2 = randomUser()
            var observedTimes = 0

            sut.observeLatestUsers()
                .drop(1) // empty initial value
                .onEach { observedTimes += 1 }
                .test {
                    sut.insertUsers(listOf(newUser1, newUser2))

                    sut.insertUser(newUser1)

                    observedTimes shouldBeEqualTo 1
                    cancelAndConsumeRemainingEvents()
                }
        }

    @Test
    fun `Given users were inserted already When insert an updated user Should propagate value to flow`() =
        runTest {
            val newUser1 = randomUser()
            val newUser2 = randomUser()
            val updatedUser1 = newUser1.copy(
                extraData = mutableMapOf(),
                name = "newUserName",
            )
            var observedTimes = 0

            sut.observeLatestUsers()
                .drop(1) // empty initial value
                .onEach { observedTimes += 1 }
                .test {
                    sut.insertUsers(listOf(newUser1, newUser2))

                    sut.insertUser(updatedUser1)

                    observedTimes shouldBeEqualTo 2
                    cancelAndConsumeRemainingEvents()
                }
        }

    @Test
    fun `When selectUser If user in cache Should return from cache without querying dao`() = runTest {
        val user = randomUser(
            privacySettings = randomPrivacySettings(),
            mutes = List(10) { randomMute() },
        )
        sut.insertUser(user)

        val result = sut.selectUser(user.id)

        assertEquals(user, result)
        verify(userDao, never()).select(any<String>())
    }

    @Test
    fun `When selectUser If user not in cache but in dao Should query dao and cache result`() = runTest {
        val userId = randomString()
        val userEntity = randomUserEntity(
            id = userId,
            originalId = userId,
            privacySettings = randomPrivacySettingsEntity(),
            mutes = List(10) { randomUserMuteEntity() },
        )
        whenever(userDao.select(userId)).thenReturn(userEntity)

        val result = sut.selectUser(userId)

        val expected = User(
            id = userEntity.id,
            name = userEntity.name,
            image = userEntity.image,
            role = userEntity.role,
            createdAt = userEntity.createdAt,
            updatedAt = userEntity.updatedAt,
            lastActive = userEntity.lastActive,
            invisible = userEntity.invisible,
            privacySettings = PrivacySettings(
                typingIndicators = TypingIndicators(
                    enabled = userEntity.privacySettings?.typingIndicators?.enabled ?: false,
                ),
                readReceipts = ReadReceipts(
                    enabled = userEntity.privacySettings?.readReceipts?.enabled ?: false,
                ),
                deliveryReceipts = DeliveryReceipts(
                    enabled = userEntity.privacySettings?.deliveryReceipts?.enabled ?: false,
                ),
            ),
            banned = userEntity.banned,
            mutes = userEntity.mutes.map { mute ->
                Mute(
                    user = mute.userId?.let(::User),
                    target = mute.targetId?.let(::User),
                    createdAt = mute.createdAt,
                    updatedAt = mute.updatedAt,
                    expires = mute.expires,
                )
            },
            teams = userEntity.teams,
            teamsRole = userEntity.teamsRole,
            avgResponseTime = userEntity.avgResponseTime,
            pushPreference = null,
            extraData = userEntity.extraData,
        )
        assertEquals(expected, result)
        // Verify it's now in cache - second call should not query dao
        val cachedResult = sut.selectUser(userId)
        assertEquals(expected, cachedResult)
        // Should only query dao once (first call), second call uses cache
        verify(userDao, times(1)).select(userId)
    }
}
