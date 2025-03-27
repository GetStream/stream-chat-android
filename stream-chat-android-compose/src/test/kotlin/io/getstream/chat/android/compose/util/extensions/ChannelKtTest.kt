package io.getstream.chat.android.compose.util.extensions

import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomChannelCapabilities
import io.getstream.chat.android.randomConfig
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class ChannelKtTest {

    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.compose.util.extensions.ChannelKtTest#inputArguments")
    fun `Given a channel, return if polls are enabled or not`(channel: Channel, expectedResult: Boolean) {
        channel.isPollEnabled() `should be equal to` expectedResult
    }

    companion object {

        @JvmStatic
        fun inputArguments() = listOf(
            Arguments.of(
                randomChannel(
                    config = randomConfig(pollsEnabled = true),
                    ownCapabilities = randomChannelCapabilities(
                        include = setOf(ChannelCapabilities.SEND_POLL)
                    ),
                ),
                true
            ),
            Arguments.of(
                randomChannel(
                    config = randomConfig(pollsEnabled = false),
                    ownCapabilities = randomChannelCapabilities(
                        include = setOf(ChannelCapabilities.SEND_POLL)
                    ),
                ),
                false
            ),
            Arguments.of(
                randomChannel(
                    config = randomConfig(pollsEnabled = true),
                    ownCapabilities = randomChannelCapabilities(
                        exclude = setOf(ChannelCapabilities.SEND_POLL)
                    ),
                ),
                false
            ),
        )
    }
}