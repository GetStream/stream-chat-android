package io.getstream.chat.android.livedata.repository

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.randomUser
import io.getstream.chat.android.livedata.randomUserEntity
import io.getstream.chat.android.offline.repository.domain.user.UserDao
import io.getstream.chat.android.offline.repository.domain.user.UserRepository
import io.getstream.chat.android.offline.repository.domain.user.UserRepositoryImpl
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldNotBeNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
internal class UserRepositoryTests {
    private lateinit var sut: UserRepository

    private lateinit var userDao: UserDao
    private val currentUser: User = randomUser(id = "currentUserId")

    @BeforeEach
    fun setup() {
        userDao = mock()
        sut = UserRepositoryImpl(userDao, currentUser)
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
    fun `When select users If previously inserted them Should return users`() = runBlockingTest {
        val user1 = randomUser()
        val user2 = randomUser()
        sut.insertUsers(listOf(user1, user2))

        val users = sut.selectUsers(listOf(user1.id, user2.id))

        users shouldBeEqualTo listOf(user1, user2)
    }

    @Test
    fun `When select users If previously not insert And dao contains such users Should return users`() =
        runBlockingTest {
            val userEntity1 = randomUserEntity(originalId = "id1")
            val userEntity2 = randomUserEntity(originalId = "id2")
            whenever(userDao.select(listOf("id1", "id2"))) doReturn listOf(userEntity1, userEntity2)

            val result = sut.selectUsers(listOf("id1", "id2"))

            result.size shouldBeEqualTo 2
            result.any { user -> user.id == "id1" } shouldBeEqualTo true
            result.any { user -> user.id == "id2" } shouldBeEqualTo true
        }

    @Test
    fun `When insert me Should insert entity with me id to dao`() = runBlockingTest {
        val user = randomUser(id = "userId")

        sut.insertCurrentUser(user)

        verify(userDao).insert(argThat { id == "me" && originalId == "userId" })
    }

    @Test
    fun `When select user map If current user is not null Should return users from dao and current user`() =
        runBlockingTest {
            val userEntity = randomUserEntity(originalId = "userId")
            whenever(userDao.select(listOf("userId"))) doReturn listOf(userEntity)

            val result = sut.selectUserMap(listOf("userId"))

            result.size shouldBeEqualTo 2
            result["userId"].shouldNotBeNull()
            result["currentUserId"] shouldBeEqualTo currentUser
        }

    @Test
    fun `When select me If dao contains user with me as Id Should return such user`() = runBlockingTest {
        val meEntity = randomUserEntity(id = "me", originalId = "userId")
        whenever(userDao.select("me")) doReturn meEntity

        val result = sut.selectCurrentUser()

        result.shouldNotBeNull()
        result.id shouldBeEqualTo "userId"
    }
}
