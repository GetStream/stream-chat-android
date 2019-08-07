
package com.getstream.sdk.chat.utils.frescoimageviewer;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

class MultiTouchViewPager extends ViewPager {

    private boolean isDisallowIntercept, isScrolled = true;

    public MultiTouchViewPager(Context context) {
        super(context);
        setScrollStateListener();
    }

    public MultiTouchViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        setScrollStateListener();
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        isDisallowIntercept = disallowIntercept;
        super.requestDisallowInterceptTouchEvent(disallowIntercept);
    }

    @Override
    public boolean dispatchTouchEvent(@NonNull MotionEvent ev) {
        if (ev.getPointerCount() > 1 && isDisallowIntercept) {
            requestDisallowInterceptTouchEvent(false);
            boolean handled = super.dispatchTouchEvent(ev);
            requestDisallowInterceptTouchEvent(true);
            return handled;
        } else {
            return super.dispatchTouchEvent(ev);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getPointerCount() > 1) {
            return false;
        } else {
            try {
                return super.onInterceptTouchEvent(ev);
            } catch (IllegalArgumentException ex) {
                return false;
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        try {
            return super.onTouchEvent(ev);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    public boolean isScrolled() {
        return isScrolled;
    }

    private void setScrollStateListener() {
        addOnPageChangeListener(new SimpleOnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(int state) {
                isScrolled = state == SCROLL_STATE_IDLE;
            }
        });
    }
}