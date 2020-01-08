package com.getstream.sdk.chat.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Button;

import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.navigation.destinations.AppSettingsDestination;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

public class PermissionChecker {

    public static void permissionCheck(@NonNull Activity activity, @Nullable Fragment fragment) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                (activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                        || activity.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                        || activity.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {

            int hasStoragePermission = activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int hasReadPermission = activity.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
            int hasCameraPermission = activity.checkSelfPermission(Manifest.permission.CAMERA);

            List<String> permissions = new ArrayList<>();
            if (hasStoragePermission != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (hasReadPermission != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            if (hasCameraPermission != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.CAMERA);            }

            if (!permissions.isEmpty()) {
                if (fragment == null)
                    activity.requestPermissions(permissions.toArray(new String[permissions.size()]),
                            Constant.PERMISSIONS_REQUEST);
                else
                    fragment.requestPermissions(permissions.toArray(new String[permissions.size()]),
                            Constant.PERMISSIONS_REQUEST);
            }
        }
    }


    public static boolean isGrantedStoragePermissions(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int hasStoragePermission = context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int hasReadPermission = context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
            return (hasStoragePermission == PackageManager.PERMISSION_GRANTED)
                    && (hasReadPermission == PackageManager.PERMISSION_GRANTED);
        } else
            return true;
    }

    public static boolean isGrantedCameraPermissions(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int hasStoragePermission = context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int hasReadPermission = context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
            int hasCameraPermission = context.checkSelfPermission(Manifest.permission.CAMERA);
            return (hasStoragePermission == PackageManager.PERMISSION_GRANTED)
                    && (hasReadPermission == PackageManager.PERMISSION_GRANTED)
                    && (hasCameraPermission == PackageManager.PERMISSION_GRANTED);
        } else
            return true;
    }

    public static void showPermissionSettingDialog(Context context, String message){
        String appName = Utils.getApplicationName(context);
        String msg = appName + " " + message;
        final AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setTitle(appName)
                .setMessage(msg)
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        alertDialog.setOnShowListener(dialog -> {
            Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(v -> {
                StreamChat.getNavigator().navigate(new AppSettingsDestination(context));
                alertDialog.dismiss();
            });
        });
        alertDialog.show();
    }
}
