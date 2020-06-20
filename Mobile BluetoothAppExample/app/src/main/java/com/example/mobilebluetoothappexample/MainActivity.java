package com.example.mobilebluetoothappexample;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.EventListener;
import java.util.UUID;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity {

    private static BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private byte hallValue = 0;
    TextView label;
    ProgressBar progressBar;
    ScrollView scrollView;
    Button startScan;


    public void checkIsBluetoothEnabled(AppCompatActivity appCompatActivity) {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            appCompatActivity.startActivityForResult(enableBtIntent, 0);
        }
    }

    @AfterPermissionGranted(0)
    public void requestLocationPermission(Activity activity) {
        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION};
        if (!EasyPermissions.hasPermissions(activity, perms)) {
            EasyPermissions.requestPermissions(activity, "Need permission for Bluetooth", 0, perms);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestLocationPermission(this);
        checkIsBluetoothEnabled(this);
        label = findViewById(R.id.value);
        progressBar = findViewById(R.id.Bar);
        scrollView = findViewById(R.id.scrollView);
        startScan = findViewById(R.id.startScan);
        startScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanForDevice();
            }
        });
    }

    private void scanForDevice() {
        bluetoothLeScanner.startScan(new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                if (result.getDevice().getAddress().toLowerCase().equals("24:6f:28:22:73:a2")) { // Mac ESP32
                    System.out.println("Found ESP32");
                    result.getDevice().connectGatt(MainActivity.this, true, new BluetoothGattCallback() {

                        @Override
                        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                            if (newState == BluetoothProfile.STATE_CONNECTED) {
                                gatt.discoverServices();
                            }
                        }

                        @Override
                        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                            BluetoothGattService service = gatt.getService(UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b"));
                            BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8"));
                            gatt.readCharacteristic(characteristic);
                        }

                        @Override
                        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                            System.out.println("Value of characteristic:" + characteristic.getValue()[0]);
                            hallValue = characteristic.getValue()[0];
                            setText(hallValue);
                            gatt.readCharacteristic(characteristic);
                        }

                    });
                }
            }
        });
    }

    private void setText(final int value) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setProgress(value);
                label.setText(String.valueOf(value));
                //scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }
}
