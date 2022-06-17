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
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.getstream.sdk.chat.utils.extensions.openSystemSettings
import io.getstream.chat.android.client.call.Call

/**
 * Handles attachment download and requesting requiered permission so that the user can save the attachment to the
 * device.
 *
 * @param onPermissionGranted Handler when the user grants the write permission. By default will download the selected
 * attachment.
 * @param onPermissionDenied Handler when the user denies the permission.
 * @param onPermissionRequired Handler when the user needs to be prompted to enable the write permission. By default
 * will prompt the user to enable the permission, of if the user was already asked will redirect him to the app
 * settings.
 */
public open class AttachmentDownloader(
    private val onPermissionGranted: (() -> Call<Unit>) -> Unit = { it().enqueue() },
    private val onPermissionDenied: () -> Unit = {},
    private val onPermissionRequired: (ActivityResultLauncher<() -> Call<Unit>>, () -> Call<Unit>) -> Unit =
        { permissionRequestLauncher, downloadCall ->
            permissionRequestLauncher.launch(downloadCall)
        },
) : ActivityResultContract<() -> Call<Unit>, Unit>() {
    /**
     * The attachment download call we wish to execute.
     */
    private var downloadCall: (() -> Call<Unit>)? = null

    /**
     * Activity request launcher which shows the prompt to enable permissions.
     */
    private lateinit var permissionRequestLauncher: ActivityResultLauncher<() -> Call<Unit>>

    /**
     * Crates an intent for the [ActivityResultLauncher].
     *
     * @param context [Context]
     * @param input The download call we want to make after the permission has been granted.
     */
    override fun createIntent(context: Context, input: () -> Call<Unit>): Intent {
        downloadCall = input
        context.onPermissionRequested(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        return ActivityResultContracts.RequestPermission()
            .createIntent(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    /**
     * Parses the result once the user gave or denied the write permission. If the permission has been granted downloads
     * the attachment automatically by default.
     */
    override fun parseResult(resultCode: Int, intent: Intent?) {
        val result = ActivityResultContracts.RequestPermission().parseResult(resultCode, intent)
        if (result) {
            downloadCall?.let(onPermissionGranted)
            downloadCall = null
        } else {
            downloadCall = null
            onPermissionDenied()
        }
    }

    /**
     * Called to register the launcher with the activity. This must be done before or when onCreate has been called.
     * Best to call it when instantiating a fragment.
     */
    public fun registerForActivityResult(fragment: Fragment) {
        permissionRequestLauncher = fragment.registerForActivityResult(this) {}
    }

    /**
     * Called to register the launcher with the activity. This must be done before or when onCreate has been called.
     * Best to call it when instantiating an activity.
     */
    public fun registerForActivityResult(activity: AppCompatActivity) {
        permissionRequestLauncher = activity.registerForActivityResult(this) {}
    }

    /**
     * Called when we have an attachment to download. By default will first check for permissions and if the device
     * needs the permission. If there is no permission user will be prompted to give it, or if he already denied it
     * will take him to the settings.
     *
     * @param context [Context]
     * @param downloadCall The call to make to download an attachment.
     */
    public open fun onDownloadAttachment(context: Context, downloadCall: () -> Call<Unit>) {
        val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
        val hasPermission =
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P || hasPermission) {
            onPermissionGranted(downloadCall)
        } else {
            this.downloadCall = downloadCall
            if (!hasPermission && !context.wasPermissionRequested(permission)) {
                onPermissionRequired(permissionRequestLauncher, downloadCall)
            } else {
                context.openSystemSettings()
            }
        }
    }
}
