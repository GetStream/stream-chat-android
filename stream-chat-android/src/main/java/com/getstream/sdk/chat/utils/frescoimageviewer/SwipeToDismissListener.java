package com.getstream.sdk.chat.utils.frescoimageviewer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

class SwipeToDismissListener implements View.OnTouchListener {

    private static final String PROPERTY_TRANSLATION_X = "translationY";

    private final View swipeView;
    private int translationLimit;
    private OnDismissListener dismissListener;
    private OnViewMoveListener moveListener;
    private boolean tracking = false;
    private float startY;
    public SwipeToDismissListener(View swipeView, OnDismissListener dismissListener,
                                  OnViewMoveListener moveListener) {
        this.swipeView = swipeView;
        this.dismissListener = dismissListener;
        this.moveListener = moveListener;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        translationLimit = v.getHeight() / 4;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Rect hitRect = new Rect();
                swipeView.getHitRect(hitRect);
                if (hitRect.contains((int) event.getX(), (int) event.getY())) {
                    tracking = true;
                }
                startY = event.getY();
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (tracking) {
                    tracking = false;
                    animateSwipeView(v.getHeight());
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                if (tracking) {
                    float translationY = event.getY() - startY;
                    swipeView.setTranslationY(translationY);
                    callMoveListener(translationY, translationLimit);
                }
                return true;
        }
        return false;
    }

    private void animateSwipeView(int parentHeight) {
        float currentPosition = swipeView.getTranslationY();
        float animateTo = 0.0f;

        if (currentPosition < -translationLimit) {
            animateTo = -parentHeight;
        } else if (currentPosition > translationLimit) {
            animateTo = parentHeight;
        }

        final boolean isDismissed = animateTo != 0.0f;
        ObjectAnimator animator = ObjectAnimator.ofFloat(
                swipeView, PROPERTY_TRANSLATION_X, currentPosition, animateTo);

        animator.setDuration(200);
        animator.setInterpolator(new AccelerateInterpolator());
        animator.addListener(
                new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        if (isDismissed) callDismissListener();
                    }
                });
        animator.addUpdateListener(
                new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        callMoveListener((float) animation.getAnimatedValue(), translationLimit);
                    }
                });
        animator.start();
    }

    private void callDismissListener() {
        if (dismissListener != null) {
            dismissListener.onDismiss();
        }
    }

    private void callMoveListener(float translationY, int translationLimit) {
        if (moveListener != null) {
            moveListener.onViewMove(translationY, translationLimit);
        }
    }

    interface OnViewMoveListener {
        void onViewMove(float translationY, int translationLimit);
    }
}