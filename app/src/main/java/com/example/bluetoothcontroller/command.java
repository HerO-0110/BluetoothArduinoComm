package com.example.bluetoothcontroller;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class command extends AppCompatActivity {

    Button sendbutton, disconnectbutton;
    EditText message;
    String address;
    TextView returntext;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    byte buffer[];
    boolean stopThread;
    InputStream inputStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_command);
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            Toast.makeText(this, "Error occurred: Address not received.", Toast.LENGTH_SHORT).show();
        } else {
            address = extras.getString("btaddress");
            Toast.makeText(this, "BT Address: " + address, Toast.LENGTH_SHORT).show();
        }
        sendbutton = (Button) findViewById(R.id.sendbutton);
        message = (EditText) findViewById(R.id.message);
        disconnectbutton = (Button) findViewById(R.id.disconnectbutton);
        returntext = (TextView) findViewById(R.id.returntext);
        sendbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand();
            }
        });
        disconnectbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disconnect();
            }
        });
        new ConnectBT().execute();
    }
    private void sendCommand(){
        if(!(btSocket == null)){
            try{
                btSocket.getOutputStream().write(message.getText().toString().getBytes());
                message.setText("");
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Error while sending command.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void disconnect() {
        if (!(btSocket == null)) {
            try {
                btSocket.close();
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Error while disconnecting.", Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }
    private class ConnectBT extends AsyncTask<Void, Void, Void>{
        private boolean ConnectSuccess = true;

        @Override
        protected void onPreExecute(){
            progress = ProgressDialog.show(command.this, "Connecting...", "Connecting...");
        }

        @Override
        protected Void doInBackground(Void... devices){
            try{
                if (btSocket == null || !isBtConnected){
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice device = myBluetooth.getRemoteDevice(address);
                    btSocket = device.createRfcommSocketToServiceRecord(myUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();
                }
            }
            catch (IOException e){
                ConnectSuccess = false;
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result){
            super.onPostExecute(result);
            if (!ConnectSuccess) {
                Toast.makeText(getApplicationContext(), "Connection failed.", Toast.LENGTH_SHORT).show();
                finish();
            }else{
                Toast.makeText(getApplicationContext(), "Connected.", Toast.LENGTH_SHORT).show();
                beginListenForData();
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }
    void beginListenForData() {
        final Handler handler = new Handler();
        buffer = new byte[1024];
        stopThread = false;
        if (!(btSocket == null)) {
            try {
                 inputStream = btSocket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    while (!Thread.currentThread().isInterrupted() && !stopThread) {
                        try {
                            int byteCount = inputStream.available();
                            if (byteCount > 0) {
                                byte[] rawBytes = new byte[byteCount];
                                inputStream.read(rawBytes);
                                final String string = new String(rawBytes, "UTF-8");
                                handler.post(new Runnable() {
                                    public void run() {
                                        returntext.append(string);
                                    }
                                });

                            }
                        } catch (IOException ex) {
                            stopThread = true;
                        }
                    }
                }
            });

            thread.start();
        }
    }
}
