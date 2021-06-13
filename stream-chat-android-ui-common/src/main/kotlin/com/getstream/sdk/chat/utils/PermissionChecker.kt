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
            listOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            )
                .all {
                    ContextCompat.checkSelfPermission(
                        context,
                        it
                    ) == PackageManager.PERMISSION_GRANTED
                }

    public fun checkStoragePermissions(
        view: View,
        onPermissionDenied: () -> Unit = { },
        onPermissionGranted: () -> Unit
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
        onPermissionGranted: () -> Unit
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
        onPermissionGranted: () -> Unit
    ) {
        checkPermissions(
            view,
            view.context.getString(R.string.stream_ui_message_input_permission_camera_title),
            view.context.getString(R.string.stream_ui_message_input_permission_camera_message),
            view.context.getString(R.string.stream_ui_message_input_permission_camera_message),
            listOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            ),
            onPermissionDenied,
            onPermissionGranted
        )
    }

    private fun checkPermissions(
        view: View,
        dialogTitle: String,
        dialogMessage: String,
        snackbarMessage: String,
        permissions: List<String>,
        onPermissionDenied: () -> Unit,
        onPermissionGranted: () -> Unit
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
        snackbarMessage: String
    ): SnackbarOnAnyDeniedMultiplePermissionsListener =
        SnackbarOnAnyDeniedMultiplePermissionsListener.Builder
            .with(view, snackbarMessage)
            .withOpenSettingsButton(R.string.stream_ui_message_input_permissions_setting_button)
            .build()

    private fun dialogPermissionsListener(
        context: Context,
        dialogTitle: String,
        dialogMessage: String
    ): DialogOnAnyDeniedMultiplePermissionsListener =
        DialogOnAnyDeniedMultiplePermissionsListener.Builder
            .withContext(context)
            .withTitle(dialogTitle)
            .withMessage(dialogMessage)
            .withButtonText(android.R.string.ok)
            .build()
}
