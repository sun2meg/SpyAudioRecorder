package com.android.sun2meg.spyaudiorecorder;

//import android.support.v7.app.AppCompatActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class SettingsAct extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    List<String> sts = new ArrayList<String>();
    List<String> lga = new ArrayList<String>();
    String tone;
    String endTone;
    String statusSwitch1, statusSwitch2;
    Switch simpleSwitch1, simpleSwitch2;
    Button submit;
    SwitchCompat switchCompat;
    SharedPreferences sharedPreferences,sharedset;

    private static SettingsAct activity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sharedPreferences = getSharedPreferences("MySharedPref",MODE_PRIVATE);
        sharedset = getSharedPreferences("save",MODE_PRIVATE);
        // initiate view's
        simpleSwitch1 = (Switch) findViewById(R.id.simpleSwitch1);
        simpleSwitch2 = (Switch) findViewById(R.id.simpleSwitch2);

        simpleSwitch1.setChecked(sharedPreferences.getBoolean("value",false));
        simpleSwitch2.setChecked(sharedPreferences.getBoolean("value2",false));
        simpleSwitch1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor myEdit = sharedPreferences.edit();
                if (simpleSwitch1.isChecked()) {
//                    statusSwitch1 = simpleSwitch1.getTextOn().toString();

                    myEdit.putBoolean("value", true);
                    myEdit.apply();
                    simpleSwitch1.setChecked(true);
                } else {
//                    statusSwitch1 = simpleSwitch1.getTextOff().toString();

                    myEdit.putBoolean("value", false);
                    myEdit.apply();
                    simpleSwitch1.setChecked(false);
                }
            }
        });





        simpleSwitch2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor myEdit = sharedPreferences.edit();
                if (simpleSwitch2.isChecked()) {
//                    statusSwitch1 = simpleSwitch1.getTextOn().toString();

                    myEdit.putBoolean("value2", true);
                    myEdit.apply();
                    simpleSwitch2.setChecked(true);
                } else {
//                    statusSwitch1 = simpleSwitch1.getTextOff().toString();

                    myEdit.putBoolean("value2", false);
                    myEdit.apply();
                    simpleSwitch2.setChecked(false);
                }
            }
        });


    }


    public  static SettingsAct getActivity(){
        return activity;
    }




    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {



    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}