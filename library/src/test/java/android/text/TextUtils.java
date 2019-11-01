package android.text;

/**
 * This needed because android.text.TextUtils is the part of Android SDK and not available for unit testing.
 */
public class TextUtils {
    public static boolean isEmpty(CharSequence str) {
        return str == null || str.length() == 0;
    }
}