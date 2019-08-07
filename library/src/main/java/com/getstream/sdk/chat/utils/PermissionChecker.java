package com.getstream.sdk.chat.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

public class PermissionChecker {
    public static void permissionCheck(Activity activity, Fragment fragment) {
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
                permissions.add(Manifest.permission.CAMERA);
            }

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

    public static void showRationalDialog(Context context, Fragment fragment) {
        final AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setTitle("You must allow these permissions to use the Attachments feature!")
                .setPositiveButton(android.R.string.ok, null)
                .create();

        alertDialog.setOnShowListener((DialogInterface dialog) -> {

            Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener((View view) -> {
                permissionCheck((Activity) context, fragment);
                alertDialog.dismiss();
            });
        });
        alertDialog.show();
    }
}
