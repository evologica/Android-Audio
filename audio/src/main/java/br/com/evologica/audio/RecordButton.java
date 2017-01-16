package br.com.evologica.audio;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.media.MediaRecorder;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class RecordButton extends FloatingActionButton implements View.OnTouchListener, OnRecordListener {
    private static final String TAG = RecordButton.class.getSimpleName();
    private static final int VIBRATE_START = 1;
    private static final int VIBRATE_STOP = 2;
    private static final int VIBRATE_DELETE = 4;
    private static final int RTL = 0;
    private static final int LTR = 1;

    private int mScaleDuration = 200;
    private int mSwipeDirection = RTL;
    private float mGrow = 2;
    private float mShrink = 1;
    private int mSwipeDuration = 200;
    private int mSwipeDistance = -1;
    private int mVibrateDuration = 100;
    private int mVibrate = VIBRATE_START + VIBRATE_STOP + VIBRATE_DELETE;
    private OnRecordListener onRecordListener;
    private OnTouchListener onTouchListener;
    private MediaRecorder mMediaRecorder;
    private boolean enabled = true;

    public RecordButton(Context context) {
        super(context);
        init(context, null);
    }

    public RecordButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public RecordButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs );
    }

    private void init(Context context, AttributeSet attributeSet) {
        super.setOnTouchListener(this);
        if (attributeSet == null) return;
        TypedArray attributes = context.getTheme().obtainStyledAttributes(attributeSet, R.styleable.RecordButton, 0, 0);
        try {
            mScaleDuration = attributes.getInt(R.styleable.RecordButton_scaleDuration, 200);
            mSwipeDuration = attributes.getInt(R.styleable.RecordButton_swipeDuration, 200);
            mGrow = attributes.getFloat(R.styleable.RecordButton_grow, 2);
            mShrink = attributes.getFloat(R.styleable.RecordButton_shrink, 1);
            mSwipeDistance = attributes.getDimensionPixelSize(R.styleable.RecordButton_swipeDistance, -1);
            mVibrate = attributes.getInteger(R.styleable.RecordButton_vibrate, VIBRATE_START + VIBRATE_STOP + VIBRATE_DELETE);
            mVibrateDuration = attributes.getInt(R.styleable.RecordButton_vibrateDuration, 100);
            mSwipeDirection = attributes.getInt(R.styleable.RecordButton_swipeDirection, RTL);
        } finally {
            attributes.recycle();
        }
    }

    private boolean containsFlag(int flags, int flag){
        return (flags|flag) == flags;
    }

    @Override
    public boolean onTouch(final View view, MotionEvent motionEvent) {
        final int width = RecordButton.this.getWidth();
        switch (motionEvent.getActionMasked()){
            case MotionEvent.ACTION_MOVE: {
                final float position = RecordButton.this.getTranslationX();
                final float translation = position + motionEvent.getX() - width/2;
                final float relativePosition = mSwipeDirection == RTL ? position : -position;
                if (relativePosition > getSwipeDistance() && enabled) {
                    ObjectAnimator shrinkX = ObjectAnimator.ofFloat(RecordButton.this, "scaleX", mShrink);
                    ObjectAnimator shrinkY = ObjectAnimator.ofFloat(RecordButton.this, "scaleY", mShrink);
                    ObjectAnimator translateToOrigin = ObjectAnimator.ofFloat(RecordButton.this, "translationX", 0);
                    translateToOrigin.setDuration(mSwipeDuration);
                    shrinkX.setDuration(mScaleDuration);
                    shrinkY.setDuration(mScaleDuration);
                    AnimatorSet shrink = new AnimatorSet();
                    shrink.play(shrinkX).with(shrinkY).with(translateToOrigin);
                    shrink.addListener(new Animator.AnimatorListener() {
                        @Override public void onAnimationStart(Animator animator) {
                            enabled = false;
                            onDelete(view);
                        }
                        @Override public void onAnimationEnd(Animator animator) {}
                        @Override public void onAnimationCancel(Animator animator) {}
                        @Override public void onAnimationRepeat(Animator animator) {}
                    });
                    shrink.start();
                }
                else if (enabled) {
                    if (mSwipeDirection == RTL && translation >= 0) RecordButton.this.setTranslationX(translation);
                    else if (mSwipeDirection == LTR && translation <= 0) RecordButton.this.setTranslationX(translation);
                }
                break;
            }
            case MotionEvent.ACTION_DOWN:{
                ObjectAnimator growX = ObjectAnimator.ofFloat(RecordButton.this, "scaleX", mGrow);
                ObjectAnimator growY = ObjectAnimator.ofFloat(RecordButton.this, "scaleY", mGrow);
                growX.setDuration(mScaleDuration);
                growY.setDuration(mScaleDuration);
                AnimatorSet grow = new AnimatorSet();
                grow.play(growX).with(growY);
                grow.addListener(new Animator.AnimatorListener() {
                    @Override public void onAnimationStart(Animator animator) {}
                    @Override public void onAnimationEnd(Animator animator) {
                        onStart(view, null);
                    }
                    @Override public void onAnimationCancel(Animator animator) {}
                    @Override public void onAnimationRepeat(Animator animator) {}
                });
                grow.start();
                break;
            }
            case MotionEvent.ACTION_UP: {
                ObjectAnimator shrinkX = ObjectAnimator.ofFloat(RecordButton.this, "scaleX", mShrink);
                ObjectAnimator shrinkY = ObjectAnimator.ofFloat(RecordButton.this, "scaleY", mShrink);
                ObjectAnimator translateToOrigin = ObjectAnimator.ofFloat(RecordButton.this, "translationX", 0);
                translateToOrigin.setDuration(mSwipeDuration);
                shrinkX.setDuration(mScaleDuration);
                shrinkY.setDuration(mScaleDuration);
                AnimatorSet shrink = new AnimatorSet();
                shrink.play(shrinkX).with(shrinkY).with(translateToOrigin);
                shrink.addListener(new Animator.AnimatorListener() {
                    @Override public void onAnimationStart(Animator animator) {}
                    @Override public void onAnimationEnd(Animator animator) {
                        onStop(view);
                        enabled = true;
                    }
                    @Override public void onAnimationCancel(Animator animator) {}
                    @Override public void onAnimationRepeat(Animator animator) {}
                });
                shrink.start();
                break;
            }
        }
        return (onTouchListener != null && onTouchListener.onTouch(view, motionEvent)) || !hasOnClickListeners();
    }

    @Override public void onStart(View view, MediaRecorder trash) {
        if (containsFlag(mVibrate, VIBRATE_START)) ((Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE)).vibrate(mVibrateDuration);
        if (onRecordListener != null) {
            mMediaRecorder = new MediaRecorder();
            onRecordListener.onStart(view, mMediaRecorder);
            try {
                mMediaRecorder.prepare();
                mMediaRecorder.start();
            } catch (Exception e) {
                onError(e);
            }
        }
    }

    @Override
    public void onStop(View view) {
        if (enabled){
            if (containsFlag(mVibrate, VIBRATE_STOP)) ((Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE)).vibrate(mVibrateDuration);
            if (onRecordListener != null && mMediaRecorder != null) {
                try {
                    mMediaRecorder.stop();
                    mMediaRecorder.release();
                    onRecordListener.onStop(view);
                }
                catch (Exception e) {
                    onError(e);
                }
            }
        }
    }

    @Override
    public void onDelete(View view) {
        if (containsFlag(mVibrate, VIBRATE_DELETE)) ((Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE)).vibrate(mVibrateDuration);
        if (onRecordListener != null && mMediaRecorder != null) {
            try {
                mMediaRecorder.stop();
                mMediaRecorder.release();
                onRecordListener.onDelete(view);
            }
            catch (Exception e) {
                onError(e);
            }
        }
        if (mMediaRecorder != null) {
            mMediaRecorder = null;
        }
    }

    @Override public void onError(Exception e) {
        if (onRecordListener != null) onRecordListener.onError(e);
    }

    @Override
    public void setOnTouchListener(OnTouchListener onTouchListener) {
        super.setOnTouchListener(this);
        this.onTouchListener = onTouchListener;
    }

    public  OnRecordListener getOnRecordListener(){
        return onRecordListener;
    }

    public void setOnRecordListener(OnRecordListener listener){
        this.onRecordListener = listener;
    }

    public int getScaleDuration() {
        return mScaleDuration;
    }

    public void setScaleDuration(int mScaleDuration) {
        this.mScaleDuration = mScaleDuration;
    }

    public float getGrow() {
        return mGrow;
    }

    public void setGrow(int mGrow) {
        this.mGrow = mGrow;
    }

    public float getShrink() {
        return mShrink;
    }

    public void setShrink(int mShrink) {
        this.mShrink = mShrink;
    }

    public int getSwipeDuration() {
        return mSwipeDuration;
    }

    public void setSwipeDuration(int mSwipeDuration) {
        this.mSwipeDuration = mSwipeDuration;
    }

    public int getSwipeDistance() {
        if (mSwipeDistance < 0) return  this.getWidth()*5;
        return mSwipeDistance;
    }

    public void setSwipeDistance(int mSwipeDistance) {
        this.mSwipeDistance = mSwipeDistance;
    }

    public MediaRecorder getMediaRecorder() {
        return mMediaRecorder;
    }

    public void setMediaRecorder(MediaRecorder mMediaRecorder) {
        this.mMediaRecorder = mMediaRecorder;
    }
}
