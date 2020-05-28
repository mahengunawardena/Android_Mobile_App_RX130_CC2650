//============================================================================
//
// Name        : MainActivity.java
// Author      : Mahendra Gunawardena
// Date        : 03/03/2020
// Version     : Rev 0.01
// Copyright   : Your copyright notice
// Description : MainActivity for an Android BLE mobile DEMO application
//             : that supports RX130 + CC2650 BLE device
//             : The mobile application can read LED status as
//             : well as detect a change in LED status
//             : The changes in LED status due to buttons press are
//             : are communicated the mobile application
//             : via notifications
//             :
//             :
//             :
//
//============================================================================
/*
 *  MainActivity.java
 *
 *  The BLE hardware implementation is using a RX130 Launchpad and CC2650 BoosterXL
 *
 *
 * Copyright Mahendra Gunawardena, Mitisa LLC
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED ''AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL I
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


package capsense.application.rx130_cc2650;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private boolean mScanning;
    private BluetoothAdapter mBluetoothAdapter;
    private ListView DevicelstView;
    private HashMap<Integer, BLE_Device> mBTDeviceHashMap = new HashMap();
    private ArrayList<BLE_Device> mBLE_DeviceArrayList;
    private BLEDeviceListAdapter mBLEDeviceListAdapter;
    private BLE_Scanner mBLE_Scanner;

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE = 1;
    private final static int STW_PERMISSIONS_REQUEST_ENABLE_BT = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(capsense.application.rx130_cc2650.R.layout.activity_main);

        mScanning = false;

        //Get User Interface Elements
        Toolbar toolbar = findViewById(capsense.application.rx130_cc2650.R.id.toolbar);
        toolbar.setTitle(R.string.app_label);
        setSupportActionBar(toolbar);


        // Use this check to determine whether BLE is supported on the device. Then
        // you can selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, capsense.application.rx130_cc2650.R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // For android Marshmallow and higher check for location permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("This application needs location access");
                    builder.setMessage("Please grant location so this application can discover bluetooth devices");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_COARSE);
                        }

                    });
                    builder.show();
                } else {
                    //Prompt user for location access
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_COARSE);
                }
            }
        }


        // Initialize Bluetooth adapter
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mBLE_Scanner = new BLE_Scanner(this, 7500, -75);

    }

    protected void onResume() {
        super.onResume();
        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, STW_PERMISSIONS_REQUEST_ENABLE_BT);
        }

        //BLEDeviceListAdapter adapter = new BLEDeviceListAdapter(this, 0, items);
        //CustomAdapter adapter = new CustomAdapter(this, 0, items);
        mBLEDeviceListAdapter = new BLEDeviceListAdapter(this, 0, mBTDeviceHashMap);

        mBLE_DeviceArrayList = new ArrayList<BLE_Device>();

        startscan();

        DevicelstView = findViewById(capsense.application.rx130_cc2650.R.id.Devicelst);

        //BLEDeviceListAdapter adapter = new BLEDeviceListAdapter(this, 0, items);
        //CustomAdapter adapter = new CustomAdapter(this, 0, items);
        //BLEDeviceListAdapter adapter = new BLEDeviceListAdapter(this, 0, mBTDeviceHashMap);

        DevicelstView.setAdapter(mBLEDeviceListAdapter);


    }

    @Override
    protected void onPause() {
        super.onPause();
        stopScan();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(capsense.application.rx130_cc2650.R.menu.menu_scan_stop, menu);

        if (!mScanning) {
            menu.findItem(capsense.application.rx130_cc2650.R.id.menu_stop).setVisible(false);
            menu.findItem(capsense.application.rx130_cc2650.R.id.menu_scan).setVisible(true);
            ///menu.findItem(R.id.menu_refresh).setActionView(null);

            //Toast.makeText(MainActivity.this, "OnCreate Begin Scanning ...." + mScanning, Toast.LENGTH_LONG).show();

            //mScanning = true;
        } else {
            menu.findItem(capsense.application.rx130_cc2650.R.id.menu_stop).setVisible(true);
            menu.findItem(capsense.application.rx130_cc2650.R.id.menu_scan).setVisible(false);
            //menu.findItem(R.id.menu_refresh).setActionView(R.layout.actionbar_indeterminate_progress);

            //Toast.makeText(MainActivity.this, "OnCreate Stop Scanning ...." + mScanning, Toast.LENGTH_LONG).show();

            //mScanning = false;
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case capsense.application.rx130_cc2650.R.id.menu_scan:
                if (!mBLE_Scanner.isScanning()) {
                    // Clear list before new scan
                    startscan();
                    Toast.makeText(MainActivity.this, "onOptionsItem  Begin Scanning ...." + mScanning, Toast.LENGTH_LONG).show();
                    item.setVisible(false);
                    invalidateOptionsMenu();
                }
                return true;
            case capsense.application.rx130_cc2650.R.id.menu_stop:
                if (mBLE_Scanner.isScanning()) {
                    stopScan();
                    Toast.makeText(MainActivity.this, "onOptionsItem Stop Scanning ...." + mScanning, Toast.LENGTH_LONG).show();
                    item.setVisible(false);
                    invalidateOptionsMenu();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if (!mScanning) {

            //Toast.makeText(MainActivity.this, "onPrepare Begin ...." + mScanning, Toast.LENGTH_LONG).show();

            menu.findItem(capsense.application.rx130_cc2650.R.id.menu_stop).setVisible(false);
            menu.findItem(capsense.application.rx130_cc2650.R.id.menu_scan).setVisible(true);
        } else {

            //Toast.makeText(MainActivity.this, "onPrepare Stop ...." + mScanning, Toast.LENGTH_LONG).show();

            menu.findItem(capsense.application.rx130_cc2650.R.id.menu_stop).setVisible(true);
            menu.findItem(capsense.application.rx130_cc2650.R.id.menu_scan).setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }


    public void addDevice(BluetoothDevice device, int rssi) {
        String address = device.getAddress();
        Boolean newdevice = true;
        if (!mBTDeviceHashMap.containsValue(device)) {
            BLE_Device ble_device = new BLE_Device(device);
            ble_device.setRSSI(rssi);
            Integer local_maxCount = mBLEDeviceListAdapter.getMaxCount();
            Log.d("BLE_S", "Address :" + address + ble_device);
            for (Map.Entry<Integer, BLE_Device> entry : mBTDeviceHashMap.entrySet()) {
                if (entry.getValue().getAddress().equals(address)) {
                    newdevice = false;
                    break;
                }
            }
            if (newdevice) {
                mBTDeviceHashMap.put(local_maxCount, ble_device);
                mBLEDeviceListAdapter.Array_List_addDevice(device);
                mBLE_DeviceArrayList.add(ble_device);
                local_maxCount++;
            }
        } else {
            for (Map.Entry<Integer, BLE_Device> entry : mBTDeviceHashMap.entrySet()) {
                if (entry.getValue().getAddress().equals(address)) {
                    System.out.println(entry.getKey());
                    BLE_Device ble_device = mBTDeviceHashMap.get(entry.getKey());
                    ble_device.setRSSI(rssi);
                    break;
                }
            }
        }

        mBLEDeviceListAdapter.notifyDataSetChanged();
    }

    public void stopScan() {
        mBLE_Scanner.stop();
        mScanning = false;
        mBLEDeviceListAdapter.setmScanning(mScanning);
    }

    public void startscan() {
        mBTDeviceHashMap.clear();
        mBLEDeviceListAdapter.Array_List_clear();
        mBLE_DeviceArrayList.clear();
        mBLE_Scanner.start();
        mScanning = true;
        mBLEDeviceListAdapter.setmScanning(mScanning);
    }

}

