package io.getstream.chat.example.adapter;

import android.view.View;
import android.widget.ImageView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.BindingAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

public class BindingAdapters {

    @BindingAdapter({"setCircleImage"})
    public static void setCircleImage(ImageView view, String image) {
        Glide.with(view.getContext())
                .load(image)
                .apply(RequestOptions.circleCropTransform())
                .into(view);
    }

    @BindingAdapter("isGone")
    public static void bindIsGone(View view, Boolean isGone) {
        if (isGone) {
            view.setVisibility(View.GONE);
        } else {
            view.setVisibility(View.VISIBLE);
        }
    }
}
