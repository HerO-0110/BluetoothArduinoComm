package com.example.bluetoothcontroller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter myBA;
    private Set<BluetoothDevice> paired;
    private Button bbutton;
    private ListView devicelist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bbutton = (Button) findViewById(R.id.bbutton);
        devicelist = (ListView)findViewById(R.id.blistView);

        myBA = BluetoothAdapter.getDefaultAdapter();

        bbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchDevices();
            }
        });

        if(myBA == null) {
            Toast.makeText(getApplicationContext(), "Bluetooth is not supported on this device.", Toast.LENGTH_SHORT).show();
        }else if(!myBA.isEnabled()){
                Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(turnBTon,1);
        }
    }
    private void searchDevices(){
        paired = myBA.getBondedDevices();
        ArrayList devices = new ArrayList();

        if(!(paired.size() == 0)){
            for(BluetoothDevice bd : paired){
                devices.add(bd.getAddress());
            }
        }else{
            Toast.makeText(getApplicationContext(), "No Bluetooth devices found!", Toast.LENGTH_SHORT).show();
        }
        ArrayAdapter devicesAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, devices);
        devicelist.setAdapter(devicesAdapter);
        devicelist.setOnItemClickListener(selectDevice);
    }
    private AdapterView.OnItemClickListener selectDevice = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String address = ((TextView) view).getText().toString();
            Intent i = new Intent(MainActivity.this, command.class);
            i.putExtra("btaddress", address);
            startActivity(i);
        }
    };
}
