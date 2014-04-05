package com.uic.ParkAssistTracker.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.uic.ParkAssistTracker.R;

/**
 * Created by AMAN on 3/13/14.
 */
public class CustomGridViewAdapter extends BaseAdapter {

    private Context context;
    private String[] gridV;
    public CustomGridViewAdapter(Context context, String[] gridValues) {
        this.context = context;
        this.gridV = gridValues ;
    }

    @Override
    public int getCount() {

        // Number of times getView method call depends upon gridValues.length
        return gridV.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View gridView;
        if(convertView == null) {
//            gridView = inflater.inflate(R.layout.fingerprintlayout, null);
            gridView = inflater.inflate(R.layout.fingerprintlayout, null);
        }
        else {
            gridView = (View) convertView;
        }
        assert gridView != null;
        final TextView tv = (TextView)gridView.findViewById(R.id.textView);
        tv.setText(gridV[position]);

        return gridView;
    }
}
