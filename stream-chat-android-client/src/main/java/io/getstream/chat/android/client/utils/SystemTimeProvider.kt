package io.getstream.chat.android.client.utils

public class SystemTimeProvider {
    public fun provideCurrentTimeInSeconds(): Long {
        return System.currentTimeMillis() / MILLIS_TO_SECONDS_FACTOR
    }

    private companion object {
        private const val MILLIS_TO_SECONDS_FACTOR = 1000L
    }
}
