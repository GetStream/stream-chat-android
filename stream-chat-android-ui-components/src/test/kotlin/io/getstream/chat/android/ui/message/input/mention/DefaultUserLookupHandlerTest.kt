package io.getstream.chat.android.ui.message.input.mention

import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.test.randomString
import io.getstream.chat.android.ui.createUser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.internal.assertEquals
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
internal class DefaultUserLookupHandlerTest {

    @Test
    fun `exact matches should be selected`() = runBlockingTest {
        val userName = randomString()

        testSimpleNameChange(userName, userName)
    }

    @Test
    fun `insertion with 2 caracteres should be selected`() = runBlockingTest {
        val userName = randomString()

        testSimpleNameChange(userName, "${userName}aa")
    }

    @Test
    fun `insertion with 4 caracteres should be removed selected`() = runBlockingTest {
        val userName = randomString()

        testSimpleNameChange(userName, "${userName}aaaa", emptyList())
    }

    @Test
    fun `diacritics should be ignored in the search`() = runBlockingTest {
        val userName = "áéàèãöüäDziękujęç"
        val userNameNoAccents = "aeaeaouaDziekujec"

        testSimpleNameChange(userName, userNameNoAccents)
    }

    @Test
    fun `transliteration should work for russian`() = runBlockingTest {
        val latinName = "Leandro"
        val cyrillicName = "Леандро"
        val cyrillicToLatinId = "Cyrl-Latn"

        val user1 = createUser().apply { name = latinName }

        val users = listOf(
            user1,
            createUser().apply { name = randomString() },
            createUser().apply { name = randomString() },
        )

        val result = DefaultUserLookupHandler(users, cyrillicToLatinId).handleUserLookup(cyrillicName)

        assertEquals(listOf(user1), result)
    }

    private suspend fun testSimpleNameChange(userName: String, query: String, expectedResult: List<User>? = null) {
        val user1 = createUser().apply { name = userName }

        val users = listOf(
            user1,
            createUser().apply { name = randomString() },
            createUser().apply { name = randomString() },
        )

        val result = DefaultUserLookupHandler(users).handleUserLookup(query)

        if (expectedResult != null) {
            assertEquals(expectedResult, result)
        } else {
            assertEquals(listOf(user1), result)
        }
    }
}
