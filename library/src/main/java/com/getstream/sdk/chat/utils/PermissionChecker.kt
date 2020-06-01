package com.getstream.sdk.chat.utils

import android.Manifest
import android.R
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Build
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.getstream.sdk.chat.Chat.Companion.getInstance
import com.getstream.sdk.chat.navigation.destinations.AppSettingsDestination
import java.util.ArrayList

object PermissionChecker {
	fun permissionCheck(activity: Activity, fragment: Fragment?) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
				(activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || activity.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED || activity.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
			val hasStoragePermission = activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
			val hasReadPermission = activity.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
			val hasCameraPermission = activity.checkSelfPermission(Manifest.permission.CAMERA)
			val permissions: MutableList<String> = ArrayList()
			if (hasStoragePermission != PackageManager.PERMISSION_GRANTED) {
				permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
			}
			if (hasReadPermission != PackageManager.PERMISSION_GRANTED) {
				permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
			}
			if (hasCameraPermission != PackageManager.PERMISSION_GRANTED) {
				permissions.add(Manifest.permission.CAMERA)
			}
			if (! permissions.isEmpty()) {
				if (fragment == null) activity.requestPermissions(permissions.toTypedArray(),
						Constant.PERMISSIONS_REQUEST) else fragment.requestPermissions(permissions.toTypedArray(),
						Constant.PERMISSIONS_REQUEST)
			}
		}
	}

	fun isGrantedStoragePermissions(context: Context): Boolean {
		return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			val hasStoragePermission = context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
			val hasReadPermission = context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
			(hasStoragePermission == PackageManager.PERMISSION_GRANTED
					&& hasReadPermission == PackageManager.PERMISSION_GRANTED)
		} else true
	}

	fun isGrantedCameraPermissions(context: Context): Boolean {
		return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			val hasStoragePermission = context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
			val hasReadPermission = context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
			val hasCameraPermission = context.checkSelfPermission(Manifest.permission.CAMERA)
			(hasStoragePermission == PackageManager.PERMISSION_GRANTED
					&& hasReadPermission == PackageManager.PERMISSION_GRANTED
					&& hasCameraPermission == PackageManager.PERMISSION_GRANTED)
		} else true
	}

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