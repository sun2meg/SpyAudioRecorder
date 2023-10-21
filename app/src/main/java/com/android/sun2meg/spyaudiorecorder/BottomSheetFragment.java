package com.android.sun2meg.spyaudiorecorder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
//import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BottomSheetFragment extends BottomSheetDialogFragment {
    private ListView fileListView;
    private List<String> recordedFiles;
    private ArrayAdapter<String> fileAdapter;
    private MediaPlayer mPlayer;
    // string variable is created for storing a file name
    private static String mFileName = null;
    private File storageDir;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        View view = View.inflate(getContext(), R.layout.fragment_bottom_sheet, null);

        fileListView = view.findViewById(R.id.fileListView);
        recordedFiles = new ArrayList<>();
//        fileAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, recordedFiles);
        fileAdapter = new RecordedFileAdapter(getContext(), R.layout.recorded_file_item, recordedFiles);
        fileListView.setAdapter(fileAdapter);

        // Load recorded files
        loadRecordedFiles();

        dialog.setContentView(view);
        return dialog;
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
            Toast.makeText(getContext(), "Dir Not found .", Toast.LENGTH_SHORT).show();


        }
    }
    private void playAudio(String filePath) throws IOException {
        if (mPlayer == null) {
            mPlayer = new MediaPlayer();
//            initializeMediaPlayer();
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
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
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

}
