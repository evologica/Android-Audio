package br.com.evologica.audio.sample;

import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import br.com.evologica.audio.AudioPlayer;
import br.com.evologica.audio.OnRecordListener;
import br.com.evologica.audio.RecordButton;

public class MainActivity extends AppCompatActivity {
    private String mFileName;
    private List<AudioPlayer> audioPlayers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName += "/audiorecordtest.3gp";
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        RecordButton fab = (RecordButton) findViewById(R.id.fab);
        fab.setOnRecordListener(new OnRecordListener() {
            @Override
            public void onStart(View view, MediaRecorder recorder) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
                recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                recorder.setOutputFile(mFileName);
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            }
            @Override public void onStop(View view) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                final AudioPlayer audioPlayer = new AudioPlayer(MainActivity.this);
                audioPlayers.add(audioPlayer);
                MediaPlayer mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(mFileName);
                    audioPlayer.setMediaPlayer(mediaPlayer);
                    ((LinearLayout)findViewById(R.id.audio_players)).addView(audioPlayer);
                } catch (IOException e ) {
                    e.printStackTrace();
                }
            }
            @Override public void onDelete(View view) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            }
            @Override public void onError(Exception e) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                e.printStackTrace();
            }
        });
    }

}
