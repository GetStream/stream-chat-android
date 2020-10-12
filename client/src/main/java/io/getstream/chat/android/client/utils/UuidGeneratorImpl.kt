package io.getstream.chat.android.client.utils

import java.util.UUID

public class UuidGeneratorImpl : UuidGenerator {
    override fun generate(): String {
        return UUID.randomUUID().toString()
    }
}
