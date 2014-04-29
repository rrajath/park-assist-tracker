package com.uic.ParkAssistTracker.activity;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import com.uic.ParkAssistTracker.R;

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

                intent.putExtra("startX", Integer.parseInt(stX));
                intent.putExtra("startY", Integer.parseInt(stY));
                intent.putExtra("direction", rbDirection.getText());

                startActivity(intent);
            }
        });


    }



}