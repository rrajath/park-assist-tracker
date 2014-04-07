package com.uic.ParkAssistTracker.activity;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.uic.ParkAssistTracker.R;

import java.util.ArrayList;

/**
 * Created by AMAN on 4/6/14.
 */
public class DirectionActivity extends ListActivity {
    public ListView directionView;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.directionlayout);
        ArrayList<String> directionList = (ArrayList<String>) getIntent().getSerializableExtra("directionList");
       // directionView = (ListView)findViewById(R.id.list);
        this.setListAdapter(new ArrayAdapter<String>(DirectionActivity.this,                android.R.layout.simple_list_item_1, directionList));
    }
}