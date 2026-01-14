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

package io.getstream.chat.android.ui.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.permissionx.guolindev.PermissionX
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.ui.common.R
import io.getstream.chat.android.ui.common.permissions.Permissions
import io.getstream.chat.android.ui.common.utils.isPermissionDeclared
import io.getstream.chat.android.ui.common.utils.openSystemSettings
import io.getstream.chat.android.ui.utils.extensions.activity
import io.getstream.chat.android.ui.utils.extensions.dpToPxPrecise

private const val SNACKBAR_ELEVATION_IN_DP = 20

@InternalStreamChatApi
public class PermissionChecker {

    public fun isGrantedCameraPermissions(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    public fun isGrantedAudioRecordPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECORD_AUDIO,
        ) == PackageManager.PERMISSION_GRANTED

    /**
     * Check if Camera Permission needs to be requested to the user
     *
     * @param context of the App
     *
     * @return True if [Manifest.permission.CAMERA] is present on the App Manifest and user didn't grant it,
     * False in another case
     */
    public fun isNeededToRequestForCameraPermissions(context: Context): Boolean =
        context.isPermissionDeclared(Manifest.permission.CAMERA) && !isGrantedCameraPermissions(context)

    /**
     * Requests the correct visual media permissions (image/video) based on the device's API level.
     *
     * @param view The view used to obtain context.
     * @param onPermissionResult Action invoked when the permission request is completed.
     */
    public fun checkVisualMediaPermissions(
        view: View,
        onPermissionResult: (Map<String, Boolean>) -> Unit,
    ) {
        checkStoragePermissions(
            view = view,
            permissions = Permissions.visualMediaPermissions(),
            onPermissionResult = onPermissionResult,
        )
    }

    /**
     * Requests the correct audio media permissions based on the device's API level.
     *
     * @param view The view used to obtain context.
     * @param onPermissionResult Action invoked when the permission request is completed.
     */
    public fun checkAudioPermissions(
        view: View,
        onPermissionResult: (Map<String, Boolean>) -> Unit,
    ) {
        checkStoragePermissions(
            view = view,
            permissions = Permissions.audioPermissions(),
            onPermissionResult = onPermissionResult,
        )
    }

    /**
     * Requests the correct files permissions (image/video/audio) based on the device's API level.
     *
     * @param view The view used to obtain context.
     * @param onPermissionResult Action invoked when the permission request is completed.
     */
    public fun checkFilesPermissions(
        view: View,
        onPermissionResult: (Map<String, Boolean>) -> Unit,
    ) {
        checkStoragePermissions(
            view = view,
            permissions = Permissions.filesPermissions(),
            onPermissionResult = onPermissionResult,
        )
    }

    private fun checkStoragePermissions(
        view: View,
        permissions: Array<String>,
        onPermissionResult: (Map<String, Boolean>) -> Unit,
    ) {
        val activity = view.activity ?: return
        PermissionX.init(activity)
            .permissions(permissions.asList())
            .onExplainRequestReason { _, _ ->
                val title = view.context.getString(R.string.stream_ui_message_composer_permission_storage_title)
                val message = view.context.getString(R.string.stream_ui_message_composer_permission_storage_message)
                showPermissionRationaleDialog(view.context, title, message)
            }
            .onForwardToSettings { _, _ ->
                val message = view.context.getString(R.string.stream_ui_message_composer_permission_setting_message)
                showPermissionDeniedSnackbar(view, message)
            }
            .request { _, granted, denied ->
                val result = granted.associateWith { true } + denied.associateWith { false }
                onPermissionResult(result)
            }
    }

