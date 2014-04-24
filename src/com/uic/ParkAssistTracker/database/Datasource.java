package com.uic.ParkAssistTracker.database;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;
import com.uic.ParkAssistTracker.entity.Cell;
import com.uic.ParkAssistTracker.entity.Fingerprint;
import com.uic.ParkAssistTracker.entity.NavCell;
import com.uic.ParkAssistTracker.entity.ParkCell;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by AMAN
 */
public class Datasource {
    private SQLiteOpenHelper DbHelper;
    private SQLiteDatabase db;

    public static final String KEY_FP_ROWID = "fp_id";              // Fingerprint ID
    public static final String KEY_BSSID = "bssid";                 // BSSID of the access point
    public static final String KEY_SSID = "ssid";                   // SSID of the access point
    public static final String KEY_RSS = "rss";                     // Signal Strength value
    public static final String KEY_MAX = "max";
    public static final String KEY_MIN = "min";


    public static final String KEY_PARK_ROWID = "park_cell_id";     // Parking Cell id
    public static final String KEY_NAV_ROWID = "nav_cell_id";       // Navigation Cell id
    public static final String KEY_DIR = "direction";               // Nav path direction
    public static final String KEY_X_CORD = "x_cord";               // X Coordinate
    public static final String KEY_Y_CORD = "y_cord";               // Y Coordinate
    public String rowId = "";

    String tableName = "";
    public String[] columns;

    public Datasource(Context ctx, String tableName) {

        if (tableName.equals("navigation_table")) {
            DbHelper = new NavCellDBHelper(ctx);
            this.tableName = "navigation_table";
            this.columns = new String[]{KEY_NAV_ROWID, KEY_FP_ROWID, KEY_DIR, KEY_X_CORD, KEY_Y_CORD};
            this.rowId = KEY_NAV_ROWID;
        }
/*
        if (tableName.equals("parking_table")) {
            DbHelper = new ParkCellDBHelper(ctx);
            this.tableName = "parking_table";
            this.columns = new String[]{KEY_PARK_ROWID, KEY_NAV_ROWID, KEY_X_CORD, KEY_Y_CORD};
            this.rowId = KEY_PARK_ROWID;
        }*/


        if (tableName.equals("fingerprint_table")) {
            DbHelper = new FingerprintDBHelper(ctx);
            this.tableName = "fingerprint_table";
            this.columns = new String[]{KEY_FP_ROWID, KEY_BSSID, KEY_SSID, KEY_RSS,KEY_MAX, KEY_MIN};
            this.rowId = KEY_FP_ROWID;
        }
    }

    /*
     * Opens database connection
     */
    public void open() throws SQLException
    {
        db = DbHelper.getWritableDatabase();
    }

    /*
     * Closes database connection
     */
    public void close()
    {
        DbHelper.close();
    }

    /*
     * Deletes all rows from the table
     */
    public void deleteSequence(String tableName)
    {
        db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME='" + tableName + "'");
    }

    public boolean deleteTable(String tableName) {
        return db.delete(tableName, null, null) > 0;
    }

    public List<Fingerprint> getAllFingerprints() //Retrieve All the rows
    {
        List<Fingerprint> fingerprints = new ArrayList<Fingerprint>();
        Cursor cursor = db.query(this.tableName, this.columns, null, null, null, null, "ssid");  //  Please take a look

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Fingerprint fingerprint = cursorToFingerprint(cursor);
            fingerprints.add(fingerprint);
            cursor.moveToNext();
        }

