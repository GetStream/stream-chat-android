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

package io.getstream.chat.android.ui.message.input.mention

import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.test.randomString
import io.getstream.chat.android.ui.createUser
import io.getstream.chat.android.ui.message.input.MessageInputView
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.internal.assertEquals
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
internal class DefaultUserLookupHandlerTest {

    @Test
    fun `exact matches should be selected`() = runTest {
        val userName = randomString()

        testNameChange(userName, userName)
    }

    @Test
    fun `insertion with 2 characters should be selected`() = runTest {
        val userName = randomString()

        testNameChange(userName, "${userName}aa")
    }

    @Test
    fun `insertion with 4 characters should be removed selected`() = runTest {
        val userName = randomString()

        testNameChange(userName, "${userName}aaaa", emptyList())
    }

    @Test
    fun `diacritics should be ignored in the search`() = runTest {
        val userName = "áéàèãöüäDziękujęç"
        val userNameNoAccents = "aeaeaouaDziekujec"

        testNameChange(userName, userNameNoAccents)
    }

    @Test
    fun `search should work for many different examples`() = runTest {
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
            createUser().apply { name = randomStringWithout(query) },
            createUser().apply { name = randomStringWithout(query) },
        )

        val result = MessageInputView.DefaultUserLookupHandler(users).handleUserLookup(query)

        if (expectedResult != null) {
            assertEquals(expectedResult, result)
        } else {
            assertEquals(listOf(user1), result)
        }
    }

    private fun randomStringWithout(query: String): String {
        return generateSequence { randomString() }.first { string -> !string.contains(query, ignoreCase = true) }
    }
}
