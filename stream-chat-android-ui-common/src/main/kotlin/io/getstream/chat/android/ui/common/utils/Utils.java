package io.getstream.chat.android.ui.common.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.text.Layout;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.Locale;

public class Utils {

    public static final Locale locale = new Locale("en", "US", "POSIX");

    public static void showSoftKeyboard(@NonNull Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (!imm.isAcceptingText())
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    public static void showSoftKeyboard(@NonNull View view) {
        showSoftKeyboard(view.getContext());
    }

    public static void hideSoftKeyboard(@NonNull View view) {
        Context context = view.getContext();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static String getMimeType(File file) {
        return getMimeType(file.getPath());
    }

    public static String getMimeType(String filePath) {
        String type = "";
        String extension = MimeTypeMap.getFileExtensionFromUrl(filePath);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
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
