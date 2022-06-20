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

package io.getstream.chat.android.ui.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.getstream.sdk.chat.utils.extensions.openSystemSettings
import io.getstream.chat.android.client.call.Call

/**
 * Abstract implementation of a permission handler.
 *
 * @param permission The permission we are prompting the user to enable.
 * @param onPermissionGranted Handler when the permission has been granted.
 * @param onPermissionDenied Handler when the permission has been denied.
 * @param onPermissionRequired Handler when the permission is required.
 */
public open class PermissionHandler<I>(
    public val permission: String,
    public val onPermissionGranted: (I) -> Unit,
    public val onPermissionDenied: () -> Unit = {},
    public val onPermissionRequired: (ActivityResultLauncher<String>, Context) -> Unit =
        { permissionRequestLauncher, context ->
            if (context.wasPermissionRequested(permission)) {
                context.openSystemSettings()
            } else {
                permissionRequestLauncher.launch(permission)
            }
        },
) {

    /**
     * Data that is passed to the handler upon which we can act given the permission is granted.
     */
    private var data: I? = null

    /**
     * The request launcher. Must call [registerForActivityResult] before calling [onHandleRequest].
     */
    private lateinit var permissionRequestLauncher: ActivityResultLauncher<String>

    /**
     * Called to register the launcher with the activity. This must be done before or when onCreate has been called.
     * Best to call it when instantiating a fragment.
     */
    public fun registerForActivityResult(fragment: Fragment) {
        permissionRequestLauncher =
            fragment.registerForActivityResult(ActivityResultContracts.RequestPermission(), ::onPermissionResult)
    }

    /**
     * Called to register the launcher with the activity. This must be done before or when onCreate has been called.
     * Best to call it when instantiating an activity.
     */
    public fun registerForActivityResult(activity: AppCompatActivity) {
        permissionRequestLauncher =
            activity.registerForActivityResult(ActivityResultContracts.RequestPermission(), ::onPermissionResult)
    }

    /**
     * Handles permission request result and notifies using [onPermissionGranted] and [onPermissionDenied] handlers.
     */
    private fun onPermissionResult(isGranted: Boolean) {
        if (isGranted) {
            data?.let(onPermissionGranted)
        } else {
            onPermissionDenied()
        }
        data = null
    }

    /**
     * Called when we have an action that requires permission. By default will first check for permissions and if the
     * device needs the permission. If there is no permission user will be prompted to give it, or if he already denied
     * the permission it will take him to the app settings.
     *
     * Must be called after [registerForActivityResult].
     *
     * @param context [Context]
     * @param data The call to make to download an attachment.
     * @param isPermissionRequired Some permissions might be required just for certain Android versions.
     */
    public open fun onHandleRequest(context: Context, data: I, isPermissionRequired: () -> Boolean = { true }) {
        val hasPermission =
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

        if (isPermissionRequired() && !hasPermission) {
            this.data = data
            onPermissionRequired(permissionRequestLauncher, context)
        } else {
            onPermissionGranted(data)
            this.data = null
        }
    }
}

/**
 * Default implementation of the [PermissionHandler] to automatically download attachments.
 */
public class DownloadPermissionHandler : PermissionHandler<() -> Call<Unit>>(
    permission = Manifest.permission.WRITE_EXTERNAL_STORAGE,
    onPermissionGranted = { it().enqueue() },
    onPermissionDenied = {},
    onPermissionRequired = { permissionRequestLauncher, context ->
        if (context.wasPermissionRequested(permission = Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            context.openSystemSettings()
        } else {
            permissionRequestLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }
)
