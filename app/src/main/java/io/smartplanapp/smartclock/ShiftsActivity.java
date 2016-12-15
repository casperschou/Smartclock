package io.smartplanapp.smartclock;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import io.smartplanapp.smartclock.storage.Database;
import io.smartplanapp.smartclock.storage.Shift;
import io.smartplanapp.smartclock.util.ShiftAdapter;

public class ShiftsActivity extends AppCompatActivity {

    List<Shift> shifts;
    TextView txtLogHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shifts);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_shifts);
        setSupportActionBar(toolbar);

        txtLogHeader = (TextView) findViewById(R.id.txt_log_header);

        Database db = new Database(this);
        db.open();
////      Sample data
//        db.createShift("1475819640000", "1475848860000", "Erhvervsakademi Aarhus");
//        db.createShift("1477036680000", "1477071300000", "Erhvervsakademi Aarhus");
//        db.createShift("1477593900000", "1477610040000", "Chido");
//        db.createShift("1478607660000", "1478638740000", "Chido");
//        db.createShift("1478759400000", "1478798820000", "Erhvervsakademi Aarhus");

        shifts = db.getAllShifts();
        ShiftAdapter shiftAdapter = new ShiftAdapter(this, shifts);

        // Populate ListView with shifts from adapter
        ListView listView = (ListView) findViewById(R.id.list_view_shifts);
        listView.setAdapter(shiftAdapter);
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> listView, View view, int position, long id) {
                Toast.makeText(getApplicationContext(),
                        listView.getItemAtPosition(position).toString(), Toast.LENGTH_LONG).show();
            }
        });

        updateLogHeader();
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    private void updateLogHeader() {
        switch (shifts.size()) {
            case 0:
                txtLogHeader.setText(getResources().getString(R.string.found_zero_shifts));
                return;
            case 1:
                txtLogHeader.setText(getResources().getString(R.string.found_one_shift));
                return;
            default:
                txtLogHeader.setText(getResources().getString(R.string.found_more_shifts,
                        shifts.size()));
        }
    }
}
