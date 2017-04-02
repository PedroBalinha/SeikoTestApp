package com.seikoinstruments.sdk.thermalprinter.sample;

import java.util.ArrayList;
import java.util.List;

import com.seikoinstruments.sdk.thermalprinter.PrinterEvent;
import com.seikoinstruments.sdk.thermalprinter.PrinterException;
import com.seikoinstruments.sdk.thermalprinter.PrinterInfo;
import com.seikoinstruments.sdk.thermalprinter.PrinterListener;
import com.seikoinstruments.sdk.thermalprinter.PrinterManager;
import com.seikoinstruments.sdk.thermalprinter.sample.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;


public class DeviceActivity extends Activity implements PrinterListener {

    private static final int DIALOG_BLUETOOTH_NO_SUPPORT = 101;
    private static final int DIALOG_SCAN_DEVICES = 102;

    private static final String DEVICE_NAME = "name";
    private static final String DEVICE_ADDRESS = "address";

    private static final int PRINTER_DISCOVERY_RETRY = 1;
    private static final int PRINTER_DISCOVERY_TIMEOUT = 10 * 1000;

    private PrinterManager mManager;
    private int mPortType;

    private Handler mHandler = new Handler();

    private DeviceListArrayAdapter mDeviceListAdapter;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_layout);

        // 「Devices」List
        ListView lstPairedDevices = (ListView)findViewById(R.id.list_paired_devices);
        List<DeviceListItem> deviceList = new ArrayList<DeviceListItem>();
        mDeviceListAdapter = new DeviceListArrayAdapter(getApplicationContext(), deviceList);
        lstPairedDevices.setAdapter(mDeviceListAdapter);
        lstPairedDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                ListView listView = (ListView) parent;
                final DeviceListItem item = (DeviceListItem)listView.getItemAtPosition(position);

                Intent result = new Intent();
                result.putExtra(DEVICE_NAME, item.getName());
                if(mPortType == PrinterManager.PRINTER_TYPE_BLUETOOTH) {
                    result.putExtra(DEVICE_ADDRESS, item.getMacAddress());
                } else {
                    result.putExtra(DEVICE_ADDRESS, item.getIpAddress());
                }
                setResult(RESULT_OK, result);

                finish();
            }
        });

        // 「Scan devices」Button
        Button btnScanDevices = (Button)findViewById(R.id.button_scan_devices);
        btnScanDevices.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                mDeviceListAdapter.clear();
                discoverDevices();
            }
        });

        mManager = new PrinterManager();

        final Intent intent = this.getIntent();
        mPortType = intent.getIntExtra("type", PrinterManager.PRINTER_TYPE_BLUETOOTH);
    }


    @Override
    public void onStart() {
        super.onStart();
        discoverDevices();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        mManager.cancelDiscoveryPrinter();
    }


    private void discoverDevices() {
        mDeviceListAdapter.clear();
        showDialog(DIALOG_SCAN_DEVICES);

        try {
            if(mPortType == PrinterManager.PRINTER_TYPE_BLUETOOTH) {
                mManager.startDiscoveryPrinter(this, getApplicationContext());
            } else {
                mManager.startDiscoveryPrinter(this, PRINTER_DISCOVERY_RETRY, PRINTER_DISCOVERY_TIMEOUT);
            }

        } catch (PrinterException e) {
            closeDialog(DIALOG_SCAN_DEVICES);
        }

    }


    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = super.onCreateDialog(id);
        switch(id) {
            case DIALOG_BLUETOOTH_NO_SUPPORT:
                dialog = createDialogBluetoothNoSupport();
                break;

            case DIALOG_SCAN_DEVICES:
                dialog = createProgressDialogScanDevices();
                break;
        }

        return dialog;
    }


    private Dialog createDialogBluetoothNoSupport() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(R.string.bluetooth);
        alertDialog.setMessage(R.string.bluetooth_not_supported);
        alertDialog.setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

        return alertDialog.create();
    }


    private Dialog createProgressDialogScanDevices() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        if(mPortType == PrinterManager.PRINTER_TYPE_TCP) {
            progressDialog.setTitle(R.string.tcpip);
        } else {
            progressDialog.setTitle(R.string.bluetooth);
        }
        progressDialog.setMessage(getText(R.string.scanning_devices));
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                mManager.cancelDiscoveryPrinter();
            }
          });

        return progressDialog;
    }


    private void closeDialog(int id) {
        try {
            dismissDialog(id);
        } catch(Exception e) {
        }
    }


    public void finishEvent(PrinterEvent event) {
        int eventType = event.getEventType();
        switch(eventType) {
            case PrinterEvent.EVENT_FINISHED_DISCOVERY:
            case PrinterEvent.EVENT_CANCELED_DISCOVERY:
                mHandler.post(new Runnable() {
                    public void run() {
                        ArrayList<PrinterInfo> list = mManager.getFoundPrinter();
                        int count = list.size();
                        for(int index = 0; index < count; index++) {
                            PrinterInfo info = list.get(index);
                            DeviceListItem item;
                            if(mPortType == PrinterManager.PRINTER_TYPE_BLUETOOTH) {
                                item = new DeviceListItem(info.getPrinterModelName(), info.getBluetoothAddress());
                            } else {
                                item = new DeviceListItem(info.getPrinterModelName(), info.getMacAddress(), info.getIpAddress());
                            }
                            mDeviceListAdapter.add(item);
                        }

                        closeDialog(DIALOG_SCAN_DEVICES);
                    }
                });
                break;
        }

    }

}
