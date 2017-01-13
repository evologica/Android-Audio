package br.com.evologica.audio;

import android.view.View;


public interface OnPlayListener {
    void onStart(View view);
    void onPause(View view);
    void onEnd(View view);
    void onRemove(View view);
    void onError(Exception e);
}
