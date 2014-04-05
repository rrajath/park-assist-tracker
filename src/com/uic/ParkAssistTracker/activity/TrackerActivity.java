package com.uic.ParkAssistTracker.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import com.uic.ParkAssistTracker.R;
import com.uic.ParkAssistTracker.util.CustomGridViewAdapter;
import com.uic.ParkAssistTracker.util.Point;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TrackerActivity extends Activity {
    /**
     * Called when the activity is first created.
     */

    GridView gridView;
    final int GRID_COLUMNS = 12;
    static String[] numbers = new String[336];
    int k =0;
    int count =1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fingerprintlayout);

        for (int i = 0; i < 336; i++) {
            if((i)-((3*k+1)) == 0){
                numbers[i] = "";
                k++;
            } else{
                numbers[i] = String.valueOf(count++);
            }
        }

        List<String> list = new ArrayList<String>(Arrays.asList(numbers));
        gridView = (GridView) findViewById(R.id.gridview);
        gridView.setNumColumns(GRID_COLUMNS);

        // Create adapter to set value for grid viewup
        Adapter adapter = new ArrayAdapter<String>(this,
                R.layout.list_item, list);

        gridView.setAdapter(new CustomGridViewAdapter(this, numbers));

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                // Calculating the grid co-ordinates
                Point parkCell = new Point();
                int x = position / GRID_COLUMNS;
                int y = position % GRID_COLUMNS;

                parkCell.setX(x);
                parkCell.setY(y);

                // PNPPNPPNPPNP
                switch (y) {
                    case 0:
                    case 2:
                    case 3:
                    case 5:
                    case 6:
                    case 8:
                    case 9:
                    case 11:
                        Point destinationPoint = getNearestNavCell(parkCell);
                        Point startPoint;   // Get the value of this from GeoFencing
                        Toast.makeText(getApplicationContext(), "(" + destinationPoint.getX() + "," + destinationPoint.getY() + ")", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public Point getNearestNavCell(Point parkCell) {
        int navX = 0;
        int navY;
        String direction = "";
        int count = 0;

        Point nearestNavCell = new Point();

        int x = parkCell.getX();
        int y = parkCell.getY();

        int[] xArray = {1, 5, 9, 13, 17, 21, 25, 29};

        // Calculate direction
        if (y > 5) {
            direction = "North";
        } else {
            direction = "South";
        }

        // Compute x co-ordinate
        for (int aXArray : xArray) {
            if (x <= aXArray) {
                navX = aXArray;
                break;
            }
            count++;
        }

        if (direction.equalsIgnoreCase("south")) {
            navX = xArray[count - 1];
        }

        nearestNavCell.setX(navX);

        // Compute y co-ordinate
        if (y % 3 == 0) {
            navY = y + 1;
        } else {
            navY = y - 1;
        }
        nearestNavCell.setY(navY);

        return nearestNavCell;
    }

    public void calculateRoute() {
        // Get start point
        // Get destination point
        // Maintain an array of intersection points
        // Go through each intersection point to check whether they are in line with the destination point
        // Use helper methods when needed
    }
}
