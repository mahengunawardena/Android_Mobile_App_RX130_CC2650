package capsense.application.rx130_cc2650;

import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.util.HashMap;
import java.util.List;

import static java.lang.String.valueOf;

public class BLE_BroadcastReceiver extends BroadcastReceiver {

    private final static String TAG = BLE_BroadcastReceiver.class.getSimpleName();
    private boolean mConnected;
    private boolean mServiceDiscovered;
    private boolean mDataActionAvailable;
    private IntentFilter intentFilter;
    private int connection_count = 0;
    private List<BluetoothGattService> gattServices;
    private HashMap<String, String> BLE_statusMap = new HashMap<String, String>();

    public BLE_BroadcastReceiver() {
        mConnected = false;
        mServiceDiscovered = false;
        mDataActionAvailable = false;
        intentFilter = new IntentFilter();
        makeGattUpdateIntentFilter();
    }

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    /*  private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() { */
    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        connection_count++;
        switch (action) {
            case BLE_Service.ACTION_GATT_CONNECTED:
                mConnected = true;
                Log.i(TAG, "GATT ACTION Connected " + mConnected);
                BLE_statusMap.put("connected", String.valueOf(mConnected));
                try {
                    DeviceControlActivity.getInstace().updateBLEStatus(BLE_statusMap);
                } catch (Exception e) {

                }
                break;
            case BLE_Service.ACTION_GATT_DISCONNECTED:
                mConnected = false;
                mServiceDiscovered = false;
                mDataActionAvailable = false;
                BLE_statusMap.put("connected", String.valueOf(mConnected));
                Log.i(TAG, "GATT ACTION Disconnected Service Discovered " + mConnected);
                BLE_statusMap.put("Service_Discovered", String.valueOf(mServiceDiscovered));
                try {
                    DeviceControlActivity.getInstace().updateBLEStatus(BLE_statusMap);
                } catch (Exception e) {

                }
                break;
            case BLE_Service.ACTION_GATT_SERVICES_DISCOVERED:
                mServiceDiscovered = true;
                mDataActionAvailable = false;
                BLE_statusMap.put("Service_Discovered", String.valueOf(mServiceDiscovered));
                Log.i(TAG, "GATT ACTION Service Discovered " + mConnected);
                try {
                    DeviceControlActivity.getInstace().updateBLEStatus(BLE_statusMap);
                } catch (Exception e) {
                    Log.e(TAG, "Service Discovery error");
                }
                break;
            case BLE_Service.ACTION_DATA_AVAILABLE:
                mDataActionAvailable = true;
                BLE_statusMap.put("Data_Action_Available", String.valueOf(mDataActionAvailable));
                Log.i(TAG, "BLE Broadcast Receiver Action Data available :" + action);
                if (intent.hasExtra(BLE_Service.EXTRA_GREEN)) {
                    Log.i(TAG, "BLE Broadcast Receiver Extra Green LED:" + intent.getExtras().getString("EXTRA_GREEN"));
                    DeviceControlActivity.getInstace().updateGreenLED();
                } else if (intent.hasExtra(BLE_Service.EXTRA_YELLOW)) {
                    Log.i(TAG, "BLE Broadcast Receiver Extra Yellow LED:" + intent.getExtras().getString("EXTRA_YELLOW"));
                    DeviceControlActivity.getInstace().updateYellowLED();
                }
                else if (intent.hasExtra(BLE_Service.EXTRA_RED)) {
                    Log.i(TAG, "BLE Broadcast Receiver Extra Red :" + intent.getExtras().getString("EXTRA_RED"));
                    DeviceControlActivity.getInstace().updateRedLED();
                }
                else if(intent.hasExtra(BLE_Service.EXTRA_NOTIFICATION)) {
                    DeviceControlActivity.getInstace().updateLED(intent.getStringExtra(BLE_Service.EXTRA_NOTIFICATION));
                }

                break;
            case BLE_Service.ACTION_WRITE_SUCCESS:
                //displayData("");
                break;
        }

        try {

        } catch (Exception e) {

        }
        //       }
    }

    public boolean getConneted() {
        return mConnected;
    }

    public boolean getServiceDiscovered() {return mServiceDiscovered;}

    public boolean getDataActionAvailable() {return mDataActionAvailable;}

    private void makeGattUpdateIntentFilter() {

        intentFilter.addAction(BLE_Service.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BLE_Service.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BLE_Service.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BLE_Service.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BLE_Service.ACTION_WRITE_SUCCESS);
    }



    public IntentFilter getIntentFilter() {
        return intentFilter;
    }
}
