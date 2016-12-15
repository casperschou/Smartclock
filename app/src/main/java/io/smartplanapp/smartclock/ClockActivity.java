package io.smartplanapp.smartclock;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import io.smartplanapp.smartclock.storage.Database;

public class ClockActivity extends AppCompatActivity {

    private static final String KEY_STATE = "state";
    private int state; // 0 = Not yet punched in   1 = Punched in   2 = Punched out
    private Long timeBegin;
    private Long timeEnd;
    private String location;
    private LinearLayout container;
    private Button btnPunch;
    private TextView txtTimeBegin;
    private TextView txtTimeEnd;
    private TextView txtTotalTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clock);

        // Check whether we're recreating a previously destroyed instance
        if (savedInstanceState != null) {
            // Restore value of members from saved state
            state = savedInstanceState.getInt(KEY_STATE);
        } else {
            location = getIntent().getExtras().getString(LocationActivity.EXTRA_LOCATION);
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_clock);
            toolbar.setTitle(location);
            setSupportActionBar(toolbar);

            container = (LinearLayout) findViewById(R.id.clock_container);
            txtTimeBegin = (TextView) findViewById(R.id.txt_time_in);
            txtTimeEnd = (TextView) findViewById(R.id.txt_time_out);
            txtTotalTime = (TextView) findViewById(R.id.txt_total_time);
            btnPunch = (Button) findViewById(R.id.btn_punch);
            btnPunch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    togglePunch();
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (state == 0) {
            punchIn();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(KEY_STATE, state);
        super.onSaveInstanceState(outState);
    }

    private void punchIn() {
        timeBegin = System.currentTimeMillis();
        txtTimeBegin.setText(millisToTimestamp(timeBegin));
        state++;
    }

    private void togglePunch() {
        switch (state) {
            case 1:
                punchOut();
                return;
            default:
                // Go to shifts activity
                btnPunch.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
                btnPunch.setText("");
                btnPunch.setEnabled(false);
                Intent intent = new Intent(ClockActivity.this, ShiftsActivity.class);
                startActivity(intent);
                finish();
        }
    }

    private void punchOut() {
        timeEnd = System.currentTimeMillis();
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        // TODO: Check if is on location
                        txtTimeEnd.setText(millisToTimestamp(timeEnd));
                        btnPunch.setText(R.string.previous_shifts);
                        btnPunch.setBackgroundColor(ContextCompat.getColor(ClockActivity.this, R.color.colorPrimary));
                        // Display total work time
                        Long timeTotal = timeEnd - timeBegin;
                        txtTotalTime.setText(millisToHoursAndMinutes(ClockActivity.this, timeTotal));
                        state++;
                        writeToDb(timeBegin.toString(), timeEnd.toString(), location);
                        Snackbar.make(container, R.string.saved, Snackbar.LENGTH_LONG).show();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Vil du stemple ud her?\n" + location + "\nTid: " + millisToTimestamp(timeEnd))
                .setPositiveButton("Ja", dialogClickListener)
                .setNegativeButton("Annuller", dialogClickListener)
                .show();
    }

    private void writeToDb(String begin, String end, String location) {
        Database db = new Database(this);
        db.open();
        db.createShift(begin, end, location);
    }

    private String millisToTimestamp(Long millis) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        return simpleDateFormat.format(new Date(millis));
    }

    private String millisToHoursAndMinutes(Context ctx, Long millis) {
        Context context = ctx;
        return String.format(context.getString(R.string.hours_and_minutes),
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)));
    }

}
