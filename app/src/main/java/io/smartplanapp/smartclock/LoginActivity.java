package io.smartplanapp.smartclock;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

public class LoginActivity extends AppCompatActivity {

    private EditText txtUsername;
    private EditText txtPassword;
    private LinearLayout container;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        txtUsername = (EditText) findViewById(R.id.txtUsername);
        txtPassword = (EditText) findViewById(R.id.txtPassword);
        container = (LinearLayout) findViewById(R.id.login_activity_container);
    }

    public void login(View view){
        if (txtUsername.getText().toString().equals(getString(R.string.admin)) &&
                txtPassword.getText().equals(R.string.admin)) {
            // Correct
            Intent intent = new Intent(this, ClockActivity.class);
            startActivity(intent);
        } else {
            Snackbar.make(container, R.string.wrong_credentials,
                    Snackbar.LENGTH_LONG).show();
        }
    }

}
