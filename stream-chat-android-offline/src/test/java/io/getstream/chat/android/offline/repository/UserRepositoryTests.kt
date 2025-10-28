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

package io.getstream.chat.android.offline.repository

import app.cash.turbine.test
import io.getstream.chat.android.client.persistance.repository.UserRepository
import io.getstream.chat.android.models.User
import io.getstream.chat.android.offline.repository.domain.user.internal.DatabaseUserRepository
import io.getstream.chat.android.offline.repository.domain.user.internal.UserDao
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.test.TestCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Rule
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

@ExperimentalCoroutinesApi
internal class UserRepositoryTests {
    @get:Rule
    val testCoroutines: TestCoroutineRule = TestCoroutineRule()

    private lateinit var sut: UserRepository

    private lateinit var userDao: UserDao
    private val currentUser: User = randomUser(id = "currentUserId")

    @BeforeEach
    fun setup() {
        userDao = mock()
        sut = DatabaseUserRepository(testCoroutines.scope, userDao)
    }

    @Test
    fun `When insert users Should insert to dao`() = runTest {
        val users = listOf(randomUser(), randomUser())

        sut.insertUsers(users)

        verify(userDao).insertMany(argThat { size == 2 })
    }

    @Test
    fun `When insert users If users list is empty Should never insert to dao`() = runTest {
        sut.insertUsers(emptyList())

        verify(userDao, never()).insertMany(any())
    }

    @Test
    fun `When insert me Should insert entity with me id to dao`() = runTest {
        val user = randomUser(id = "userId")

        sut.insertCurrentUser(user)

        verify(userDao).insert(argThat { id == "me" && originalId == "userId" })
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
    fun `Given users were inserted When insert a new user Should propagated new value to flow`() = runTest {
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
    fun `Given users were inserted already When insert a user Shouldn't be propagated value to flow again`() = runTest {
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
    fun `Given users were inserted already When insert an updated user Should propagate value to flow`() = runTest {
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
}
