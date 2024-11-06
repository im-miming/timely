package com.cs046_project.timely.listener;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public abstract class SwipeGestureListener implements View.OnTouchListener {

    private GestureDetector gestureDetector;

    public SwipeGestureListener(Context context) {
        gestureDetector = new GestureDetector(context, new GestureListener());
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    public abstract void onSwipeUp();

    public abstract void onSwipeDown();

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float deltaY = e2.getY() - e1.getY();
            float deltaX = e2.getX() - e1.getX();
            if (Math.abs(deltaX) < Math.abs(deltaY)) {
                if (Math.abs(deltaY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (deltaY < 0) {
                        onSwipeUp();
                    } else {
                        onSwipeDown();
                    }
                    return true;
                }
            }
            return false;
        }
    }
}