package com.getstream.sdk.chat.utils;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CaptureController {

    private static Uri imageUri, videoUri;

    public static Uri getImage() {
        return imageUri;
    }

    public static Uri getVideo() {
        return videoUri;
    }

    public static Intent getTakePictureIntent(Context context) {
        File newFile = createFile("IMG_" + getFileName(), ".jpg", context);
        imageUri = Uri.fromFile(newFile);
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        return takePictureIntent;
    }

    public static Intent getTakeVideoIntent(Context context) {
        File newFile = createFile("VID_" + getFileName(), ".mp4", context);
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

    private static File createFile(String fileName, String extension, Context context) {
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = null;
        try {
            image = File.createTempFile(
                    fileName,
                    extension,
                    storageDir
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
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
