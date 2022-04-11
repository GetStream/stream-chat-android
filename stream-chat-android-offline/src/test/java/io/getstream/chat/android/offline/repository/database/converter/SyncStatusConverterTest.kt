/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.offline.repository.database.converter

import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.offline.repository.database.converter.internal.SyncStatusConverter
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
