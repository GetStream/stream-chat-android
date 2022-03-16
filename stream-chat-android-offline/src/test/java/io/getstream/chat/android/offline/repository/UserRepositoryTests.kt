package io.getstream.chat.android.offline.repository

import app.cash.turbine.test
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.offline.randomUser
import io.getstream.chat.android.offline.repository.domain.user.internal.UserDao
import io.getstream.chat.android.offline.repository.domain.user.internal.UserRepository
import io.getstream.chat.android.offline.repository.domain.user.internal.UserRepositoryImpl
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

@ExperimentalCoroutinesApi
internal class UserRepositoryTests {
    private lateinit var sut: UserRepository

    private lateinit var userDao: UserDao
    private val currentUser: User = randomUser(id = "currentUserId")

    @BeforeEach
    fun setup() {
        userDao = mock()
        sut = UserRepositoryImpl(userDao)
    }

    @Test
    fun `When insert users Should insert to dao`() = runBlockingTest {
        val users = listOf(randomUser(), randomUser())

        sut.insertUsers(users)

        verify(userDao).insertMany(argThat { size == 2 })
    }

    @Test
    fun `When insert users If users list is empty Should never insert to dao`() = runBlockingTest {
        sut.insertUsers(emptyList())

        verify(userDao, never()).insertMany(any())
    }

    @Test
    fun `When insert me Should insert entity with me id to dao`() = runBlockingTest {
        val user = randomUser(id = "userId")

        sut.insertCurrentUser(user)

        verify(userDao).insert(argThat { id == "me" && originalId == "userId" })
    }

    @Test
    fun `When insert user Should propagate updates to flow`() = runBlockingTest {
        val newUser = randomUser()
        val flow = sut.observeLatestUsers()

        sut.insertUser(newUser)

        val userMapFlow = flow.value
        userMapFlow.size shouldBeEqualTo 1
        userMapFlow[newUser.id] shouldBeEqualTo newUser
    }

    @Test
    fun `When insert users Should propagate updates to flow`() = runBlockingTest {
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
        runBlockingTest {
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
        runBlockingTest {
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
        runBlockingTest {
            val newUser1 = randomUser()
            val newUser2 = randomUser()
            val updatedUser1 = newUser1.copy(extraData = mutableMapOf()).apply {
                name = "newUserName"
            }
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
