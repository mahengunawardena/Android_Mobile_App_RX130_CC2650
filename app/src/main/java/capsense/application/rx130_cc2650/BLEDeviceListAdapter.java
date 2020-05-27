package capsense.application.rx130_cc2650;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;


public class BLEDeviceListAdapter extends ArrayAdapter <HashMap<Integer, BLE_Device>>{

    //private ArrayAdapter<String> mBTDeviceHashMap ;
    private HashMap<Integer, BLE_Device > mBTDeviceHashMap ;
    private ArrayList<BluetoothDevice> mLeDevices;
    private LayoutInflater mInflater;
    private boolean mScanning;
    private Integer maxCount;
    Context con;


    //public BLEDeviceListAdapter (@NonNull Context context, int resource, ArrayList<String> map) {
    public BLEDeviceListAdapter (@NonNull Context context, int resource, HashMap<Integer, BLE_Device > map) {
        super(context, resource);
        //mBTDeviceHashMap = new ArrayAdapter<String>(context,android.R.layout.simple_list_item_1,android.R.id.text1,map);
        mBTDeviceHashMap = map;
        con = context;
        mInflater = (LayoutInflater) con.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mLeDevices = new ArrayList<BluetoothDevice>();
        maxCount = 0;
    }


    @Override
    public int getCount() {
        //return items.getCount();
        return  mBTDeviceHashMap.size();
    }

    public void Array_List_addDevice(BluetoothDevice device) {
        Log.d("BLE_S","BluetoothDevice :" + device );
        if (!mLeDevices.contains(device)) {
            mLeDevices.add(device);
            maxCount++;
        }
    }

    public Integer getMaxCount() {
        return maxCount;
    }

    public BluetoothDevice Array_List_getDevice(int position) {
        return mLeDevices.get(position);
    }

    public void Array_List_clear() {
        mLeDevices.clear();
        maxCount=0;
    }

    //@Override
    public int Array_List_getCount() {
        return mLeDevices.size();
    }

    //@Override
    public Object Array_List_getItem(int i) {
        return mLeDevices.get(i);
    }

    //@Override
    public long Array_List_getItemId(int i) {
        return i;
    }

    public void setmScanning (boolean flag){
        mScanning = flag;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        //return super.getView(position, convertView, parent);

        View rowView = convertView;
        // reuse views
        if (rowView == null) {

            rowView = mInflater.inflate(capsense.application.rx130_cc2650.R.layout.listitem, null);

            // configure view holder
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.tv_device_address = rowView.findViewById(capsense.application.rx130_cc2650.R.id.txtDevice_Address);
            viewHolder.tv_device_name =  rowView.findViewById(capsense.application.rx130_cc2650.R.id.txtDevice_Name);
            viewHolder.tv_rssi = rowView.findViewById(capsense.application.rx130_cc2650.R.id.txtrssi);
            viewHolder.b_connect = rowView.findViewById(capsense.application.rx130_cc2650.R.id.btn_connect);
           //viewHolder.image = (ImageView) rowView.findViewById(R.id.imgItem);
            rowView.setTag(viewHolder);
        }

        // fill data
        //BLEDeviceListAdapter.ViewHolder holder = (BLEDeviceListAdapter.ViewHolder) rowView.getTag();
        ViewHolder holder = (ViewHolder) rowView.getTag();

        //holder.text.setText(items.getItem(position).toString());
        final BLE_Device device = mBTDeviceHashMap.get(position);
        //holder.tv_device_address.setText(mBTDeviceHashMap.get(position).getAddress());
        holder.tv_device_address.setText(" ");
        if (mBTDeviceHashMap.get(position).getName()!=null) {
            holder.tv_device_name.setText(mBTDeviceHashMap.get(position).getName());
        } else {
            holder.tv_device_name.setText("Unknown");
            holder.tv_device_name.setTypeface(Typeface.DEFAULT,Typeface.ITALIC);
        }
        holder.tv_rssi.setText("   "+ mBTDeviceHashMap.get(position).getRSSI());
        holder.b_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("BLE_C", " Button Click");
                //Toast.makeText(getContext(),"Connect Button "+position, Toast.LENGTH_SHORT).show();
                if (device!= null){
                    final Intent intent = new Intent(con, DeviceControlActivity.class);
                    intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_NAME, device.getName());
                    intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
                    if (mScanning){
                        Toast.makeText(getContext()," Stop Scanning ", Toast.LENGTH_SHORT).show();
                    }
                    con.startActivity(intent);
                }
            }
        });
        //ImageView img = (ImageView ) rowView.findViewById(R.id.imgItem);
        //holder.image.setImageResource(con.getResources().getIdentifier("ico" + position, "drawable", con.getPackageName()));
        return rowView;
    }



    static class ViewHolder {
        public TextView tv_device_address;
        public TextView tv_device_name;
        public TextView tv_rssi;
        public Button b_connect;
        public ImageView image;
    }

}
