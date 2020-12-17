package com.getstream.sdk.chat.utils.frescoimageviewer.drawee;

import android.view.ViewParent;

import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.view.DraweeView;

import me.relex.photodraweeview.Attacher;
import me.relex.photodraweeview.OnScaleChangeListener;

class NonInterceptableAttacher extends Attacher {

    private OnScaleChangeListener scaleChangeListener;

    public NonInterceptableAttacher(DraweeView<GenericDraweeHierarchy> draweeView) {
        super(draweeView);
    }

    @Override
    public void onDrag(float dx, float dy) {
        DraweeView<GenericDraweeHierarchy> draweeView = getDraweeView();
        if (draweeView != null) {
            getDrawMatrix().postTranslate(dx, dy);
            checkMatrixAndInvalidate();

            ViewParent parent = draweeView.getParent();
            if (parent == null) {
                return;
            }

            if (getScale() == 1.0f) {
                parent.requestDisallowInterceptTouchEvent(false);
            } else {
                parent.requestDisallowInterceptTouchEvent(true);
            }
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override
    public void setOnScaleChangeListener(OnScaleChangeListener listener) {
        this.scaleChangeListener = listener;
    }

    @Override
    public void onScale(float scaleFactor, float focusX, float focusY) {
        super.onScale(scaleFactor, focusX, focusY);
        if (scaleChangeListener != null) {
            scaleChangeListener.onScaleChange(scaleFactor, focusX, focusY);
        }
    }
}