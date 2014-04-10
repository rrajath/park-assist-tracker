package com.uic.ParkAssistTracker.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
                startActivity(intent);
            }
        });


    }

}