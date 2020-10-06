package io.getstream.chat.android.livedata.repository

import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import io.getstream.chat.android.livedata.dao.UserDao
import io.getstream.chat.android.livedata.randomUser
import io.getstream.chat.android.livedata.randomUserEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.When
import org.amshove.kluent.any
import org.amshove.kluent.calling
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldNotBeNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
internal class UserRepositoryTests {
    private lateinit var sut: UserRepository

    private lateinit var userDao: UserDao

    @BeforeEach
    fun setup() {
        userDao = mock()
        sut = UserRepository(userDao, null)
    }

    @Test
    fun `When insert users Should insert to dao`() = runBlockingTest {
        val users = listOf(randomUser(), randomUser())

        sut.insert(users)

        verify(userDao).insertMany(argThat { size == 2 })
    }

    @Test
    fun `When insert users If users list is empty Should never insert to dao`() = runBlockingTest {
        sut.insert(emptyList())

        verify(userDao, never()).insertMany(any())
    }

    @Test
    fun `When select users If previously inserted them Should return users`() = runBlockingTest {
        val user1 = randomUser()
        val user2 = randomUser()
        sut.insert(listOf(user1, user2))

        val users = sut.select(listOf(user1.id, user2.id))

        users shouldBeEqualTo listOf(user1, user2)
    }

    @Test
    fun `When select users If previously not insert And dao contains such users Should return users`() = runBlockingTest {
        val userEntity1 = randomUserEntity(id = "id1")
        val userEntity2 = randomUserEntity(id = "id2")
        When calling userDao.select(listOf("id1", "id2")) doReturn listOf(userEntity1, userEntity2)

        val result = sut.select(listOf("id1", "id2"))

        result.size shouldBeEqualTo 2
        result.any { user -> user.id == "id1" } shouldBeEqualTo true
        result.any { user -> user.id == "id2" } shouldBeEqualTo true
    }

    @Test
    fun `When insert me Should insert entity with me id to dao`() = runBlockingTest {
        val user = randomUser(id = "userId")

        sut.insertMe(user)

        verify(userDao).insert(argThat { id == "me" && originalId == "userId" })
    }

    @Test
    fun `When select user map If current user is not null Should return users from dao and current user`() = runBlockingTest {
        val currentUser = randomUser(id = "currentUserId")
        sut = UserRepository(userDao, currentUser)
        val userEntity = randomUserEntity(id = "userId")
        When calling userDao.select(listOf("userId")) doReturn listOf(userEntity)

        val result = sut.selectUserMap(listOf("userId"))

        result.size shouldBeEqualTo 2
        result["userId"].shouldNotBeNull()
        result["currentUserId"] shouldBeEqualTo currentUser
    }
}
