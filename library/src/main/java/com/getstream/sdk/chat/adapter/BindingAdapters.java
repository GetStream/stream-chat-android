package com.getstream.sdk.chat.adapter;

import androidx.databinding.BindingAdapter;

import android.graphics.Bitmap;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

public class BindingAdapters {

    @BindingAdapter({"imageResourceId"})
    public static <T> void setImage(ImageView imageView, int resourceId) {
        imageView.setImageResource(resourceId);
    }
    @BindingAdapter({"backGroundResourceId"})
    public static <T> void setBackground(View view, int resourceId) {
        view.setBackgroundResource(resourceId);
    }
    @BindingAdapter("imageFromUrl")
    public static <T> void bindImageFromUrl(ImageView imageView, String imageUrl) {
        if (!TextUtils.isEmpty(imageUrl) && imageView.getVisibility() == View.VISIBLE) {
            String badImage = "https://getstream.io/random_svg/";
            if (imageUrl.contains(badImage)){
                imageView.setVisibility(View.INVISIBLE);
                return;
            }
            Glide.with(imageView.getContext())
                    .load(imageUrl)
                    .into(imageView);
        }
    }
    @BindingAdapter("circleImageFromUrl")
    public static <T> void bindCircleImageFromUrl(ImageView imageView, String imageUrl) {
        if (!TextUtils.isEmpty(imageUrl) && imageView.getVisibility() == View.VISIBLE) {
            String badImage = "https://getstream.io/random_svg/";
            if (imageUrl.contains(badImage)){
                imageView.setVisibility(View.INVISIBLE);
                return;
            }
            Glide.with(imageView.getContext()).load(imageUrl).asBitmap().centerCrop().into(new BitmapImageViewTarget(imageView) {
                @Override
                protected void setResource(Bitmap resource) {
                    RoundedBitmapDrawable circularBitmapDrawable =
                            RoundedBitmapDrawableFactory.create(view.getContext().getResources(), resource);
                    circularBitmapDrawable.setCircular(true);
                    view.setImageDrawable(circularBitmapDrawable);
                }
            });
        }
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
