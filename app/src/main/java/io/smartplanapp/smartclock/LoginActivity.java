package io.smartplanapp.smartclock;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import static android.os.Build.VERSION_CODES.M;
import static io.smartplanapp.smartclock.R.id.btnLogin;

public class LoginActivity extends AppCompatActivity {

    private EditText txtUsername;
    private EditText txtPassword;
    private RelativeLayout container;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        txtUsername = (EditText) findViewById(R.id.txtUsername);
        txtPassword = (EditText) findViewById(R.id.txtPassword);
        container = (RelativeLayout) findViewById(R.id.login_activity_container);
        Button btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = txtUsername.getText().toString();
                String password = txtPassword.getText().toString();
                if (username.equals("admin") && password.equals("admin")) {
                    Intent intent = new Intent(LoginActivity.this, ClockActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.wrong_credentials,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
