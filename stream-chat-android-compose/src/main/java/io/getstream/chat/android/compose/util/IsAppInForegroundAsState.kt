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

package io.getstream.chat.android.compose.util

import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Produces a [State] that indicates whether the app is currently in the foreground.
 */
@Composable
internal fun isAppInForegroundAsState(): State<Boolean> {
    val lifecycleOwner = LocalLifecycleOwner.current
    return produceState(initialValue = true, lifecycleOwner) {
        val lifecycle = lifecycleOwner.lifecycle
        val observer = LifecycleEventObserver { _, event ->
            value = when (event) {
                Lifecycle.Event.ON_START -> true
                Lifecycle.Event.ON_STOP -> false
                else -> value
            }
        }
        withContext(Dispatchers.Main.immediate) {
            lifecycle.addObserver(observer)
        }
        awaitDispose {
            lifecycle.removeObserverOnMainThread(observer)
        }
    }
}

/**
 * Removes a lifecycle observer on the main thread.
 *
 * [Lifecycle.removeObserver] must be called on the main thread. During normal app execution
 * this is typically the case, but in instrumentation tests this might be called on the
 * Compose test dispatcher thread. To avoid IllegalStateException, we ensure the removal
 * happens on the main thread.
 *
 * @param observer The [LifecycleEventObserver] to remove.
 */
private fun Lifecycle.removeObserverOnMainThread(observer: LifecycleEventObserver) {
    if (Looper.myLooper() == Looper.getMainLooper()) {
        removeObserver(observer)
    } else {
        Handler(Looper.getMainLooper()).post {
            removeObserver(observer)
        }
    }
}
