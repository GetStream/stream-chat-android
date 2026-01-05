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

package io.getstream.chat.android.ui.common.utils.extensions

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach

/**
 * Calls the [onFirst] lambda when the first element is emitted by the upstream [Flow].
 *
 * @param onFirst The lambda called when the first element is emmited by the upstream [Flow].
 *
 * @return The upstream [Flow] this operator was called on.
 */
public fun <T> Flow<T>.onFirst(onFirst: (T) -> Unit): Flow<T> {
    var wasFirstEmission = false

    return onEach {
        if (wasFirstEmission) return@onEach
        wasFirstEmission = true
        onFirst(it)
    }
}
