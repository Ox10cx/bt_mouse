package com.uascent.android.pethunting.myviews;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.widget.SeekBar;

import com.uascent.android.pethunting.tools.Lg;

import java.util.Timer;
import java.util.TimerTask;


public class VerticalSeekBar extends SeekBar {
    private final static String TAG = "VerticalSeekBar";
    private boolean mIsDragging;
    private float mTouchDownY;
    private int mScaledTouchSlop;
    private boolean isInScrollingContainer = false;
    private Timer timer = null;
    private static float startProgress = 0;
    private static float progress;

    public boolean isInScrollingContainer() {
        return isInScrollingContainer;
    }

    public void setInScrollingContainer(boolean isInScrollingContainer) {
        this.isInScrollingContainer = isInScrollingContainer;
    }

    /**
     * On touch, this offset plus the scaled value from the position of the
     * touch will form the progress value. Usually 0.
     */
    float mTouchProgressOffset;

    public VerticalSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mScaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

    }

    public VerticalSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VerticalSeekBar(Context context) {
        super(context);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {

        super.onSizeChanged(h, w, oldh, oldw);

    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec,
                                          int heightMeasureSpec) {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec);
        setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        canvas.rotate(-90);
        canvas.translate(-getHeight(), 0);
        super.onDraw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (isInScrollingContainer()) {

                    mTouchDownY = event.getY();
                } else {
                    setPressed(true);

                    invalidate();
                    onStartTrackingTouch();
                    trackTouchEvent(event);
                    attemptClaimDrag();

                    onSizeChanged(getWidth(), getHeight(), 0, 0);
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (mIsDragging) {
                    trackTouchEvent(event);

                } else {
                    final float y = event.getY();
                    if (Math.abs(y - mTouchDownY) > mScaledTouchSlop) {
                        setPressed(true);

                        invalidate();
                        onStartTrackingTouch();
                        trackTouchEvent(event);
                        attemptClaimDrag();

                    }
                }
                onSizeChanged(getWidth(), getHeight(), 0, 0);
                break;

            case MotionEvent.ACTION_UP:
                if (mIsDragging) {
                    trackTouchEvent(event);
                    onStopTrackingTouch();
                    setPressed(false);

                } else {
                    // Touch up when we never crossed the touch slop threshold
                    // should
                    // be interpreted as a tap-seek to that location.
                    onStartTrackingTouch();
                    trackTouchEvent(event);
                    onStopTrackingTouch();

                }
                onSizeChanged(getWidth(), getHeight(), 0, 0);
                // ProgressBar doesn't know to repaint the thumb drawable
                // in its inactive state when the touch stops (because the
                // value has not apparently changed)
                invalidate();
                break;
        }
        return true;

    }

    private void trackTouchEvent(MotionEvent event) {
        final int height = getHeight();
        final int top = getPaddingTop();
        final int bottom = getPaddingBottom();
        final int available = height - top - bottom;
        int y = (int) event.getY();
        float scale;
//        float progress = 0;
        if (y > height - bottom) {
            scale = 0.0f;
        } else if (y < top) {
            scale = 1.0f;
        } else {
            scale = (float) (available - y + top) / (float) available;
            progress = mTouchProgressOffset;
        }

        final int max = getMax();
        progress += scale * max;
        setProgress((int) progress);
        if (progress > 70) {
            progress = 70;
        }
        //停止滑动超过1.5s
        if (timer != null) {
//            Lg.i(TAG, "progress->>" + progress + "  startProgress->>" + startProgress);
            if (Math.abs(progress - startProgress) > 5) {
                timer.cancel();
                timer = new Timer();
//                Lg.i(TAG,"pretime:"+System.currentTimeMillis());
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (onSeekBarStopTouchListener != null) {
                            onSeekBarStopTouchListener.onSeekBarStopTouch();
                            Lg.i(TAG, "dotime:" + System.currentTimeMillis());
                            startProgress = progress;
                        }
                    }
                }, 100);
            }
        }

    }

    /**
     * This is called when the user has started touching this widget.
     */
    void onStartTrackingTouch() {
        mIsDragging = true;
        timer = new Timer();
        startProgress = 0;
//        Lg.i(TAG, "onStartTrackingTouch");
    }

    /**
     * This is called when the user either releases his touch or the touch is
     * canceled.
     */
    void onStopTrackingTouch() {
        if (onSeekBarStopListener != null) {
            onSeekBarStopListener.onSeekBarStop();
        }
        mIsDragging = false;
    }

    private void attemptClaimDrag() {
        ViewParent p = getParent();
        if (p != null) {
            p.requestDisallowInterceptTouchEvent(true);
        }
    }

    @Override
    public synchronized void setProgress(int progress) {

        super.setProgress(progress);
        onSizeChanged(getWidth(), getHeight(), 0, 0);
    }

    /**
     * 松开滑块
     */
    public interface OnSeekBarStopListener {
        void onSeekBarStop();
    }

    static public void setSeekBarStopListener(OnSeekBarStopListener listener) {
        onSeekBarStopListener = listener;
    }

    private static OnSeekBarStopListener onSeekBarStopListener;

    /**
     * 滑块停止滑动，但没有松开滑动
     */
    public interface OnSeekBarStopTouchListener {
        void onSeekBarStopTouch();
    }

    static public void setSeekBarStopTouchListener(OnSeekBarStopTouchListener listener) {
        onSeekBarStopTouchListener = listener;
    }

    private static OnSeekBarStopTouchListener onSeekBarStopTouchListener;

}