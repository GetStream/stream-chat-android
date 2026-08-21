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

package io.getstream.chat.android.ui.feature.search

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.ui.PaparazziViewTest
import io.getstream.chat.android.ui.common.utils.SearchDebounce
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class SearchInputViewTest : PaparazziViewTest() {

    override val deviceConfig = DeviceConfig.PIXEL_2

    override val paparazzi = Paparazzi(deviceConfig = deviceConfig)

    private val testDispatcher = StandardTestDispatcher()

    init {
        DispatcherProvider.set(mainDispatcher = testDispatcher, ioDispatcher = testDispatcher)
    }

    @After
    fun resetDispatchers() {
        DispatcherProvider.reset()
    }

    @Test
    fun `debounced listener is notified once the input is stable`() = runTest(testDispatcher) {
        val queries = mutableListOf<String>()
        val searchInputView = searchInputView { queries += it }

        searchInputView.setQuery("abc")
        advanceTimeBy(DEFAULT_DEBOUNCE_MS)
        assertEquals(emptyList<String>(), queries)

        runCurrent()
        assertEquals(listOf("abc"), queries)
    }

    @Test
    fun `short input is held for longer than regular input`() = runTest(testDispatcher) {
        val queries = mutableListOf<String>()
        val searchInputView = searchInputView { queries += it }

        searchInputView.setQuery("ab")
        advanceTimeBy(SearchDebounce.SHORT_QUERY_DEBOUNCE_MS)
        assertEquals(emptyList<String>(), queries)

        runCurrent()
        assertEquals(listOf("ab"), queries)
    }

    @Test
    fun `clearing the input drops the debounce pending from the last keystroke`() = runTest(testDispatcher) {
        val queries = mutableListOf<String>()
        val searchInputView = searchInputView { queries += it }
        searchInputView.setQuery("abc")

        searchInputView.clear()
        advanceUntilIdle()

        // Without dropping it, the pending debounce notifies "abc" after the cleared query.
        assertEquals(listOf(""), queries)
    }

    private fun searchInputView(onInputChanged: (String) -> Unit) =
        SearchInputView(paparazzi.context).apply {
            setDebouncedInputChangedListener(onInputChanged)
        }

    private companion object {
        private const val DEFAULT_DEBOUNCE_MS = 300L
    }
}
