package com.uic.ParkAssistTracker.activity;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.uic.ParkAssistTracker.R;
import com.uic.ParkAssistTracker.database.Datasource;
import com.uic.ParkAssistTracker.util.CustomGridViewAdapter;
import com.uic.ParkAssistTracker.util.Point;

import java.util.*;

public class TrackerActivity extends Activity {
    /**
     * Called when the activity is first created.
     */

    GridView gridView;
    final int GRID_COLUMNS = 12;
    static String[] numbers = new String[336];
    int k = 0;
    int count = 1;
    ArrayList<String> routeStrings = new ArrayList<String>();       // List of navigation instructions
    List scanResultsList;
    ArrayList<String> nextDirection = new ArrayList<String>();      // List of direction pairs
    int routeListCounter = 0;
    Point lastKnownLocation = new Point(26,10,"north");
    ArrayList<Point> next3Points = new ArrayList<Point>();          // next3Points bucket
    Point destinationPoint;                                         // ParkCell selected by user

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fingerprintlayout);

        for (int i = 0; i < 336; i++) {
            if ((i) - ((3 * k + 1)) == 0) {
                numbers[i] = "";
                k++;
            } else {
                numbers[i] = String.valueOf(count++);
            }
        }

        gridView = (GridView) findViewById(R.id.gridview);
        gridView.setNumColumns(GRID_COLUMNS);

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
                        destinationPoint = getNearestNavCell(parkCell);
                        nextDirection.clear();
                        calculateRoute(destinationPoint);
                        Point startPoint = getCurrentPoint();   // Get the value of this from GeoFencing
                        routeStrings.clear();
                        // Calculate the route for start and end points
/*
                        calculateRoute(startPoint, destinationPoint);
                        Intent intent = new Intent(TrackerActivity.this , DirectionActivity.class);
                        intent.putExtra("directionList" , routeStrings);
                        startActivity(intent);
*/
//                        getLocation();
                        // Toast.makeText(getApplicationContext(), routeStrings.toString(), Toast.LENGTH_LONG).show();

                }
            }
        });
