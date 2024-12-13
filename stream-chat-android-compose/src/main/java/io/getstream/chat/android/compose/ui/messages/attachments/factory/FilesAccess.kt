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

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_AUDIO
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner

/**
 * Defines the possible states in which the files storage access can be.
 *
 * - [FilesAccess.AUDIO_AND_FULL_VISUAL] when the app has full access to visual media and audio.
 * - [FilesAccess.AUDIO_AND_PARTIAL_VISUAL] when the app has partial access to visual media and full access to audio.
 * - [FilesAccess.AUDIO] when the app has access to audio and no access to visual media.
 * - [FilesAccess.FULL_VISUAL] when the app has full access to visual media and no access to audio.
 * - [FilesAccess.PARTIAL_VISUAL] when the app has partial access to visual media and no access to audio.
 * - [FilesAccess.DENIED] when the app has no access to visual media or audio.
 */
internal enum class FilesAccess {
    AUDIO_AND_FULL_VISUAL,
    AUDIO_AND_PARTIAL_VISUAL,
    AUDIO,
    FULL_VISUAL,
    PARTIAL_VISUAL,
    DENIED,
}

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
        val eventObserver = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                val visualMediaAccess = resolveVisualMediaAccessState(context)
                val audioAccess = isAudioAccessGranted(context)
                value = when (visualMediaAccess) {
                    VisualMediaAccess.FULL -> if (audioAccess) {
                        FilesAccess.AUDIO_AND_FULL_VISUAL
                    } else {
                        FilesAccess.FULL_VISUAL
                    }
                    VisualMediaAccess.PARTIAL -> if (audioAccess) {
                        FilesAccess.AUDIO_AND_PARTIAL_VISUAL
                    } else {
                        FilesAccess.PARTIAL_VISUAL
                    }
                    VisualMediaAccess.DENIED -> if (audioAccess) {
                        FilesAccess.AUDIO
                    } else {
                        FilesAccess.DENIED
                    }
                }
                onResume(value)
            }
        }
        lifecycleOwner.lifecycle.addObserver(eventObserver)
        awaitDispose {
            lifecycleOwner.lifecycle.removeObserver(eventObserver)
        }
    }
}

private fun isAudioAccessGranted(context: Context): Boolean {
    val isPermissionGranted = { permission: String ->
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        isPermissionGranted(READ_MEDIA_AUDIO)
    } else {
        isPermissionGranted(READ_EXTERNAL_STORAGE)
    }
}
