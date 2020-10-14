package io.getstream.chat.android.client.utils

public class SystemTimeProvider {
    public fun provideCurrentTimeInSeconds() = System.currentTimeMillis() / MILLIS_TO_SECONDS_FACTOR

    private companion object {
        const val MILLIS_TO_SECONDS_FACTOR = 1000L
    }
}
