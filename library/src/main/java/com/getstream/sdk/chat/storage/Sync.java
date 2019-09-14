package com.getstream.sdk.chat.storage;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class Sync {

    public static final int LOCAL_ONLY = 0;
    public static final int LOCAL_UPDATE_PENDING = 1;
    public static final int SYNCED = 2;

    public Sync(@Status int status) {
        System.out.println("status :" + status);
    }

    @IntDef({LOCAL_ONLY, LOCAL_UPDATE_PENDING, SYNCED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Status {
    }
}