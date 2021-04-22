package io.getstream.chat.android.offline.repository.database.converter

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.livedata.BaseTest
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class SyncStatusConverterTest : BaseTest() {
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
