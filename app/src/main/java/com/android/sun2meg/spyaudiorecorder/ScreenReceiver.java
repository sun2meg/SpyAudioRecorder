package com.android.sun2meg.spyaudiorecorder;
import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import java.io.IOException;

public class ScreenReceiver extends BroadcastReceiver {

    public static boolean wasScreenOn = true;
    int Count=0;
    int counter;
     boolean rec = false;
     Activity main;
    Vibrator v;
    private MainActivity mainActivity;
    Boolean countRec=true;
    SharedPreferences sharedPreferences;
String silent= "MIN_VOLUME";
int starTone;
int endTone;

    @Override
    public void onReceive(final Context context, final Intent intent) {
        Log.e("LOB","onReceive");
        sharedPreferences = context.getSharedPreferences("MySharedPref",context.MODE_PRIVATE);
        boolean actv = sharedPreferences.getBoolean("value",false);
        boolean deactv = sharedPreferences.getBoolean("value2",false);
//          Toast.makeText(context.getApplicationContext(), "Switch1 :" + actv + "\n" + "Switch2 :" + deactv, Toast.LENGTH_LONG).show(); // display the current state for switch's

        if(actv)
            starTone =80;
        else
            starTone=0;
        if(deactv)
            endTone =80;
        else
            endTone=0;

        this.main = mainActivity;
        v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            Count++;

                } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            // and do whatever you need to do here

            if(Count>1 && countRec){
                if(!rec) {
                    ToneGenerator tone = new ToneGenerator(AudioManager.STREAM_RING, starTone);
//                    ToneGenerator tone = new ToneGenerator(AudioManager.STREAM_RING, 0);
                    tone.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
                    v.vibrate(700);
                    MainActivity.getActivity().startRecording();

                    rec=true;
                } else {
                    v.vibrate(500);
                    ToneGenerator tone = new ToneGenerator(AudioManager.STREAM_RING, endTone);
                    tone.startTone(ToneGenerator.TONE_CDMA_ANSWER, 200);
                    MainActivity.getActivity().stopRecording();
                    rec=false;
                }

                new CountDownTimer(5000,1000){

                    @Override
                    public void onTick(long l) {
//                    Toast.makeText(ScreenReceiver.this, counter, Toast.LENGTH_SHORT).show();
                        countRec=false;
                        ToneGenerator tone = new ToneGenerator(AudioManager.STREAM_RING, 0);
                        tone.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
                        counter++;
                    }

                    @Override
                    public void onFinish() {
                        counter=0;
                        countRec =true;
                    }
                }.start();

            }
            Count++;
            wasScreenOn = true;
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable(){
                @Override
                public void run(){
                    Count=0;
                }
            },2000);

            Log.e("LOB","wasScreenOn"+wasScreenOn);



        }

    }

}
