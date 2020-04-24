package io.getstream.chat.android.livedata.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.livedata.BaseDomainTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserRepositoryTest : BaseDomainTest() {
    val repo by lazy { chatDomain.repos.users }

    @Test
    fun testInsertAndRead() = runBlocking(Dispatchers.IO) {
        repo.insertManyUsers(listOf(data.user1))
        val user1Entity = repo.select(data.user1.id)
        val user1 = user1Entity!!.toUser()
        Truth.assertThat(data.user1).isEqualTo(user1)

        val userMap = repo.selectUserMap(listOf(data.user1.id, "missing"))
        Truth.assertThat(userMap[data.user1.id]).isEqualTo(data.user1)
        Truth.assertThat(userMap["missing"]).isNull()
    }
}
