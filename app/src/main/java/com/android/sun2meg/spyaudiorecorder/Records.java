package com.android.sun2meg.spyaudiorecorder;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Records extends AppCompatActivity {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.MANAGE_EXTERNAL_STORAGE};
    private boolean permissionToRecordAccepted = false;

    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;
    private String audioFilePath;

    private ListView fileListView;
    private List<String> recordedFiles;
    private ArrayAdapter<String> fileAdapter;
    Button playButton,stopButton,recordButton;
    private LinearLayout audioListLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_records);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.MANAGE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
        }
        audioFilePath = createFile();
//        audioFilePath = getExternalCacheDir().getAbsolutePath() + "/recorded_audio.3gp";
         stopButton = findViewById(R.id.stopBut);
      recordButton = findViewById(R.id.recordButton);
        playButton = findViewById(R.id.playButton);

        audioListLayout = findViewById(R.id.audioListLayout);
        stopButton.setEnabled(false);
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaRecorder == null) {
                    initializeMediaRecorder();
                }
                try {
                    mediaRecorder.prepare();
                    mediaRecorder.start();
                    recordButton.setEnabled(false);
                    playButton.setEnabled(false);
                    stopButton.setEnabled(true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer == null) {
                    initializeMediaPlayer();
                }
                mediaPlayer.start();
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecording();
                stopButton.setEnabled(false); // Disable the stop button after stopping recording
                recordButton.setEnabled(true); // Enable the record button again
                playButton.setEnabled(true);
            }
        });

        fileListView = findViewById(R.id.fileListView);
        recordedFiles = new ArrayList<>();
        fileAdapter = new RecordedFileAdapter(this, R.layout.recorded_file_item, recordedFiles);
        fileListView.setAdapter(fileAdapter);

        Button showFilesButton = findViewById(R.id.showFilesButton);
        showFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadRecordedFiles();
            }
        });

        fileListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String filePath = recordedFiles.get(position);
                playAudio(filePath);
            }
        });

        displayAudioFiles();

    }
    private void stopRecording() {
        if (mediaRecorder != null) {
            try {
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } finally {
                recordButton.setEnabled(true); // Enable the record button
                playButton.setEnabled(true);   // Enable the play button
            }
        }
        Toast.makeText(Records.this, "Already Paused", Toast.LENGTH_SHORT).show();
    }

    private String createFile(){
        String subDirName = "myAudio";
        File subDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), subDirName);

// Ensure that the subdirectory exists, or create it if it doesn't
        if (!subDir.exists()) {
            subDir.mkdirs(); // Create the subdirectory
        }
// Generate a unique filename using a timestamp
        String timestamp = String.valueOf(System.currentTimeMillis());
        return new File(subDir, "recorded_audio_" + timestamp + ".3gp").getAbsolutePath();
    }
    private void initializeMediaRecorder() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(audioFilePath);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
    }

    private void initializeMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(audioFilePath);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadRecordedFiles() {
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "myAudio");
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
        }
    }


    private void displayAudioFiles() {
        File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"myAudio");
//
//        File directory = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
        File[] files = directory.listFiles();

        if (files != null) {
            for (final File file : files) {
                if (file.isFile() && file.getName().endsWith(".3gp")) {
                    View audioItemView = LayoutInflater.from(this).inflate(R.layout.audio_item, null);
                    TextView audioFileName = audioItemView.findViewById(R.id.audioFileName);
                    Button deleteButton = audioItemView.findViewById(R.id.deleteButton);

                    audioFileName.setText(file.getName());

                    deleteButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            deleteAudioFile(file);
                        }
                    });

                    audioListLayout.addView(audioItemView);
                }
            }
        }
    }

    private void deleteAudioFile(File file) {
        if (file.exists()) {
            file.delete();
            Toast.makeText(this, "Audio file deleted", Toast.LENGTH_SHORT).show();
            displayAudioFiles();
        }
    }

    private void playAudio(String filePath) {
        if (mediaPlayer == null) {
            initializeMediaPlayer();
        }
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepare();
            mediaPlayer.start();
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
                    playAudio(filePath);
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
            AlertDialog.Builder builder = new AlertDialog.Builder(Records.this);
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

        private void deleteFile(String filePath) {
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
            }
        }

        private void stopAudio() {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (mediaRecorder != null) {
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted) {
            finish();
        }
    }
}
