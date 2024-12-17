/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.ui.messages.attachments.factory

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import io.getstream.chat.android.ui.common.permissions.FilesAccess
import io.getstream.chat.android.ui.common.permissions.resolveFilesAccessState

/**
 * Produces the current [FilesAccess] as [State] that can be observed in a [Composable] function.
 * It updates the value on the "onResume" lifecycle event, to ensure that the latest permission state is reflected,
 * to cover the case where the user changes the permission from settings and returns to the app.
 *
 * @param context The context to use to check the files access.
 * @param lifecycleOwner The lifecycle owner to observe the files access changes.
 * @param onResume Callback invoked on the "onResume" lifecycle event. It provides the latest [FilesAccess] state, and
 * should be used to access the data from storage (if possible).
 */
@Composable
internal fun filesAccessAsState(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    onResume: (FilesAccess) -> Unit,
): State<FilesAccess> {
    return produceState(
        initialValue = FilesAccess.DENIED,
        context,
        lifecycleOwner,
    ) {
        val eventObserver = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                value = resolveFilesAccessState(context)
                onResume(value)
            }
        }
        lifecycleOwner.lifecycle.addObserver(eventObserver)
        awaitDispose {
            lifecycleOwner.lifecycle.removeObserver(eventObserver)
        }
    }
}
