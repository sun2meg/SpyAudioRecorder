package com.android.sun2meg.spyaudiorecorder;


import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.DocumentsContract;
import android.view.LayoutInflater;
import android.view.View;

import androidx.core.content.FileProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.annotation.NonNull;
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
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;

import java.text.SimpleDateFormat;
import java.util.List;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import org.jetbrains.annotations.Nullable;

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
    private static final int REQUEST_CODE_FOLDER_ACCESS = 1234;

    private boolean isRecording = false;
    private static MainActivity activity;
    public static final String ACTION_START_RECORDING = "START_RECORDING";
    public static final String ACTION_STOP_RECORDING = "STOP_RECORDING";
//    @RequiresApi(api = Build.VERSION_CODES.O)

    private ListView fileListView;
    private List<String> recordedFiles;
    private ArrayAdapter<String> fileAdapter;


    private Button btnStopService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Request audio recording permission
        if (!CheckPermissions()) {
            RequestPermissions();
        }
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
//        stopRec.setBackgroundColor(getResources().getColor(R.color.gray));
        playRec.setBackgroundColor(getResources().getColor(R.color.purple_700));
        stopplayRec.setBackgroundColor(getResources().getColor(R.color.gray));
        btnStopService=findViewById(R.id.stopButton);

        fileListView = findViewById(R.id.fileListView);
        recordedFiles = new ArrayList<>();
        fileAdapter = new RecordedFileAdapter(this, R.layout.recorded_file_item, recordedFiles);
        fileListView.setAdapter(fileAdapter);

        startRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecording();


            }
        });
        stopRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pauseRecording();

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
        btnStopService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopBackService();
            }
        });

        fileListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String filePath = recordedFiles.get(position);
                try {
                    playAudio(filePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }
    private void loadRecordedFiles() {
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"myAudio");
//        String folderName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)+"/myFolder";
//        File dir = new File(folderName);
//        File dir = getExternalCacheDir();
        if (dir != null) {
            File[] files = dir.listFiles();
            if (files != null) {
                recordedFiles.clear();
                for (File file : files) {
                    if (file.isFile()) {
                        recordedFiles.add(file.getAbsolutePath());
                    }
                }
                fileAdapter.notifyDataSetChanged();
            }
        }  else {
            Toast.makeText(this, "Dir Not found .", Toast.LENGTH_SHORT).show();

        }
    }
    private void playAudio(String filePath) throws IOException {
        if (mPlayer == null) {
            initializeMediaPlayer();
        }
        try {
            mPlayer.reset();
            mPlayer.setDataSource(filePath);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initializeMediaPlayer() throws IOException {
        mPlayer = new MediaPlayer();
        mFileName = createAudioFile0();
        try {
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class RecordedFileAdapter extends ArrayAdapter<String> {

        public RecordedFileAdapter(@NonNull Context context, int resource, @NonNull List<String> objects) {
            super(context, resource, objects);
        }

        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.recorded_file_item, parent, false);
            }

            final String filePath = getItem(position);

            Button playButton = convertView.findViewById(R.id.playButton);
            Button stopButton = convertView.findViewById(R.id.stopButton);
            Button deleteButton = convertView.findViewById(R.id.deleteButton);
            TextView fileNameTextView = convertView.findViewById(R.id.fileNameTextView);

            playButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        playAudio(filePath);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            stopButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    stopAudio();
                }
            });

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDeleteDialog(filePath, position);
                }
            });

            fileNameTextView.setText(new File(filePath).getName());

            return convertView;
        }
        private void showDeleteDialog(final String filePath, final int position) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Delete File");
            builder.setMessage("Are you sure you want to delete this file?");
            builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    deleteFile(filePath);
                    recordedFiles.remove(position);
                    fileAdapter.notifyDataSetChanged();
                }
            });
            builder.setNegativeButton("Cancel", null);
            builder.show();
        }

        private void stopAudio() {
            if (mPlayer != null && mPlayer.isPlaying()) {
                mPlayer.stop();
            }
        }
        private void deleteFile(String filePath) {
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
            }
        }
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
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String audFileName = "AUD_" + timeStamp + "_";
        String folderName = "AudioRecordings";


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                StorageManager storageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
                StorageVolume storageVolume = storageManager.getPrimaryStorageVolume();
                Method getDirectoryMethod = storageVolume.getClass().getMethod("getDirectory");
                File primaryDir = (File) getDirectoryMethod.invoke(storageVolume);
                storageDir = new File(primaryDir, folderName);
            } catch (Exception e) {
                // Fallback to using Environment.getExternalStorageDirectory() on exception
                storageDir = new File(Environment.getExternalStorageDirectory(), folderName);
            }
        } else {
            // For devices with API level lower than 24, use Environment.getExternalStorageDirectory()
            storageDir = new File(Environment.getExternalStorageDirectory(), folderName);
        }

        if (!storageDir.exists()) {
            if (!storageDir.mkdirs()) {
                throw new IOException("Failed to create directory: " + storageDir.getAbsolutePath());
            }
        }

        File audioFile = File.createTempFile(
                audFileName,  /* prefix */
                ".3gp",       /* suffix */
                storageDir    /* directory */
        );

        return audioFile.toString();
    }
    public String createAudioFile0() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String audFileName = "AUD_" + timeStamp + "_";
