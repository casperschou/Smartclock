package io.smartplanapp.smartclock;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.android.gms.nearby.messages.Messages;
import com.google.android.gms.nearby.messages.MessagesOptions;
import com.google.android.gms.nearby.messages.NearbyMessagesStatusCodes;
import com.google.android.gms.nearby.messages.NearbyPermissions;
import com.google.android.gms.nearby.messages.Strategy;
import com.google.android.gms.nearby.messages.SubscribeOptions;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    // Constant used for the permission workflow for Nearby
    private static final int FINE_LOCATION_REQUEST_CODE = 123;
    private static final String KEY_SUBSCRIBED = "subscribed";

    private GoogleApiClient googleApiClient;

    // Holds a reference to the View which will display messages from beacons
    private RelativeLayout container;

    /**
     * Tracks subscription state. Set to true when a call to
     * {@link Messages#subscribe(GoogleApiClient, MessageListener)} succeeds.
     */
    private boolean subscribed = false;

    /**
     * Adapter for working with messages from nearby beacons.
     */
    private ArrayAdapter<String> nearbyMessagesArrayAdapter;

    /**
     * Backing data structure for {@code nearbyMessagesArrayAdapter}.
     */
    private List<String> nearbyMessagesList = new ArrayList<>();

    // ---------------------------------------------------------------------------------------
    // ACTIVITY LIFECYCLE METHODS
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState != null) {
            subscribed = savedInstanceState.getBoolean(KEY_SUBSCRIBED, false);
        }

        container = (RelativeLayout) findViewById(R.id.main_activity_container);

        // Request permission to access fine location if not already granted
        if (!havePermission()) {
            Log.i(TAG, "Requesting access to fine location needed for this app.");
            requestPermission();
        }

        final List<String> cachedMessages = Utils.getCachedMessages(this);
        if (cachedMessages != null) {
            nearbyMessagesList.addAll(cachedMessages);
        }

        final ListView nearbyMessagesListView = (ListView) findViewById(R.id.nearby_messages_list_view);
        nearbyMessagesArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, nearbyMessagesList);
        if (nearbyMessagesListView != null) {
            nearbyMessagesListView.setAdapter(nearbyMessagesArrayAdapter);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSharedPreferences(getApplicationContext().getPackageName(), Context.MODE_PRIVATE)
                .registerOnSharedPreferenceChangeListener(this);
        if (havePermission()) {
            buildGoogleApiClient();
        }
    }

    @Override
    protected void onPause() {
        getSharedPreferences(getApplicationContext().getPackageName(), Context.MODE_PRIVATE)
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_SUBSCRIBED, subscribed);
    }
    // ---------------------------------------------------------------------------------------
    // CALLBACK METHOD FROM LISTENER INTERFACE
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (TextUtils.equals(key, Utils.KEY_CACHED_MESSAGES)) {
            nearbyMessagesList.clear();
            nearbyMessagesList.addAll(Utils.getCachedMessages(this));
            nearbyMessagesArrayAdapter.notifyDataSetChanged();
        }
    }

    // ---------------------------------------------------------------------------------------
    // GOOGLE API CLIENT BUILDER AND CONNECTION CALLBACKS
    private synchronized void buildGoogleApiClient() {
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
        Log.i(TAG, "GoogleApiClient connected");
        subscribe();
    }

    //  Logs the error code for the suspended GoogleApiClient connection
    @Override
    public void onConnectionSuspended(int i) {
        Log.w(TAG, "Connection suspended. Error code: " + i);
    }

    // Displays a Snackbar notifying the user that the connection to GoogleApiClient has failed
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (container != null) {
            Snackbar.make(container, "Exception while connecting to Google Play services: " +
                            connectionResult.getErrorMessage(),
                    Snackbar.LENGTH_INDEFINITE).show();
        }
    }
    // ---------------------------------------------------------------------------------------
    // METHODS USED IN THE LOCATION PERMISSION WORKFLOW
    // Checks whether the app has permission to access fine location
    private boolean havePermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    // Requests permission to access fine location
    private void requestPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_REQUEST_CODE);
    }

    // Displays a Snackbar giving the user a chance to re-initiate the permission workflow
    private void showRequestPermissionSnackbar() {
        if (container != null) {
            Snackbar.make(container, R.string.permission_needed,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.understood, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            requestPermission();
                        }
                    }).show();
        }
    }

    // Displays a Snackbar with a link to Settings if permission was not granted
    private void showLinkToSettingsSnackbar() {
        if (container == null) {
            return;
        }
        Snackbar.make(container,
                R.string.permission_settings,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.settings, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Build intent that displays the App settings screen.
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
        if (requestCode == FINE_LOCATION_REQUEST_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    if (shouldShowRequestPermissionRationale(permission)) {
                        // Access fine location permission denied
                        showRequestPermissionSnackbar();
                    } else {
                        // Access fine location permission denied, selected >Never ask again<
                        showLinkToSettingsSnackbar();
                    }
                } else {
                    // Permission granted
                    buildGoogleApiClient();
                }
            }
        }
    }

    // ---------------------------------------------------------------------------------------

    /**
     * Calls {@link Messages#subscribe(GoogleApiClient, MessageListener, SubscribeOptions)},
     * using a {@link Strategy} for BLE scanning. Attaches a {@link ResultCallback} to monitor
     * whether the call to {@code subscribe()} succeeded or failed.
     */
    private void subscribe() {
        // In this sample, we subscribe when the activity is launched, but not on device orientation
        // change.
        if (subscribed) {
            Log.i(TAG, "Already subscribed.");
            return;
        }

        SubscribeOptions options = new SubscribeOptions.Builder()
                .setStrategy(Strategy.BLE_ONLY)
                .build();

        Nearby.Messages.subscribe(googleApiClient, getPendingIntent(), options)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if (status.isSuccess()) {
                            Log.i(TAG, "Subscribed successfully.");
                            startService(getBackgroundSubscribeServiceIntent());
                        } else {
                            Log.e(TAG, "Operation failed. Error: " +
                                    NearbyMessagesStatusCodes.getStatusCodeString(
                                            status.getStatusCode()));
                        }
                    }
                });
    }

    private PendingIntent getPendingIntent() {
        return PendingIntent.getService(this, 0,
                getBackgroundSubscribeServiceIntent(), PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private Intent getBackgroundSubscribeServiceIntent() {
        return new Intent(this, BackgroundSubscribeIntentService.class);
    }

}
