package io.smartplanapp.smartclock;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;

public class ClockActivity extends Activity {

    private Chronometer chronometer;
    private ImageView imgButton;
    private boolean timing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clock);

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
