package com.example.testchat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
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
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

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

    private String service_id;
    private String myName;

    int studentId;
    private Set<String> connectedEndpoints = new HashSet<>();
    private ArrayAdapter<String> mMessageAdapter;

    private boolean mIsHost;
    private boolean mIsConnected;

    private String mRemoteHostEndpoint;
    private List<String> mRemotePeerEndpoints = new ArrayList<String>();

    private static final long CONNECTION_TIME_OUT = 10000L;

    private static final String[] REQUIRED_PERMISSIONS = new String[] {
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
        studentId = new Random().nextInt(90);
        service_id = getPackageName();
        myName = generateName(); //TODO ACA PONGO EL NOMBRE DEL WACHIN DE NAVIGATION ACITIVITY
        initViews();
    }

    //TODO ESTO Y GENERATE NAME SE TIENE QUE BORRAR CUANDO SAQUE EL TODO DE ARRIBA
    private final String[] COLORS = new String[] {
        "Red",
        "Orange",
        "Yellow",
        "Green",
        "Blue",
        "Indigo",
        "Violet",
        "Purple",
        "Lavender",
        "Fuchsia",
        "Plum",
        "Orchid",
        "Magenta",
    };

    public String generateName() {
        String color = COLORS[new Random().nextInt(COLORS.length)];
        return color;
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
           case R.id.button_send: {

               @SuppressLint("DefaultLocale") String msg = String.format("Alumno:%d", studentId);

               send(Payload.fromBytes(msg.getBytes()), connectedEndpoints);
               Toast.makeText(this, connectedEndpoints.toString(), Toast.LENGTH_SHORT).show();
           }
       }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d("[onConnected]", "Here");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("[onConnectionSuspended]", "Here");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("[onConnectionFailed]", "Here");
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
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                // Going to connect to selected device from the list
                connectToEndpoint((String) adapterView.getItemAtPosition(i));
            }
        });
    }

    private void addToList(String string){
        mRemotePeerEndpoints.add(string);
        mMessageAdapter.notifyDataSetChanged();
    }

    private final EndpointDiscoveryCallback mEndpointDiscoveryCallback = new EndpointDiscoveryCallback() {

        @Override
        public void onEndpointFound(@NonNull String s, @NonNull DiscoveredEndpointInfo discoveredEndpointInfo) {
            Log.d("[onEndpointFound]", "Found " + s + " discoveredEndpointInfo: name" + discoveredEndpointInfo.getEndpointName());
            Toast.makeText(MainActivity.this, "Found " + s + " discoveredEndpointInfo: name" + discoveredEndpointInfo.getEndpointName(), Toast.LENGTH_LONG).show();
            connectedEndpoints.add(s);
            addToList(s);

            //connectToEndpoint(s);
        }

        @Override
        public void onEndpointLost(@NonNull String s) {
            Log.d("[onEndpointLost]", "Lost " + s);
            Toast.makeText(MainActivity.this, "Lost " + s, Toast.LENGTH_LONG).show();
            mRemotePeerEndpoints.remove(s);
            mMessageAdapter.remove(s);
            mMessageAdapter.notifyDataSetChanged(); //TODO ACA HAY QUE REMOVER DE UNA COPIA, COMO ESTA EN EL SE REALM START ATTENDANCE
        }
    };

    private void discover() {
        DiscoveryOptions.Builder discoveryOptions = new DiscoveryOptions.Builder();
        discoveryOptions.setStrategy(getStrategy());

        client
                .startDiscovery(getServiceId(), mEndpointDiscoveryCallback, discoveryOptions.build())
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
        advertisingOptions.setStrategy(getStrategy());

        client.stopAllEndpoints();
        client.stopAdvertising();

        client.
                startAdvertising(getName(),  getServiceId(), mConnectionLifecycleCallback, advertisingOptions.build())
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


    private void send(Payload payload, Set<String> endpoints) {
        client
                .sendPayload(new ArrayList<>(endpoints), payload)
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("[onFailure]", "Error sending payload: " + e.getMessage());
                            }
                        });
    }

    protected String getName() {
        return myName;
    }

    protected String getServiceId() {
        return "serviceID";
    }

    protected Strategy getStrategy() {
        return Strategy.P2P_STAR;
    }


    private final PayloadCallback mPayloadCallback =
            new PayloadCallback() {
                @Override
                public void onPayloadReceived(String endpointId, Payload payload) {
                    Log.d(  "onPayloadReceived",String.format("onPayloadReceived(endpointId=%s, payload=%s)", endpointId, payload));
                    String payloadString = new String(payload.asBytes());
                    Toast.makeText(MainActivity.this, payloadString, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update) {
                    //Toast.makeText(MainActivity.this, "onPayloadTransferUpdate", Toast.LENGTH_SHORT).show();
                    Log.d("onPayloadTransferUpdate",
                            String.format(
                                    "onPayloadTransferUpdate(endpointId=%s, update=%s)", endpointId, update));
                }
            };


    private final ConnectionLifecycleCallback mConnectionLifecycleCallback =
            new ConnectionLifecycleCallback() {
                @Override
                public void onConnectionInitiated(String endpointId, ConnectionInfo connectionInfo) {
                    Log.d("onConnectionInitiated",
                            String.format(
                                    "onConnectionInitiated(endpointId=%s, endpointName=%s)",
                                    endpointId, connectionInfo.getEndpointName()));
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    client.acceptConnection(endpointId, mPayloadCallback);
                }

                @Override
                public void onConnectionResult(String endpointId, ConnectionResolution result) {
                    switch (result.getStatus().getStatusCode()) {
                        case ConnectionsStatusCodes.STATUS_OK:
                            Log.d("[onConnectionResult]", "connected");
                            Toast.makeText(MainActivity.this, "CONNECTED!", Toast.LENGTH_SHORT).show();
                            break;
                        case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                            // The connection was rejected by one or both sides.
                            Log.d("[onConnectionResult]", "rejected");
                            Toast.makeText(MainActivity.this, "REJECTED!", Toast.LENGTH_SHORT).show();
                            break;
                        case ConnectionsStatusCodes.STATUS_ERROR:
                            // The connection broke before it was able to be accepted.
                            Log.d("[onConnectionResult]", "error");
                            Toast.makeText(MainActivity.this, "ERROR!", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            // Unknown status code
                    }
                }


                @Override
                public void onDisconnected(String endpointId) {
                    Toast.makeText(MainActivity.this, "DISCONNECTED!", Toast.LENGTH_SHORT).show();
                }
            };

    protected void connectToEndpoint(final String endpoint) {
        Log.d("connectToEndpoint", "Sending a connection request to endpoint " + endpoint);
        // Mark ourselves as connecting so we don't connect multiple times
        // Ask to connect
        client
                .requestConnection("randomname", endpoint, mConnectionLifecycleCallback)
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(MainActivity.this, "Success requestConnection ", Toast.LENGTH_SHORT).show();
                            }
                        }
                )
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("connectToEndpoint","requestConnection() failed." + e.getMessage());
                                Toast.makeText(MainActivity.this, "Failed requestConnection " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                connectToEndpoint(endpoint);
                            }
                        });
    }
}