//        getLocation();
    }


    /**
     * Populates the next 3 references points in the next3Points bucket, makes a database query and
     * returns the current location. This returned location will be the lastKnownLocation. Also,
     * this method will be called every second or two.
     * @return currentPoint
     */
    private Point getCurrentPoint() {
        // Counter pointing to the routeStrings (i.e. Strings containing step-by-step navigation)
        // SHOULD WE RESET IT EVERY TIME?
        routeListCounter = 0;

        // Clear next3Points every time the method is called
        next3Points.clear();
        // start search from last known location
        // calculate route to get direction list

        // Flag to check if direction has changed. In this context, direction change means,
        // changing checkpoints
        boolean directionChanged = true;
        String directionPair;                       // direction of previous and next checkpoint
        int[] pointsArray = null;                   // array of integers pointing to reference points
        Point lastPoint = lastKnownLocation;        // lastPoint is lastKnownLocation to start the search

        // Until the next3Points bucket is filled
        while (next3Points.size() < 3) {
            if (directionChanged) {
                // get directionPair from nextDirection list currently pointed to by routeListCounter
                directionPair = nextDirection.get(routeListCounter);
                // List of integers (co-ordinates) for that direction pair
                pointsArray = getPointsArray(directionPair);
            }

            // If length of returned array is greater than 1, search index of the current coordinate
            // in the pointsArray
            if (pointsArray.length > 1) {           // The list returned is a set of x-coordinates
                // Search for lastPoint's x-coordinate from the array and return the index
                int index = searchIndex(pointsArray, lastPoint.getX());
                int y = lastPoint.getY();
                int counter = 0;
                // Traverse through the array and add points to the next3Points bucket until it is full
                // Array traversal excludes current point. Hence for(i = index + 1...)
                for (int i = index + 1; i < pointsArray.length; i++) {
                    next3Points.add(new Point(pointsArray[i], y));
                    counter++;
                    if (counter == 3 || next3Points.size() == 3) {
                        break;
                    }
                }
            } else {
                int x;          // The x-coordinate of the Point to be added to the next3Points bucket
                if (next3Points.size() != 0) {
                    lastPoint = next3Points.get(next3Points.size() - 1);
                    x = lastPoint.getX();
                } else {
                    x = lastKnownLocation.getX();
                }
                int y = pointsArray[0];     // Size of this array is always 1. So we hardcode it to pointsArray[0]
                next3Points.add(new Point(x, y));
            }
            // If the bucket size reaches 3, we increment the routeListCounter
            directionChanged = next3Points.size() < 3;
            if (directionChanged) {
                routeListCounter++;
            }
        }
        // Toast the output for testing purposes. This displays the next 3 points from the current point
        String output = "(" + String.valueOf(next3Points.get(0).getX()) + "," + String.valueOf(next3Points.get(0).getY()) + ")\n";
        output += "(" + String.valueOf(next3Points.get(1).getX()) + "," + String.valueOf(next3Points.get(1).getY()) + ")\n";
        output += "(" + String.valueOf(next3Points.get(2).getX()) + "," + String.valueOf(next3Points.get(2).getY()) + ")";

        Toast.makeText(getApplicationContext(), output, Toast.LENGTH_LONG).show();

        // search the database with the 3 coordinates obtained and return the current point
        // assign current point to last known location
        lastKnownLocation = getLocation(); // The current location obtained by matching the next 3 feature points
        Toast.makeText(getApplicationContext(), "RESULT: " + lastKnownLocation.toString(), Toast.LENGTH_LONG).show();
        return lastKnownLocation;
    }

    /**
     * Search for current point's x-coordinate in the array and return the index
     * @param pointsArray - array of x-coordinates
     * @param value - x coordinate of the current point
     * @return index
     */
    private int searchIndex(int[] pointsArray, int value) {
        for (int i = 0; i < pointsArray.length; i++)
            if (value == pointsArray[i])
                return i;
        return -1;
    }


    /**
     * Online scan. One scan happens and the scan results are saved to the hmOnlineScan HashMap
     * @return
     */
    public HashMap<String, Integer> singleScan() {

        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiManager.startScan();
        scanResultsList = wifiManager.getScanResults();

        HashMap<String, Integer> hmOnlineScan = new HashMap<String, Integer>();

        for (Object aScanResultList : scanResultsList) {
            ScanResult scanResult = (ScanResult) aScanResultList;
            hmOnlineScan.put(scanResult.BSSID, WifiManager.calculateSignalLevel(scanResult.level,100));
        }

        return hmOnlineScan;
    }


    /**
     * Query the database with minimized search space (i.e. just the next 3 points from the current location)
     * Get the most probable point of the 3 points queried
     * @return returned location from the database
     */
    public Point getLocation() {
        HashMap<String, Integer> hmOnlineScan;
        HashMap<String, Integer> sortedMap;
        // Do a live scan to collect fingerprints
        hmOnlineScan = singleScan();
        Point returnedPoint = null;

        Datasource datasource = new Datasource(getApplicationContext(), "fingerprint_table");
        datasource.open();

        // hmCordMap is used to store the number of occurrences of each of the 3 coordinates from the query result
        HashMap<String, Integer> hmCordMap = new HashMap<String, Integer>();
        for (Map.Entry<String, Integer> entry : hmOnlineScan.entrySet()) {
            returnedPoint = datasource.getCurrentCoordinates(entry.getKey(), entry.getValue(), next3Points);
            String xy = null;
            // If the database is not able to match anything, the lastKnownLocation is displayed. Else, use
            // the returnedPoint
            if (returnedPoint == null) {
                returnedPoint = lastKnownLocation;
            } else {
                xy = returnedPoint.toString();
            }
            int value;      // Part of the <xy,value> pair of hmCordMap
            try {
                if (xy != null) {
                    if (hmCordMap.containsKey(xy)) {
                        value = hmCordMap.get(xy);
                        hmCordMap.put(xy, value + 1);
                    } else {
                        hmCordMap.put(xy, 1);
                    }
                }
            } catch (Exception e) {
                Log.e("hmCordMap", "hmCordMap screwed up");
            }
        }
        datasource.close();
        if(!hmCordMap.isEmpty()) {
            // Sort the hmCordMap
            sortedMap = (HashMap<String, Integer>) sortByComparator(hmCordMap);
            String result = "";
            for (Map.Entry<String, Integer> entry : sortedMap.entrySet()){
                result += entry.getKey() + " " + entry.getValue().toString() + "\n";
            }
            Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();

        } else {
            // If hmCordMap is empty. This will happen when none of the coordinates match or if xy is null.
            // Hence returning lastKnownLocation
            return lastKnownLocation;
        }

        return returnedPoint;
    }


    /**
     * Sorting the hmCordMap using comparator. This sorting is done in descending order to get the most probable
     * current location
     * @param unsortedMap - the unsorted hmCordMap
     * @return sorted hmCordMap
     */
    private static Map<String, Integer> sortByComparator(Map<String, Integer> unsortedMap) {
        //Putting the map in linkedlist
        List<Map.Entry<String, Integer>> cordList = new LinkedList<Map.Entry<String, Integer>>(unsortedMap.entrySet());
        //Sorting the list on values
        Collections.sort(cordList, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> lhs, Map.Entry<String, Integer> rhs) {
                if (lhs.getValue() < rhs.getValue()) {
                    return 1;
                } else if (lhs.getValue() > rhs.getValue()) {
                    return -1;
                } else return 0;
            }
        });

        // Reconstructing the HashMap into a sorted one
        Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
        for (Map.Entry<String, Integer> entry : cordList) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }


    /**
     * Given any parking cell as the destination point, get the nearest navigation cell to navigate to.
     * @param parkCell - destination point selected by the user
     * @return - nearest navigation cell
     */
    public Point getNearestNavCell(Point parkCell) {
        int navX = 0;
        int navY;
        String direction = "";
        int count = 0;

        int x = parkCell.getX();
        int y = parkCell.getY();

        int[] xArray = {1, 5, 9, 13, 17, 21, 26};

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


    /**
     * Returning the direction for the coordinate
     * @param x - x coordinate of the point
     * @param y - y coordinate of the point
     * @return - direction
     */
    public String getDirection(int x, int y) {
        String direction;
        if (x == 1) {
            direction = "west";
        } else if (x == 26) {
            direction = "east";
        } else if (y > 5) {
            direction = "north";
        } else {
            direction = "south";
        }

        return direction;
    }


    /**
     * Generate navigation instructions for the given start and end point
     * @param start - start point
     * @param end - end point
     * @return - String containing navigation instructions
     */
    public String generateRouteString(Point start, Point end) {
        String direction = start.getDirection();
        int distance;
        int lastElement;
        if (direction.equals("southwest")) {
            direction = "west";
        }

        distance = calculateDistance(start, end);

        String route = "Go " + distance + " meters " + direction + "\n";

        if (routeStrings.size() > 0) {
            lastElement = routeStrings.size() - 1;
            String lastRoute = routeStrings.get(lastElement);
            String[] routeElements = lastRoute.split(" ");
            String dir = routeElements[routeElements.length - 1].trim();
            int previousDistance = Integer.parseInt(routeElements[1]);

            // If the direction of the previous route string and the current is the same,
            // Add the distances.
            // For example, "Go 9 meters west" and "Go 9 meters west" becomes "Go 18 meters west"
            if (dir.equals(direction)) {
                distance += previousDistance;
                route = lastRoute.replace(String.valueOf(previousDistance), String.valueOf(distance));
                routeStrings.remove(lastElement);
            }
        }
        return route;
    }


    /**
     * Calculate distance between start and end point. For now, the cell distance is taken to be 3 meters
     * @param start - start point
     * @param end - end point
     * @return distance between start and end points
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

    /**
     * Calculate the complete route between current point and destination points and update the global
     * variables routeStrings and nextDirection
     * @param dest - destination point
     */
    public void calculateRoute(Point dest) {

        // An array of checkpoints. Later used for traversing through the navigation path
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

        // HashMap to store checkpoints. The values can be used to traverse through the checkpointArray
        HashMap<String, Integer> chkPointMap = new HashMap<String, Integer>();
        chkPointMap.put("26,10", 0);
        chkPointMap.put("1,10", 1);
        chkPointMap.put("1,7", 2);
        chkPointMap.put("1,4", 3);
        chkPointMap.put("1,1", 4);
        chkPointMap.put("26,1", 5);
        chkPointMap.put("26,4", 6);
        chkPointMap.put("26,7", 7);

        // Go through each intersection point to check whether they are in line with the destination point
        // WHILE LOOP
        // check direction of destination point with checkpoint
        // if true, end it
        // else, getNextCheckpoint()

        // Get current point
        // Get next checkpoint from current point
        // Calculate distance between current point and next checkpoint

        // Used to traverse through checkpointArray
        int counter = 0;

        // To start with, currentPoint is set to lastKnownLocation
        Point currentPoint = lastKnownLocation;

        // Next checkpoint is obtained from the method by passing the currentPoint
        Point nextCheckpoint = getNextCheckpoint(currentPoint);

        // Get the distance between currentPoint and nextCheckpoint. This case occurs if the current point
        // is not one of the checkpoints
        int distance = calculateDistance(currentPoint, nextCheckpoint);

        // Stores each navigation instruction as and when they are computed
        String route;

        /* distance != 0 -> Occurs when currentPoint is on a checkpoint
         * dest.getX() == currentPoint.getX() -> If currentPoint is in the same x coordinate. For example,
         * currentPoint(1,10) and nextCheckpoint(1,7)
         * dest.getY() != currentPoint.getY() -> currentPoint must not be in the same column as nextCheckpoint
         */
        if (distance != 0 && dest.getX() == currentPoint.getX() && dest.getY() != currentPoint.getY()) {
            // Add navigation instruction to routeStrings
            routeStrings.add(generateRouteString(currentPoint, nextCheckpoint));
            // Add direction pair to nextDirection
            nextDirection.add(currentPoint.getDirection() + "-" + nextCheckpoint.getDirection());
        }
        // Get position of the next checkpoint from the checkpoint hashmap
        counter = chkPointMap.get(nextCheckpoint.toString());

        // Run this loop until all navigation instructions are generated upto the destination point
        while (true) {
//            currentPoint = checkpointArray[counter];
            // check if destination point is in the same co-ordinate and has the same direction. This is usually
            // the last segment of the route
            if (dest.getY() == currentPoint.getY() && currentPoint.getDirection().contains(dest.getDirection())) {
                distance = calculateDistance(currentPoint, dest);
                if (distance != 0) {
                    nextDirection.add(currentPoint.getDirection() + "-" + getNextCheckpoint(dest).getDirection());
                    route = "Go " + distance + " meters " + dest.getDirection() + "\n";
                    routeStrings.add(route);
                }
                break;
            } else {
                // This block is executed when the next navigation instruction is NOT for the last segment
                // of the route
                distance = calculateDistance(currentPoint, nextCheckpoint);
                if (distance != 0) {
                    nextDirection.add(currentPoint.getDirection() + "-" + nextCheckpoint.getDirection());
                    routeStrings.add(generateRouteString(currentPoint, nextCheckpoint));
                }
                currentPoint = nextCheckpoint;
                nextCheckpoint = checkpointArray[(counter + 1) % 8];
            }
            counter++;
        }
    }


    /**
     * Given the current point, get next checkpoint
     * @param point current point
     * @return next checkpoint
     */
    public Point getNextCheckpoint(Point point) {
        // Initialize nextCheckpoint to a dummy value
        Point nextCheckpoint = new Point(0, 0);
        // If y coordinate is more than 5, the next checkpoint will be in the right half of the parking lot.
        // Else, left half.
        if (point.getY() > 5) {
            nextCheckpoint.setX(1);
            nextCheckpoint.setY(point.getY());
            nextCheckpoint.setDirection("west");
        } else {
            nextCheckpoint.setX(26);
            nextCheckpoint.setY(point.getY());
            nextCheckpoint.setDirection("east");
        }
        return nextCheckpoint;
    }


    /**
     * Given the direction pair, retrieve an array of x or y coordinates
     * @param direction direction-pair
     * @return array of coordinates
     */
    public int[] getPointsArray(String direction) {
        if (direction.equalsIgnoreCase("north-west")) {
            return new int[]{26, 21, 17, 13, 9, 5, 1};
        } else if (direction.equalsIgnoreCase("west-west")) {
            return new int[]{7};
        } else if (direction.equalsIgnoreCase("west-southwest")) {
            return new int[]{4};
        } else if (direction.equalsIgnoreCase("southwest-south")) {
            return new int[]{1};
        } else if (direction.equalsIgnoreCase("south-east")) {
            return new int[]{1, 5, 9, 13, 17, 21, 26};
        } else if (direction.equalsIgnoreCase("east-east")) {
            return new int[]{4};
        } else if (direction.equalsIgnoreCase("east-northeast")) {
            return new int[]{7};
        } else if (direction.equalsIgnoreCase("northeast-north")) {
            return new int[]{10};
        } else if (direction.equalsIgnoreCase("southwest-east")) {
            return new int[]{1, 5, 9, 13, 17, 21, 26};
        } else if (direction.equalsIgnoreCase("northeast-west")) {
            return new int[]{26, 21, 17, 13, 9, 5, 1};
        }

        return null;
    }
}
