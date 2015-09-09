package com.smart.travel.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ScrollView;

/**
 * Created by Tindle Wei.
 */
public class ElasticScrollView extends ScrollView {

    private static final String TAG = "ElasticScrollView";

    /**
     * 手指抖动误差
     */
    private static final int SHAKE_MOVE_VALUE = 8;
    /**
     * Scrollview内部的view
     */
    private View innerView;
    /**
     * 记录innerView最初的Y位置
     */
    private float startY;
    /**
     * 记录原始innerView的大小位置
     */
    private Rect outRect = new Rect();

    private boolean animationFinish = true;

    public ElasticScrollView(Context context) {
        super(context);
    }

    public ElasticScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * 继承自View
     * 在xml的所有布局加载完之后执行
     */
    @Override
    protected void onFinishInflate() {
        if (getChildCount() > 0) {
            innerView = getChildAt(0);
        }
        Log.d(TAG, innerView.getClass().getName());
        super.onFinishInflate();
    }

    /**
     * 继承自ViewGroup
     * 返回true, 截取触摸事件
     * 返回false, 将事件传递给onTouchEvent()和子控件的dispatchTouchEvent()
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        // 判断 点击子控件 or 按住子控件滑动
        // 如果点击子控件，则返回 false, 子控件响应点击事件
        // 如果按住子控件滑动，则返回 true, 滑动布局
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                startY = ev.getY();
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                float currentY = ev.getY();
                float scrollY = currentY - startY;

                // 是否返回 true
                return Math.abs(scrollY) > SHAKE_MOVE_VALUE;
            }
        }
        // 默认返回 false
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (innerView == null) {
            return super.onTouchEvent(ev);
        } else {
            myTouchEvent(ev);
        }

        if(ev.getAction() == MotionEvent.ACTION_MOVE){
            return false;
        }
        return super.onTouchEvent(ev);
    }

    public void myTouchEvent(MotionEvent ev) {
        if (animationFinish) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startY = ev.getY();
                    super.onTouchEvent(ev);
                    break;

                case MotionEvent.ACTION_UP:
                    startY = 0;
                    if (isNeedAnimation()) {
                        animation();
                    }
                    super.onTouchEvent(ev);
                    break;

                case MotionEvent.ACTION_MOVE:
                    final float preY =
                            startY == 0 ? ev.getY() : startY;
                    float nowY = ev.getY();
                    int deltaY = (int) (preY - nowY);
                    startY = nowY;

                    // 当滚动到最上或者最下时就不会再滚动，这时移动布局
                    if (isNeedMove()) {
                        if (outRect.isEmpty()) {
                            // 保存正常的布局位置
                            outRect.set(innerView
                                            .getLeft(), innerView.getTop(),
                                    innerView.getRight(),
                                    innerView.getBottom());
                        }
                        // 移动布局
                        // 这里 deltaY/2 为了操作体验更好
                        innerView.layout(innerView.getLeft(),
                                innerView.getTop() - deltaY / 2,
                                innerView.getRight(),
                                innerView.getBottom() - deltaY / 2);
                    } else {
                        super.onTouchEvent(ev);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 开启移动动画
     */
    public void animation() {
        TranslateAnimation ta = new TranslateAnimation(0, 0, 0, outRect.top - innerView.getTop());
        ta.setDuration(400);
        // 减速变化 为了用户体验更好
        ta.setInterpolator(new DecelerateInterpolator());
        ta.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                animationFinish = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                innerView.clearAnimation();
                // 设置innerView回到正常的布局位置
                innerView.layout(outRect.left,
                        outRect.top, outRect.right, outRect.bottom);
                outRect.setEmpty();
                animationFinish = true;
            }
        });
        innerView.startAnimation(ta);
    }

    /**
     * 是否需要开启动画
     */
    public boolean isNeedAnimation() {
        return !outRect.isEmpty();
    }

    /**
     * 是否需要移动布局
     */
    public boolean isNeedMove() {
        int offset = innerView.getMeasuredHeight() - getHeight();
        offset = (offset < 0) ? 0 : offset;
        int scrollY = getScrollY();
        return (offset == 0 || scrollY == offset);
    }
}