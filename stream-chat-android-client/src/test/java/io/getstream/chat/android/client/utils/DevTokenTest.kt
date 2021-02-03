package io.getstream.chat.android.client.utils

import com.nhaarman.mockitokotlin2.mock
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.token.FakeTokenManager
import org.amshove.kluent.`should be equal to`
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(manifest = Config.NONE)
internal class DevTokenTest(private val userId: String, private val expectedToken: String) {

    private val client = ChatClient(
        config = mock(),
        api = mock(),
        socket = mock(),
        notifications = mock(),
        tokenManager = FakeTokenManager("")
    )

    @Test
    fun `Should return valid dev token`() {
        client.devToken(userId) `should be equal to` expectedToken
    }

    companion object {

        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{index}: {0} => {1}")
        fun data(): Collection<Array<Any?>> = listOf(
            arrayOf(
                "jc",
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiamMifQ==.devtoken"
            ),
            arrayOf(
                "vishal",
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoidmlzaGFsIn0=.devtoken"
            ),
            arrayOf(
                "amin",
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiYW1pbiJ9.devtoken"
            )
        )
    }
}
