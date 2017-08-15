package com.ojogaze.video;

import android.app.Fragment;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.widget.Toast;

import com.google.vr.sdk.base.AndroidCompat;
import com.google.vr.sdk.base.GvrActivity;
import com.google.vr.sdk.base.GvrView;

import java.io.IOException;

import care.dovetail.ojo.EyeController;
import care.dovetail.ojo.EyeEvent;
import care.dovetail.ojo.Gesture;
import care.dovetail.ojo.bluetooth.EogDevice;

public class MainActivity extends GvrActivity {

    private static final String TAG = "MainActivity";

    private final String SAMPLE_VIDEO_PATH =
            "android.resource://com.ojogaze.video/raw/" + R.raw.sample360;

    private final EyeController eyeController = new EyeController(this);

    private RajawaliVideoStereoRenderer renderer;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initGvrView();
        Fragment fragment = getFragmentManager().findFragmentById(R.id.stats);
        ((Gesture.Observer) fragment).setEyeEventSource((EyeEvent.Source) eyeController.processor);
        eyeController.device.add((EogDevice.Observer) fragment);
    }

    @Override
    public void onResume() {
        super.onResume();
        eyeController.connect();
    }

    @Override
    public void onPause() {
        eyeController.disconnect();
        super.onPause();
    }

    private void initGvrView() {
        GvrView gvrView = (GvrView) findViewById(R.id.gvr_view);
        gvrView.setEGLConfigChooser(8, 8, 8, 8, 16, 8);

        // renderer = new GlesVideoStereoRenderer(this);
        createMediaPlayer();
        prepareMediaPlayer();
        renderer = new RajawaliVideoStereoRenderer(this, mediaPlayer);

        gvrView.setRenderer(renderer);
        gvrView.setTransitionViewEnabled(false);

        gvrView.enableCardboardTriggerEmulation();

        if (gvrView.setAsyncReprojectionEnabled(true)) {
            // Async reprojection decouples the app framerate from the display framerate,
            // allowing immersive interaction even at the throttled clockrates set by
            // sustained performance mode.
            AndroidCompat.setSustainedPerformanceMode(this, true);
        }

        setGvrView(gvrView);
    }

    private void createMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setLooping(true);
        try {
            mediaPlayer.setDataSource(this, Uri.parse(SAMPLE_VIDEO_PATH), null);
        } catch (IOException e) {
            Log.e(TAG, e.toString(), e);
        }
    }

    void initMediaPlayer(Surface surface) {
        mediaPlayer.setSurface(surface);
        prepareMediaPlayer();
    }

    private void prepareMediaPlayer() {
        toast("Preparing video...");
        mediaPlayer.setOnPreparedListener(
                new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        play();
                    }
                });
        mediaPlayer.setOnBufferingUpdateListener(
                new MediaPlayer.OnBufferingUpdateListener() {
                    @Override
                    public void onBufferingUpdate(MediaPlayer mp, int percent) {
                        toast("Buffering " + percent + "%");
                    }
                });
        mediaPlayer.prepareAsync();
    }

    private void toast(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void play() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }
}
