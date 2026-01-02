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

package io.getstream.chat.android.models

import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class SyncStatusTest {

    @ParameterizedTest
    @MethodSource("syncStatusFromIntArguments")
    fun testSyncStatusFromInt(value: Int, expected: SyncStatus) {
        SyncStatus.fromInt(value) `should be equal to` expected
    }

    companion object {

        @JvmStatic
        fun syncStatusFromIntArguments() = listOf(
            Arguments.of(-1, SyncStatus.SYNC_NEEDED),
            Arguments.of(1, SyncStatus.COMPLETED),
            Arguments.of(2, SyncStatus.FAILED_PERMANENTLY),
            Arguments.of(3, SyncStatus.IN_PROGRESS),
            Arguments.of(4, SyncStatus.AWAITING_ATTACHMENTS),
        )
    }
}
