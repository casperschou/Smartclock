package io.smartplanapp.smartclock;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.Toast;

public class ClockActivity extends AppCompatActivity {

    private Chronometer chronometer;
    private ImageView imgButton;
    private boolean timing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clock);

        // TODO: Få det her til at virke
        String location = getIntent().getExtras().getString(MainActivity.EXTRA_LOCATION, "Nothing found");

        Toast.makeText(this, "location", Toast.LENGTH_LONG).show();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        toolbar.setTitle(location);
        setSupportActionBar(toolbar);

        chronometer = (Chronometer) findViewById(R.id.chronometer);

        imgButton = (ImageView) findViewById(R.id.imgButton);
        imgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleClock();
            }
        });
    }

    private void toggleClock() {
        if (!timing) {
            imgButton.setImageResource(R.drawable.button_down);
            chronometer.start();
        } else {
            imgButton.setImageResource(R.drawable.button_up);
            chronometer.stop();
        }
        timing = !timing;
    }

}
