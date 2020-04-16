package io.getstream.chat.android.livedata.converter

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.livedata.BaseTest
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*


@RunWith(AndroidJUnit4::class)
class SyncStatusConverterTest: BaseTest() {
    @Test
    fun testEncoding() {
        val converter = SyncStatusConverter()
        val output = converter.syncStatusToString(SyncStatus.SYNC_NEEDED)
        val converted = converter.stringToSyncStatus(output)
        Truth.assertThat(converted).isEqualTo(SyncStatus.SYNC_NEEDED)
    }

}