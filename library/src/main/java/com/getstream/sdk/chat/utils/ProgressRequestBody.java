package com.getstream.sdk.chat.utils;

import android.os.Handler;
import android.os.Looper;

import com.getstream.sdk.chat.rest.interfaces.UploadFileCallback;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

public class ProgressRequestBody extends RequestBody {

    private File mFile;
    private UploadFileCallback mListener;
    private String content_type;

    private static final int DEFAULT_BUFFER_SIZE = 2048;

    public ProgressRequestBody(final File file, String content_type, final UploadFileCallback listener) {
        this.content_type = content_type;
        mFile = file;
        mListener = listener;
    }


    @Nullable
    @Override
    public MediaType contentType() {
        return MediaType.parse(content_type);
    }

    @Override
    public long contentLength() {
        return mFile.length();
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        long fileLength = mFile.length();
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        FileInputStream in = new FileInputStream(mFile);
        long uploaded = 0;

        try {
            int read;
            Handler handler = new Handler(Looper.getMainLooper());
            while ((read = in.read(buffer)) != -1) {

                // update progress on UI thread
                handler.post(new ProgressUpdater(uploaded, fileLength));

                uploaded += read;
                sink.write(buffer, 0, read);
            }
        } finally {
            in.close();
        }
    }

    private class ProgressUpdater implements Runnable {
        private long mUploaded;
        private long mTotal;

        public ProgressUpdater(long uploaded, long total) {
            mUploaded = uploaded;
            mTotal = total;
        }

        @Override
        public void run() {
            mListener.onProgress((int) (100 * mUploaded / mTotal));
        }
    }
}