//        String folderName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)+"/myFolder";
        storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"myAudio");
//       storageDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
//         storageDir = new File(getExternalFilesDir(null) + "/" + "silenceRec" + "/");
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

    void startRecording() {
        if(recEnabled) {
            // check permission method is used to check
            // that the user has granted permission
            // to record nd store the audio.



//
//            ToneGenerator tone = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
//            tone.startTone(ToneGenerator.TONE_CDMA_ANSWER, 200);

            if (CheckPermissions()) {
                Drawable drawable;
//if (playRec.getBackground()==getColor(R.color.gray)){}

                // setbackgroundcolor method will change
                // the background color of text view.
                stopRec.setBackgroundColor(getResources().getColor(R.color.purple_700));
                startRec.setBackgroundColor(getResources().getColor(R.color.gray));

                // below method is used to initialize
                // the media recorder class
                mRecorder = new MediaRecorder();

                // below method is used to set the audio
                // source which we are using a mic.
                mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);

                // below method is used to set
                // the output format of the audio.
                mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);

                // below method is used to set the
                // audio encoder for our recorded audio.
                mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

                // below method is used to set the
                // output file location for our recorded audio
//                mRecorder.setOutputFile(mFileName);


//
//                mRecorder.setMaxDuration(24000);
                try {
                    mFileName= createAudioFile0();
                    // below method will prepare
                    // our audio recorder class
                    mRecorder.setOutputFile(mFileName);
                    mRecorder.prepare();
                } catch (IOException e) {
                    Log.e("TAG", "prepare() failed");
                }

                // start method will start
                // the audio recording.
                mRecorder.start();
                //////////////////////////////////////////////////////////////////
                statusRec.setText("Recording");
                recEnabled = false;
                stopRecEnabled=true;
            } else {
                // if audio recording permissions are
                // not granted by user below method will
                // ask for runtime permission for mic and storage.
                RequestPermissions();
            }
        }else
            Snackbar.make(findViewById(android.R.id.content),"already recording!", Snackbar.LENGTH_LONG).show();
    }


    void startRecording00() throws IOException {
        if(recEnabled) {

            if (CheckPermissions()) {
                stopRec.setBackgroundColor(getResources().getColor(R.color.purple_700));
                startRec.setBackgroundColor(getResources().getColor(R.color.gray));
                mRecorder = new MediaRecorder();

                mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
//                mRecorder.setMaxDuration(24000);
                try {
                    mFileName = createAudioFile0();
                    mRecorder.setOutputFile(mFileName);
                    mRecorder.prepare();
                    mRecorder.start();
                    //////////////////////////////////////////////////////////////////
                    statusRec.setText("Recording");
                    recEnabled = false;
                    stopRecEnabled=true;
                } catch (IOException e) {
                    Log.e("TAG", "prepare() failed");
                }
             } else {
                RequestPermissions();
            }
        }else
            Snackbar.make(findViewById(android.R.id.content),"already recording!", Snackbar.LENGTH_LONG).show();
    }

    public void pauseRecording() {
        if(stopRecEnabled) {
            stopRec.setBackgroundColor(getResources().getColor(R.color.gray));
            startRec.setBackgroundColor(getResources().getColor(R.color.purple_700));

            mRecorder.stop();

            // below method will release
            // the media recorder class.
            mRecorder.release();
            mRecorder = null;
            statusRec.setText("Recording Stopped");

            recEnabled=true;
            ////////////////////////////////////////////////
            stopRecEnabled=false;

//            playEnabled = true;
        }else {
            Snackbar.make(findViewById(android.R.id.content),"Record already Paused!", Snackbar.LENGTH_LONG).show();

        }
    }


    public void pauseRecording00() {
        if(stopRecEnabled) {
            stopRec.setBackgroundColor(getResources().getColor(R.color.gray));
            startRec.setBackgroundColor(getResources().getColor(R.color.purple_700));
            // the audio recording.
            mRecorder.stop();
            // below method will release
            // the media recorder class.
            mRecorder.release();
            mRecorder = null;
            statusRec.setText("Recording Stopped");
            recEnabled=true;
            ////////////////////////////////////////////////
            stopRecEnabled=false;
//            playEnabled = true;
        }else {
            Snackbar.make(findViewById(android.R.id.content),"Record already Paused!", Snackbar.LENGTH_LONG).show();

        }
    }

    public void playAudio() {
        if (playEnabled) {
            mPlayer = new MediaPlayer();
            try {
                if(mFileName != null) {

                    playRec.setBackgroundColor(getResources().getColor(R.color.gray));
                    stopplayRec.setBackgroundColor(getResources().getColor(R.color.purple_700));
                    mPlayer.setDataSource(mFileName);
                    mPlayer.prepare();
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

    public void stopPlaying() {
        if (!playEnabled) {
//            if (stopPlayEnabled) {

            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
            statusRec.setText("Playback Stopped");
            playRec.setBackgroundColor(getResources().getColor(R.color.purple_700));
            stopplayRec.setBackgroundColor(getResources().getColor(R.color.gray));
            statusRec.setText("Playing Stopped");
            playEnabled=true;
            stopPlayEnabled = false;
        } else {
            Snackbar.make(findViewById(android.R.id.content),"Play already Paused!", Snackbar.LENGTH_LONG).show();
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // this method is called when user will
        // grant the permission for audio recording.
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_AUDIO_PERMISSION_CODE:
                if (grantResults.length > 0) {
                    boolean permissionToRecord = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean permissionToStore = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean permissionToFolder = grantResults[2] == PackageManager.PERMISSION_GRANTED;
                    if (permissionToRecord && permissionToStore && permissionToFolder) {

                        Snackbar.make(findViewById(android.R.id.content),"Permission Granted", Snackbar.LENGTH_LONG).show();
                    } else {
                        Snackbar.make(findViewById(android.R.id.content),"Permission Denied", Snackbar.LENGTH_LONG).show();
                                 }
                }
                break;
        }
    }

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



    private void openSpecificFolder(String folderPath) {
        // Create a File object for the folder you want to open
        File folder = new File(folderPath);

        // Check if the folder exists
        if (folder.exists()) {
            // Create an intent to open a file manager app
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            // Generate URI using FileProvider
            Uri uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", folder);
            // Set the MIME type based on the type of files in the folder
            intent.setDataAndType(uri, "*/*");
            // Validate that the device can open your File
            PackageManager pm = getPackageManager();
            if (intent.resolveActivity(pm) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, "No file manager app found.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Folder does not exist.", Toast.LENGTH_SHORT).show();
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
            loadRecordedFiles();
//            getFolder();

//            openSpecificFolder(String.valueOf(Environment.getExternalStorageDirectory()));
//            openDirectory();   ////works but
//            openDownloadsDirectory();

//            Intent intent = new Intent(getApplicationContext(), location.class);
//            startActivity(intent);
     }  else if(id == R.id.instruction){
            Intent intent = new Intent(getApplicationContext(), Slider.class);
            startActivity(intent);
        }


        return super.onOptionsItemSelected(item);
    }

    @TargetApi(Build.VERSION_CODES.Q)
    private void getFolder() {
        StorageManager storageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
        Intent intent = storageManager.getPrimaryStorageVolume().createOpenDocumentTreeIntent();
        String targetDirectory = "Android";
//        String targetDirectory = "Android%2Fmedia%2Fcom.whatsapp%2FWhatsapp%2FMedia%2FStatuses";

        Uri uri = intent.getParcelableExtra("android.provider.extra.INITIAL_URI");
        String scheme = uri.toString();
        scheme += "%3A" + targetDirectory;
        uri = Uri.parse(scheme);

        intent.putExtra("android.provider.extra.INITIAL_URI", uri);
        intent.putExtra("android.content.extra.SHOW_ADVANCED", true);
        startActivityForResult(intent, 1234);
    }

    private void openFolder() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        Uri uri = Uri.parse(mFileName);
        intent.setDataAndType(uri, "*/*");
        startActivity(Intent.createChooser(intent, "Open folder"));
    }

    private void openDirectory() {
        // Create an intent to open a file manager app
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        Uri uri = Uri.parse(Environment.getExternalStorageDirectory().getPath() + "/audioRec");
//        Uri uri = Uri.parse(Environment.getExternalStorageDirectory().getPath()); // Use the root directory here

        intent.setDataAndType(uri, "audio/3gpp"); // Set the MIME type to open any file type

        // Check if there is a file manager app available
        PackageManager pm = getPackageManager();
        if (intent.resolveActivity(pm) != null) {
            startActivity(intent);
        } else {
            // If no file manager app is available, you can show a message to the user
            Toast.makeText(this, "No file manager app found.", Toast.LENGTH_SHORT).show();
        }
    }



    private void openDir(){
        File folder = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);

        if (folder != null) {
            Uri folderUri = FileProvider.getUriForFile(
                    this,
                    getApplicationContext().getPackageName() + ".provider",
                    folder
            );

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(folderUri, "*/*");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, "No app available to open the folder.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Downloads folder not found.", Toast.LENGTH_SHORT).show();
        }

    }

    private void openAudioFolder() {
        // Get the external storage directory
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
//        File storageDir = new File(Environment.getExternalStorageDirectory(), "pictures");

        if (storageDir.exists() && storageDir.isDirectory()) {
            // Create an intent to view the folder using a file explorer app
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri uri = Uri.fromFile(storageDir);
            intent.setDataAndType(uri, "*/*");
            startActivity(intent);
        } else {
            // Handle the case when the folder doesn't exist
            // You can display a message or take appropriate action here
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
private void stopBackService() {
    Intent serviceIntent = new Intent(this, LockService.class);
    stopService(serviceIntent);
}
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
//        return NavigationUI.navigateUp(navController, appBarConfiguration)
//                || super.onSupportNavigateUp();
        return true;
    }



        public void onClickFile()  {
        // Get the directory path
    File directory = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);

                if (directory != null) {
        // Create an intent to open the directory in a file manager app

                    Intent intent = new Intent( Intent.ACTION_GET_CONTENT);
//        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(directory);
        intent.setDataAndType(uri, "*/*");

        if (intent.resolveActivity(getPackageManager()) != null) {
            // If a file manager app is available, start the intent to open the directory
            startActivity(intent);
        } else {
            // If no file manager app is available, show a message to the user
            Toast.makeText(MainActivity.this, "No file manager app found.", Toast.LENGTH_SHORT).show();
        }
    } else {
        // External storage is not available or accessible
        Toast.makeText(MainActivity.this, "External storage is not accessible.", Toast.LENGTH_SHORT).show();
    }
}



}
