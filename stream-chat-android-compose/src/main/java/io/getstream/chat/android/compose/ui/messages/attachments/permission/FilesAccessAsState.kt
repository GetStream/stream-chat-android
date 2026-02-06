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

package io.getstream.chat.android.compose.ui.messages.attachments.permission

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
 * @param onAccessChange A callback invoked when files access is available
 * (either initially or after a permission change).
 * It provides the latest [FilesAccess] state and is the recommended place to trigger data loading from storage.
 * @return A [State] holding the current [FilesAccess] status.
 */
@Composable
internal fun filesAccessAsState(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    onAccessChange: (FilesAccess) -> Unit,
): State<FilesAccess> =
    produceState(
        initialValue = resolveFilesAccessState(context),
        context,
        lifecycleOwner,
    ) {
        // Trigger callback with the initial value
        onAccessChange(value)

        // Continue observing ON_RESUME to handle permission changes, e.g., when returning from settings.
        val eventObserver = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val newAccess = resolveFilesAccessState(context)
                // Only call onAccessChange if the access state actually changed.
                if (newAccess != value) {
                    value = newAccess
                    onAccessChange(newAccess)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(eventObserver)
        awaitDispose {
            lifecycleOwner.lifecycle.removeObserver(eventObserver)
        }
    }
