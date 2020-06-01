package com.getstream.sdk.chat.utils

import android.Manifest
import android.R
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Build
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.getstream.sdk.chat.Chat.Companion.getInstance
import com.getstream.sdk.chat.navigation.destinations.AppSettingsDestination

object PermissionChecker {

	fun isGrantedStoragePermissions(context: Context): Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
			listOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,
					Manifest.permission.WRITE_EXTERNAL_STORAGE,
					Manifest.permission.READ_EXTERNAL_STORAGE)
					.all { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }

	fun isGrantedCameraPermissions(context: Context): Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
			listOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,
					Manifest.permission.WRITE_EXTERNAL_STORAGE,
					Manifest.permission.CAMERA)
					.all { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }

	fun showPermissionSettingDialog(context: Context, message: String) {
		val appName = Utils.getApplicationName(context)
		val msg = "$appName $message"
		val alertDialog = AlertDialog.Builder(context)
				.setTitle(appName)
				.setMessage(msg)
				.setPositiveButton(R.string.ok, null)
				.setNegativeButton(R.string.cancel, null)
				.create()
		alertDialog.setOnShowListener { dialog: DialogInterface? ->
			val button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
			button.setOnClickListener { v: View? ->
				getInstance().navigator.navigate(AppSettingsDestination(context))
				alertDialog.dismiss()
			}
		}
		alertDialog.show()
	}
}