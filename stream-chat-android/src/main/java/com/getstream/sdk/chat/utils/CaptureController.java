package com.getstream.sdk.chat.utils;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import com.getstream.sdk.chat.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.annotation.Nullable;

public class CaptureController {

    private static Uri imageUri, videoUri;

    public static Intent getTakePictureIntent(Context context) {
        File newFile = new File(getFolderPath(true, context), "IMG_" + getFileName() + ".jpg");
        imageUri = Uri.fromFile(newFile);
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        return takePictureIntent;
    }

    public static Intent getTakeVideoIntent(Context context) {
        File newFile = new File(getFolderPath(false, context), "VID_" + getFileName() + ".mp4");
        videoUri = Uri.fromFile(newFile);
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        ContentValues values = new ContentValues();
        values.put(MediaStore.Video.Media.TITLE, "New Video");
        values.put(MediaStore.Video.Media.DESCRIPTION, "From your Camera");
        takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri);
        return takeVideoIntent;
    }

    @SuppressLint("SimpleDateFormat")
    private static String getFileName() {
        return new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date().getTime());
    }

    private static String getFolderPath(boolean isImage, Context context) {
        String appName = Utils.getApplicationName(context);
        File f1 = new File(Environment.getExternalStorageDirectory() + "/" + appName, context.getString(isImage ? R.string.stream_image : R.string.stream_video));
        if (!f1.exists()) {
            f1.mkdirs();
        }
        return f1.getAbsolutePath();
    }

    @Nullable
    public static File getCaptureFile(boolean isImage) {
        return getFileFromUri(isImage ? imageUri : videoUri);
    }

    @Nullable
    public static File getFileFromUri(Uri uri) {
        String path = uri.getPath();
        if (path == null)
            return null;
        else
            return new File(path);
    }
}
