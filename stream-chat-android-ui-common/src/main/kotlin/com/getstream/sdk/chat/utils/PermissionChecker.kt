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
 
package com.getstream.sdk.chat.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.view.View
import androidx.core.content.ContextCompat
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.listener.multi.BaseMultiplePermissionsListener
import com.karumi.dexter.listener.multi.DialogOnAnyDeniedMultiplePermissionsListener
import com.karumi.dexter.listener.multi.SnackbarOnAnyDeniedMultiplePermissionsListener
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.ui.common.R

@InternalStreamChatApi
public class PermissionChecker {

    public fun isGrantedStoragePermissions(context: Context): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    public fun isGrantedCameraPermissions(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    /**
     * Check if Camera Permission needs to be requested to the user
     *
     * @param context of the App
     *
     * @return True if [Manifest.permission.CAMERA] is present on the App Manifest and user didn't grant it, False in another case
     */
    public fun isNeededToRequestForCameraPermissions(context: Context): Boolean =
        isPermissionContainedOnManifest(context, Manifest.permission.CAMERA) && !isGrantedCameraPermissions(context)

    public fun checkStoragePermissions(
        view: View,
        onPermissionDenied: () -> Unit = { },
        onPermissionGranted: () -> Unit,
    ) {
        checkPermissions(
            view,
            view.context.getString(R.string.stream_ui_message_input_permission_storage_title),
            view.context.getString(R.string.stream_ui_message_input_permission_storage_message),
            view.context.getString(R.string.stream_ui_message_input_permission_setting_message),
            listOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            onPermissionDenied,
            onPermissionGranted
        )
    }

    public fun checkWriteStoragePermissions(
        view: View,
        onPermissionDenied: () -> Unit = { },
        onPermissionGranted: () -> Unit,
    ) {
        checkPermissions(
            view,
            view.context.getString(R.string.stream_ui_message_input_permission_storage_title),
            view.context.getString(R.string.stream_ui_message_input_permission_storage_message),
            view.context.getString(R.string.stream_ui_message_input_permission_setting_message),
            listOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            onPermissionDenied,
            onPermissionGranted
        )
    }

    public fun checkCameraPermissions(
        view: View,
        onPermissionDenied: () -> Unit = { },
        onPermissionGranted: () -> Unit,
    ) {
        checkPermissions(
            view,
            view.context.getString(R.string.stream_ui_message_input_permission_camera_title),
            view.context.getString(R.string.stream_ui_message_input_permission_camera_message),
            view.context.getString(R.string.stream_ui_message_input_permission_camera_message),
            listOf(Manifest.permission.CAMERA),
            onPermissionDenied,
            onPermissionGranted
        )
    }

    /**
     * Check if the [permission] was declared on the App Manifest
     *
     * @param context of the current Application
     * @param permission name to be checked
     *
     * @return True if the permission is present on the App Manifest
     */
    private fun isPermissionContainedOnManifest(context: Context, permission: String): Boolean =
        context.packageManager
            .getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS)
            .requestedPermissions
            .contains(permission)

    private fun checkPermissions(
        view: View,
        dialogTitle: String,
        dialogMessage: String,
        snackbarMessage: String,
        permissions: List<String>,
        onPermissionDenied: () -> Unit,
        onPermissionGranted: () -> Unit,
    ) {

        val permissionsListener = object : BaseMultiplePermissionsListener() {

            override fun onPermissionsChecked(mumultiplePermissionsReport: MultiplePermissionsReport) {
                if (mumultiplePermissionsReport.areAllPermissionsGranted()) {
                    onPermissionGranted()
                } else {
                    if (mumultiplePermissionsReport.isAnyPermissionPermanentlyDenied) {
                        snackbarPermissionsListener(view, snackbarMessage).onPermissionsChecked(
                            mumultiplePermissionsReport
                        )
                    } else {
                        dialogPermissionsListener(
                            view.context,
                            dialogTitle,
                            dialogMessage
                        ).onPermissionsChecked(mumultiplePermissionsReport)
                    }
                    onPermissionDenied()
                }
            }
        }

        Dexter.withContext(view.context)
            .withPermissions(permissions)
            .withListener(permissionsListener)
            .check()
    }

    private fun snackbarPermissionsListener(
        view: View,
        snackbarMessage: String,
    ): SnackbarOnAnyDeniedMultiplePermissionsListener =
        SnackbarOnAnyDeniedMultiplePermissionsListener.Builder
            .with(view, snackbarMessage)
            .withOpenSettingsButton(R.string.stream_ui_message_input_permissions_setting_button)
            .build()

    private fun dialogPermissionsListener(
        context: Context,
        dialogTitle: String,
        dialogMessage: String,
    ): DialogOnAnyDeniedMultiplePermissionsListener =
        DialogOnAnyDeniedMultiplePermissionsListener.Builder
            .withContext(context)
            .withTitle(dialogTitle)
            .withMessage(dialogMessage)
            .withButtonText(android.R.string.ok)
            .build()
}
