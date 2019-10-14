package com.getstream.sdk.chat.adapter;

import android.view.View;
import android.widget.ImageView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.BindingAdapter;

public class BindingAdapters {

    @BindingAdapter({"imageResourceId"})
    public static <T> void setImage(ImageView imageView, int resourceId) {
        imageView.setImageResource(resourceId);
    }

    @BindingAdapter({"backGroundResourceId"})
    public static <T> void setBackground(View view, int resourceId) {
        view.setBackgroundResource(resourceId);
    }


    @BindingAdapter("setHorizontalBias")
    public static <T> void setHorizontalBias(View view, float bias) {
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) view.getLayoutParams();
        params.horizontalBias = bias;
        view.setLayoutParams(params);
    }

    @BindingAdapter("isGone")
    public static <T> void bindIsGone(View view, Boolean isGone) {
        if (isGone) {
            view.setVisibility(View.GONE);
        } else {
            view.setVisibility(View.VISIBLE);
        }
    }

    @BindingAdapter("isHide")
    public static <T> void bindIsHide(View view, Boolean isHide) {
        if (isHide) {
            view.setVisibility(View.INVISIBLE);
        } else {
            view.setVisibility(View.VISIBLE);
        }
    }
}
