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

package io.getstream.chat.android.compose.handlers

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Environment
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.shouldShowRationale
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.state.extensions.downloadAttachment
import io.getstream.chat.android.ui.common.utils.extensions.onPermissionRequested
import io.getstream.chat.android.ui.common.utils.extensions.wasPermissionRequested
import io.getstream.chat.android.uiutils.util.openSystemSettings

/**
 * Interface for implementing custom permission handlers.
 */
public interface PermissionHandler {

    /**
     * Checks whether the permission handler can handle the given permission.
     *
     * @param permission The permission needed to handle.
     *
     * @return If the handler can handle the permission or not.
     */
    public fun canHandle(permission: String): Boolean

    /**
     * Checks whether the permission handler can handle the given permissions.
     *
     * @param permissions The permissions that need to be handled.
     *
     * @return If the handler can handle the permissions or not.
     */
    public fun canHandle(permissions: List<String>): Boolean {
        return permissions.all { canHandle(it) }
    }

    /**
     * Called to handle a request if the permission is granted, prompts the user to enable the permission otherwise.
     *
     * @param payload The custom payload to do operations on if the permission is granted.
     */
    public fun onHandleRequest(payload: Map<String, Any> = mapOf())
}

/**
 * Default implementation of the download permission handler. By default will request the user to enable the permission
 * and once it has been granted will download the attachment.
 *
 * @param permissionState The [permissionState] for the permission we need. This should be
 * [Manifest.permission.WRITE_EXTERNAL_STORAGE].
 * @param context The context for executing actions.
 * @param onPermissionRequired Handler when the user wants to download a file but the permission has not been granted.
 * By default it will prompt the user for the permission or take him directly to settings.
 * @param onPermissionGranted Handler when the user grants the permission. By default will download the requested file.
 */
@OptIn(ExperimentalPermissionsApi::class)
public class DownloadPermissionHandler(
    private val permissionState: PermissionState,
    private val context: Context,
    private inline val onPermissionRequired: () -> Unit = {
        if (!context.wasPermissionRequested(permissionState.permission) || permissionState.status.shouldShowRationale) {
            permissionState.launchPermissionRequest()
        } else {
            context.openSystemSettings()
        }
    },
    private inline val onPermissionGranted: (Map<String, Any>) -> Unit = { payload ->
        (payload[PayloadAttachment] as? Attachment)?.let {
            ChatClient
                .instance()
                .downloadAttachment(context, it)
                .enqueue()
        }
    },
) : PermissionHandler {

    /**
     * Payload to be downloaded once the permission has been granted.
     */
    private var lastPayload: Map<String, Any>? = null

    /**
     * Observes the change in the permission state and automatically downloads the attachment once the permission has
     * been granted.
     */
    @Composable
    public fun ObservePermissionChanges() {
        LaunchedEffect(key1 = permissionState.status.isGranted) {
            if (permissionState.status.isGranted) lastPayload?.let {
                onPermissionGranted(it)
                lastPayload = null
            }
        }
    }

    /**
     * Checks whether the permission handler can handle the given permissions.
     *
     * @param permission The permission needed to be handled.
     *
     * @return If the handler can handle the permissions or not.
     */
    override fun canHandle(permission: String): Boolean {
        return permissionState.permission == permission
    }

    override fun onHandleRequest(payload: Map<String, Any>) {
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !Environment.isExternalStorageLegacy()) ||
            permissionState.status.isGranted
        ) {
            onPermissionGranted(payload)
            lastPayload = null
        } else {
            lastPayload = payload
            onPermissionRequired()
            context.onPermissionRequested(permissionState.permission)
        }
    }

    public companion object {
        public const val PayloadAttachment: String = "attachment"
    }
}
