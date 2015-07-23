package com.example.mdzahidul.bluetoothclient;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by Md.Zahidul on 15-Jun-15.
 */
public class BluetoothDeviceList extends Activity {

    private BluetoothAdapter adapter;
    private Set<BluetoothDevice> pairedDevices;
    List<BluetoothDevice> device;
    ListView listView;
    Intent intent;
    Boolean isSelected = false;
    SharedPreferences saveUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_bluetooth_devices);
        setTitle("Select Your Device");

        saveUrl = getSharedPreferences("urlSave",100);
        SharedPreferences.Editor editor = saveUrl.edit();
        editor.putString("isBtListEnable", "true");
        editor.commit();

        intent = getIntent();
        if(intent.getExtras() != null){
            Toast.makeText(this,intent.getStringExtra("check"),Toast.LENGTH_SHORT).show();
        }

        listView = (ListView) findViewById(R.id.lv_bt);
        device = new ArrayList<BluetoothDevice>();

        adapter = BluetoothAdapter.getDefaultAdapter();
        if(!adapter.isEnabled()){
            adapter.enable();
        }
        list();
    }

    private void list(){
        pairedDevices = adapter.getBondedDevices();
        ArrayList list = new ArrayList();

        for(BluetoothDevice bt : pairedDevices) {
            device.add(bt);
            list.add(bt.getName());
        }

        final ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            intent.putExtra("bt_address",device.get(i).getAddress());
            isSelected = true;
            setResult(RESULT_OK, intent);
            finish();
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        SharedPreferences.Editor editor = saveUrl.edit();
        editor.putString("isBtListEnable", "false");
        editor.commit();
        if(!isSelected) setResult(RESULT_CANCELED, intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
