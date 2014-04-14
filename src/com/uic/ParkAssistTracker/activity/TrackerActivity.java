package com.uic.ParkAssistTracker.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
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
    ArrayList<String> routeStrings = new ArrayList<String>();
    List scanResultsList;

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
                int x = position / GRID_COLUMNS;
                int y = position % GRID_COLUMNS;

                Point parkCell = new Point(x, y);

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
                        // Fix the start and destination points
                        Point destinationPoint = getNearestNavCell(parkCell);
                        Point startPoint = new Point(26, 10);   // Get the value of this from GeoFencing
                        routeStrings.clear();
                        // Calculate the route for start and end points
                        calculateRoute(startPoint, destinationPoint);
                        Intent intent = new Intent(TrackerActivity.this , DirectionActivity.class);
                        intent.putExtra("directionList" , routeStrings);
                        startActivity(intent);
                       // Toast.makeText(getApplicationContext(), routeStrings.toString(), Toast.LENGTH_LONG).show();

                }
            }
        });
        getLocation();
    }


    public ArrayList<String> singleScan(){


        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiManager.startScan();
        scanResultsList = wifiManager.getScanResults();
        ArrayList<String> bssidList = new ArrayList<String>();
        ArrayList<String> wifiList = new ArrayList<String>();

        String ssid;
        for(Object aScanResultList: scanResultsList){
            ScanResult scanResult = (ScanResult)aScanResultList;
            ssid = scanResult.BSSID + " | " + scanResult.SSID + " | " + scanResult.level;
            wifiList.add(ssid);
            bssidList.add(scanResult.BSSID);
        }

        return  bssidList;

    }
    public void getLocation(){

     ArrayList<String> bssid = new ArrayList<String>();
     bssid = singleScan();
     String query = "";
     String bssid_query = "";

        for(int i = 0; i < bssid.size();i++){

         if(i == bssid.size()-1){bssid_query =  bssid_query + "'" + bssid.get(i) + "'";}
         else
          bssid_query =  bssid_query + "'" + bssid.get(i) + "'" + " ," ;

     }
        query = "select f.ssid, f.rss, n.x_cord, n.y_cord from fingerprint_table f, navigation_table n where ssid in (" +
                 bssid_query + ") and  n.fp_id = f.fp_id ;";
        Toast.makeText(getApplicationContext(), query, Toast.LENGTH_LONG).show();


    }



    /*
     * Description: Get the nearest navigation cell with respect to the selected parking cell
     * Params: parking cell selected by the user
     * Returns: Navigation Cell
     */
    public Point getNearestNavCell(Point parkCell) {
        int navX = 0;
        int navY;
        String direction = "";
        int count = 0;

        int x = parkCell.getX();
        int y = parkCell.getY();

        int[] xArray = {1, 5, 9, 13, 17, 21, 25, 26};

        // Calculate direction
        direction = getDirection(x, y);

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

        // Compute y co-ordinate
        if (y % 3 == 0) {
            navY = y + 1;
        } else {
            navY = y - 1;
        }

        return new Point(navX, navY, direction);
    }

    /*
     * Description: Return the direction of the given coordinates
     * Params: x and y coordinate
     * Returns: Direction for that coordinate
     */
    public String getDirection(int x, int y) {
        String direction;
        if (x == 1) {
            direction = "west";
        } else if (y > 5) {
            direction = "north";
        } else {
            direction = "south";
        }

        return direction;
    }

    /*
     * Description: Generate the Route String
     * Params: start and end points
     * Return: Route string
     */
    public String generateRouteString(Point start, Point end) {
        // Helper method to calculate distance
        // calculateDistance
        // Direction = start direction
        String direction = start.getDirection();
        int distance;
        int lastElement;
        if (direction.equals("southwest")) {
            direction = "west";
        }

        distance = calculateDistance(start, end);

        String route = "Go " + distance + " meters " + direction + "\n";

        if (routeStrings.size() > 0) {
            lastElement = routeStrings.size()-1;
            String lastRoute = routeStrings.get(lastElement);
            String[] routeElements = lastRoute.split(" ");
            String dir = routeElements[routeElements.length - 1].trim();
            int previousDistance = Integer.parseInt(routeElements[1]);

            if (dir.equals(direction)) {
                distance += previousDistance;
                route = lastRoute.replace(String.valueOf(previousDistance), String.valueOf(distance));
                routeStrings.remove(lastElement);
            }
        }
        return route;
    }

    /*
     * Description: Calculate distance depending on the distance between start and end cell
     * Params: start and end points
     * Returns: distance between start and end points
     */
    public int calculateDistance(Point start, Point end) {
        if (start.getX() == end.getX()) {
            return Math.abs(end.getY() - start.getY()) * 3;
        } else {
            return Math.abs(end.getX() - start.getX()) * 3;
        }
    }

    /*
     * Description: Calculate the complete route between the start and destination points
     * Params: Start and destination points
     * Returns: nothing
     */
    public void calculateRoute(Point start, Point dest) {

        Point[] checkpointArray = {
                new Point(26, 10, "north"),
                new Point(1, 10, "west"),
                new Point(1, 7, "west"),
                new Point(1, 4, "southwest"),
                new Point(1, 1, "south"),
                new Point(26, 1, "east"),
                new Point(26, 4, "east"),
                new Point(26, 7, "northeast")
        };

        // Go through each intersection point to check whether they are in line with the destination point
        // WHILE LOOP
        // check direction of destination point with checkpoint
        // if true, end it
        // else, getNextCheckpoint()

        int counter = 0;
        while (true) {
            Point currentPoint = checkpointArray[counter];
            // check if destination point is in the same co-ordinate
            if (dest.getY() == currentPoint.getY() && currentPoint.getDirection().contains(dest.getDirection())) {
                    int distance = calculateDistance(currentPoint, dest);
                    String route = "Go " + distance + " meters " + dest.getDirection() + "\n";
                    routeStrings.add(route);
                    break;
            } else {
                // Generate Route String and add it to array
                Point nextCheckpoint = checkpointArray[counter + 1];
                routeStrings.add(generateRouteString(currentPoint, nextCheckpoint));
            }
            counter++;
        }
    }
}
