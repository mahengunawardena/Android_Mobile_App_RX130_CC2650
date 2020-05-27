package capsense.application.rx130_cc2650;

import android.bluetooth.BluetoothDevice;

public class BLE_Device {
    private BluetoothDevice mBluetoothDevice;
    private  int mRSSI;

    public BLE_Device(BluetoothDevice BluetoothDevice) {
        this.mBluetoothDevice = BluetoothDevice;
    }

    public String getAddress() {
        return mBluetoothDevice.getAddress();
    }

    public String getName() {
        return mBluetoothDevice.getName();
    }

    public void setRSSI (int rssi){
        this.mRSSI = rssi;
    }

    public int getRSSI (){
        return mRSSI;
    }

}
