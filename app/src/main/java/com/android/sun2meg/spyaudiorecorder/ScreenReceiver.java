package com.android.sun2meg.spyaudiorecorder;
import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ScreenReceiver extends BroadcastReceiver {

    private static final String TAG = "ScreenReceiver";
    File storageDir;
    private int count = 0;
    private int counter = 0;
    private boolean isRecording = false;
    private boolean rec = true;
    private Vibrator vibrator;
    private ToneGenerator toneGenerator,toneGenerator2;
    private MediaRecorder mediaRecorder;

    private SharedPreferences sharedPreferences;
    private int startTone;
    private int endTone;
    private String fileName;
    private Button playButton;
    private MediaPlayer mediaPlayer;
    private Context appContext;
    //    private MainActivity mainActivity;///////////////////////////////
    private boolean recEnabled;
    private static final int PERMISSION_REQUEST_CODE = 123; // You can choose any code

    @Override
    public void onReceive(final Context context, final Intent intent) {
        Log.e(TAG, "onReceive");
        appContext = context.getApplicationContext();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
//                    context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
//                    context.checkSelfPermission(Manifest.permission.MANAGE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                // Request permissions
//                requestPermissions(context);
//                return;
//            }
//        }
//        setMainActivity(MainActivity.getActivity());/////////////////////////////////////
        sharedPreferences = context.getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
        boolean activateSwitch = sharedPreferences.getBoolean("value", false);
        boolean deactivateSwitch = sharedPreferences.getBoolean("value2", false);

        startTone = activateSwitch ? 80 : 0;
        endTone = deactivateSwitch ? 80 : 0;

        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        toneGenerator = new ToneGenerator(AudioManager.STREAM_RING, startTone);
        toneGenerator2 = new ToneGenerator(AudioManager.STREAM_RING, endTone);
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                toneGenerator = new ToneGenerator(AudioManager.STREAM_RING, startTone);
//                // Rest of your code here
//            }
//        }, 1000); // Delay for 1 second

        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            handleScreenOff();
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            handleScreenOn(context);
        }


    }

    @TargetApi(Build.VERSION_CODES.R)
    private void requestPermissions(Context context) {
        // Request permissions from the user
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = {
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.MANAGE_EXTERNAL_STORAGE
            };

            // Request permissions
            ((Activity) context).requestPermissions(permissions, PERMISSION_REQUEST_CODE);
        }
    }

    private synchronized void handleScreenOff() {
        count++;
    }

    private synchronized void handleScreenOn(Context context) {
        if (count > 1 && rec) {
            try {
//                if (mainActivity != null) {
                if (!isRecording) {
                    try {
//                        vibrator.vibrate(700);
//                        toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
                        startRecording(context);
//                        isRecording = true;
                        // Rest of your code here
                    } catch (Exception e) {
                        e.printStackTrace();
                        showToast(context, "Tone error: " + e.getMessage());
                        // Handle the exception, log it, or show an error message
                    }

//                        mainActivity.startRecording();
                    // Your other code here

                } else {
                    try {
                        pauseRecording(context);

//                        vibrator.vibrate(700);
//                        toneGenerator2.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
//                        // Your other code here
//                        isRecording = false;
                        // Rest of your code here
                    } catch (Exception e) {
                        e.printStackTrace();
                        showToast(context, "Tone error: " + e.getMessage());
                        // Handle the exception, log it, or show an error message
                    }
//                        mainActivity.pauseRecording();

                }
            } catch (Exception e) {
                e.printStackTrace();
                showToast(context, "Exception: " + e.getMessage());
            }

            new CountDownTimer(5000, 1000) {
                @Override
                public void onTick(long l) {
                    rec = false;
//                    toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
                    counter++;
                }

                @Override
                public void onFinish() {
                    counter = 0;
                    rec = true;
                }
            }.start();
        }

        count++;

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            count = 0;
        }, 2000);
    }

    private void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }



    void startRecording(Context context) {

//        if (mediaRecorder.getState() == MediaRecorder.State.INITIALIZED) {
        if (mediaRecorder == null) {

            try {
                mediaRecorder = new MediaRecorder();
                fileName = createAudioFile();
                mediaRecorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
                    @Override
                    public void onError(MediaRecorder mr, int what, int extra) {
                        // Handle the error, log the details, or show an error message to the user
                        switch (what) {
                            case MediaRecorder.MEDIA_RECORDER_ERROR_UNKNOWN:
                                // Handle unknown errors
                                showToast(context, "MEDIA_RECORDER_ERROR_UNKNOWN");
                                break;
                            case MediaRecorder.MEDIA_ERROR_SERVER_DIED:
                                // Handle server died error
                                showToast(context, "MEDIA_ERROR_SERVER_DIED");
                                break;
                            // Add more cases for specific error types as needed
                        }
                    }
                });

//                            fileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/audio.3gp";
                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                mediaRecorder.setOutputFile(fileName);
                mediaRecorder.prepare();
                mediaRecorder.start();
//                  fileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/audio.3gp";
//                    mediaRecorder.setOutputFile(fileName);
//                    mediaRecorder.prepare();
//                    mediaRecorder.start();
//
                vibrator.vibrate(700);
                toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
                recEnabled = false;
                isRecording=true;
                showToast(context, "recording");
            } catch (IOException e) {
//                recEnabled = true;
//                isRecording=false;
                Log.e("TAG", "prepare() failed");
                releaseMediaRecorder();
                showToast(context, String.valueOf(e));
            } catch (IllegalStateException e) {
                recEnabled = true;
                isRecording=false;
                e.printStackTrace();
                releaseMediaRecorder();
                showToast(context, String.valueOf(e));
                // Handle the IllegalStateException, log the error, or show an error message to the user
            }
        } else {
            showToast(context, "MediaRecorder is not in the correct state for recording.");
        }
    }


    public void pauseRecording(Context context) {

        try {
            // the audio recording.
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
            recEnabled = true;

            vibrator.vibrate(700);
            toneGenerator2.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
            // Your other code here
            isRecording = false;

            showToast(context, "recording Stopped");
        } catch (Exception e) {
            e.printStackTrace();
            showToast(context, String.valueOf(e));
        }

    }


    public String createAudioFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String audFileName = "AUD_" + timeStamp + "_";

