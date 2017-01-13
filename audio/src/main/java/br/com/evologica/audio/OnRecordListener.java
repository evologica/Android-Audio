package br.com.evologica.audio;

import android.media.MediaRecorder;
import android.view.View;

public interface OnRecordListener {
    void onStart(View view, MediaRecorder recorder);
    void onStop(View view);
    void onDelete(View view);
    void onError(Exception e);
}
