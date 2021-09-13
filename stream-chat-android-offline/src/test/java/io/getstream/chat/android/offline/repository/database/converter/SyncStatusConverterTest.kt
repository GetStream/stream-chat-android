package io.getstream.chat.android.offline.repository.database.converter

import io.getstream.chat.android.client.utils.SyncStatus
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test

internal class SyncStatusConverterTest {
    @Test
    fun testEncoding() {
        val options = listOf(SyncStatus.SYNC_NEEDED, SyncStatus.FAILED_PERMANENTLY, SyncStatus.COMPLETED)

        for (option in options) {
            val converter = SyncStatusConverter()
            val output = converter.syncStatusToString(option)
            val converted = converter.stringToSyncStatus(output)
            converted shouldBeEqualTo option
        }
    }
}