//        String folderName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)+"/myFolder";
        storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"myAudio");

        if(!storageDir.exists()){
            storageDir.mkdirs();
        }
        File vid = File.createTempFile(
                audFileName,  /* prefix */
                ".3gp",         /* suffix */
                storageDir      /* directory */
        );

        return vid.toString();
    }

    private void releaseMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }


}




//public class ScreenReceiver extends BroadcastReceiver {
//
//    private static final String TAG = "ScreenReceiver";
//
//    private int count = 0;
//    private int counter = 0;
//    private boolean isRecording = false;
//    private boolean rec = true;
//    private Vibrator vibrator;
//    private ToneGenerator toneGenerator;
//    private MediaRecorder mediaRecorder;
//
//    private SharedPreferences sharedPreferences;
//    private int startTone;
//    private int endTone;
////    private MainActivity mainActivity;
//
//    @Override
//    public void onReceive(final Context context, final Intent intent) {
//        Log.e(TAG, "onReceive");
//
//        sharedPreferences = context.getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
//        boolean activateSwitch = sharedPreferences.getBoolean("value", false);
//        boolean deactivateSwitch = sharedPreferences.getBoolean("value2", false);
////        setMainActivity(MainActivity.getActivity());
//        startTone = activateSwitch ? 80 : 0;
//        endTone = deactivateSwitch ? 80 : 0;
//
//        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                        toneGenerator = new ToneGenerator(AudioManager.STREAM_RING, startTone);
//                    mediaRecorder = new MediaRecorder();
//                        // Rest of your code here
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        // Handle the exception, log it, or show an error message
//                    }
//
//                // Rest of your code here
//            }
//        }, 1000); // Delay for 1 second
//
//        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
//            handleScreenOff();
//        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
//            handleScreenOn(context);
//        }
//    }
//
////    // Set MainActivity reference from outside
////    public void setMainActivity(MainActivity activity) {
////        this.mainActivity = activity;
////    }
//
//    private synchronized void handleScreenOff() {
//        count++;
//    }
//
//    private synchronized void handleScreenOn(Context context) {
//        if (count > 1 && rec) {
//            try {
//                if (!isRecording) {
//                    // Check if MainActivity is not null before calling methods
//                    sendCustomIntent(context, MainActivity.ACTION_START_RECORDING);
////                    toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
//                    vibrator.vibrate(700);
////                    if (mainActivity != null) {
////                        sendCustomIntent(context, MainActivity.ACTION_START_RECORDING);
////                        toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
////                        vibrator.vibrate(700);
////                        // Start audio recording
//////                        startRecording();
////                        isRecording = true;
////                    } else {
////                        Log.e(TAG, "MainActivity is null");
////                        showToast(context, "MainActivity is null");
////                        setMainActivity(MainActivity.getActivity());
////                    }
//                } else {
//                    // Check if MainActivity is not null before calling methods
//
//                    sendCustomIntent(context, MainActivity.ACTION_STOP_RECORDING);
//                    vibrator.vibrate(500);
////                    toneGenerator.startTone(ToneGenerator.TONE_CDMA_ANSWER, 200);
////                    if (mainActivity != null) {
//////                        mainActivity.stopRecording();
////                        sendCustomIntent(context, MainActivity.ACTION_STOP_RECORDING);
////                        vibrator.vibrate(500);
////                        toneGenerator.startTone(ToneGenerator.TONE_CDMA_ANSWER, 200);
////
////                        // Stop audio recording
////                        stopRecording();
////
////                        isRecording = false;
////                    } else {
////                        Log.e(TAG, "MainActivity is null");
////                        showToast(context, "MainActivity is null");
////                        setMainActivity(MainActivity.getActivity());
////                    }
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//                showToast(context, "Exception: " + e.getMessage());
//            }
//
//            new CountDownTimer(5000, 1000) {
//                @Override
//                public void onTick(long l) {
//                    rec = false;
////                    toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
////                    counter++;
//                }
//
//                @Override
//                public void onFinish() {
//                    counter = 0;
//                    rec = true;
//                }
//            }.start();
//        }
//
//        count++;
//
//        new Handler(Looper.getMainLooper()).postDelayed(() -> {
//            count = 0;
//        }, 2000);
//    }
//
//    private void showToast(Context context, String message) {
//        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
//    }
//
//    private void startRecording() {
//        try {
//            String fileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/audio.3gp";
//            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
//            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
//            mediaRecorder.setOutputFile(fileName);
//            mediaRecorder.prepare();
//            mediaRecorder.start();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void stopRecording() {
//        try {
//            mediaRecorder.stop();
//            mediaRecorder.release();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void sendCustomIntent(Context context, String action) {
//        Intent intent = new Intent(action);
//        context.sendBroadcast(intent);
//    }
//}

