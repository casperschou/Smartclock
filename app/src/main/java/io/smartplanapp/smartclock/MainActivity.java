package io.smartplanapp.smartclock;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.android.gms.nearby.messages.MessagesOptions;
import com.google.android.gms.nearby.messages.NearbyPermissions;
import com.google.android.gms.nearby.messages.Strategy;
import com.google.android.gms.nearby.messages.SubscribeOptions;

import java.util.ArrayList;
import java.util.List;

import io.smartplanapp.smartclock.util.LocationAdapter;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public static final String EXTRA_LOCATION = "location";

    // GUI elements
    private LinearLayout baseLayout;
    private ImageView imgStatus;
    private TextView apiStatus;
    private TextView messagesHeader;

    // Nearby Messages API
    private static final int REQUEST_CODE_FINE_LOCATION = 123;
    private GoogleApiClient googleApiClient;
    private MessageListener messageListener;
    private List<String> messages = new ArrayList<>();
    private LocationAdapter adapter;
    private boolean subscribing;

    // --------------------------------------------------------------------------------------------
    // ACTIVITY LIFECYCLE METHODS

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize GUI elements
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        baseLayout = (LinearLayout) findViewById(R.id.list_view_container);
        imgStatus = (ImageView)findViewById(R.id.img_status);
        apiStatus = (TextView) findViewById(R.id.txt_api_status);
        messagesHeader = (TextView) findViewById(R.id.txt_msg_header);

        // ArrayAdapter and ListView for Nearby Messages
        adapter = new LocationAdapter(this, messages);
        final ListView messagesListView = (ListView) findViewById(R.id.list_view_messages);
        messagesListView.setAdapter(adapter);

        messagesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> listView, View view,
                                    int position, long id) {
                Intent intent = new Intent(MainActivity.this, ClockActivity.class);
                intent.putExtra(EXTRA_LOCATION, listView.getItemAtPosition(position).toString());
                startActivity(intent);
                Snackbar.make(baseLayout, listView.getItemAtPosition(position).toString(),
                        Snackbar.LENGTH_INDEFINITE).show();
            }
        });

        // Permission to access fine location
        if (!havePermission()) {
            apiStatus.setText(R.string.permission_missing);
            requestPermission();
        }

        initMessageListener();
    }

    // Reset list of message results, so locations can not be used outside of range
    @Override
    protected void onStart() {
        super.onStart();
        messages.clear();
        adapter.notifyDataSetChanged();
        messagesHeader.setText(R.string.found_zero);
    }

    // Check if Bluetooth or fine location access have been disabled
    @Override
    protected void onResume() {
        super.onResume();
        if (!isBluetoothAvailable()) {
            apiStatus.setText(getString(R.string.error_bluetooth));
            messagesHeader.setText(R.string.error_bluetooth_rationale);
            imgStatus.setImageResource(R.drawable.ic_bluetooth_disabled_white_36dp);
        } else {
            imgStatus.setImageResource(R.drawable.ic_nearby);
            if (havePermission()) {
                buildGoogleApiClient();
            }
        }
    }



    // Unsubscribe with every pause, since active foreground scanning is battery consuming
    @Override
    protected void onPause() {
        if (subscribing) {
            unsubscribe();
        }
        googleApiClient = null;
        super.onPause();
    }

    // --------------------------------------------------------------------------------------------
    // MESSAGE LISTENER FOR NEARBY MESSAGES

    private void initMessageListener() {
        messageListener = new MessageListener() {

            @Override
            public void onFound(Message message) {
                String msgContent = new String(message.getContent());
                messages.add(msgContent);
                messages.add("Test 1");
                messages.add("Test 2");
                messages.add("Test 3");
                messages.add("Test 4");
                messages.add("Test 5");
                adapter.notifyDataSetChanged();
                updateMessagesHeader();
            }

            @Override
            public void onLost(Message message) {
                String msgContent = new String(message.getContent());
                messages.remove(msgContent);
                adapter.notifyDataSetChanged();
                updateMessagesHeader();
                Snackbar.make(baseLayout, getString(R.string.scan_result_lost) + msgContent,
                        Snackbar.LENGTH_LONG).show();
            }
        };
    }

    // --------------------------------------------------------------------------------------------
    // GOOGLE API CLIENT BUILDER AND CONNECTION CALLBACKS

    private synchronized void buildGoogleApiClient() {
        apiStatus.setText(R.string.connecting);
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Nearby.MESSAGES_API, new MessagesOptions.Builder()
                            .setPermissions(NearbyPermissions.BLE).build())
                    .addConnectionCallbacks(this)
                    .enableAutoManage(this, this)
                    .build();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        apiStatus.setText(R.string.connected);
        subscribe();
    }

    @Override
    public void onConnectionSuspended(int i) {
        apiStatus.setText(R.string.connection_suspended);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        apiStatus.setText(R.string.connection_failed);
        if (connectionResult.getErrorMessage() != null) {
            Snackbar.make(baseLayout, getString(R.string.error) +
                            connectionResult.getErrorMessage(),
                    Snackbar.LENGTH_INDEFINITE).show();
        }
    }

    // --------------------------------------------------------------------------------------------
    // NEARBY MESSAGES SUBSCRIPTION

    private void subscribe() {
        // Subscribe to Bluetooh Low Energy only
        SubscribeOptions options = new SubscribeOptions.Builder()
                .setStrategy(Strategy.BLE_ONLY)
                .build();

        Nearby.Messages.subscribe(googleApiClient, messageListener, options)
                // ResultCallback checks if the subscription succeeds
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        subscribing = status.isSuccess();
                        if (subscribing) {
                            apiStatus.setText(R.string.scanning);
                            displayNearbyIcon();
                        } else {
                            apiStatus.setText(R.string.scan_error);
                            if (status.getStatusCode() == 7) {
                                messagesHeader.setText(R.string.error_network_rationale);
                                imgStatus.setImageResource(R.drawable.ic_signal_wifi_off_white_36dp);
                            }
                        }
                    }
                });
    }

    private void unsubscribe() {
        Nearby.Messages.unsubscribe(googleApiClient, messageListener);
        subscribing = false;
        imgStatus.setVisibility(View.INVISIBLE);
    }

    // --------------------------------------------------------------------------------------------
    // FINE LOCATION PERMISSION WORKFLOW

    // Checks whether the app has permission to access fine location
    private boolean havePermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    // Requests permission to access fine location
    private void requestPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_FINE_LOCATION);
    }

    // Displays a Snackbar giving the user a chance to re-initiate the permission workflow
    private void showRequestPermissionSnackbar() {
        if (baseLayout != null) {
            Snackbar.make(baseLayout, R.string.permission_needed, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.understood, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            requestPermission();
                        }
                    }).show();
        }
    }

    // Displays a Snackbar with a link to Settings if permission was denied
    private void showLinkToSettingsSnackbar() {
        Snackbar.make(baseLayout, R.string.permission_settings, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.settings, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package",
                                BuildConfig.APPLICATION_ID, null);
                        intent.setData(uri);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                }).show();
    }

    // Handles the permission workflow in response to user interaction
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_FINE_LOCATION) {
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    if (shouldShowRequestPermissionRationale(permission)) {
                        // Access to fine location denied
                        showRequestPermissionSnackbar();
                    } else {
                        // Access to fine location denied + Never ask again
                        showLinkToSettingsSnackbar();
                    }
                } else {
                    // Permission granted
                    buildGoogleApiClient();
                }
            }
        }
    }

    // --------------------------------------------------------------------------------------------
    // UTILITY METHODS

    private void displayNearbyIcon() {
        Animation blinkAnimation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.blink_anim);
        imgStatus.setVisibility(View.VISIBLE);
        imgStatus.startAnimation(blinkAnimation);
    }

    private void updateMessagesHeader() {
        switch (messages.size()) {
            case 0:
                messagesHeader.setText(getResources().getString(R.string.found_zero));
                return;
            case 1:
                messagesHeader.setText(getResources().getString(R.string.found_one));
                return;
            default:
                messagesHeader.setText(getResources().getString(R.string.found_more,
                        messages.size()));
        }
    }

    private static boolean isBluetoothAvailable() {
        final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return (bluetoothAdapter != null && bluetoothAdapter.isEnabled());
    }

}
