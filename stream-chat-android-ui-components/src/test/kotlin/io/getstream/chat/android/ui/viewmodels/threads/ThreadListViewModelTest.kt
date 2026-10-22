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

package io.getstream.chat.android.ui.viewmodels.threads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import io.getstream.chat.android.test.InstantTaskExecutorExtension
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.chat.android.ui.common.feature.threads.ThreadListController
import io.getstream.chat.android.ui.common.state.threads.ThreadListState
import io.getstream.chat.android.ui.viewmodel.threads.ThreadListViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

internal class ThreadListViewModelTest {

    @Test
    fun `clearing the view model closes the controller`() {
        val controller: ThreadListController = mock {
            on { state } doReturn MutableStateFlow(
                ThreadListState(
                    threads = emptyList(),
                    isLoading = true,
                    isLoadingMore = false,
                    unseenThreadsCount = 0,
                ),
            )
        }
        val store = ViewModelStore()
        ViewModelProvider(
            store,
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T = ThreadListViewModel(controller) as T
            },
        )[ThreadListViewModel::class.java]

        store.clear()

        verify(controller).close()
    }

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines: TestCoroutineExtension = TestCoroutineExtension()

        @JvmField
        @RegisterExtension
        val instantExecutorExtension: InstantTaskExecutorExtension = InstantTaskExecutorExtension()
    }
}
