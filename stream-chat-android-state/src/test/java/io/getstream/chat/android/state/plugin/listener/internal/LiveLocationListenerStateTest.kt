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

package io.getstream.chat.android.state.plugin.listener.internal

import io.getstream.chat.android.models.Location
import io.getstream.chat.android.state.plugin.state.global.internal.MutableGlobalState
import io.getstream.result.Error
import io.getstream.result.Result
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class LiveLocationListenerStateTest {

    private val mutableGlobalState: MutableGlobalState = mock()
    private val sut: LiveLocationListenerState = LiveLocationListenerState(mutableGlobalState)

    @Test
    fun `onUpdateLiveLocationPrecondition always returns success`() = runTest {
        val location = mock<Location>()
        val result = sut.onUpdateLiveLocationPrecondition(location)
        assert(result is Result.Success)
    }

    @Test
    fun `onQueryActiveLocationsResult with success calls addLiveLocations`() = runTest {
        val locations = listOf(mock<Location>())
        doNothing().whenever(mutableGlobalState).addLiveLocations(any())
        sut.onQueryActiveLocationsResult(Result.Success(locations))
        verify(mutableGlobalState, times(1)).addLiveLocations(locations)
    }

    @Test
    fun `onQueryActiveLocationsResult with failure does not call addLiveLocations`() = runTest {
        sut.onQueryActiveLocationsResult(Result.Failure(Error.GenericError("error")))
        verify(mutableGlobalState, times(0)).addLiveLocations(any())
    }

    @Test
    fun `onUpdateLiveLocationResult with success calls addLiveLocation`() = runTest {
        val location = mock<Location>()
        doNothing().whenever(mutableGlobalState).addLiveLocation(any())
        sut.onUpdateLiveLocationResult(location, Result.Success(location))
        verify(mutableGlobalState, times(1)).addLiveLocation(location)
    }

    @Test
    fun `onUpdateLiveLocationResult with failure does not call addLiveLocation`() = runTest {
        val location = mock<Location>()
        sut.onUpdateLiveLocationResult(location, Result.Failure(Error.GenericError("error")))
        verify(mutableGlobalState, times(0)).addLiveLocation(any())
    }

    @Test
    fun `onStopLiveLocationSharingResult with success calls removeExpiredLiveLocations`() = runTest {
        val location = mock<Location>()
        doNothing().whenever(mutableGlobalState).removeExpiredLiveLocations()
        sut.onStopLiveLocationSharingResult(location, Result.Success(location))
        verify(mutableGlobalState, times(1)).removeExpiredLiveLocations()
    }

    @Test
    fun `onStopLiveLocationSharingResult with failure does not call removeExpiredLiveLocations`() = runTest {
        val location = mock<Location>()
        sut.onStopLiveLocationSharingResult(location, Result.Failure(Error.GenericError("error")))
        verify(mutableGlobalState, times(0)).removeExpiredLiveLocations()
    }

    @Test
    fun `onStartLiveLocationSharingResult with success calls addLiveLocation`() = runTest {
        val location = mock<Location>()
        doNothing().whenever(mutableGlobalState).addLiveLocation(any())
        sut.onStartLiveLocationSharingResult(location, Result.Success(location))
        verify(mutableGlobalState, times(1)).addLiveLocation(location)
    }

    @Test
    fun `onStartLiveLocationSharingResult with failure does not call addLiveLocation`() = runTest {
        val location = mock<Location>()
        sut.onStartLiveLocationSharingResult(location, Result.Failure(Error.GenericError("error")))
        verify(mutableGlobalState, times(0)).addLiveLocation(any())
    }
}
