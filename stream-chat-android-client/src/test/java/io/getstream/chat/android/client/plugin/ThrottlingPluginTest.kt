/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.plugin

import io.getstream.chat.android.models.Location
import io.getstream.chat.android.randomString
import io.getstream.result.Result
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class ThrottlingPluginTest {

    private lateinit var sut: ThrottlingPlugin
    private var currentTime = 1000L

    @BeforeEach
    fun setUp() {
        sut = ThrottlingPlugin(now = { currentTime })
    }

    @Test
    fun `onUpdateLiveLocationPrecondition allows first call`() = runTest {
        val result = sut.onUpdateLiveLocationPrecondition(location = Location())
        assertTrue(result is Result.Success)
    }

    @Test
    fun `onUpdateLiveLocationPrecondition throttles repeated calls within threshold`() = runTest {
        val location = Location()
        sut.onUpdateLiveLocationPrecondition(location)
        currentTime += 1000L // less than LIVE_LOCATION_THROTTLE_MS
        val result = sut.onUpdateLiveLocationPrecondition(location)
        assertTrue(result is Result.Failure)
    }

    @Test
    fun `onUpdateLiveLocationPrecondition allows call after threshold`() = runTest {
        val location = Location()
        sut.onUpdateLiveLocationPrecondition(location)
        currentTime += 4000L // more than LIVE_LOCATION_THROTTLE_MS
        val result = sut.onUpdateLiveLocationPrecondition(location)
        assertTrue(result is Result.Success)
    }

    @Test
    fun `onUserDisconnected clears mark read map`() = runTest {
        sut.onChannelMarkReadPrecondition(channelType = randomString(), channelId = randomString())
        sut.onUserDisconnected()
        currentTime += 1000L
        val result = sut.onChannelMarkReadPrecondition(channelType = randomString(), channelId = randomString())
        assertTrue(result is Result.Success)
    }

    @Test
    fun `onUserDisconnected clears live location map`() = runTest {
        sut.onUpdateLiveLocationPrecondition(location = Location())
        sut.onUserDisconnected()
        currentTime += 1000L
        val result = sut.onUpdateLiveLocationPrecondition(location = Location())
        assertTrue(result is Result.Success)
    }
}
