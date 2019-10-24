package com.getstream.sdk.chat.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Layout;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.model.Member;
import com.getstream.sdk.chat.model.ModelType;
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

public class Utils {

    public static final Locale locale = new Locale("en", "US", "POSIX");
    public static final DateFormat messageDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", locale);
    private static String TAG = Utils.class.getSimpleName();
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
        Glide.with(view.getContext())
                .asBitmap()
                .load(StreamChat.getInstance(view.getContext()).getUploadStorage().signGlideUrl(url))
                .centerCrop()
                .into(new BitmapImageViewTarget(view) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(view.getContext().getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        view.setImageDrawable(circularBitmapDrawable);
                    }
                });
    }

    public static boolean isSVGImage(String url){
        return (TextUtils.isEmpty(url) || url.contains("random_svg"));
    }

    public static void showMessage(Context mContext, String message) {
        Toast toast = Toast.makeText(mContext, message, Toast.LENGTH_SHORT);
        View toastView = toast.getView();
        TextView toastMessage = toastView.findViewById(android.R.id.message);
        toastMessage.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) toastMessage.getLayoutParams();
        params.leftMargin = dpToPx(10);
        params.rightMargin = dpToPx(10);
        toastMessage.setLayoutParams(params);
        toast.show();
    }

    public static void showSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (!inputMethodManager.isAcceptingText())
            inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
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

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
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

                    if (!file.exists()) continue;

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
            File file = new File(imagecursor.getString(dataColumnIndex));
            if (!file.exists()) continue;
            attachment.config.setFilePath(imagecursor.getString(dataColumnIndex));

            if (t == Constant.MEDIA_TYPE_IMAGE) {
                attachment.setType(ModelType.attach_image);
            } else if (t == Constant.MEDIA_TYPE_VIDEO) {
                float videolengh = imagecursor.getLong(imagecursor.getColumnIndex(MediaStore.Video.VideoColumns.DURATION));
                attachment.setType(ModelType.attach_file);
                attachment.setMime_type(ModelType.attach_mime_mp4);
                attachment.config.setVideoLengh((int) (videolengh / 1000));
            }
            attachments.add(attachment);
        }

        return attachments;
    }

    public static List<String> getMentionedUserIDs(ChannelState channelState, String text) {
        if (TextUtils.isEmpty(text)) return null;

        List<String> mentionedUserIDs = new ArrayList<>();
        if (channelState.getMembers() != null && !channelState.getMembers().isEmpty()) {
            for (Member member : channelState.getMembers()) {
                String userName = member.getUser().getName();
                if (text.contains("@" + userName)) {
                    mentionedUserIDs.add(member.getUser().getId());
                }
            }
        }
        return mentionedUserIDs;
    }

    public static abstract class TextViewLinkHandler extends LinkMovementMethod {
        public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
            if (event.getAction() != MotionEvent.ACTION_UP)
                return super.onTouchEvent(widget, buffer, event);

            int x = (int) event.getX();
            int y = (int) event.getY();

            x -= widget.getTotalPaddingLeft();
            y -= widget.getTotalPaddingTop();

            x += widget.getScrollX();
            y += widget.getScrollY();

            Layout layout = widget.getLayout();
            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, x);

            URLSpan[] link = buffer.getSpans(off, off, URLSpan.class);
            if (link.length != 0) {
                onLinkClick(link[0].getURL());
            }
            return true;
        }
        abstract public void onLinkClick(String url);
    }
}
