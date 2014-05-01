package com.uic.ParkAssistTracker.activity;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import com.uic.ParkAssistTracker.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Scanner;

/**
 * Created by AMAN on 4/8/14.
 */
public class IntroActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.introactivity);

        Button bManageDB = (Button) findViewById(R.id.bManageDB);
        bManageDB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(IntroActivity.this, ManageDBActivity.class);
                startActivity(intent);
            }
        });
        String path = getApplicationContext().getExternalFilesDir(null).getAbsolutePath();
        String filename  = path + "/" + "lastknownlocation" + ".txt";
        String x_cord = "";
        String y_cord = "";
        String direction = "";
        try{

            File file = new File(filename);
            Scanner scanner = new Scanner(file);
            String[]  cord =  scanner.next().split(",");
            x_cord = cord[0];
            y_cord = cord[1];
            direction = cord[2];

        }
        catch (FileNotFoundException e){
            x_cord = "26";
            y_cord = "10";
            direction = "North";

        }



        final  String x_intent = x_cord;
        final   String y_intent = y_cord;
        final  String  direction_intent  = direction;
        Button bTracker = (Button) findViewById(R.id.bTracker);
        bTracker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(IntroActivity.this, TrackerActivity.class);
                EditText etStartX = (EditText) findViewById(R.id.startX);
                String stX = String.valueOf(etStartX.getText());

                EditText etStartY = (EditText) findViewById(R.id.startY);
                String stY = String.valueOf(etStartY.getText());

                RadioGroup rgDirection = (RadioGroup) findViewById(R.id.radioDirection);
                RadioButton rbDirection = (RadioButton) findViewById(rgDirection.getCheckedRadioButtonId());

                intent.putExtra("startX", Integer.parseInt(x_intent));
                intent.putExtra("startY", Integer.parseInt(y_intent));
                intent.putExtra("direction", direction_intent);

                startActivity(intent);
            }
        });


    }



}