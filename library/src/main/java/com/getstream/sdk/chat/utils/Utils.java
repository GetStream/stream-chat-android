package com.getstream.sdk.chat.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.model.Member;
import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.response.ChannelState;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import top.defaults.drawabletoolbox.DrawableBuilder;

public class Utils {

    public static final Locale locale = new Locale("en", "US", "POSIX");
    public static final DateFormat messageDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", locale);
    public static String TAG = "Utils";
    public static List<Attachment> attachments = new ArrayList<>();

    public static String readInputStream(InputStream inputStream) {
        Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");
        return scanner.hasNext() ? scanner.next() : "";
    }

    public static Uri getUriFromBitmap(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }


    public static void circleImageLoad(ImageView view, String url) {
        Glide.with(view.getContext()).asBitmap().load(url).centerCrop().into(new BitmapImageViewTarget(view) {
            @Override
            protected void setResource(Bitmap resource) {
                RoundedBitmapDrawable circularBitmapDrawable =
                        RoundedBitmapDrawableFactory.create(view.getContext().getResources(), resource);
                circularBitmapDrawable.setCircular(true);
                view.setImageDrawable(circularBitmapDrawable);
            }
        });
    }

    public static void showMessage(Context mContext, String message) {
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    }

    public static void hideSoftKeyboard(Activity activity) {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
        }
    }

    public static int getScreenResolution(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        return height;
    }

    public static void setButtonDelayEnable(View v) {
        v.setEnabled(false);
        new Handler().postDelayed(() -> v.setEnabled(true), 1000);
    }

    public static Drawable getDrawable(boolean isRect, int strokeColor, int strokeWidth, int solidColor, int topLeftRadius, int topRightRadius ) {
        if (isRect)
            return new DrawableBuilder()
                    .rectangle()
                    .strokeColor(strokeColor)
                    .strokeWidth(strokeWidth)
                    .solidColor(solidColor)
                    .cornerRadii(0, 0, 20, 20) // the same as the two lines above
                    .build();
        else
            return new DrawableBuilder()
                    .oval()
                    .strokeColor(0)
                    .strokeWidth(0)
                    .cornerRadii(0, 0, 0, 0)
                    .solidColor(0)
                    .build();

    }


    public static List<Attachment> Search_Dir(File dir) {
        String pdfPattern = ".pdf";
        String pptPattern = ".ppt";
        String csvPattern = ".csv";
        String docPattern = ".doc";
        String docxPattern = ".docx";
        String txtPattern = ".txt";
        String xlsxPattern = ".xlsx";
        String zipPattern = ".zip";
        String tarPattern = ".tar";
        String movPattern = ".mov";
        String mp3Pattern = ".mp3";

        File[] FileList = dir.listFiles();
        if (FileList != null) {
            for (File file : FileList) {
                if (file.isDirectory()) {
                    Search_Dir(file);
                } else {
                    Attachment attachment = new Attachment();

                    if (file.getName().endsWith(pdfPattern)) {
                        attachment.setMime_type(ModelType.attach_mime_pdf);
                    } else if (file.getName().endsWith(pptPattern)) {
                        attachment.setMime_type(ModelType.attach_mime_ppt);
                    } else if (file.getName().endsWith(csvPattern)) {
                        attachment.setMime_type(ModelType.attach_mime_csv);
                    } else if (file.getName().endsWith(xlsxPattern)) {
                        attachment.setMime_type(ModelType.attach_mime_xlsx);
                    } else if (file.getName().endsWith(docPattern)) {
                        attachment.setMime_type(ModelType.attach_mime_doc);
                    } else if (file.getName().endsWith(docxPattern)) {
                        attachment.setMime_type(ModelType.attach_mime_docx);
                    } else if (file.getName().endsWith(txtPattern)) {
                        attachment.setMime_type(ModelType.attach_mime_txt);
                    } else if (file.getName().endsWith(zipPattern)) {
                        attachment.setMime_type(ModelType.attach_mime_zip);
                    } else if (file.getName().endsWith(tarPattern)) {
                        attachment.setMime_type(ModelType.attach_mime_tar);
                    } else if (file.getName().endsWith(movPattern)) {
                        attachment.setMime_type(ModelType.attach_mime_mov);
                    } else if (file.getName().endsWith(mp3Pattern)) {
                        attachment.setMime_type(ModelType.attach_mime_mp3);
                    }

                    if (attachment.getMime_type() != null) {
                        attachment.setType(ModelType.attach_file);
                        attachment.setTitle(file.getName());
                        attachment.config.setFilePath(file.getPath());
                        long size = file.length();
                        attachment.setFile_size((int) size);
                        attachments.add(attachment);
                    }
                }
            }
        }
        return attachments;
    }

    public static ArrayList<Attachment> getAllShownImagesPath(Context context) {
        String[] columns = {MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.DATE_ADDED,
                MediaStore.Files.FileColumns.MEDIA_TYPE,
                MediaStore.Files.FileColumns.MIME_TYPE,
                MediaStore.Files.FileColumns.TITLE,
                MediaStore.Video.Media.RESOLUTION,
                MediaStore.Video.VideoColumns.DURATION,
        };

        String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                + " OR "
                + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

        Uri queryUri = MediaStore.Files.getContentUri("external");

        @SuppressWarnings("deprecation")
        ContentResolver mContentResolver = context.getContentResolver();

        Cursor imagecursor = mContentResolver.query(queryUri,
                columns,
                selection,
                null, // Selection args (none).
                MediaStore.Files.FileColumns.DATE_ADDED + " DESC" // QuerySort order.
        );

        int image_column_index = imagecursor.getColumnIndex(MediaStore.Files.FileColumns._ID);
        int count = imagecursor.getCount();

        ArrayList<Attachment> attachments = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            Attachment attachment = new Attachment();
            imagecursor.moveToPosition(i);
            int id = imagecursor.getInt(image_column_index);
            int dataColumnIndex = imagecursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
            int type = imagecursor.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE);
            int t = imagecursor.getInt(type);
            attachment.config.setFilePath(imagecursor.getString(dataColumnIndex));

            if (t == Constant.MEDIA_TYPE_IMAGE) {
                attachment.setType(ModelType.attach_image);
            } else if (t == Constant.MEDIA_TYPE_VIDEO) {
                float videolengh = imagecursor.getLong(imagecursor.getColumnIndex(MediaStore.Video.VideoColumns.DURATION));
                attachment.setType(ModelType.attach_file);
                attachment.setMime_type(ModelType.attach_mime_mp4);
                attachment.config.setVideoLengh((int) (videolengh / 1000));
                attachments.add(attachment);
            }
            attachments.add(attachment);
        }

        return attachments;
    }

    public static List<String> getMentionedUserIDs(ChannelState response, String text) {
        if (TextUtils.isEmpty(text)) return null;

        List<String> mentionedUserIDs = new ArrayList<>();
        if (response.getMembers() != null && !response.getMembers().isEmpty()) {
            for (Member member : response.getMembers()) {
                String userName = member.getUser().getName();
                if (text.contains("@" + userName)) {
                    mentionedUserIDs.add(member.getUser().getId());
                }
            }
        }
        return mentionedUserIDs;
    }

    public static String getMentionedText(Message message) {
        if (message == null) return null;
        String text = message.getText();
        if (message.getMentionedUsers() != null && !message.getMentionedUsers().isEmpty()) {
            for (User mentionedUser : message.getMentionedUsers()) {
                String userName = mentionedUser.getName();
                text = text.replace("@" + userName, "**" + "@" + userName + "**");
            }
        }
        return text;
    }
}
