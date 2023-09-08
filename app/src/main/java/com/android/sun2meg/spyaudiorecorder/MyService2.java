package com.android.sun2meg.spyaudiorecorder;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.IBinder;
import android.os.PowerManager;
//import android.support.v4.media.session.MediaSessionCompat;
//import android.support.v4.media.session.PlaybackStateCompat;
//import androidx.media.session.MediaSessionCompat;
import android.os.Vibrator;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

public class MyService2 extends Service {
    //    private static final String TAG = "MyService";
    private AudioManager mAudioManager;
    private MediaSessionCompat mMediaSession;
    private boolean mIsActive = false;
    private PowerManager.WakeLock mWakeLock;
    private static final String TAG = "MyService:WakeLock";
    Vibrator v;
    @Override
    public void onCreate() {
        super.onCreate();
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        mWakeLock.acquire();
        Toast.makeText(getApplicationContext(), " pre-volume", Toast.LENGTH_SHORT).show();
        // Create a new MediaSessionCompat object
        mMediaSession = new MediaSessionCompat(this, TAG);
        mMediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mMediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public boolean onMediaButtonEvent(Intent mediaButtonIntent) {
                String action = mediaButtonIntent.getAction();
                KeyEvent event = mediaButtonIntent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);

//                if (action != null && action.equals(Intent.ACTION_MEDIA_BUTTON) && event != null && event.getAction() == KeyEvent.ACTION_DOWN) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP) {
                    mIsActive = true;
                    Log.d(TAG, "Volume up long press detected");
                    Toast.makeText(getApplicationContext(), "volume", Toast.LENGTH_SHORT).show();
                    ToneGenerator tone = new ToneGenerator(AudioManager.STREAM_RING, 100);
                    tone.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);

                    // Do something when volume up button is long pressed
                    // ...
                    return true;
                }
//                }

                return super.onMediaButtonEvent(mediaButtonIntent);
            }
        });
        mMediaSession.setActive(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Release MediaSessionCompat
        if (mMediaSession != null) {
            mMediaSession.setActive(false);
            mMediaSession.release();
            mMediaSession = null;
        }

        // Release wake lock
        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopSelf();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

