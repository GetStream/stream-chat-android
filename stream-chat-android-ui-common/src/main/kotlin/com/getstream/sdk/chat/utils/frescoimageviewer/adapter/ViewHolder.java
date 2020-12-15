package com.getstream.sdk.chat.utils.frescoimageviewer.adapter;

import android.os.Bundle;
import android.os.Parcelable;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

public abstract class ViewHolder {

    private static final String STATE = ViewHolder.class.getSimpleName();

    public final View itemView;

    boolean mIsAttached;

    int mPosition;

    public ViewHolder(View itemView) {
        if (itemView == null) {
            throw new IllegalArgumentException("itemView should not be null");
        }
        this.itemView = itemView;
    }

    void attach(ViewGroup parent, int position) {
        mIsAttached = true;
        mPosition = position;
        parent.addView(itemView);
    }

    void detach(ViewGroup parent) {
        parent.removeView(itemView);
        mIsAttached = false;
    }

    void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            SparseArray<Parcelable> ss = bundle.containsKey(STATE) ? bundle.getSparseParcelableArray(STATE) : null;
            if (ss != null) {
                itemView.restoreHierarchyState(ss);
            }
        }
    }

    Parcelable onSaveInstanceState() {
        SparseArray<Parcelable> state = new SparseArray<>();
        itemView.saveHierarchyState(state);
        Bundle bundle = new Bundle();
        bundle.putSparseParcelableArray(STATE, state);
        return bundle;
    }
}