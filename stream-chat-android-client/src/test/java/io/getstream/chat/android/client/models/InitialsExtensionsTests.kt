package io.getstream.chat.android.client.models

import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class InitialsExtensionsTests {

    /** [provideNames] */
    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.models.InitialsExtensionsTests#provideNames")
    fun `Should return initials of the user name`(name: String, initials: String) {
        val user = User(extraData = mutableMapOf("name" to name))

        user.initials `should be equal to` initials
    }

    /** [provideNames] */
    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.client.models.InitialsExtensionsTests#provideNames")
    fun `Should return initials of the channel name`(name: String, initials: String) {
        val channel = Channel(extraData = mutableMapOf("name" to name))

        channel.initials `should be equal to` initials
    }

    companion object {

        @JvmStatic
        fun provideNames() = listOf(
            Arguments.of("    AaaA  A", "AA"),
            Arguments.of("", ""),
            Arguments.of(" ", ""),
            Arguments.of("    ", ""),
            Arguments.of("    a", "A"),
            Arguments.of("    A", "A"),
            Arguments.of("    Aaa", "A"),
            Arguments.of("    AaaA", "A"),
            Arguments.of("    AaaA A", "AA"),
            Arguments.of("    AaaA A   ", "AA"),
            Arguments.of("AaaA  A   ", "AA"),
            Arguments.of("AaaA  B   ", "AB"),
            Arguments.of("AaaA  b   ", "AB"),
            Arguments.of("baaA  b   ", "BB"),
            Arguments.of("caaA  b asdf  asdf asdf ", "CB"),
            Arguments.of("@   ", "@"),
            Arguments.of("$   ", "$"),
            Arguments.of(" #   ", "#"),
            Arguments.of("@  @ ", "@@"),
            Arguments.of("$   $", "$$"),
            Arguments.of(" #   #", "##"),
        )
    }
}
