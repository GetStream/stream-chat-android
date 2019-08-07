package com.getstream.sdk.chat.utils;

import android.content.Context;
import android.content.DialogInterface;
import androidx.appcompat.app.AlertDialog;
import android.view.View;
import android.widget.Button;

import com.getstream.sdk.chat.R;

public class ErrorChecker {
    public static void showErrorDialog(Context context, String error) {
        final AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setTitle(R.string.title_error)
                .setMessage(error)
                .setPositiveButton(android.R.string.ok, null)
                .create();

        alertDialog.setOnShowListener((DialogInterface dialog) -> {
            Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener((View view) -> {
                alertDialog.dismiss();
            });
        });
        alertDialog.show();
    }
}
