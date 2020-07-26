package com.example.testchat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {

    private static final int REQUEST_CODE_REQUIRED_PERMISSIONS = 1;
    private ConnectionsClient client;
    private Spinner mTypeSpinner;
    private TextView mStatusText;
    private Button mConnectionButton;
    private Button mSendButton;
    private ListView mListView;
    private ViewGroup mSendTextContainer;
    private EditText mSendEditText;

    private ArrayAdapter<String> mMessageAdapter;

    private boolean mIsHost;
    private boolean mIsConnected;

    private String mRemoteHostEndpoint;
    private List<String> mRemotePeerEndpoints = new ArrayList<String>();

    private static final long CONNECTION_TIME_OUT = 10000L;

    private static final String[] REQUIRED_PERMISSIONS =
            new String[] {
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
            };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        client = Nearby.getConnectionsClient(getApplicationContext());

        initViews();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!hasPermissions(this, REQUIRED_PERMISSIONS)) {
            if (!hasPermissions(this, REQUIRED_PERMISSIONS)) {
                if (Build.VERSION.SDK_INT < 23) {
                    ActivityCompat.requestPermissions(
                            this, REQUIRED_PERMISSIONS, REQUEST_CODE_REQUIRED_PERMISSIONS);
                } else {
                    requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_REQUIRED_PERMISSIONS);
                }
            }
        }
    }

    @Override
    public void onClick(View view) {
       switch (view.getId()) {
           case R.id.button_connection: {
               if( mIsConnected ) {
                   //disconnect();
                   mStatusText.setText("Disconnected");
               }  else if( getString( R.string.connection_type_host ).equalsIgnoreCase( mTypeSpinner.getSelectedItem().toString() ) ) {
                   mIsHost = true;
                   advertise();
               }  else {
                   mIsHost = false;
                   discover();
               }
               break;
           }
       }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void initViews() {
        mStatusText = findViewById( R.id.text_status );
        mConnectionButton = findViewById( R.id.button_connection );
        mSendButton = findViewById( R.id.button_send );
        mListView = findViewById( R.id.list );
        mSendTextContainer = findViewById( R.id.send_text_container );
        mSendEditText = findViewById( R.id.edit_text_send );
        mTypeSpinner = findViewById( R.id.spinner_type );

        setupButtons();
        setupConnectionTypeSpinner();
        setupMessageList();
    }

    private void setupButtons() {
        mConnectionButton.setOnClickListener(this);
        mSendButton.setOnClickListener(this);
    }

    private void setupConnectionTypeSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.connection_types,
                android.R.layout.simple_spinner_item );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mTypeSpinner.setAdapter(adapter);
    }

    private void setupMessageList() {
        mMessageAdapter = new ArrayAdapter<String>( this, android.R.layout.simple_list_item_1 );
        mListView.setAdapter( mMessageAdapter );
    }

    /** Callbacks for payloads (bytes of data) sent from another device to us. */
    private final PayloadCallback mPayloadCallback = new PayloadCallback() {
        @Override
        public void onPayloadReceived(String endpointId, Payload payload) {
            Log.d("[PAYLOAD]", String.format("onPayloadReceived(endpointId=%s, payload=%s)", endpointId, payload));
            Log.d("[PAYLOAD]", "MSG = " + new String(payload.asBytes()));
            String incomingMessage = new String(payload.asBytes());
            //messageTextView.setText(incomingMessage);
            Toast.makeText(getApplicationContext(), "MSG_RECEIVED", Toast.LENGTH_SHORT).show();
            /*
            if (mTypeSpinner.getSelectedItem().toString().equals("Host")) {
                String messageToSend = "RECIBIDO 8)";
                send(messageToSend, endpoint.getName());
            }
            */
        }

        @Override
        public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update) {
            Log.d("[PAYLOAD]", String.format("onPayloadTransferUpdate(endpointId=%s, update=%s)", endpointId, update));
        }
    };

    private final ConnectionLifecycleCallback mConnectionLifecycleCallback = new ConnectionLifecycleCallback() {
        @Override
        public void onConnectionInitiated(@NonNull String s, @NonNull ConnectionInfo connectionInfo) {
            Log.d("[onEndpointFound]", String.format("onConnectionInitiated(endpointId=%s, endpointName=%s)", s, connectionInfo.getEndpointName()));
            client.acceptConnection(s, mPayloadCallback); // acceptConnection
        }

        @Override
        public void onConnectionResult(@NonNull String s, @NonNull ConnectionResolution connectionResolution) {
            Log.d("[onEndpointFound]", String.format("onConnectionResponse(endpointId=%s, result=%s)", s, connectionResolution));

            // We're no longer connecting
            mIsConnected = false;

            if (!connectionResolution.getStatus().isSuccess()) {
                Log.d("[onEndpointFound]", String.format("Connection failed. Received status %s.", MainActivity.toString(connectionResolution.getStatus())));
                //onConnectionFailed(mPendingConnections.remove(endpointId));
                discover();
                return;
            }
            Log.d("[onEndpointFound]", String.format("connectedToEndpoint(endpoint=%s)", s));
            //mEstablishedConnections.put(endpoint.getId(), endpoint);
            //mPendingConnections.remove(endpointId
            Toast.makeText(getApplicationContext(), "Connected to " + s, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onDisconnected(@NonNull String s) {
            Log.d("[onEndpointFound]", String.format("disconnectedFromEndpoint(endpoint=%s)", s));
            //mEstablishedConnections.remove(s);
            Toast.makeText(getApplicationContext(), "Disconnected from " + s, Toast.LENGTH_SHORT).show();
        }
    };

    private static String toString(Status status) {
        return String.format(
                Locale.US,
                "[%d]%s",
                status.getStatusCode(),
                status.getStatusMessage() != null
                        ? status.getStatusMessage()
                        : ConnectionsStatusCodes.getStatusCodeString(status.getStatusCode()));
    }

    private final EndpointDiscoveryCallback mEndpointDiscoveryCallback = new EndpointDiscoveryCallback() {

        @Override
        public void onEndpointFound(@NonNull String s, @NonNull DiscoveredEndpointInfo discoveredEndpointInfo) {
            Log.d("[onEndpointFound]", "Found " + s + " discoveredEndpointInfo: name" + discoveredEndpointInfo.getEndpointName());
            Toast.makeText(MainActivity.this, "Found " + s + " discoveredEndpointInfo: name" + discoveredEndpointInfo.getEndpointName(), Toast.LENGTH_LONG).show();
            mRemotePeerEndpoints.add(s);
            mMessageAdapter.notifyDataSetChanged();
        }

        @Override
        public void onEndpointLost(@NonNull String s) {
            Log.d("[onEndpointLost]", "Lost " + s);
            Toast.makeText(MainActivity.this, "Lost " + s, Toast.LENGTH_LONG).show();
        }
    };

    private void discover() {
        DiscoveryOptions.Builder discoveryOptions = new DiscoveryOptions.Builder();
        discoveryOptions.setStrategy(Strategy.P2P_STAR);

        client
                .startDiscovery("SARASA", mEndpointDiscoveryCallback, discoveryOptions.build())
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unusedResult) {
                                Log.d("[addOnSuccessListener]", "addOnSuccessListener");
                                Toast.makeText(MainActivity.this, "addOnSuccessListener", Toast.LENGTH_LONG).show();
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("[OnFailureListener]", "Failure OnFailureListener: " + e.getMessage());
                                Toast.makeText(MainActivity.this, "OnFailureListener: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
    }

    private void advertise() {

        AdvertisingOptions.Builder advertisingOptions = new AdvertisingOptions.Builder();
        advertisingOptions.setStrategy(Strategy.P2P_STAR);

        client.stopAllEndpoints();
        client.stopAdvertising();

        client.
                startAdvertising("advertising",  "SARASA", mConnectionLifecycleCallback, advertisingOptions.build())
                .addOnSuccessListener(
                    new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unusedResult) {
                            Log.d("[onSucess]", "pasé por onSucess");
                            Toast.makeText(MainActivity.this, "onSucess", Toast.LENGTH_LONG).show();
                        }
                    })
                .addOnFailureListener(
                    new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("[onFailure]", "pasé por onFailure: " + e.getMessage());
                            Toast.makeText(MainActivity.this, "OnFailure error " + e.getMessage(), Toast.LENGTH_LONG).show();

                        }
                    });
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }


}
