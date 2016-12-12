package io.smartplanapp.smartclock;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public class ClockActivity extends AppCompatActivity {

    private int toggleStatus; // 0 = Time not ticking   1 = Punched in   2 = Punched out
    private Long timeBegin;
    private Long timeEnd;
    private Long timeTotal;
    private Button btnPunch;
    private TextView txtTimeBegin;
    private TextView txtTimeEnd;
    private TextView txtTotalTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clock);

        String location = getIntent().getExtras().getString(MainActivity.EXTRA_LOCATION);
        getSupportActionBar().setTitle(location);

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

    private void togglePunch() {
        switch (toggleStatus) {
            case 0:
                timeBegin = System.currentTimeMillis();
                txtTimeBegin.setText(millisToTimestamp(timeBegin));
                btnPunch.setText(R.string.punch_out);
                btnPunch.setBackgroundColor(ContextCompat.getColor(this, R.color.colorButtonOut));
                txtTimeBegin.setVisibility(View.VISIBLE);
                toggleStatus++;
                return;
            case 1:
                timeEnd = System.currentTimeMillis();
                txtTimeEnd.setText(millisToTimestamp(timeEnd));
                btnPunch.setText(R.string.exit);
                btnPunch.setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent));
                txtTimeEnd.setVisibility(View.VISIBLE);
                // Calculate total work time
                timeTotal = timeEnd-timeBegin;
                txtTotalTime.setText(millisToHoursAndMinutes(timeTotal));
                txtTotalTime.setVisibility(View.VISIBLE);
                toggleStatus++;
                return;
            default:
                btnPunch.setBackgroundColor(ContextCompat.getColor(this, R.color.colorBackground));
                btnPunch.setEnabled(false);
                finish();
        }
    }


    private String millisToTimestamp(Long millis) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", getLocale());
        return simpleDateFormat.format(new Date(millis));
    }

    private String millisToHoursAndMinutes(Long millis) {
        return String.format(getResources().getString(R.string.hours_and_minutes),
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)));
    }

    private Locale getLocale(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            return getResources().getConfiguration().getLocales().get(0);
        } else{
            return getResources().getConfiguration().locale;
        }
    }

}
