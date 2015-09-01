package com.smart.travel;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.ViewFlipper;

public class ScrollViewFlipper extends ViewFlipper implements Animation.AnimationListener{

    private GestureDetector gestureDetector;
    private ViewGroup indicator;
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            showNext();
            startFlipping();
        }
    };

    public ScrollViewFlipper(Context context) {
        super(context);
    }

    public ScrollViewFlipper(Context context, AttributeSet attrs) {
        super(context, attrs);
        gestureDetector = new GestureDetector(context, new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                stopFlipping();
                removeCallbacks(runnable);
                return true;
            }

            @Override
            public void onShowPress(MotionEvent e) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {

            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1.getX() - e2.getX() > 120) {
                    // right-->left
                    setInAnimation(getContext(), R.anim.flipper_left_in);
                    setOutAnimation(getContext(), R.anim.flipper_left_out);
                    getOutAnimation().setAnimationListener(ScrollViewFlipper.this);
                    showNext();

                    resetFlipper();
                    return true;
                } else if (e1.getX() - e2.getX() < -120) {
                    // left-->right
                    setInAnimation(getContext(), R.anim.flipper_right_in);
                    setOutAnimation(getContext(), R.anim.flipper_right_out);
                    getOutAnimation().setAnimationListener(ScrollViewFlipper.this);
                    showPrevious();

                    resetFlipper();
                    return true;
                }

                return false;
            }
        });

        getOutAnimation().setAnimationListener(this);
    }

    private void resetFlipper() {
        setInAnimation(getContext(), R.anim.flipper_left_in);
        setOutAnimation(getContext(), R.anim.flipper_left_out);
        getOutAnimation().setAnimationListener(ScrollViewFlipper.this);

        postDelayed(runnable, 3000);
    }

    public void updateIndicator() {
        indicator.removeAllViews();
        if (getChildCount() < 2) {
            return;
        }
        for (int i = 0; i < getChildCount(); i++) {
            ImageView imageView = new ImageView(getContext());
            if (i == getDisplayedChild()) {
                imageView.setImageResource(R.drawable.circled_blue_26);
            } else {
                imageView.setImageResource(R.drawable.circled_26);
            }
            indicator.addView(imageView);
        }
    }

    public void setIndicator(ViewGroup viewGroup) {
        indicator = viewGroup;
        updateIndicator();
    }

    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {
        updateIndicator();
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }
}
