package io.getstream.chat.android.livedata.worker

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import com.google.common.truth.Truth
import io.getstream.chat.android.livedata.BaseDisconnectedMockedTest
import kotlinx.coroutines.runBlocking
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SyncWorkerTest : BaseDisconnectedMockedTest() {
    @Test
    @Ignore("not finished yet")
    fun run() = runBlocking {
        setupWorkManager()
        val STREAM_CHANNEL_CID = "STREAM_CHANNEL_CID"

        val data = workDataOf(STREAM_CHANNEL_CID to data.channel1.cid, "STREAM_USER_ID" to data.user1.id)

        val worker =
            TestListenableWorkerBuilder<SyncWorker>(ApplicationProvider.getApplicationContext())
                .setInputData(data)
                .build()

                val syncWorker: SyncWorker = worker as SyncWorker
        val result = syncWorker.doWork()

        Truth.assertThat(result).isNotNull()
    }
}
