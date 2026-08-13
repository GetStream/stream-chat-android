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

package io.getstream.chat.android.client.internal.offline.repository.domain.channel.internal

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.client.internal.offline.createRoomDB
import io.getstream.chat.android.client.internal.offline.randomChannelEntity
import io.getstream.chat.android.client.internal.offline.repository.database.internal.ChatDatabase
import io.getstream.chat.android.randomDate
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Verifies the round-trip of the [ChannelEntity] channel state columns through Room, covering both the value and the
 * absent cases so a column that is silently dropped cannot pass by matching the default.
 */
@RunWith(AndroidJUnit4::class)
internal class ChannelEntityConverterTest {

    private lateinit var database: ChatDatabase
    private lateinit var dao: ChannelDao

    @Before
    fun setUp() {
        database = createRoomDB()
        dao = database.channelStateDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `channel state columns round-trip their values`() = runTest {
        val truncatedAt = randomDate()
        val entity = randomChannelEntity(truncatedAt = truncatedAt, disabled = true, blocked = true)

        dao.insert(entity)
        val read = dao.select(entity.cid)

        assertEquals(truncatedAt, read?.truncatedAt)
        assertTrue(read?.disabled == true)
        assertTrue(read?.blocked == true)
    }

    @Test
    fun `absent channel state columns round-trip as absent`() = runTest {
        val entity = randomChannelEntity(truncatedAt = null, disabled = false, blocked = null)

        dao.insert(entity)
        val read = dao.select(entity.cid)

        assertNull(read?.truncatedAt)
        assertEquals(false, read?.disabled)
        assertNull(read?.blocked)
    }
}
