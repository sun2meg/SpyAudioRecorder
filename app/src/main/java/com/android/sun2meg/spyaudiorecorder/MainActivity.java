package com.android.sun2meg.spyaudiorecorder;


import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DownloadManager;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;

import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;

import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;

import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import java.text.SimpleDateFormat;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {
    Boolean recEnabled=true;
    Boolean stopRecEnabled=false;
    File storageDir;
    Boolean playEnabled=true;
    Boolean stopPlayEnabled=false;
    //     LockService s;
    // Initializing all variables..
    private TextView startRec, stopRec, playRec, stopplayRec, statusRec;

    // creating a variable for medi recorder object class.
    private MediaRecorder mRecorder;

    // creating a variable for mediaplayer class
    private MediaPlayer mPlayer;

    // string variable is created for storing a file name
    private static String mFileName = null;

    // constant for storing audio permission
    public static final int REQUEST_AUDIO_PERMISSION_CODE = 1;


    private boolean isRecording = false;
    private static MainActivity activity;
//    @RequiresApi(api = Build.VERSION_CODES.O)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RequestPermissions();
        activity=this;
        if(!foregroundServiceRunning()) {
            startService();

        }
        // initialize all variables with their layout items.
        statusRec = findViewById(R.id.idRecstatus);
        startRec = findViewById(R.id.btnRecord);
        stopRec = findViewById(R.id.btnStop);
        playRec = findViewById(R.id.btnPlay);
        stopplayRec = findViewById(R.id.btnStopPlay);
        stopRec.setBackgroundColor(getResources().getColor(R.color.gray));
        playRec.setBackgroundColor(getResources().getColor(R.color.purple_700));
        stopplayRec.setBackgroundColor(getResources().getColor(R.color.gray));

        startRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isRecording) {
                    startRecording();
                } else {
                    stopRecording();
                }
            }
        });
        stopRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecording();

            }
        });
        playRec.setOnClickListener(v -> {
            // play audio method will play
            // the audio which we have recorded
            playAudio();
        });
        stopplayRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // pause play method will
                // pause the play of audio
                stopPlaying();
            }
        });

        // Request audio recording permission
        if (!hasAudioRecordingPermission()) {
            requestAudioRecordingPermission();
        }
    }


    private boolean hasAudioRecordingPermission() {
        // Check if audio recording permission is granted
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        return permission == PackageManager.PERMISSION_GRANTED;
    }

    private void requestAudioRecordingPermission() {
        // Request audio recording permission
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.RECORD_AUDIO},
                REQUEST_AUDIO_PERMISSION_CODE
        );
    }

    public  static MainActivity getActivity(){
        return activity;
    }


    public boolean foregroundServiceRunning(){
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo service: activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if(LockService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    public void startService() {


        Intent notificationIntent = new Intent(this, LockService.class);
        notificationIntent.setAction("Start");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                getApplicationContext().startForegroundService(notificationIntent);
            }else
        ContextCompat.startForegroundService(this, notificationIntent);
//                Snackbar.make(findViewById(android.R.id.content),"Service Started!", Snackbar.LENGTH_LONG).show();
//            }
//            else{
//                Toast.makeText(this, "Require build", Toast.LENGTH_SHORT).show();}
    }


    public void stops(){

        stopplayRec.performClick();
    }



    private String createAudioFile1() throws IOException {
        // Create an ima    ge file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String audioFileName = "3GP_" + timeStamp + "_";
        String storageDir = Environment.getExternalStorageDirectory() + "/audioRec";
        File dir = new File(storageDir);
        if (!dir.exists())
            dir.mkdir();

        String audio =storageDir + "/" + audioFileName + ".3gp";

        return audio;
    }


    public String createAudioFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String audFileName = "AUD_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
//         storageDir = new File(getExternalFilesDir(null) + "/" + "silenceRec" + "/");
//         if(!storageDir.exists()){
//             storageDir.mkdirs();
//         }
        File vid = File.createTempFile(
                audFileName,  /* prefix */
                ".3gp",         /* suffix */
                storageDir      /* directory */
        );

        return vid.toString();
    }


     void startRecording() {
        if (hasAudioRecordingPermission()) {
            mFileName = getExternalCacheDir().getAbsolutePath() + "/audioRecord.3gp";

            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setOutputFile(mFileName);

            try {
                mRecorder.prepare();
                mRecorder.start();
                statusRec.setText("Recording...");
                isRecording = true;
                startRec.setText("Pause");
                stopRec.setEnabled(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // Request permission if not granted
            requestAudioRecordingPermission();
        }
    }

    void stopRecording() {
        if (isRecording) {
            try {
                mRecorder.stop();
                mRecorder.release();
                mRecorder = null;
                statusRec.setText("Recording Stopped");
                isRecording = false;
                startRec.setText("Record");
                stopRec.setEnabled(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void playAudio() {
        if (mFileName != null) {
            mPlayer = new MediaPlayer();
            try {
                mPlayer.setDataSource(mFileName);
                mPlayer.prepare();
                mPlayer.start();
                statusRec.setText("Playing...");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void stopPlaying() {
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
            statusRec.setText("Playback Stopped");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_AUDIO_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can start recording
            } else {
                // Permission denied, handle accordingly (show a message, etc.)
                Toast.makeText(this, "Audio recording permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

//
//    void startRecording() {
//        if(recEnabled) {
//
//            if (CheckPermissions()) {
//                Drawable drawable;
////if (playRec.getBackground()==getColor(R.color.gray)){}
//
//                // setbackgroundcolor method will change
//                // the background color of text view.
//                stopRec.setBackgroundColor(getResources().getColor(R.color.purple_700));
//                startRec.setBackgroundColor(getResources().getColor(R.color.gray));
//
//                // below method is used to initialize
//                // the media recorder class
//                mRecorder = new MediaRecorder();
//
//                // below method is used to set the audio
//                // source which we are using a mic.
//                mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//
//                // below method is used to set
//                // the output format of the audio.
//                mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
//
//                // below method is used to set the
//                // audio encoder for our recorded audio.
//                mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
//
//                // below method is used to set the
//                // output file location for our recorded audio
////                mRecorder.setOutputFile(mFileName);
//
//
////
////                mRecorder.setMaxDuration(24000);
//                try {
//                    mFileName= createAudioFile();
//                    // below method will prepare
//                    // our audio recorder class
//                    mRecorder.setOutputFile(mFileName);
//                    mRecorder.prepare();
//                } catch (IOException e) {
//                    Log.e("TAG", "prepare() failed");
//                }
//
//                // start method will start
//                // the audio recording.
//                mRecorder.start();
//                //////////////////////////////////////////////////////////////////
//                statusRec.setText("Recording");
//                recEnabled = false;
//                stopRecEnabled=true;
//            } else {
//                // if audio recording permissions are
//                // not granted by user below method will
//                // ask for runtime permission for mic and storage.
//                RequestPermissions();
//            }
//        }else
//                    Snackbar.make(findViewById(android.R.id.content),"already recording!", Snackbar.LENGTH_LONG).show();
//    }

//    public void pauseRecording() {
//        if(stopRecEnabled) {
//            stopRec.setBackgroundColor(getResources().getColor(R.color.gray));
//            startRec.setBackgroundColor(getResources().getColor(R.color.purple_700));
//
//            // the audio recording.
//            mRecorder.stop();
//
//            // below method will release
//            // the media recorder class.
//            mRecorder.release();
//            mRecorder = null;
//            statusRec.setText("Recording Stopped");
//
//            recEnabled=true;
//            ////////////////////////////////////////////////
//            stopRecEnabled=false;
//
////            playEnabled = true;
//        }else {
//            Snackbar.make(findViewById(android.R.id.content),"Record already Paused!", Snackbar.LENGTH_LONG).show();
//
//        }
//    }


//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        // this method is called when user will
//        // grant the permission for audio recording.
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        switch (requestCode) {
//            case REQUEST_AUDIO_PERMISSION_CODE:
//                if (grantResults.length > 0) {
//                    boolean permissionToRecord = grantResults[0] == PackageManager.PERMISSION_GRANTED;
//                    boolean permissionToStore = grantResults[1] == PackageManager.PERMISSION_GRANTED;
//                    boolean permissionToFolder = grantResults[2] == PackageManager.PERMISSION_GRANTED;
//                    if (permissionToRecord && permissionToStore && permissionToFolder) {
//
//                        Snackbar.make(findViewById(android.R.id.content),"Permission Granted", Snackbar.LENGTH_LONG).show();
//                    } else {
//                        Snackbar.make(findViewById(android.R.id.content),"Permission Denied", Snackbar.LENGTH_LONG).show();
//                                 }
//                }
//                break;
//        }
//    }

    public boolean CheckPermissions() {
        // this method is used to check permission
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO);
        int result2 = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);

        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED && result2 == PackageManager.PERMISSION_GRANTED;
    }

    private void RequestPermissions() {
        // this method is used to request the
        // permission for audio recording and storage.
//        ActivityCompat.requestPermissions(MainActivity.this, new String[]{RECORD_AUDIO, WRITE_EXTERNAL_STORAGE}, REQUEST_AUDIO_PERMISSION_CODE,READ_EXTERNAL_STORAGE);

        ActivityCompat.requestPermissions(MainActivity.this, new String[]{RECORD_AUDIO, WRITE_EXTERNAL_STORAGE,READ_EXTERNAL_STORAGE}, REQUEST_AUDIO_PERMISSION_CODE);

    }



    public void playAudio2() {
        if (playEnabled) {
            ///////////////////////////////////////////////////////////////////
//            stopRec.setBackgroundColor(getResources().getColor(R.color.gray));
//            startRec.setBackgroundColor(getResources().getColor(R.color.gray));
            ///////////////////////////////////////////////////////////////////////


            // for playing our recorded audio
            // we are using media player class.
            mPlayer = new MediaPlayer();
            try {
                if(mFileName != null) {


                    playRec.setBackgroundColor(getResources().getColor(R.color.gray));
                    stopplayRec.setBackgroundColor(getResources().getColor(R.color.purple_700));
                    playEnabled=false;
                    stopPlayEnabled=true;

                    // below method is used to set the
                    // data source which will be our file name
                    mPlayer.setDataSource(mFileName);

                    // below method will prepare our media player
                    mPlayer.prepare();

                    // below method will start our media player.
                    mPlayer.start();
                    statusRec.setText(" Playing");
                    playRec.setBackgroundColor(getResources().getColor(R.color.gray));
                    stopplayRec.setBackgroundColor(getResources().getColor(R.color.purple_700));

                    playEnabled=false;
                    stopPlayEnabled=true;

                }else
                    Snackbar.make(findViewById(android.R.id.content),"No Record yet!", Snackbar.LENGTH_LONG).show();

            } catch (IOException e) {
                Log.e("TAG", "prepare() failed");
            }
        } else 
        Snackbar.make(findViewById(android.R.id.content),"already playing!", Snackbar.LENGTH_LONG).show();

    }

    public void pausePlaying() {
        if (stopPlayEnabled) {
            // this method will release the media player
            // class and pause the playing of our recorded audio.
            mPlayer.release();
            mPlayer = null;
            //////////////////////////////////////////////////////////////////////////
//            stopRec.setBackgroundColor(getResources().getColor(R.color.gray));
//            startRec.setBackgroundColor(getResources().getColor(R.color.purple_200));
            playRec.setBackgroundColor(getResources().getColor(R.color.purple_700));
            stopplayRec.setBackgroundColor(getResources().getColor(R.color.gray));
            statusRec.setText("Playing Stopped");
///////////////////////////////////////////////////////////////////////////
            //            recEnabled=true;

            playEnabled=true;
            stopPlayEnabled = false;
        } else {
            Snackbar.make(findViewById(android.R.id.content),"Play already Paused!", Snackbar.LENGTH_LONG).show();
        }
    }
    public void openFolder(String location) {
        if (!location.isEmpty()){
            // location = "/sdcard/my_folder";
//            Intent intentD = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);

//            Intent intent = new Intent(Intent.ACTION_PICK);

            Uri mydir = Uri.parse(location);
//            intent.setDataAndType(mydir, "application/*");    // or use */*
//        intent.setDataAndType(mydir,"*/*");

            File fil= new File(Environment.getExternalStorageDirectory(),"pictures");
            intent.setDataAndType(Uri.fromFile(fil),"audio/3gpp");
//            intent.setDataAndType(mydir,"audio/3gpp");
            startActivity(intent);
//            startActivity(Intent.createChooser(intent,"Open Folder"));
        }else {
            Snackbar.make(findViewById(android.R.id.content),"No file yet!", Snackbar.LENGTH_LONG).show();


        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(getApplicationContext(), SettingsAct.class);
            startActivity(intent);
            return true;
        } else if(id == R.id.folder){
            openDownloadsDirectory();

//            Intent intent = new Intent(getApplicationContext(), location.class);
//            startActivity(intent);
     }  else if(id == R.id.instruction){
            Intent intent = new Intent(getApplicationContext(), Slider.class);
            startActivity(intent);
        }


        return super.onOptionsItemSelected(item);
    }

    private void openDownloadsDirectory() {

//        Uri selectedUri = Uri.parse(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath());
        Uri selectedUri = Uri.parse(Environment.getExternalStorageDirectory().toString() + "/Download/");

//        Uri selectedUri2 = Uri.parse(Environment.getExternalStorageDirectory().toString() + "/DCIM/");

        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setData(selectedUri);
        intent.setType("*/*");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

//        Uri downloadsUri = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3ADownload");
//        Intent intent = new Intent(Intent.ACTION_VIEW);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent.setDataAndType(downloadsUri, DocumentsContract.Document.MIME_TYPE_DIR);
//
//        if (intent.resolveActivity(getPackageManager()) != null) {
//            startActivity(intent);
//        } else {
//            // Handle the case where there is no app to handle the intent
//            showToast("No app available to open the Downloads directory.");
//        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
//    private void openDownloadsDirectory() {
//        Uri downloadsUri = Uri.parse(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath());
//        Intent intent = new Intent(Intent.ACTION_VIEW);
//        intent.setDataAndType(downloadsUri, "audio/3gpp");
////        intent.setDataAndType(downloadsUri, "*/*");
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//
//        if (intent.resolveActivity(getPackageManager()) != null) {
//            startActivity(intent);
//        } else {
//            // Handle the case where there is no app to handle the intent
//            showToast("No app available to open the Downloads directory.");
//        }
//    }
//
//    private void showToast(String message) {
//        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
//    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
//        return NavigationUI.navigateUp(navController, appBarConfiguration)
//                || super.onSupportNavigateUp();
        return true;
    }



}