        cursor.close();
        return fingerprints;
    }

    public List getAllRows() {
        List objects = new ArrayList();
        Cursor cursor = db.query(this.tableName, this.columns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            if (tableName.equals("fingerprint_table")) {
                Fingerprint fingerprint = cursorToFingerprint(cursor);
                objects.add(fingerprint);
                cursor.moveToNext();
            }/* else if (tableName.equals("parking_table")) {
                ParkCell parkCell = cursorToParkCell(cursor);
                objects.add(parkCell);
                cursor.moveToNext();
            }*/ else {
                NavCell navCell = cursorToNavCell(cursor);
                objects.add(navCell);
                cursor.moveToNext();
            }

        }
        cursor.close();
        return objects;
    }

    //---retrieves a particular row---
    public Cursor getRss(long rowId) throws SQLException {
        Cursor mCursor =
                db.query(true, this.tableName,
                        this.columns,
                        this.rowId + "=" + rowId,
                        null,
                        null,
                        null,
                        null,
                        null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }

        return mCursor;
    }

    public void refreshDB() {
        DbHelper.onUpgrade(db, 1, 2);
    }

    public long insertFingerprint(Fingerprint fp) {        //Insert into Fingerprint Table
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_RSS, fp.getRss());
        initialValues.put(KEY_SSID, fp.getSsid());
        initialValues.put(KEY_BSSID, fp.getBssid());
        initialValues.put(KEY_MAX,fp.getMax());
        initialValues.put(KEY_MIN,fp.getMin());

        return db.insert(this.tableName, null, initialValues);
    }

    // Insert into ParkCell Table
    public long insertParkCell(ParkCell parkCell) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_NAV_ROWID, parkCell.getNavCellId());  // Navigation id "foreign key"
        initialValues.put(KEY_X_CORD, parkCell.getXCord());
        initialValues.put(KEY_Y_CORD, parkCell.getYCord());

        return db.insert(tableName, null, initialValues);
    }

    // Insert into NavCell Table
    public long insertNavCell(NavCell navCell) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_NAV_ROWID, navCell.getNavCellId());
        initialValues.put(KEY_FP_ROWID, navCell.getFpId());     //fp id " foreign key"
        initialValues.put(KEY_DIR, navCell.getDirection());
        initialValues.put(KEY_X_CORD, navCell.getXCord());
        initialValues.put(KEY_Y_CORD, navCell.getYCord());
        return db.insert(tableName, null, initialValues);
    }

    public int updateParkCell(int x, int y, int navCellId) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("nav_cell_id", navCellId);
        String whereClause = KEY_X_CORD + " = " + x + " AND " + KEY_Y_CORD + " = " + y;
        return db.update(tableName, contentValues, whereClause, null);
    }

    public Cell getCell(int x, int y) {
        String whereClause = " X_CORD = " + x + " AND Y_CORD = " + y;
        Cursor cursor = db.query(this.tableName, this.columns, whereClause, null, null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();
        }

        Cell cell;
        //if (tableName.equals("navigation_table")) {
            cell = (NavCell)cursorToNavCell(cursor);
        //}
        /*else {
            cell = (NavCell)cursorToNavCell(cursor);
        }*/

        return cell;
    }

    public int getMaxNavCellId() {
        Cursor cursor = db.rawQuery("SELECT MAX(NAV_CELL_ID) FROM NAVIGATION_TABLE", null);

        cursor.moveToFirst();

        return cursor.getInt(0);
    }

    private Fingerprint cursorToFingerprint(Cursor cursor) {
        Fingerprint fingerprint = new Fingerprint();
        fingerprint.setFpId(cursor.getInt(0));
        fingerprint.setBssid(cursor.getString(1));
        fingerprint.setSsid(cursor.getString(2));
        fingerprint.setRss(cursor.getInt(3));
        fingerprint.setMax(cursor.getInt(4));
        fingerprint.setMin(cursor.getInt(5));

        return fingerprint;
    }

    /*private ParkCell cursorToParkCell(Cursor cursor) {
        ParkCell parkCell = new ParkCell();
        parkCell.setParkCellId(cursor.getInt(0));
        parkCell.setNavCellId(cursor.getInt(1));
        parkCell.setXCord(cursor.getInt(2));
        parkCell.setYCord(cursor.getInt(3));

        return parkCell;
    }*/

    private NavCell cursorToNavCell(Cursor cursor) {
        if (cursor.getCount() <= 0) {
            return null;
        }
        NavCell navCell = new NavCell();
        navCell.setNavCellId(cursor.getInt(0));
        navCell.setFpId(cursor.getInt(1));
        navCell.setDirection(cursor.getString(2));
        navCell.setXCord(cursor.getInt(3));
        navCell.setYCord(cursor.getInt(4));

        return navCell;
    }

    public String getCurrentCoordinates(String bssid, int rss) {
        String query = "select x_cord, y_cord from navigation_table " +
                "where fp_id = (select f.fp_id from fingerprint_table f " +
                "where f.bssid = '" + bssid + "' order by abs(" + rss + " - rss) limit 1)";
         String s1=    "select x_cord, y_cord from navigation_table where fp_id in (select f.fp_id from fingerprint_table f" +
                 " where f.rss >= " + (rss-3) + " and  f.rss <= "+(rss+3)+"  ) group by x_cord , y_cord limit 1";

        Cursor cursor = null;
       /* try {
            cursor = db.rawQuery(query, null);

            if (cursor.getCount()>0){
            cursor.moveToFirst();}
        } catch (CursorIndexOutOfBoundsException e) {
            Log.e("Datasource", e.getMessage());
        }

        if (cursor.getCount()>0) {
            return String.valueOf(cursor.getInt(0)) + "," + String.valueOf(cursor.getInt(1));
        } else {
            return null;
        }*/

      cursor = db.rawQuery(query,null);


          int cursorCount = cursor.getCount();

          if( cursorCount>0){

            cursor.moveToFirst();
            return String.valueOf(cursor.getInt(0)) + "," + String.valueOf(cursor.getInt(1));
         }

           return null;



    }

}
