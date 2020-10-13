package io.getstream.chat.android.client.utils

class SystemTimeProvider {
    fun provideTime() = System.currentTimeMillis() / MILLIS_TO_SECONDS_FACTOR

    companion object {
        private const val MILLIS_TO_SECONDS_FACTOR = 1000L
    }
}
