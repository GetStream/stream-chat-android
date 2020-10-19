package com.getstream.sdk.chat.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.EditText;

import androidx.core.view.inputmethod.EditorInfoCompat;
import androidx.core.view.inputmethod.InputConnectionCompat;
import com.getstream.sdk.chat.model.ModelType;

@SuppressLint("AppCompatCustomView")
public class GifEditText extends EditText {
    private InputConnectionCompat.OnCommitContentListener callback;

    public GifEditText(Context context) {
        super(context);
    }

    public GifEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GifEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo editorInfo) {
        final InputConnection ic = super.onCreateInputConnection(editorInfo);
        EditorInfoCompat.setContentMimeTypes(editorInfo,
                new String[]{ModelType.attach_mime_gif});
        if (callback != null)
            return InputConnectionCompat.createWrapper(ic, editorInfo, callback);
        else return null;
    }

    public void setCallback(InputConnectionCompat.OnCommitContentListener callback) {
        this.callback = callback;
    }
}