    /**
     * Checks is [Manifest.permission.WRITE_EXTERNAL_STORAGE] is needed an requests if necessary.
     * Permission will be requested on versions below [Build.VERSION_CODES.Q]
     * or if legacy external storage is enabled.
     * Simply runs [onPermissionGranted] if the permission is not needed.
     *
     * The method is being used to get access to external download folder used by download attachment process.
     *
     * @param view The view used to obtain context and show the snackbar.
     * @param onPermissionDenied Lambda to be run when permission is denied.
     * @param onPermissionGranted Lambda to be run when permission is granted.
     */
    public fun checkWriteStoragePermissions(
        view: View,
        onPermissionDenied: () -> Unit = { },
        onPermissionGranted: () -> Unit,
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || Environment.isExternalStorageLegacy()) {
            checkPermissions(
                view,
                view.context.getString(R.string.stream_ui_message_composer_permission_storage_title),
                view.context.getString(R.string.stream_ui_message_composer_permission_storage_message),
                view.context.getString(R.string.stream_ui_message_composer_permission_setting_message),
                listOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                onPermissionDenied,
                onPermissionGranted,
            )
        } else {
            onPermissionGranted()
        }
    }

    public fun checkCameraPermissions(
        view: View,
        onPermissionDenied: () -> Unit = { },
        onPermissionGranted: () -> Unit,
    ) {
        checkPermissions(
            view,
            view.context.getString(R.string.stream_ui_message_composer_permission_camera_title),
            view.context.getString(R.string.stream_ui_message_composer_permission_camera_message),
            view.context.getString(R.string.stream_ui_message_composer_permission_camera_message),
            listOf(Manifest.permission.CAMERA),
            onPermissionDenied,
            onPermissionGranted,
        )
    }

    public fun checkAudioRecordPermissions(
        view: View,
        onPermissionDenied: () -> Unit = { },
        onPermissionGranted: () -> Unit = { },
    ) {
        checkPermissions(
            view,
            view.context.getString(R.string.stream_ui_message_composer_permission_audio_record_title),
            view.context.getString(R.string.stream_ui_message_composer_permission_audio_record_message),
            view.context.getString(R.string.stream_ui_message_composer_permission_audio_record_message),
            listOf(Manifest.permission.RECORD_AUDIO),
            onPermissionDenied,
            onPermissionGranted,
        )
    }

    @Suppress("LongParameterList")
    private fun checkPermissions(
        view: View,
        dialogTitle: String,
        dialogMessage: String,
        snackbarMessage: String,
        permissions: List<String>,
        onPermissionDenied: () -> Unit,
        onPermissionGranted: () -> Unit,
    ) {
        val activity = view.activity ?: return

        PermissionX.init(activity)
            .permissions(permissions)
            .onExplainRequestReason { _, _ ->
                showPermissionRationaleDialog(view.context, dialogTitle, dialogMessage)
            }
            .onForwardToSettings { _, _ ->
                showPermissionDeniedSnackbar(view, snackbarMessage)
            }
            .request { allGranted, _, _ ->
                if (allGranted) onPermissionGranted() else onPermissionDenied()
            }
    }

    /**
     * Shows permission rationale dialog.
     *
     * @param context The context to show alert dialog.
     * @param dialogTitle The title of the dialog.
     * @param dialogMessage The message to display.
     */
    private fun showPermissionRationaleDialog(
        context: Context,
        dialogTitle: String,
        dialogMessage: String,
    ) {
        AlertDialog.Builder(context)
            .setTitle(dialogTitle)
            .setMessage(dialogMessage)
            .setPositiveButton(android.R.string.ok) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    /**
     * Shows a [Snackbar] whenever a permission has been denied.
     *
     * @param view The anchor view for the Snackbar.
     * @param snackbarMessage The message displayed in the Snackbar.
     */
    private fun showPermissionDeniedSnackbar(
        view: View,
        snackbarMessage: String,
    ) {
        Snackbar.make(view, snackbarMessage, Snackbar.LENGTH_LONG).apply {
            setAction(R.string.stream_ui_message_composer_permissions_setting_button) {
                context.openSystemSettings()
            }
            addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                override fun onShown(sb: Snackbar?) {
                    super.onShown(sb)
                    sb?.view?.elevation = SNACKBAR_ELEVATION_IN_DP.dpToPxPrecise()
                }
            })
            show()
        }
    }
}
