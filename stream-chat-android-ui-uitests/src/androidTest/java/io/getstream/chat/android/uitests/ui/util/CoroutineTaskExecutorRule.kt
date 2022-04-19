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

package io.getstream.chat.android.uitests.ui.util

import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import kotlinx.coroutines.Dispatchers
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * A Junit Test Rule that helps to synchronize Espresso tests with the work that is
 * being done on the IO dispatcher.
 */
internal class CoroutineTaskExecutorRule : TestWatcher() {

    /**
     * The wrapped IO dispatcher
     */
    private val dispatcher = DelegatingIdlingResourceDispatcher(Dispatchers.IO)

    /**
     * Invoked when a test is about to start.
     */
    @OptIn(InternalStreamChatApi::class)
    override fun starting(description: Description) {
        super.starting(description)
        DispatcherProvider.set(
            mainDispatcher = Dispatchers.Main,
            ioDispatcher = dispatcher
        )
    }

    /**
     * Invoked when a test method finishes (whether passing or failing).
     */
    @OptIn(InternalStreamChatApi::class)
    override fun finished(description: Description) {
        super.finished(description)
        DispatcherProvider.reset()
        dispatcher.reset()
    }
}
