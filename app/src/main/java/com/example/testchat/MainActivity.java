package com.example.testchat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {

    private static final int REQUEST_CODE_REQUIRED_PERMISSIONS = 1;
    private ConnectionsClient client;
    private TextView mStatusText;
    private Button mConnectionButton;
    private ListView mListView;
    private String service_id;
    private String myName;
    private static final Strategy STRATEGY = Strategy.P2P_STAR;

    private ArrayAdapter<String> mMessageAdapter;

    private boolean mIsConnected = false;  // TODO - ARRANCA DEFAULT FALSE TENGO QUE PONERLE TRUE EN ALGUN LADO

    private Button professor1;
    private Button professor2;

    private List<String> mRemotePeerEndpoints = new ArrayList<String>();

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
           case R.id.button_advertise: {
               if( mIsConnected ) {
                   //disconnect(); //TODO ESTO NO ESTA HECHO DEBERÍA DESCONECTARME DE TODO / DE DONDE ESTOY
                   mStatusText.setText("Disconnected");
               }  else {
                   advertise();
               }
               break;
           }
           case R.id.button4: {
               myName = "EIA";
               Toast.makeText(this, "PROF_SUBJECT = " + myName, Toast.LENGTH_SHORT).show();
               break;
           }
           case R.id.button5: {
               myName = "BDD";
               Toast.makeText(this, "PROF_SUBJECT = " + myName, Toast.LENGTH_SHORT).show();
               break;
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
        mConnectionButton = findViewById( R.id.button_advertise );
        mListView = findViewById( R.id.list );

        professor1 = findViewById(R.id.button4);
        professor2 = findViewById(R.id.button5);

        setupButtons();
        setupMessageList();
    }

    private void setupButtons() {
        mConnectionButton.setOnClickListener(this);

        professor1.setOnClickListener(this);
        professor2.setOnClickListener(this);
    }

    private void setupMessageList() {
        mMessageAdapter = new ArrayAdapter<String>( this, android.R.layout.simple_list_item_1, mRemotePeerEndpoints);
        mListView.setAdapter( mMessageAdapter );
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //TODO ACA VA LO QUE TENGO EN SE REALM
                Log.d("CLICK-LIST", "YOU MAKE A CLICK");
            }
        });
    }

    private void addToList(String string) {
        mRemotePeerEndpoints.add(string);
        mMessageAdapter.notifyDataSetChanged();
    }

    private void advertise() {

        AdvertisingOptions.Builder advertisingOptions = new AdvertisingOptions.Builder();
        advertisingOptions.setStrategy(STRATEGY);

        client.stopAllEndpoints();
        client.stopAdvertising();

        client.startAdvertising(myName,  service_id, mConnectionLifecycleCallback, advertisingOptions.build()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unusedResult) {
                Log.d("[onSucess]", "pasé por onSucess");
                Toast.makeText(MainActivity.this, "onSucess", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
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

    protected String getName() {
        return myName;
    }

    protected String getServiceId() {
        return service_id;
    }

    protected Strategy getStrategy() {
        return STRATEGY;
    }

    private final PayloadCallback mPayloadCallback = new PayloadCallback() {
        @Override
        public void onPayloadReceived(String endpointId, Payload payload) {
            Log.d(  "onPayloadReceived",String.format("onPayloadReceived(endpointId=%s, payload=%s)", endpointId, payload));
            String payloadString = new String(payload.asBytes());
            Toast.makeText(MainActivity.this, payloadString, Toast.LENGTH_SHORT).show();

            String message = new String(payload.asBytes());
            addToList(message);
        }

        @Override
        public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update) {
            Log.d("onPayloadTransferUpdate", String.format("onPayloadTransferUpdate(endpointId=%s, update=%s)", endpointId, update));
        }
    };


    private final ConnectionLifecycleCallback mConnectionLifecycleCallback = new ConnectionLifecycleCallback() {
        @Override
        public void onConnectionInitiated(String endpointId, ConnectionInfo connectionInfo) {
            Log.d("onConnectionInitiated",String.format("onConnectionInitiated(endpointId=%s, endpointName=%s)", endpointId, connectionInfo.getEndpointName()));
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
                    mIsConnected = true;
                    mStatusText.setText("Connected");
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
}
