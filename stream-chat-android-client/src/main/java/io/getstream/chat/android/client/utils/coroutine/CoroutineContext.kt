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

package io.getstream.chat.android.client.utils.coroutine

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlin.coroutines.CoroutineContext

/**
 * Cancels all children of the [Job] in this context, without touching the state of this job itself
 * with optional cancellation [exclusion] and [cause]. See [Job.cancel].
 * It does not do anything if there is no job in the context or it has no children.
 */
internal fun CoroutineContext.cancelChildrenExcept(exclusion: Job?, cause: CancellationException? = null) {
    this[Job]?.children?.forEach {
        if (exclusion != it) {
            it.cancel(cause)
        }
    }
}
