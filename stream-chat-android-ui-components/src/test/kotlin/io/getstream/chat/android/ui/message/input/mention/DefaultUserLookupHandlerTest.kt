package io.getstream.chat.android.ui.message.input.mention

import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.test.randomString
import io.getstream.chat.android.ui.createUser
import io.getstream.chat.android.ui.message.input.MessageInputView
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.internal.assertEquals
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
internal class DefaultUserLookupHandlerTest {

    @Test
    fun `exact matches should be selected`() = runBlockingTest {
        val userName = randomString()

        testNameChange(userName, userName)
    }

    @Test
    fun `insertion with 2 caracteres should be selected`() = runBlockingTest {
        val userName = randomString()

        testNameChange(userName, "${userName}aa")
    }

    @Test
    fun `insertion with 4 caracteres should be removed selected`() = runBlockingTest {
        val userName = randomString()

        testNameChange(userName, "${userName}aaaa", emptyList())
    }

    @Test
    fun `diacritics should be ignored in the search`() = runBlockingTest {
        val userName = "áéàèãöüäDziękujęç"
        val userNameNoAccents = "aeaeaouaDziekujec"

        testNameChange(userName, userNameNoAccents)
    }

    @Test
    fun `search should work for many different examples`() = runBlockingTest {
        testNameChange("Leandro", "Le")
        testNameChange("Leandro", "Leubdro")
        testNameChange("Blah", "Bleh")
        testNameChange("Asdfghj", "AS")
        testNameChange("Asdfghj", "ASdf")
        testNameChange("Xablau", "Xublau")
    }

    private suspend fun testNameChange(userName: String, query: String, expectedResult: List<User>? = null) {
        val user1 = createUser().apply { name = userName }

        val users = listOf(
            user1,
            createUser().apply { name = randomString() },
            createUser().apply { name = randomString() },
        )

        val result = MessageInputView.DefaultUserLookupHandler(users).handleUserLookup(query)

        if (expectedResult != null) {
            assertEquals(expectedResult, result)
        } else {
            assertEquals(listOf(user1), result)
        }
    }
}
