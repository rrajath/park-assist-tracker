package com.uic.ParkAssistTracker.database;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by AMAN on 3/5/14.
 */
public class NavCellDBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_TABLE = " navigation_table";

    public static final String DATABASE_NAV_CREATE =
            "create table " + DATABASE_TABLE + " (" +
                    "nav_cell_id integer," +
                    "fp_id integer not null," +
                    "direction Text," +
                    "x_cord  integer not null, " +
                    "y_cord integer not null," +
                    "FOREIGN KEY(fp_id) REFERENCES fingerprint_table(fp_id));";


    public NavCellDBHelper(Context context) {
        super(context, FingerprintDBHelper.DATABASE_NAME, null, FingerprintDBHelper.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_NAV_CREATE);

    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion,
                          int newVersion) {
        Log.w(FingerprintDBHelper.class.getName(), "Upgrading database from version " + oldVersion
                + " to "
                + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
        onCreate(db);
    }
}

