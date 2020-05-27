package capsense.application.rx130_cc2650;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static capsense.application.rx130_cc2650.R.drawable.led_dark_green;
import static capsense.application.rx130_cc2650.R.drawable.led_dark_red;
import static capsense.application.rx130_cc2650.R.drawable.led_dark_yellow;
import static capsense.application.rx130_cc2650.R.drawable.led_green;
import static capsense.application.rx130_cc2650.R.drawable.led_red;
import static capsense.application.rx130_cc2650.R.drawable.led_yellow;


public class DeviceControlActivity extends AppCompatActivity {

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private final static String TAG = DeviceControlActivity.class.getSimpleName();

    private String mDeviceName;
    private String mDeviceAddress;
    private TextView mConnectionState;

    private BluetoothGattCharacteristic characteristic_Green_LED;
    private BluetoothGattCharacteristic characteristic_Yellow_LED;
    private BluetoothGattCharacteristic characteristic_Red_LED;

    private EditText mDataField;
    private BLE_Service mBLE_Service;
    private BLE_BroadcastReceiver mbroadcastReceiver;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private HashMap<String, BluetoothGattCharacteristic> mGattCharacteristicMap = new HashMap<>();

    private static DeviceControlActivity ins;

    private LineGraphSeries<DataPoint> series;
    private GraphView graph;
    private DataPoint[] iGraphData;
    //private int index = 0;
    private int count = 100;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    private ImageView imgGreenLED = null;
    private ImageView imgYellowLED = null;
    private ImageView imgRedLED = null;
    private boolean bGreen_LED = false;
    private boolean bYellow_LED = false;
    private boolean bRed_LED = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(capsense.application.rx130_cc2650.R.layout.devicecontrolactivity);

        Toolbar toolbar = findViewById(capsense.application.rx130_cc2650.R.id.toolbar);
        toolbar.setTitle(R.string.app_label);
        setSupportActionBar(toolbar);

        ins = this;

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        ((TextView) findViewById(capsense.application.rx130_cc2650.R.id.tv_address)).setText(mDeviceAddress);

