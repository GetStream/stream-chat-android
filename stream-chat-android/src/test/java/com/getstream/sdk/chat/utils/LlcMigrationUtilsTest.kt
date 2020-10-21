package com.getstream.sdk.chat.utils

import com.getstream.sdk.chat.createChannel
import com.getstream.sdk.chat.createUser
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class LlcMigrationUtilsTest {

    @ParameterizedTest
    @MethodSource("com.getstream.sdk.chat.utils.LlcMigrationUtilsTest#provideNames")
    fun `Should return initials of the user name`(name: String, initials: String) {
        val user = createUser(extraData = mutableMapOf<String, Any>("name" to name))

        LlcMigrationUtils.getInitials(user) `should be equal to` initials
    }

    @ParameterizedTest
    @MethodSource("com.getstream.sdk.chat.utils.LlcMigrationUtilsTest#provideNames")
    fun `Should return initials of the channel name`(name: String, initials: String) {
        val channel = createChannel(extraData = mutableMapOf<String, Any>("name" to name))

        LlcMigrationUtils.getInitials(channel) `should be equal to` initials
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
