/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.client.internal.offline.repository.database.internal

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import io.getstream.chat.android.randomString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
internal class ChatDatabaseTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `the same user always gets the same instance`() {
        val userId = randomString()

        assertSame(ChatDatabase.getDatabase(context, userId), ChatDatabase.getDatabase(context, userId))
    }

    @Test
    fun `different users get different instances`() {
        assertNotSame(
            ChatDatabase.getDatabase(context, randomString()),
            ChatDatabase.getDatabase(context, randomString()),
        )
    }

    @Test
    fun `concurrent first calls for the same user build a single instance`() {
        val executor = Executors.newFixedThreadPool(THREADS)
        try {
            repeat(ROUNDS) {
                val userId = randomString()
                val start = CountDownLatch(1)
                val futures = List(THREADS) {
                    executor.submit<ChatDatabase> {
                        start.await()
                        ChatDatabase.getDatabase(context, userId)
                    }
                }
                start.countDown()

                val instances = futures.map { it.get(TIMEOUT_SECONDS, TimeUnit.SECONDS) }.distinct()
                assertEquals(1, instances.size)
            }
        } finally {
            executor.shutdownNow()
        }
    }

    private companion object {
        const val THREADS = 8
        const val ROUNDS = 20
        const val TIMEOUT_SECONDS = 10L
    }
}