        mbroadcastReceiver = new BLE_BroadcastReceiver();
        Intent gattServiceIntent = new Intent(getApplicationContext(), BLE_Service.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        final Button btnStatus = findViewById(R.id.btnStatus);
        final ToggleButton tbGreen = findViewById(R.id.tbGreen);

        imgGreenLED = findViewById(R.id.imgGreen_LED);
        imgYellowLED = findViewById(R.id.imgYellow_LED);
        imgRedLED = findViewById(R.id.imgRed_LED);

        btnStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showStatus();
            }
        });

        tbGreen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                showStatus();
                bGreen_LED = isChecked;
                updateGreenLED();
                Green_LEDshowStatus();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mbroadcastReceiver, mbroadcastReceiver.getIntentFilter());
        if (mBLE_Service != null) {
            final boolean result = mBLE_Service.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
        Log.i(TAG, "Reached onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mbroadcastReceiver);
    }

    public static DeviceControlActivity getInstace() {
        return ins;
    }

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            BLE_Service.LocalBinder mLocalBinder = (BLE_Service.LocalBinder) service;
            mBLE_Service = mLocalBinder.getService();

            if (!mBLE_Service.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBLE_Service.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBLE_Service.close();
            mBLE_Service = null;
        }
    };

    public void updateGreenLED() {
        DeviceControlActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                final byte[] data = characteristic_Green_LED.getValue();
                if (data != null && data.length > 0) {
                    final StringBuilder stringBuilder = new StringBuilder(data.length);
                    int value = 0;
                    int number = 0;
                    for (byte byteChar : data) {
                        stringBuilder.append(String.format("%02x ", byteChar));
                        number = byteChar;
                        value = value * 255;
                        if (number < 0) {
                            number = 256 + number;
                        }
                        value = value + number;
                    }
                    if (data != null) {
                        String strData = null;
                        if (value == 0) {
                            if (bGreen_LED == true) {
                                strData = "01";
                            }
                        } else if (value == 1) {
                            if (bGreen_LED == false) {
                                strData = "00";
                            }
                        }
                        //Write 1 to Update LED
                        if ((characteristic_Green_LED != null) && (strData != null)) {
                            final int charaProp = characteristic_Green_LED.getProperties();
                            if ((charaProp | BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {

                                int len = strData.length();
                                byte[] wdata = new byte[len / 2];
                                for (int i = 0; i < len; i += 2) {
                                    wdata[i / 2] = (byte) ((Character.digit(strData.charAt(i), 16) << 4)
                                            + Character.digit(strData.charAt(i + 1), 16));
                                }
                                characteristic_Green_LED.setValue(wdata);
                                mBLE_Service.writeCharacteristic(characteristic_Green_LED);
                            }
                        }
                    }
                }
            }
        });
    }

    public void updateYellowLED() {
        DeviceControlActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                final byte[] data = characteristic_Yellow_LED.getValue();
                if (data != null && data.length > 0) {
                    final StringBuilder stringBuilder = new StringBuilder(data.length);
                    int value = 0;
                    int number = 0;
                    for (byte byteChar : data) {
                        stringBuilder.append(String.format("%02x ", byteChar));
                        number = byteChar;
                        value = value * 255;
                        if (number < 0) {
                            number = 256 + number;
                        }
                        value = value + number;
                    }
                    if (data != null) {
                        String strData = null;
                        if (value == 0) {
                            if (bYellow_LED == true) {
                                strData = "01";
                            }
                        } else if (value == 1) {
                            if (bYellow_LED == false) {
                                strData = "00";
                            }
                        }
                        //Write 1 to Update LED
                        if ((characteristic_Yellow_LED != null) && (strData != null)) {
                            final int charaProp = characteristic_Yellow_LED.getProperties();
                            if ((charaProp | BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
                                int len = strData.length();
                                byte[] wdata = new byte[len / 2];
                                for (int i = 0; i < len; i += 2) {
                                    wdata[i / 2] = (byte) ((Character.digit(strData.charAt(i), 16) << 4)
                                            + Character.digit(strData.charAt(i + 1), 16));
                                }
                                characteristic_Yellow_LED.setValue(wdata);
                                mBLE_Service.writeCharacteristic(characteristic_Yellow_LED);
                            }
                        }
                    }
                }
            }
        });
    }

    public void updateRedLED() {
        DeviceControlActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                final byte[] data = characteristic_Red_LED.getValue();
                if (data != null && data.length > 0) {
                    final StringBuilder stringBuilder = new StringBuilder(data.length);
                    int value = 0;
                    int number = 0;
                    for (byte byteChar : data) {
                        stringBuilder.append(String.format("%02x ", byteChar));
                        number = byteChar;
                        value = value * 255;
                        if (number < 0) {
                            number = 256 + number;
                        }
                        value = value + number;
                    }
                    if (data != null) {
                        String strData = null;
                        if (value == 0) {
                            if (bRed_LED == true) {
                                strData = "01";
                            }
                        } else if (value == 1) {
                            if (bRed_LED == false) {
                                strData = "00";
                            }
                        }
                        //Write 1 to Update LED
                        if ((characteristic_Red_LED != null) && (strData != null)) {
                            final int charaProp = characteristic_Red_LED.getProperties();
                            if ((charaProp | BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
                                int len = strData.length();
                                byte[] wdata = new byte[len / 2];
                                for (int i = 0; i < len; i += 2) {
                                    wdata[i / 2] = (byte) ((Character.digit(strData.charAt(i), 16) << 4)
                                            + Character.digit(strData.charAt(i + 1), 16));
                                }
                                characteristic_Red_LED.setValue(wdata);
                                mBLE_Service.writeCharacteristic(characteristic_Red_LED);
                            }
                        }
                    }
                }
            }
        });
    }


    public void updateLED(String text) {

        String input = text.replace(" ", "");
        int len = input.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(input.charAt(i), 16) << 4)
                    + Character.digit(input.charAt(i + 1), 16));
        }
        //final byte[] data = input.getBytes();//characteristic_time.getValue();
        if (data != null && data.length > 0) {
            final StringBuilder stringBuilder = new StringBuilder(data.length);
            int value = 0;
            int number = 0;
            for (byte byteChar : data) {
                stringBuilder.append(String.format("%02x ", byteChar));
                number = byteChar;
                value = value * 255;
                if (number < 0) {
                    number = 256 + number;
                }
                value = value + number;
            }
            if (value == 3) {
                updateRedLEDView(true);
                updateYellowLEDView(true);
            } else if (value == 2) {
                updateRedLEDView(false);
                updateYellowLEDView(true);
            } else if (value == 1) {
                updateRedLEDView(true);
                updateYellowLEDView(false);
             }
        }
    }

    public void updateBLEStatus(final HashMap<String, String> mBLE_statusMap) {
        DeviceControlActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                if (mBLE_statusMap.containsKey("connected")) {
                    TextView textV1 = findViewById(capsense.application.rx130_cc2650.R.id.tv_connected);
                    textV1.setText("Connected :" + mBLE_statusMap.get("connected"));
                }
                if (mBLE_statusMap.containsKey("Service_Discovered")) {
                    initGattServiceUI(mBLE_Service.getGattServices());
                }
            }
        });
    }

    /**
     * Iterate through the supported GATT Services/Characteristics,
     * and initialize UI elements displaying them.
     * <p>
     * Display Gatt Service
     */
    private void initGattServiceUI(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = getResources().getString(capsense.application.rx130_cc2650.R.string.unknown_service);
        String unknownCharaString = getResources().getString(capsense.application.rx130_cc2650.R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(LIST_NAME, capsenseAttribute.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData = new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(LIST_NAME, capsenseAttribute.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
                mGattCharacteristicMap.put(capsenseAttribute.lookup(uuid, unknownCharaString), gattCharacteristic);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }
        Log.i(TAG, "Init Gatt Service UI Connected: " + mbroadcastReceiver.getConneted() + " Service Discovered :" + mbroadcastReceiver.getServiceDiscovered());
        characteristic_Green_LED = mGattCharacteristicMap.get("Green_LED");
        characteristic_Yellow_LED = mGattCharacteristicMap.get("Yellow_LED");
        characteristic_Red_LED = mGattCharacteristicMap.get("Red_LED");
    }

    public void updateConnectionStatus(final int resourceId) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int resourceId;
                if (mbroadcastReceiver.getConneted()) {
                    resourceId = capsense.application.rx130_cc2650.R.string.connected;
                } else {
                    resourceId = capsense.application.rx130_cc2650.R.string.disconnected;
                }
                mConnectionState.setText(resourceId);
                invalidateOptionsMenu();
            }
        });
    }

    private void clearUI() {
        mDataField.setText(capsense.application.rx130_cc2650.R.string.no_data);
    }

    private void showStatus() {
        Log.i(TAG, "Show Status");
        Green_LEDshowStatus();
        Yellow_LEDshowStatus();
        Red_LEDshowStatus();
    }

    private void Green_LEDshowStatus() {
        Log.i(TAG, "Green LED Show Status");
        if (characteristic_Green_LED != null) {
            final int charaProp = characteristic_Green_LED.getProperties();
            if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                mBLE_Service.readCharacteristic(characteristic_Green_LED);
                final byte[] data = characteristic_Green_LED.getValue();
                if (data != null && data.length > 0) {
                    final StringBuilder stringBuilder = new StringBuilder(data.length);
                    int value = 0;
                    int number = 0;
                    for (byte byteChar : data) {
                        stringBuilder.append(String.format("%02x ", byteChar));
                        number = byteChar;
                        value = value * 255;
                        if (number < 0) {
                            number = 256 + number;
                        }
                        value = value + number;
                    }
                    if (value == 0) {
                        updateGreenLEDView(false);
                    } else if (value == 1) {
                        updateGreenLEDView(true);
                    }
                }
            }
        }
    }

    private void Yellow_LEDshowStatus() {
        Log.i(TAG, "Yellow LED Show Status");
        if (characteristic_Yellow_LED != null) {
            final int charaProp = characteristic_Yellow_LED.getProperties();
            if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                mBLE_Service.readCharacteristic(characteristic_Yellow_LED);
                final byte[] data = characteristic_Yellow_LED.getValue();
                if (data != null && data.length > 0) {
                    final StringBuilder stringBuilder = new StringBuilder(data.length);
                    int value = 0;
                    int number = 0;
                    for (byte byteChar : data) {
                        stringBuilder.append(String.format("%02x ", byteChar));
                        number = byteChar;
                        value = value * 255;
                        if (number < 0) {
                            number = 256 + number;
                        }
                        value = value + number;
                    }
                    if (value == 0) {
                        updateYellowLEDView(false);
                    } else if (value == 1) {
                        updateYellowLEDView(true);
                    }
                }
            }
        }
    }

    private void Red_LEDshowStatus() {
        Log.i(TAG, "Red LED Show Status");

        if (characteristic_Red_LED != null) {
            final int charaProp = characteristic_Red_LED.getProperties();
            if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                mBLE_Service.readCharacteristic(characteristic_Red_LED);
                final byte[] data = characteristic_Red_LED.getValue();
                if (data != null && data.length > 0) {
                    final StringBuilder stringBuilder = new StringBuilder(data.length);
                    int value = 0;
                    int number = 0;
                    for (byte byteChar : data) {
                        stringBuilder.append(String.format("%02x ", byteChar));
                        number = byteChar;
                        value = value * 255;
                        if (number < 0) {
                            number = 256 + number;
                        }
                        value = value + number;
                    }
                    if (value == 0) {
                        updateRedLEDView(false);
                    } else if (value == 1) {
                        updateRedLEDView(true);
                    }
                }
            }
        }
    }

    public void updateGreenLEDView(boolean status) {
        if (status == false) {
            imgGreenLED.setBackgroundResource(led_green);
        } else {
            imgGreenLED.setBackgroundResource(led_dark_green);
        }
    }

    public void updateYellowLEDView(boolean status) {
        if (status == false) {
            imgYellowLED.setBackgroundResource(led_yellow);
        } else {
            imgYellowLED.setBackgroundResource(led_dark_yellow);
        }
    }

    public void updateRedLEDView(boolean status) {
        if (status == false) {
            imgRedLED.setBackgroundResource(led_red);
        } else {
            imgRedLED.setBackgroundResource(led_dark_red);
        }
    }
}

