package io.smartplanapp.smartclock;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.util.List;

import io.smartplanapp.smartclock.storage.Shift;
import io.smartplanapp.smartclock.storage.ShiftsDatabase;
import io.smartplanapp.smartclock.util.ShiftAdapter;

public class ShiftsLogActivity extends Activity {

    private ShiftsDatabase db;
    private SimpleCursorAdapter adapter;
    private ShiftAdapter shiftAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shifts_log);

        db = new ShiftsDatabase(this);
        db.open();

        //Clean all data
        db.deleteAllShifts();
        //Add some data
        db.insertSampleShifts();

        //Generate ListView from SQLite ShiftsDatabase
        displayListView();
    }

    private void displayListView() {

        List<Shift> shifts = db.getAllShiftsList();
        Cursor cursor = db.getAllShifts();

        // The desired columns to be bound
        String[] columns = new String[]{
                ShiftsDatabase.KEY_BEGIN,
                ShiftsDatabase.KEY_END,
        };

        // the XML defined views which the data will be bound to
        int[] views = new int[] { R.id.txt_begin, R.id.txt_end};

        // create the adapter using the cursor pointing to the desired data
        adapter = new SimpleCursorAdapter(this, R.layout.list_item_shift, cursor, columns, views, 0);

        shiftAdapter = new ShiftAdapter(this, shifts);

        ListView listView = (ListView) findViewById(R.id.list_view_log);
        listView.setAdapter(shiftAdapter);
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> listView, View view,
                                    int position, long id) {
                // Get the cursor, positioned to the corresponding row in the result set
                Cursor cursor = (Cursor) listView.getItemAtPosition(position);

                Toast.makeText(getApplicationContext(), cursor.toString(), Toast.LENGTH_SHORT).show();

            }
        });
    }

}
