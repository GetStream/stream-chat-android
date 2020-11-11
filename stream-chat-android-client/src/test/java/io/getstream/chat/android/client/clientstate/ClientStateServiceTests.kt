package io.getstream.chat.android.client.clientstate

import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class ClientStateServiceTests {

    private lateinit var sut: ClientStateService

    @BeforeEach
    fun setUp() {
        sut = ClientStateService()
    }

    @Test
    fun `When initialized Should have Idle state`() {
        sut.state shouldBeEqualTo ClientState.Idle
    }
}