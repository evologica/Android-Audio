package br.com.evologica.audio;

import android.content.Context;
import android.content.res.ColorStateList;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import java.io.IOException;

public class AudioPlayer extends LinearLayoutCompat implements SeekBar.OnSeekBarChangeListener, View.OnClickListener, OnPlayListener{
    private static final String TAG = AudioPlayer.class.getSimpleName();
    private AppCompatSeekBar mSeekBar;
    private AppCompatImageButton mPlayPauseButton;
    private AppCompatImageButton mCancelButton;
    private CardView mCardView;
    private SeekBar.OnSeekBarChangeListener onSeekBarChangeListener;
    private OnClickListener onClickListener;
    private OnPlayListener onPlayListener;

    private int mPauseIcon = R.drawable.ic_pause;
    private int mPlayIcon = R.drawable.ic_play;

    private MediaPlayer mMediaPlayer;
    private int mDuration = -1;
    private int mPlayPosition = -1;

    private boolean isPlaying = false;

    // Initialization
    public AudioPlayer(Context context) {
        super(context);
        init(context);
    }
    public AudioPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    public AudioPlayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }
    private void init(Context context){
        LayoutInflater.from(context).inflate(R.layout.audio_player_view, this);
    }
    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mPlayPauseButton = (AppCompatImageButton) findViewById(R.id.audio_player_play_pause_button);
        mCancelButton = (AppCompatImageButton) findViewById(R.id.audio_player_cancel_button);
        mSeekBar = (AppCompatSeekBar) findViewById(R.id.audio_player_seek_bar);
        mCardView =  (CardView) findViewById(R.id.audio_player_card);
        mPlayPauseButton.setOnClickListener(this);
        mCancelButton.setOnClickListener(cancelClick);
        mSeekBar.setOnSeekBarChangeListener(this);
    }

    // Events
    @Override public void onStart(View view) {
        if (mMediaPlayer == null) return;
        if (onPlayListener != null) onPlayListener.onStart(view);
        try {
            mMediaPlayer.start();
            if (mPlayPosition >= 0) {
                mMediaPlayer.seekTo(mPlayPosition);
                mPlayPosition = -1;
            }
            isPlaying = true;
            new Tracker().execute();
            mPlayPauseButton.setImageResource(mPauseIcon);
        } catch (Exception e) {
            onError(e);
        }
    }
    @Override public void onPause(View view) {
        if (mMediaPlayer == null) return;
        try {
            mMediaPlayer.pause();
            if (onPlayListener != null) onPlayListener.onPause(view);
            isPlaying = false;
            mPlayPauseButton.setImageResource(mPlayIcon);
        } catch (Exception e){
            onError(e);
        }
    }
    @Override public void onEnd(View view) {
        if (mMediaPlayer == null) return;
        try {
            if (onPlayListener != null) onPlayListener.onEnd(view);
            if (isPlaying)mMediaPlayer.stop();
            isPlaying = false;
            mPlayPauseButton.setImageResource(mPlayIcon);
            mSeekBar.setProgress(0);
        } catch (Exception e){
            onError(e);
        }
    }
    @Override public void onError(Exception e) {
        if (onPlayListener != null) onPlayListener.onError(e);
        e.printStackTrace();
    }

    @Override
    public void onRemove(View view) {
        try {
            if (onPlayListener != null) onPlayListener.onRemove(view);
            if (mMediaPlayer != null) {
                if (isPlaying) mMediaPlayer.stop();
                mMediaPlayer.release();
            }
            isPlaying = false;
        } catch (Exception e){
            onError(e);
        }
    }

    @Override public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        if (onSeekBarChangeListener != null) onSeekBarChangeListener.onProgressChanged(seekBar, i, b);
    }
    @Override public void onStartTrackingTouch(SeekBar seekBar) {
        if (onSeekBarChangeListener != null) onSeekBarChangeListener.onStartTrackingTouch(seekBar);
        if (isPlaying) onPause(this);
    }
    @Override public void onStopTrackingTouch(SeekBar seekBar) {
        if (onSeekBarChangeListener != null) onSeekBarChangeListener.onStopTrackingTouch(seekBar);
        mPlayPosition = mDuration*seekBar.getProgress()/100;
//        mMediaPlayer.seekTo(mDuration*seekBar.getProgress()/100);
    }
    @Override public void onClick(View view) {
        if (isPlaying) onPause(view);
        else onStart(view);
        if (onClickListener != null) onClickListener.onClick(view);
    }
    private OnClickListener cancelClick = new OnClickListener() {
        @Override public void onClick(View view) {
            ((ViewGroup) getParent()).removeView(AudioPlayer.this);
            onRemove(AudioPlayer.this);
        }
    };

    // Setters and Getters
    public void setOnTouchListener(OnTouchListener listener){
        mPlayPauseButton.setOnTouchListener(listener);
    }
    public void setOnClickListener(OnClickListener listener){
        onClickListener = listener;
    }
    public OnClickListener getOnClickListener(){
        return onClickListener;
    }
    public void setOnSeekBarChangeListener(SeekBar.OnSeekBarChangeListener listener){
        onSeekBarChangeListener = listener;
    }
    public SeekBar.OnSeekBarChangeListener getOnSeekBarChangeListener(){
        return onSeekBarChangeListener;
    }
    public void setOnPlayListener(OnPlayListener listener){
        onPlayListener = listener;
    }
    public OnPlayListener getOnPlayListener(){
        return onPlayListener;
    }
    public MediaPlayer getMediaPlayer() {
        return mMediaPlayer;
    }
    public void setMediaPlayer(MediaPlayer mediaPlayer) {
        mMediaPlayer = mediaPlayer;
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override public void onCompletion(MediaPlayer mediaPlayer) {
                onEnd(AudioPlayer.this);
            }
        });
        try {
            mMediaPlayer.prepare();
        } catch (IOException e){
            e.printStackTrace();
        }
        mDuration = mMediaPlayer.getDuration();
    }
    public int getPauseIcon() {
        return mPauseIcon;
    }
    public void setPauseIcon(int mPauseIcon) {
        this.mPauseIcon = mPauseIcon;
    }
    public int getPlayIcon() {
        return mPlayIcon;
    }
    public void setPlayIcon(int mPlayIcon) {
        this.mPlayIcon = mPlayIcon;
    }
    public void setCardBackGroundColor(int color){
        mCardView.setCardBackgroundColor(color);
    }
    public ColorStateList getCardBackGroundColor(){
        return mCardView.getCardBackgroundColor();
    }

    // SeekBar Tracker
    private class Tracker extends AsyncTask<Void, Integer, Void>{
        @Override protected Void doInBackground(Void... aVoid) {
            while (isPlaying){
                try {
                    Thread.currentThread();
                    Thread.sleep(200);
                    if (isPlaying) publishProgress((int) (((float) mMediaPlayer.getCurrentPosition()) / ((float) mDuration) * 100));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            return null;
        }
        @Override protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            if (values[0] < 100) mSeekBar.setProgress(values[0]);
        }
    }
}
