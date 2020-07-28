package com.example.testchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.nearby.Nearby;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class StudentActivity extends AppCompatActivity {

    private boolean mIsConnected = false;  // TODO - ARRANCA DEFAULT FALSE TENGO QUE PONERLE TRUE EN ALGUN LADO
    private String mySubject; // TODO - ESTO SE VA CUANDO AGARRE DEL SPINNER EN MI
    private TextView mStatusText;
    private String msg;
    private String myName;
    private static final Strategy STRATEGY = Strategy.P2P_STAR;
    private ConnectionsClient client;
    private String service_id;
    int studentId;
    private HashMap<String, String> mEndpoints = new HashMap<>();
    private static final int REQUEST_CODE_REQUIRED_PERMISSIONS = 1;
    private Button student1;
    private Button student2;
    private Button mSendButton;

    private static final String[] REQUIRED_PERMISSIONS = new String[] {
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);

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

    private void initViews() {
        mStatusText = findViewById( R.id.text_status );
        mSendButton = findViewById( R.id.button_send );

        student1 = findViewById(R.id.button2);
        student2 = findViewById(R.id.button3);

        setupButtons();
    }

    private void setupButtons() {
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( mIsConnected ) {
                    //disconnect(); //TODO ESTO NO ESTA HECHO DEBERÍA DESCONECTARME DE TODO / DE DONDE ESTOY
                    mStatusText.setText("Disconnected");
                }  else {
                    discover();
                }
            }
        });
        student1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mySubject = "EIA";
                myName = "PABLI";
                msg = "Alumno: " + myName;
                Toast.makeText(StudentActivity.this, "Subject = " + mySubject, Toast.LENGTH_SHORT).show();
            }
        });
        student2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mySubject = "BDD";
                myName = "FERCHI";
                msg = "Alumno: " + myName;
                Toast.makeText(StudentActivity.this, "Subject = " + mySubject, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void discover() {
        DiscoveryOptions.Builder discoveryOptions = new DiscoveryOptions.Builder();
        discoveryOptions.setStrategy(STRATEGY);

        client
                .startDiscovery(service_id, mEndpointDiscoveryCallback, discoveryOptions.build())
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unusedResult) {
                                Log.d("[addOnSuccessListener]", "addOnSuccessListener");
                                Toast.makeText(StudentActivity.this, "addOnSuccessListener", Toast.LENGTH_LONG).show();
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("[OnFailureListener]", "Failure OnFailureListener: " + e.getMessage());
                                Toast.makeText(StudentActivity.this, "OnFailureListener: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
    }

    private final EndpointDiscoveryCallback mEndpointDiscoveryCallback = new EndpointDiscoveryCallback() {

        @Override
        public void onEndpointFound(@NonNull String s, @NonNull DiscoveredEndpointInfo discoveredEndpointInfo) {
            Log.d("[onEndpointFound]", "Found " + s + " discoveredEndpointInfo: name" + discoveredEndpointInfo.getEndpointName());
            Toast.makeText(StudentActivity.this, "Found " + s + " discoveredEndpointInfo: name" + discoveredEndpointInfo.getEndpointName(), Toast.LENGTH_LONG).show();

            mEndpoints.put(s, discoveredEndpointInfo.getEndpointName());
            //addToList(discoveredEndpointInfo.getEndpointName());


            for (Map.Entry<String, String> entry : mEndpoints.entrySet()) {
                if(entry.getValue().equals(mySubject)) {
                    connectToEndpoint(entry.getKey());
                }
            }
        }

        @Override
        public void onEndpointLost(@NonNull String s) {
            Log.d("[onEndpointLost]", "Lost " + s);
            Toast.makeText(StudentActivity.this, "Lost " + s, Toast.LENGTH_LONG).show();
            mEndpoints.remove(s);
        }
    };

    protected void connectToEndpoint(final String endpoint) {
        Log.d("connectToEndpoint", "Sending a connection request to endpoint " + endpoint);
        // Mark ourselves as connecting so we don't connect multiple times
        // Ask to connect
        client
                .requestConnection(myName, endpoint, mConnectionLifecycleCallback)
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(StudentActivity.this, "Success requestConnection ", Toast.LENGTH_SHORT).show();
                            }
                        }
                )
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("connectToEndpoint","requestConnection() failed." + e.getMessage());
                                Toast.makeText(StudentActivity.this, "Failed requestConnection " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                //connectToEndpoint(endpoint);
                            }
                        });
    }

    private final PayloadCallback mPayloadCallback = new PayloadCallback() {
        @Override
        public void onPayloadReceived(String endpointId, Payload payload) {
            Log.d(  "onPayloadReceived",String.format("onPayloadReceived(endpointId=%s, payload=%s)", endpointId, payload));
            String payloadString = new String(payload.asBytes());
            Toast.makeText(StudentActivity.this, payloadString, Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(StudentActivity.this, "CONNECTED!", Toast.LENGTH_SHORT).show();

                    for (Map.Entry<String, String> entry : mEndpoints.entrySet()) {
                        if(entry.getValue().equals(mySubject)) {
                            send(Payload.fromBytes(msg.getBytes()), entry.getKey());
                            Toast.makeText(getApplicationContext(), "MSG_SEND", Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
                case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:

                    // The connection was rejected by one or both sides.
                    Log.d("[onConnectionResult]", "rejected");
                    Toast.makeText(StudentActivity.this, "REJECTED!", Toast.LENGTH_SHORT).show();
                    break;
                case ConnectionsStatusCodes.STATUS_ERROR:

                    // The connection broke before it was able to be accepted.
                    Log.d("[onConnectionResult]", "error");
                    Toast.makeText(StudentActivity.this, "ERROR!", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    // Unknown status code
            }
        }

        @Override
        public void onDisconnected(String endpointId) {
            Toast.makeText(StudentActivity.this, "DISCONNECTED!", Toast.LENGTH_SHORT).show();
        }
    };

    private void send(Payload payload, String endpoint) {
        client.sendPayload(endpoint, payload).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("[onFailure]", "Error sending payload: " + e.getMessage());
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