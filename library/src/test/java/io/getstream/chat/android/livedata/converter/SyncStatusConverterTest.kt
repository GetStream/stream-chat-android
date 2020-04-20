package io.getstream.chat.android.livedata.converter

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.livedata.BaseTest
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class SyncStatusConverterTest : BaseTest() {
    @Test
    fun testEncoding() {
        val options = listOf(SyncStatus.SYNC_NEEDED, SyncStatus.SYNC_FAILED, SyncStatus.SYNCED)

        for (option in options) {
            val converter = SyncStatusConverter()
            val output = converter.syncStatusToString(option)
            val converted = converter.stringToSyncStatus(output)
            Truth.assertThat(converted).isEqualTo(option)
        }
    }

}