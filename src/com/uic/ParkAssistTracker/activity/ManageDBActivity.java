package com.uic.ParkAssistTracker.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import com.uic.ParkAssistTracker.R;
import com.uic.ParkAssistTracker.database.Datasource;
import com.uic.ParkAssistTracker.entity.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Created by AMAN on 4/9/14.
 */
public class ManageDBActivity extends Activity {
    Datasource datasource;
    String table;
    Spinner spinner;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.managedb);
        spinner = (Spinner) findViewById(R.id.spinner_tables);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.tables, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

    }
    public void clearSequences(View view) {
        table = spinner.getSelectedItem().toString();
        datasource = new Datasource(this, table);
        datasource.open();
        datasource.deleteSequence(table);

        Toast.makeText(getApplicationContext(), table + " sequence deleted", Toast.LENGTH_LONG).show();
        datasource.close();
    }


    //Read the data.This method is called after clicking ImportDb button
    public void readData(View view){
        int count =0;
      table = spinner.getSelectedItem().toString();
      if(table.equals("fingerprint_table")){
          File fpFile = new File("/storage/sdcard0/Android/data/com.example.ParkAssist/files/fingerprint_table.txt");
          try{
              Scanner scan = new Scanner(fpFile);
              datasource = new Datasource(this , table);
              datasource.open();
              while(scan.hasNextLine()){
                  FingerprintInsert(scan.nextLine(), datasource);
                  count++;
              }
              Toast.makeText(getApplicationContext(), count + " records are inserted", Toast.LENGTH_LONG).show();
              count =0;
              scan.close();
              datasource.close();
          }
          catch(FileNotFoundException ex){
            ex.printStackTrace();

          }
      }
      else{

          File NavFile = new File("/storage/sdcard0/Android/data/com.example.ParkAssist/files/navigation_table.txt");
          try{
              Scanner scan = new Scanner(NavFile);
              datasource = new Datasource(this , table);
              datasource.open();
              while(scan.hasNextLine()){
                  NavCellDataInsert(scan.nextLine(), datasource);
                  count++;
              }
              Toast.makeText(getApplicationContext(), count + " records are inserted", Toast.LENGTH_LONG).show();
              count =0;
              scan.close();
              datasource.close();
          }
          catch(FileNotFoundException ex){
              ex.printStackTrace();

          }

      }

    }


    public void clearTables(View view) {
        table = spinner.getSelectedItem().toString();
        datasource = new Datasource(this, table);
        datasource.open();
        datasource.deleteTable(table);

        Toast.makeText(getApplicationContext(), table + " deleted", Toast.LENGTH_LONG).show();
        datasource.close();
    }
    public void refreshDB(View view) {
        table = spinner.getSelectedItem().toString();
        datasource = new Datasource(this, table);
        datasource.open();
        datasource.refreshDB();
        Toast.makeText(getApplicationContext(), table + " re-created", Toast.LENGTH_LONG).show();
        datasource.close();
    }
    public void viewDB(View view) {
        table = spinner.getSelectedItem().toString();
        Intent intent = new Intent(ManageDBActivity.this, ViewDBActivity.class);
        intent.putExtra("table", table);
        startActivity(intent);
    }


   //Read the row from Fingerprint text file .
   public void FingerprintInsert(String row ,Datasource datasource){
       String[] elements = row.split(",");
       Fingerprint fp = new Fingerprint();
       fp.setFpId(Integer.parseInt(elements[0]));
       fp.setBssid(elements[1]);
       fp.setSsid(elements[2]);
       fp.setRss(Integer.parseInt(elements[3]));
       datasource.insertFingerprint(fp);
   }
   ////Read the row from Navigation text file
   public void NavCellDataInsert(String row , Datasource datasource){
       table = "navigation_table";
       String[] elements = row.split(",");
       NavCell navCell = new NavCell();
       navCell.setNavCellId(Integer.parseInt(elements[0]));
       navCell.setFpId(Integer.parseInt(elements[1]));
       navCell.setDirection(elements[2]);
       navCell.setXCord(Integer.parseInt(elements[3]));
       navCell.setYCord(Integer.parseInt(elements[4]));
       datasource.insertNavCell(navCell);
   }
}