package io.getstream.chat.android.client.utils

public class SystemTimeProvider {
    public fun provideTime(): Long = System.currentTimeMillis()
}
