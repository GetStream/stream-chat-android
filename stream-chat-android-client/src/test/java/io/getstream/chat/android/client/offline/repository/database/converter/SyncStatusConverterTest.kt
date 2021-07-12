package io.getstream.chat.android.offline.repository.database.converter

import com.google.common.truth.Truth
import io.getstream.chat.android.client.utils.SyncStatus
import org.junit.Test

internal class SyncStatusConverterTest {
    @Test
    fun testEncoding() {
        val options = listOf(SyncStatus.SYNC_NEEDED, SyncStatus.FAILED_PERMANENTLY, SyncStatus.COMPLETED)

        for (option in options) {
            val converter = SyncStatusConverter()
            val output = converter.syncStatusToString(option)
            val converted = converter.stringToSyncStatus(output)
            Truth.assertThat(converted).isEqualTo(option)
        }
    }
}