//
//public class ScreenReceiver extends BroadcastReceiver {
//
//    public static boolean wasScreenOn = true;
//    int Count=0;
//    int counter;
//     boolean rec = false;
//     Activity main;
//    Vibrator v;
//    private MainActivity mainActivity;
//    Boolean countRec=true;
//    SharedPreferences sharedPreferences;
//String silent= "MIN_VOLUME";
//int starTone;
//int endTone;
//
//        private Button playButton;
//    private MediaPlayer mediaPlayer;
//    private Context appContext;
//    private MediaRecorder mediaRecorder;
//        private String fileName;
//
//    @Override
//    public void onReceive(final Context context, final Intent intent) {
//        Log.e("LOB","onReceive");
//
//        appContext = context.getApplicationContext();
//        sharedPreferences = context.getSharedPreferences("MySharedPref",context.MODE_PRIVATE);
//        boolean actv = sharedPreferences.getBoolean("value",false);
//        boolean deactv = sharedPreferences.getBoolean("value2",false);
////          Toast.makeText(context.getApplicationContext(), "Switch1 :" + actv + "\n" + "Switch2 :" + deactv, Toast.LENGTH_LONG).show(); // display the current state for switch's
//        mediaRecorder = new MediaRecorder();
//        if(actv)
//            starTone =80;
//        else
//            starTone=0;
//        if(deactv)
//            endTone =80;
//        else
//            endTone=0;
//
//        this.main = mainActivity;
//        v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
//
//
////        // Find and initialize the play button
////        playButton = new Button(context);
////        playButton.setText("Play Recorded Audio");
////
////        // Add the play button to your layout
////        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
////                RelativeLayout.LayoutParams.WRAP_CONTENT,
////                RelativeLayout.LayoutParams.WRAP_CONTENT
////        );
////        layoutParams.addRule(RelativeLayout.BELOW, R.id.idRecstatus); // Adjust the rule as needed
////        playButton.setLayoutParams(layoutParams);
////        ((MainActivity) context).addContentView(playButton, layoutParams);
////
////        // Set a click listener for the play button
////        playButton.setOnClickListener(new View.OnClickListener() {
////            @Override
////            public void onClick(View v) {
////                // Handle the play button click event here
////                // You can add code to play the recorded audio
////                try {
////                    playRecordedAudio();
////                } catch (IOException e) {
////                    e.printStackTrace();
////                }
////            }
////        });
//
//        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
//            Count++;
//
//                } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
//            // and do whatever you need to do here
//
//            if(Count>1 && countRec){
//                if(!rec) {
//
////                    try {
////                        toneGenerator = new ToneGenerator(AudioManager.STREAM_RING, startTone);
////                        // Rest of your code here
////                    } catch (Exception e) {
////                        e.printStackTrace();
////                        // Handle the exception, log it, or show an error message
////                    }
//
//                    ToneGenerator tone = new ToneGenerator(AudioManager.STREAM_RING, starTone);
////                    ToneGenerator tone = new ToneGenerator(AudioManager.STREAM_RING, 0);
//                    tone.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
//                    v.vibrate(700);
//                    startRecording();
////                        MainActivity.getActivity().startRecording();
//
//                    rec=true;
//                } else {
//                    v.vibrate(500);
//                    ToneGenerator tone = new ToneGenerator(AudioManager.STREAM_RING, endTone);
//                    tone.startTone(ToneGenerator.TONE_CDMA_ANSWER, 200);
//                    stopRecording();
////                    MainActivity.getActivity().pauseRecording();
//                    rec=false;
//                }
//
//                new CountDownTimer(5000,1000){
//
//                    @Override
//                    public void onTick(long l) {
////                    Toast.makeText(ScreenReceiver.this, counter, Toast.LENGTH_SHORT).show();
//                        countRec=false;
//                        ToneGenerator tone = new ToneGenerator(AudioManager.STREAM_RING, 0);
//                        tone.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
//                        counter++;
//                    }
//
//                    @Override
//                    public void onFinish() {
//                        counter=0;
//                        countRec =true;
//                    }
//                }.start();
//
//            }
//            Count++;
//            wasScreenOn = true;
//            new Handler(Looper.getMainLooper()).postDelayed(new Runnable(){
//                @Override
//                public void run(){
//                    Count=0;
//                }
//            },2000);
//
//            Log.e("LOB","wasScreenOn"+wasScreenOn);
//
//
//
//        }
//
//    }
//
//
//    private void startRecording() {
//        try {
//            Toast.makeText(appContext, "Playing Recorded Audio", Toast.LENGTH_SHORT).show();
//
//            fileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/audio.3gp";
//            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
//            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
//            mediaRecorder.setOutputFile(fileName);
//            mediaRecorder.prepare();
//            mediaRecorder.start();
//        } catch (IOException e) {
//            e.printStackTrace();
//
//            Toast.makeText(appContext, String.valueOf(e), Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    private void stopRecording() {
//        try {
//            mediaRecorder.stop();
//            mediaRecorder.release();
//
//            Toast.makeText(appContext, "Stopping Recorded Audio", Toast.LENGTH_SHORT).show();
//        } catch (Exception e) {
//            e.printStackTrace();
//
//            Toast.makeText(appContext, String.valueOf(e), Toast.LENGTH_SHORT).show();
//        }
//    }
//
//
//    private void playRecordedAudio() throws IOException {
//        // Add code here to play the recorded audio using MediaPlayer or other audio player
//        try {
//            if (fileName != null) {
//                Toast.makeText(appContext, "Playing Recorded Audio", Toast.LENGTH_SHORT).show();
//
//                mediaPlayer = new MediaPlayer();
//                mediaPlayer.setDataSource(fileName);
//                mediaPlayer.prepare();
//                mediaPlayer.start();
//            }
//        } catch (IOException e) {
//            Log.e("TAG", "prepare() failed");
//        }
//    }
//
//}
