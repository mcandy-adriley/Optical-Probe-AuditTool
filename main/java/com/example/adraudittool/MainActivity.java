package com.example.adraudittool;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private static final String TAG = "MainActivity";

    BluetoothAdapter mBluetoothAdaptor;
    Button btnEnableDisable_Discoverable;
    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
    public DeviceListAdapter mDeviceListAdapter;
    ListView lvNewDevices;


    //Create a BroadcastReceiver for On Off
    private final BroadcastReceiver mBluetoothAdaptor1 = new BroadcastReceiver() {
       public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //When On Off
            if (action.equals(mBluetoothAdaptor.ACTION_STATE_CHANGED)){
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,mBluetoothAdaptor.ERROR);

                switch (state){
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG,"mBluetoothAdaptor1: STATE OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG,"mBluetoothAdaptor1: STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG,"mBluetoothAdaptor1: STATE ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG,"mBluetoothAdaptor1: STATE TURNING ON");
                        break;
                }
            }
        }
    };
    //Create a BroadcastReceiver for Discoverable On Off
    private final BroadcastReceiver mBluetoothAdaptor2 = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //When Discovery finds a device
            if (action.equals(mBluetoothAdaptor.ACTION_SCAN_MODE_CHANGED)){
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE,mBluetoothAdaptor.ERROR);

                switch (state){
                    //Device is in Discoverable mode
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(TAG,"mBluetoothAdaptor2: Discoverability Enabled.");
                        break;
                    //Device not in Discoverable mode
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG,"mBluetoothAdaptor2: Discoverability Disabled. Able to receive connections.");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(TAG,"mBluetoothAdaptor2: Discoverability Disabled. Not able to receive connections.");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d(TAG,"mBluetoothAdaptor2: Connecting....");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d(TAG,"mBluetoothAdaptor2: Connected.");
                        break;
                }
            }
        }
    };
    //Create a BroadcastReceiver for Discover On Off
    private BroadcastReceiver mBluetoothAdaptor3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG,"mBluetoothAdaptor3: ACTION FOUND.");
            //When Discovery finds a device
            if (action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device=intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mBTDevices.add(device);
                Log.d(TAG, "mBluetoothAdaptor3: "+device.getName()+" : "+device.getAddress());
                mDeviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, mBTDevices);
            }
        }
    };
    //Create a BroadcastReceiver for Discover On Off
    private BroadcastReceiver mBluetoothAdaptor4 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action =intent.getAction();
            //
            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                BluetoothDevice mDevice=intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //3 Cases:
                //Case 1: Bonded all ready
                if(mDevice.getBondState() == BluetoothDevice.BOND_BONDED){
                    Log.d(TAG,"mBluetoothAdaptor4: BOND BONED.");
                }
                //Case 2: Creating a Bond
                if(mDevice.getBondState() == BluetoothDevice.BOND_BONDING){
                    Log.d(TAG,"mBluetoothAdaptor4: BOND BONDING.");
                }
                //Case 3: Bond is broken
                if(mDevice.getBondState() == BluetoothDevice.BOND_NONE){
                    Log.d(TAG,"mBluetoothAdaptor4: BOND NONE.");
                }
            }


        }
    };

    @Override
    protected void onDestroy() {
        Log.d(TAG,"onDestroy: called.");
        super.onDestroy();
        unregisterReceiver(mBluetoothAdaptor1);
        unregisterReceiver(mBluetoothAdaptor2);
        unregisterReceiver(mBluetoothAdaptor3);
        unregisterReceiver(mBluetoothAdaptor4);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btnONOFF = (Button) findViewById(R.id.btnONOFF);
        btnEnableDisable_Discoverable = (Button) findViewById(R.id.BtnDiscoverable_on_off);
        lvNewDevices=(ListView) findViewById(R.id.lvNewDevices);
        mBTDevices = new ArrayList<>();

        //Broadcasts when bond state changes
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBluetoothAdaptor4, filter);

        mBluetoothAdaptor=BluetoothAdapter.getDefaultAdapter();

        lvNewDevices.setOnItemClickListener(MainActivity.this);

        btnONOFF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Enable/Disable Bluetooth.");
                enableDisableBT();
            }
        });
    }


//onClick enableDisableBT
    public void enableDisableBT(){
        if(mBluetoothAdaptor== null){
            Log.d(TAG, "enableDisableBT: Does not have BT Capabilities");
           }
           if(!mBluetoothAdaptor.isEnabled()){
                Log.d(TAG,"enableDisableBT: enabling BT");
                Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivity(enableBTIntent);

               IntentFilter BTIntent = new IntentFilter (BluetoothAdapter.ACTION_STATE_CHANGED);
               registerReceiver(mBluetoothAdaptor1, BTIntent);
        }
           if(mBluetoothAdaptor.isEnabled()) {
                Log.d(TAG,"enableDisableBT: disabling BT");
                mBluetoothAdaptor.disable();

                IntentFilter BTIntent = new IntentFilter (BluetoothAdapter.ACTION_STATE_CHANGED);
                registerReceiver(mBluetoothAdaptor1, BTIntent);
        }
    }
//onClick btnEnableDisable_Discoverable
    public  void  btnEnableDisable_Discoverable(View view){
        Log.d(TAG, "btnEnableDisable_Discoverable: Making device discoverable for 300 seconds.");

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,300);
        startActivity(discoverableIntent);

        IntentFilter intentFilter = new IntentFilter(mBluetoothAdaptor.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(mBluetoothAdaptor2,intentFilter);
    }
//onClick Start Discover
    public  void btnDiscover(View view) {
        Log.d(TAG, "btnDiscover: Looking for unpaired devices.");
        //if currently discovering cancel discovery
        if (mBluetoothAdaptor.isDiscovering()) {
            mBluetoothAdaptor.cancelDiscovery();
            Log.d(TAG, "btnDiscover: Canceling Discovery.");
            //Check BT permissions in Manifest
            checkBTPermissions();
            mBluetoothAdaptor.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBluetoothAdaptor3, discoverDevicesIntent);
        }
        //if not discovering, start
        if (!mBluetoothAdaptor.isDiscovering()) {
            //Check BT permissions in Manifest
            checkBTPermissions();
            mBluetoothAdaptor.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBluetoothAdaptor3, discoverDevicesIntent);
        }
    }
//This is required for all devices running API23+
    private void checkBTPermissions(){
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permissionACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {

                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);//any number//
                }
            }else{
                Log.d(TAG,"checkPermissions: No need to check permissions. SDK version <LOLLIPOP.");
            }

        }

//onItemClick You selected a device
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int i, long l) {
        //first cancel discovery
        mBluetoothAdaptor.cancelDiscovery();
        Log.d(TAG,"onItemClick: You clicked on a device");
        String devicename =mBTDevices.get(i).getName();
        String deviceaddress= mBTDevices.get(i).getAddress();

        Log.d(TAG, "onItemClick: device Name = "+devicename);
        Log.d(TAG, "onItemClick: device Address = "+deviceaddress);

        //Create the bond
        //Note: Requires API 18+
        if (Build.VERSION.SDK_INT>Build.VERSION_CODES.JELLY_BEAN_MR2){
            Log.d(TAG,"Trying to pair with "+ devicename);
            mBTDevices.get(i).createBond();

        }

    }
}
