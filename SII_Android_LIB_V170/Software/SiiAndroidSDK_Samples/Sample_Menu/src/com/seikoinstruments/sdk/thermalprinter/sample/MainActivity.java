package com.seikoinstruments.sdk.thermalprinter.sample;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import com.seikoinstruments.sdk.thermalprinter.printerenum.BarcodeSymbol;
import com.seikoinstruments.sdk.thermalprinter.printerenum.CharacterFont;
import com.seikoinstruments.sdk.thermalprinter.printerenum.CharacterReverse;
import com.seikoinstruments.sdk.thermalprinter.printerenum.CharacterScale;
import com.seikoinstruments.sdk.thermalprinter.printerenum.CharacterBold;
import com.seikoinstruments.sdk.thermalprinter.printerenum.CharacterUnderline;
import com.seikoinstruments.sdk.thermalprinter.printerenum.CuttingMethod;
import com.seikoinstruments.sdk.thermalprinter.printerenum.DrawerNum;
import com.seikoinstruments.sdk.thermalprinter.printerenum.ErrorCorrection;
import com.seikoinstruments.sdk.thermalprinter.printerenum.HriPosition;
import com.seikoinstruments.sdk.thermalprinter.printerenum.ModuleSize;
import com.seikoinstruments.sdk.thermalprinter.printerenum.NwRatio;
import com.seikoinstruments.sdk.thermalprinter.printerenum.Pdf417Symbol;
import com.seikoinstruments.sdk.thermalprinter.printerenum.PrintAlignment;
import com.seikoinstruments.sdk.thermalprinter.printerenum.QrModel;
import com.seikoinstruments.sdk.thermalprinter.PrinterException;
import com.seikoinstruments.sdk.thermalprinter.PrinterManager;
import com.seikoinstruments.sdk.thermalprinter.printerenum.PulseWidth;
import com.seikoinstruments.sdk.thermalprinter.sample.R;
import com.seikoinstruments.sdk.thermalprinter.sample.FileListDialog.onFileListDialogListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;


/**
 * Main Activity
 *
 */
public class MainActivity extends Activity {

    private static final int REQUEST_ENABLE_BLUETOOTH = 1;
    private static final int REQUEST_SELECT_DEVICE = 2;
    private static final int REQUEST_SETTING_PROPERTY = 3;
    private static final String BLUETOOTH_DEVICE_ADDRESS = "address";

    // Dialog ID
    /** connect */
    private static final int DIALOG_SELECT_PRINTER_MODEL = 1;
    /** sendText */
    private static final int DIALOG_INPUT_TEXT = 2;
    /** sendBinary */
    private static final int DIALOG_INPUT_BINARY = 3;
    /** getPrinterResponse */
    private static final int DIALOG_SELECT_PRINTER_RESPONSE = 4;
    /** registerLogo(mobile) */
    private static final int DIALOG_REGISTER_LOGO_ID1 = 5;
    /** registerLogo(pos) */
    private static final int DIALOG_REGISTER_LOGO_ID2 = 6;
    /** unregisterLogo(mobile) */
    private static final int DIALOG_UNREGISTER_LOGO_ID1 = 7;
    /** unregisterLogo(pos) */
    private static final int DIALOG_UNREGISTER_LOGO_ID2 = 8;
    /** registerStyleSheet */
    private static final int DIALOG_REGISTER_STYLE_SHEET_NO = 9;
    /** unregisterStyleSheet */
    private static final int DIALOG_UNREGISTER_STYLE_SHEET_NO = 10;
    /** sendTextEx */
    private static final int DIALOG_INPUT_TEXT_EX = 11;
    /** printBarcode */
    private static final int DIALOG_PRINT_BARCODE = 13;
    /** printPDF417 */
    private static final int DIALOG_PRINT_PDF417 = 15;
    /** printQRcode */
    private static final int DIALOG_PRINT_QRCODE = 16;
    /** printLogo */
    private static final int DIALOG_CUT_PAPER = 17;
    /** printLogo */
    private static final int DIALOG_OPEN_DRAWER = 18;
    /** printLogo */
    private static final int DIALOG_BUZZER = 19;
    /** printLogo(mobile) */
    private static final int DIALOG_PRINT_LOGO1 = 20;
    /** printLogo(pos) */
    private static final int DIALOG_PRINT_LOGO2 = 21;
    /** sendDataFile */
    private static final int DIALOG_SEND_DATA_FILE = 22;


    /** Finish Application */
    private static final int DIALOG_FINISH_APP = 99;
    /** Bluetooth no support */
    private static final int DIALOG_BLUETOOTH_NO_SUPPORT = 101;

    /** PrinterManager SDK */
    private PrinterManager mPrinterManager;

    /** Window rotation */
    private ArrayList<PrinterManager> mSaveList;

    /** Select port */
    private int mSelectPort = PrinterManager.PRINTER_TYPE_BLUETOOTH;

    /** Select file */
    private File mSelectFile = null;

    /** Line feed Escape sequence**/
    private static final String LINE_FEED = "\n";    

    /** Called when the activity is first created. */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        setContentView(R.layout.main);

        final EditText edtDeviceAddress = (EditText)findViewById(R.id.edittext_device_address);
        final Button btnDeviceList = (Button)findViewById(R.id.button_device_list);

        // Window rotation
        mSaveList = (ArrayList<PrinterManager>)getLastNonConfigurationInstance();
        if(mSaveList != null) {
            mPrinterManager = mSaveList.get(0);
            if(mPrinterManager.getPortType() != PrinterManager.PRINTER_TYPE_USB) {
                edtDeviceAddress.setEnabled(true);
                btnDeviceList.setEnabled(true);
            } else {
                edtDeviceAddress.setEnabled(false);
                btnDeviceList.setEnabled(false);
            }
        } else {
            mPrinterManager = new PrinterManager();
            SampleApplication application = (SampleApplication)this.getApplication();
            application.setPrinterManager(mPrinterManager);
        }

        // [Connection type]Radio Button
        RadioGroup radioGroup = (RadioGroup)findViewById(R.id.radiogroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton radioButton = (RadioButton)findViewById(checkedId);
                String text = radioButton.getText().toString();
                if(text.equals(getText(R.string.usb).toString())) {
                    mSelectPort = PrinterManager.PRINTER_TYPE_USB;
                    edtDeviceAddress.setEnabled(false);
                    btnDeviceList.setEnabled(false);
                } else if(text.equals(getText(R.string.tcpip).toString())
                        || text.equals(getText(R.string.tcp).toString())) {
                    mSelectPort = PrinterManager.PRINTER_TYPE_TCP;
                    edtDeviceAddress.setEnabled(true);
                    btnDeviceList.setEnabled(true);
                } else {
                    mSelectPort = PrinterManager.PRINTER_TYPE_BLUETOOTH;
                    edtDeviceAddress.setEnabled(true);
                    btnDeviceList.setEnabled(true);
                }
            }
        });

        // [List]Button
        if(btnDeviceList != null) {
            btnDeviceList.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    Intent intent = new Intent(MainActivity.this, DeviceActivity.class);
                    intent.putExtra("type", mSelectPort);
                    if(mSelectPort == PrinterManager.PRINTER_TYPE_TCP) {
                        intent.putExtra("retry", 1);
                    } else {
                        intent.putExtra("retry", 0);
                    }
                    startActivityForResult(intent, REQUEST_SELECT_DEVICE);
                }
            });
        }

        // [connect]Button
        Button btnConnect = (Button)findViewById(R.id.button_connect);
        if(btnConnect != null) {
            btnConnect.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    if (!isFinishing()) {
                        showDialog(DIALOG_SELECT_PRINTER_MODEL);
                    }
                }
            });
        }

        // [disconnect]Button
        Button btnDisconnect = (Button)findViewById(R.id.button_disconnect);
        if(btnDisconnect != null) {
            btnDisconnect.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    writeLog("disconnect", true);

                    int ret = 0;
                    String msg;
                    try {
                        mPrinterManager.disconnect();
                        msg = "disconnect OK.";
                    } catch(PrinterException e) {
                        ret = e.getErrorCode();
                        msg = "disconnect NG.[" + ret + "]";
                    }
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();

                    writeLog("disconnect", false, ret, "");
                }
            });
        }

        // [sendText]Button
        Button btnSendText = (Button)findViewById(R.id.button_send_text);
        if(btnSendText != null) {
            btnSendText.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    if (!isFinishing()) {
                        showDialog(DIALOG_INPUT_TEXT);
                    }
                }
            });
        }

        // [sendTextEx]Button
        Button btnSendTextEx = (Button)findViewById(R.id.button_send_text_ex);
        if(btnSendTextEx != null) {
            btnSendTextEx.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    if (!isFinishing()) {
                        showDialog(DIALOG_INPUT_TEXT_EX);
                    }
                }
            });
        }

        // [printBarcode]Button
        Button btnPrintBarcode = (Button)findViewById(R.id.button_print_barcode);
        if(btnPrintBarcode != null) {
            btnPrintBarcode.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    showDialog(DIALOG_PRINT_BARCODE);
                }
            });
        }

        // [printPDF417]Button
        Button btnPrintPdf417 = (Button)findViewById(R.id.button_print_pdf417);
        if(btnPrintPdf417 != null) {
            btnPrintPdf417.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    if (!isFinishing()) {
                        showDialog(DIALOG_PRINT_PDF417);
                    }
                }
            });
        }

        // [printQRcode]Button
        Button btnPrintQrcode = (Button)findViewById(R.id.button_print_qrcode);
        if(btnPrintQrcode != null) {
            btnPrintQrcode.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    if (!isFinishing()) {
                        showDialog(DIALOG_PRINT_QRCODE);
                    }
                }
            });
        }

        // [cutPaper]Button
        Button btnCutPaper = (Button)findViewById(R.id.button_cut_paper);
        if(btnCutPaper != null) {
            btnCutPaper.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    if (!isFinishing()) {
                        showDialog(DIALOG_CUT_PAPER);
                    }
                }
            });
        }

        // [openDrawer]Button
        Button btnOpenDrawer = (Button)findViewById(R.id.button_print_open_drawer);
        if(btnOpenDrawer != null) {
            btnOpenDrawer.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    if (!isFinishing()) {
                        showDialog(DIALOG_OPEN_DRAWER);
                    }
                }
            });
        }

        // [buzzer]Button
        Button btnBuzzer = (Button)findViewById(R.id.button_buzzer);
        if(btnBuzzer != null) {
            btnBuzzer.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    if (!isFinishing()) {
                        showDialog(DIALOG_BUZZER);
                    }
                }
            });
        }

        // [sendBinary]Button
        Button btnSendBinary = (Button)findViewById(R.id.button_send_binary);
        if(btnSendBinary != null) {
            btnSendBinary.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    if (!isFinishing()) {
                        showDialog(DIALOG_INPUT_BINARY);
                    }
                }
            });
        }


        // [sendDataFile]Button
        Button btnSendDataFile = (Button)findViewById(R.id.button_send_data_file);
        if(btnSendDataFile != null) {
            btnSendDataFile.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    if (!isFinishing()) {
                        FileListDialog dialog = new FileListDialog(MainActivity.this);
                        dialog.setDirectorySelect(false);
                        dialog.setOnFileListDialogListener(new onFileListDialogListener() {
                            public void onClickFileList(File file) {
                                if(file == null){
                                    //not select
                                }else{
                                    mSelectFile = file;
                                    if (!isFinishing()) {
                                        int printerModel = mPrinterManager.getPrinterModel();
                                        if((printerModel == PrinterManager.PRINTER_MODEL_RP_E10)
                                                || (printerModel == PrinterManager.PRINTER_MODEL_RP_D10)) {
                                            showDialog(DIALOG_SEND_DATA_FILE);
                                        } else {
                                            writeLog("sendDataFile", true);

                                            int ret = 0;
                                            String msg;
                                            try {
                                                mPrinterManager.sendDataFile(file.getAbsolutePath());
                                                msg = "sendDataFile OK.";
                                            } catch(PrinterException e) {
                                                ret = e.getErrorCode();
                                                msg = "sendDataFile NG.[" + ret + "]";
                                            }
                                            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();

                                            writeLog("sendDataFile", false, ret, "");
                                        }
                                    }
                                }
                            }
                        });
                        dialog.show("/", "Select file.");
                    }
                }
            });
        }

        // [abort]Button
        Button btnAbort = (Button)findViewById(R.id.button_abort);
        if(btnAbort != null) {
            btnAbort.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    writeLog("abort", true);

                    int ret = 0;
                    String msg;
                    try {
                        mPrinterManager.abort();
                        msg = "abort OK.";
                    } catch(PrinterException e) {
                        ret = e.getErrorCode();
                        msg = "abort NG.[" + ret + "]";
                    }
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();

                    writeLog("abort", false, ret, "");
                }
            });
        }


        // [getStatus]Button
        Button btnGetStatus = (Button)findViewById(R.id.button_get_status);
        if(btnGetStatus != null) {
            btnGetStatus.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    writeLog("getStatus", true);

                    int[] buf = new int[1];
                    int ret = 0;
                    String msg;
                    try {
                        mPrinterManager.getStatus(buf);
                        msg = "getStatus OK. Status:0x" + String.format("%08X", buf[0]);
                    } catch(PrinterException e) {
                        ret = e.getErrorCode();
                        msg = "getStatus NG.[" + ret + "]";
                    }
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();

                    writeLog("getStatus", false, ret, "Status:0x" + String.format("%08X", buf[0]));
                }
            });
        }

        // [getPrinterResponse]Button
        Button btnGetPrinterResponse = (Button)findViewById(R.id.button_get_printer_response);
        if(btnGetPrinterResponse != null) {
            btnGetPrinterResponse.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    if (!isFinishing()) {
                        showDialog(DIALOG_SELECT_PRINTER_RESPONSE);
                    }
                }
            });
        }

        // [registerLogo]Button
        Button btnRegisterLogo = (Button)findViewById(R.id.button_register_logo);
        if(btnRegisterLogo != null) {
            btnRegisterLogo.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    if (!isFinishing()) {
                        FileListDialog dialog = new FileListDialog(MainActivity.this);
                        dialog.setDirectorySelect(false);
                        dialog.setOnFileListDialogListener(new onFileListDialogListener() {
                            public void onClickFileList(File file) {
                                if(file == null){
                                    //not select
                                }else{
                                    mSelectFile = file;
                                    if (!isFinishing()) {
                                        int printerModel = mPrinterManager.getPrinterModel();
                                        if((printerModel == PrinterManager.PRINTER_MODEL_RP_E10)
                                                || (printerModel == PrinterManager.PRINTER_MODEL_RP_D10)) {
                                            showDialog(DIALOG_REGISTER_LOGO_ID2);
                                        } else {
                                            showDialog(DIALOG_REGISTER_LOGO_ID1);
                                        }
                                    }
                                }
                            }
                        });
                        dialog.show("/", "Select file.");
                    }
                }
            });
        }

        // [printLogo]Button
        Button btnPrintLogo = (Button)findViewById(R.id.button_prin_logo);
        if(btnPrintLogo != null) {
            btnPrintLogo.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    if (!isFinishing()) {
                        int printerModel = mPrinterManager.getPrinterModel();
                        if((printerModel == PrinterManager.PRINTER_MODEL_RP_E10)
                                || (printerModel == PrinterManager.PRINTER_MODEL_RP_D10)) {
                            showDialog(DIALOG_PRINT_LOGO2);
                        } else {
                            showDialog(DIALOG_PRINT_LOGO1);
                        }
                    }
                }
            });
        }

        // [unregisterLogo]Button
        Button btnUnregisterLogo = (Button)findViewById(R.id.button_unregister_logo);
        if(btnUnregisterLogo != null) {
            btnUnregisterLogo.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    if (!isFinishing()) {
                        int printerModel = mPrinterManager.getPrinterModel();
                        if((printerModel == PrinterManager.PRINTER_MODEL_RP_E10)
                                || (printerModel == PrinterManager.PRINTER_MODEL_RP_D10)) {
                            showDialog(DIALOG_UNREGISTER_LOGO_ID2);
                        } else {
                            showDialog(DIALOG_UNREGISTER_LOGO_ID1);
                        }
                    }
                }
            });
        }

        // [registerStyleSheet]Button
        Button btnRegisterStyleSheet = (Button)findViewById(R.id.button_register_style_sheet);
        if(btnRegisterStyleSheet != null) {
            btnRegisterStyleSheet.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    if (!isFinishing()) {
                        FileListDialog dialog = new FileListDialog(MainActivity.this);
                        dialog.setDirectorySelect(false);
                        dialog.setOnFileListDialogListener(new onFileListDialogListener() {
                            public void onClickFileList(File file) {
                                if(file == null){
                                    //not select
                                }else{
                                    mSelectFile = file;
                                    if (!isFinishing()) {
                                        showDialog(DIALOG_REGISTER_STYLE_SHEET_NO);
                                    }
                                }
                            }
                        });
                        dialog.show("/", "Select file.");
                    }
                }
            });
        }

        // [unregisterStyleSheet]Button
        Button btnUnregisterStyleSheet = (Button)findViewById(R.id.button_unregister_style_sheet);
        if(btnUnregisterStyleSheet != null) {
            btnUnregisterStyleSheet.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    if (!isFinishing()) {
                        showDialog(DIALOG_UNREGISTER_STYLE_SHEET_NO);
                    }
                }
            });
        }

        // [resetPrinter]Button
        Button btnResetPrinter = (Button)findViewById(R.id.button_reset_printer);
        if(btnResetPrinter != null) {
            btnResetPrinter.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    writeLog("resetPrinter", true);

                    int ret = 0;
                    String msg;
                    try {
                        mPrinterManager.resetPrinter();
                        msg = "resetPrinter OK.";
                    } catch(PrinterException e) {
                        ret = e.getErrorCode();
                        msg = "resetPrinter NG.[" + ret + "]";
                    }
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();

                    writeLog("resetPrinter", false, ret, "");
                }
            });
        }

        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        Editor editor = pref.edit();
        if(Locale.JAPAN.equals(Locale.getDefault())) {
            editor.putString(getText(R.string.key_international_character).toString(), "8");
            editor.putString(getText(R.string.key_code_page).toString(), "1");
        } else {
            editor.putString(getText(R.string.key_international_character).toString(), "0");
            editor.putString(getText(R.string.key_code_page).toString(), "16");
        }
        editor.commit();

    }


    @Override
    public void onStart() {
        super.onStart();

        checkBluetooth();
    }


    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void onPause() {
        super.onPause();
    }


    @Override
    public void onRestart() {
        super.onRestart();
    }


    @Override
    public void onStop() {
        super.onStop();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    public void finish() {
        showDialog(DIALOG_FINISH_APP);
    }


    private void finishApp() {
        try {
            mPrinterManager.disconnect();
        } catch (PrinterException e) {
        }

        super.finish();
    }


    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_menu, menu);

        return true;
    }


    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_settings:
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivityForResult(intent, REQUEST_SETTING_PROPERTY);
                return true;
        }

        return false;
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case REQUEST_SELECT_DEVICE:
                if(resultCode == RESULT_OK) {
                    EditText edtDeviceAddress = (EditText)findViewById(R.id.edittext_device_address);
                    edtDeviceAddress.setText(data.getStringExtra(BLUETOOTH_DEVICE_ADDRESS));
                }
                break;

            case REQUEST_SETTING_PROPERTY:
                setProperty();
                break;

            case REQUEST_ENABLE_BLUETOOTH:
                if(resultCode == RESULT_OK) {
                    Toast.makeText(getApplicationContext(), R.string.enabled_bluetooth, Toast.LENGTH_LONG);
                }
                break;
        }
    }


    public Object onRetainNonConfigurationInstance() {
        mSaveList = new ArrayList<PrinterManager>(1);
        mSaveList.add(mPrinterManager);

        return mSaveList;
    }


    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = super.onCreateDialog(id);

        switch(id) {
            case DIALOG_SELECT_PRINTER_MODEL:
                dialog = createDialogSelectPrinterModel();
                break;

            case DIALOG_INPUT_TEXT:
                dialog = createDialogInputText();
                break;

            case DIALOG_INPUT_TEXT_EX:
                dialog = createDialogInputTextEx();
                break;

            case DIALOG_PRINT_BARCODE:
                dialog = createDialogPrintBarcode();
                break;

            case DIALOG_PRINT_PDF417:
                dialog = createDialogPrintPdf417();
                break;

            case DIALOG_PRINT_QRCODE:
                dialog = createDialogPrintQrcode();
                break;

            case DIALOG_CUT_PAPER:
                dialog = createDialogCutPaper();
                break;

            case DIALOG_OPEN_DRAWER:
                dialog = createDialogOpenDrawer();
                break;

            case DIALOG_BUZZER:
                dialog = createDialogBuzzer();
                break;

            case DIALOG_INPUT_BINARY:
                dialog = createDialogInputBinary();
                break;

            case DIALOG_SEND_DATA_FILE:
                dialog = createDialogSendDataFile();
                break;

            case DIALOG_SELECT_PRINTER_RESPONSE:
                dialog = createDialogSelectPrinterResponse();
                break;

            case DIALOG_REGISTER_LOGO_ID1:
                dialog = createDialogRegisterLogoID1();
                break;

            case DIALOG_REGISTER_LOGO_ID2:
                dialog = createDialogRegisterLogoID2();
                break;

            case DIALOG_PRINT_LOGO1:
                dialog = createDialogPrintLogoID1();
                break;

            case DIALOG_PRINT_LOGO2:
                dialog = createDialogPrintLogoID2();
                break;

            case DIALOG_UNREGISTER_LOGO_ID1:
                dialog = createDialogUnregisterLogoID1();
                break;

            case DIALOG_UNREGISTER_LOGO_ID2:
                dialog = createDialogUnregisterLogoID2();
                break;

            case DIALOG_REGISTER_STYLE_SHEET_NO:
                dialog = createDialogRegisterStyleSheetNo();
                break;

            case DIALOG_UNREGISTER_STYLE_SHEET_NO:
                dialog = createDialogUnregisterStyleSheetNo();
                break;

            case DIALOG_FINISH_APP:
                dialog = createDialogConfirmFinishApp();
                break;

            case DIALOG_BLUETOOTH_NO_SUPPORT:
                dialog = createDialogBluetoothNoSupport();
                break;

        }

        return dialog;
    }


    private Dialog createDialogSelectPrinterModel() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(R.string.dialog_connect_title);
        alertDialog.setItems(R.array.printer_model_list, new DialogInterface.OnClickListener() {
            private String[] printerModels = getResources().getStringArray(R.array.printer_model_values_list);
            public void onClick(DialogInterface dialog, int which) {

                int model = Integer.parseInt(printerModels[which]);
                EditText edtDeviceAddress = (EditText)findViewById(R.id.edittext_device_address);
                writeLog("connect", true, 0, edtDeviceAddress.getText().toString());

                int ret = 0;
                String msg;
                try {
                    setProperty();
                    switch(mSelectPort) {
                        case PrinterManager.PRINTER_TYPE_USB:
                            mPrinterManager.connect(model, getApplicationContext());
                            break;

                        case PrinterManager.PRINTER_TYPE_TCP:
                            mPrinterManager.connect(model, edtDeviceAddress.getText().toString());
                            break;

                        default:
                            final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                            final boolean secure = pref.getBoolean(getString(R.string.key_secure_connection), true);
                            mPrinterManager.connect(model, edtDeviceAddress.getText().toString(), secure);
                    }
                    msg = "connect OK.";
                } catch(PrinterException e) {
                    ret = e.getErrorCode();
                    msg = "connect NG.[" + ret + "]";
                }
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();

                writeLog("connect", false, ret, "");
            }
        });

        return alertDialog.create();
    }


    private Dialog createDialogInputText() {
        final EditText editView = new EditText(MainActivity.this);
        editView.setInputType(InputType.TYPE_CLASS_TEXT);
        InputFilter[] inputFilter = new InputFilter[1];
        inputFilter[0] = new InputFilter.LengthFilter(256);
        editView.setFilters(inputFilter);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setIcon(android.R.drawable.ic_dialog_info);
        alertDialog.setTitle(R.string.dialog_send_text_title);
        alertDialog.setView(editView);
        alertDialog.setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String text = editView.getText().toString();
                        writeLog("sendText", true, 0, text);
                        text += LINE_FEED;

                        int ret = 0;
                        String msg;
                        try {
                            mPrinterManager.sendText(text);
                            msg = "sendText OK.";
                        } catch(PrinterException e) {
                            ret = e.getErrorCode();
                            msg = "sendText NG.[" + ret + "]";
                        }
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();

                        writeLog("sendText", false, ret, "");

                        editView.setText("");
                    }
                });
        alertDialog.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        return alertDialog.create();
    }


    private Dialog createDialogInputTextEx() {
        LayoutInflater inflater = (LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.send_text_ex_layout, (ViewGroup)findViewById(R.id.layout_send_text_ex));

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setIcon(android.R.drawable.ic_dialog_info);
        alertDialog.setTitle(R.string.dialog_send_text_title);
        alertDialog.setView(layout);
        alertDialog.setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        EditText edtText = (EditText)layout.findViewById(R.id.edittext_text);
                        String text = edtText.getText().toString();
                        edtText.setText("");
                        writeLog("sendTextEx", true, 0, text);
                        text += LINE_FEED;

                        CheckBox chkBold = (CheckBox)layout.findViewById(R.id.checkbox_bold);
                        CharacterBold bold;
                        if(chkBold.isChecked()) {
                            bold = CharacterBold.BOLD;
                        } else {
                            bold = CharacterBold.BOLD_CANCEL;
                        }
                        chkBold.setChecked(false);

                        Spinner spnrUnderline = (Spinner)layout.findViewById(R.id.spinner_underline);
                        String underlineItem = (String)spnrUnderline.getSelectedItem();
                        CharacterUnderline underline;
                        if(underlineItem.equals("UNDERLINE_CANCEL")) {
                            underline = CharacterUnderline.UNDERLINE_CANCEL;
                        } else if(underlineItem.equals("UNDERLINE_1")) {
                            underline = CharacterUnderline.UNDERLINE_1;
                        } else if(underlineItem.equals("UNDERLINE_2")) {
                            underline = CharacterUnderline.UNDERLINE_2;
                        } else {
                            underline = CharacterUnderline.UNDERLINE_CANCEL;
                        }
                        spnrUnderline.setSelection(0);

                        CheckBox chkReverse = (CheckBox)layout.findViewById(R.id.checkbox_reverse);
                        CharacterReverse reverse;
                        if(chkReverse.isChecked()) {
                            reverse = CharacterReverse.REVERSE;
                        } else {
                            reverse = CharacterReverse.REVERSE_CANCEL;
                        }
                        chkReverse.setChecked(false);

                        Spinner spnrFont = (Spinner)layout.findViewById(R.id.spinner_font);
                        String fontItem = (String)spnrFont.getSelectedItem();
                        CharacterFont font;
                        if(fontItem.equals("FONT_A")) {
                            font = CharacterFont.FONT_A;
                        } else if(fontItem.equals("FONT_B")) {
                            font = CharacterFont.FONT_B;
                        } else {
                            font = CharacterFont.FONT_A;
                        }
                        spnrFont.setSelection(0);

                        Spinner spnrScale = (Spinner)layout.findViewById(R.id.spinner_scale);
                        String sizeItem = (String)spnrScale.getSelectedItem();
                        CharacterScale scale;
                        if(sizeItem.equals("VARTICAL_1_HORIZONTAL_1")) {
                            scale = CharacterScale.VARTICAL_1_HORIZONTAL_1;
                        } else if(sizeItem.equals("VARTICAL_1_HORIZONTAL_2")) {
                            scale = CharacterScale.VARTICAL_1_HORIZONTAL_2;
                        } else if(sizeItem.equals("VARTICAL_1_HORIZONTAL_3")) {
                            scale = CharacterScale.VARTICAL_1_HORIZONTAL_3;
                        } else if(sizeItem.equals("VARTICAL_1_HORIZONTAL_4")) {
                            scale = CharacterScale.VARTICAL_1_HORIZONTAL_4;
                        } else if(sizeItem.equals("VARTICAL_2_HORIZONTAL_1")) {
                            scale = CharacterScale.VARTICAL_2_HORIZONTAL_1;
                        } else if(sizeItem.equals("VARTICAL_2_HORIZONTAL_2")) {
                            scale = CharacterScale.VARTICAL_2_HORIZONTAL_2;
                        } else if(sizeItem.equals("VARTICAL_2_HORIZONTAL_3")) {
                            scale = CharacterScale.VARTICAL_2_HORIZONTAL_3;
                        } else if(sizeItem.equals("VARTICAL_2_HORIZONTAL_4")) {
                            scale = CharacterScale.VARTICAL_2_HORIZONTAL_4;
                        } else if(sizeItem.equals("VARTICAL_2_HORIZONTAL_6")) {
                            scale = CharacterScale.VARTICAL_2_HORIZONTAL_6;
                        } else if(sizeItem.equals("VARTICAL_3_HORIZONTAL_1")) {
                            scale = CharacterScale.VARTICAL_3_HORIZONTAL_1;
                        } else if(sizeItem.equals("VARTICAL_3_HORIZONTAL_2")) {
                            scale = CharacterScale.VARTICAL_3_HORIZONTAL_2;
                        } else if(sizeItem.equals("VARTICAL_3_HORIZONTAL_3")) {
                            scale = CharacterScale.VARTICAL_3_HORIZONTAL_3;
                        } else if(sizeItem.equals("VARTICAL_3_HORIZONTAL_4")) {
                            scale = CharacterScale.VARTICAL_3_HORIZONTAL_4;
                        } else if(sizeItem.equals("VARTICAL_4_HORIZONTAL_1")) {
                            scale = CharacterScale.VARTICAL_4_HORIZONTAL_1;
                        } else if(sizeItem.equals("VARTICAL_4_HORIZONTAL_2")) {
                            scale = CharacterScale.VARTICAL_4_HORIZONTAL_2;
                        } else if(sizeItem.equals("VARTICAL_4_HORIZONTAL_3")) {
                            scale = CharacterScale.VARTICAL_4_HORIZONTAL_3;
                        } else if(sizeItem.equals("VARTICAL_4_HORIZONTAL_4")) {
                            scale = CharacterScale.VARTICAL_4_HORIZONTAL_4;
                        } else if(sizeItem.equals("VARTICAL_4_HORIZONTAL_6")) {
                            scale = CharacterScale.VARTICAL_4_HORIZONTAL_6;
                        } else if(sizeItem.equals("VARTICAL_4_HORIZONTAL_8")) {
                            scale = CharacterScale.VARTICAL_4_HORIZONTAL_8;
                        } else if(sizeItem.equals("VARTICAL_6_HORIZONTAL_2")) {
                            scale = CharacterScale.VARTICAL_6_HORIZONTAL_2;
                        } else if(sizeItem.equals("VARTICAL_6_HORIZONTAL_4")) {
                            scale = CharacterScale.VARTICAL_6_HORIZONTAL_4;
                        } else if(sizeItem.equals("VARTICAL_6_HORIZONTAL_6")) {
                            scale = CharacterScale.VARTICAL_6_HORIZONTAL_6;
                        } else if(sizeItem.equals("VARTICAL_6_HORIZONTAL_8")) {
                            scale = CharacterScale.VARTICAL_6_HORIZONTAL_8;
                        } else if(sizeItem.equals("VARTICAL_8_HORIZONTAL_4")) {
                            scale = CharacterScale.VARTICAL_8_HORIZONTAL_4;
                        } else if(sizeItem.equals("VARTICAL_8_HORIZONTAL_6")) {
                            scale = CharacterScale.VARTICAL_8_HORIZONTAL_6;
                        } else if(sizeItem.equals("VARTICAL_8_HORIZONTAL_8")) {
                            scale = CharacterScale.VARTICAL_8_HORIZONTAL_8;
                        } else {
                            scale = CharacterScale.VARTICAL_1_HORIZONTAL_1;
                        }
                        spnrScale.setSelection(0);

                        Spinner spnrAliment = (Spinner)layout.findViewById(R.id.spinner_alignment);
                        String alignmentItem = (String)spnrAliment.getSelectedItem();
                        PrintAlignment alignment;
                        if(alignmentItem.equals("ALIGNMENT_LEFT")) {
                            alignment = PrintAlignment.ALIGNMENT_LEFT;
                        } else if(alignmentItem.equals("ALIGNMENT_CENTER")) {
                            alignment = PrintAlignment.ALIGNMENT_CENTER;
                        } else if(alignmentItem.equals("ALIGNMENT_RIGHT")) {
                            alignment = PrintAlignment.ALIGNMENT_RIGHT;
                        } else {
                            alignment = PrintAlignment.ALIGNMENT_LEFT;
                        }
                        spnrAliment.setSelection(0);

                        int ret = 0;
                        String msg;
                        try {
                            int printerModel = mPrinterManager.getPrinterModel();
                            if((printerModel == PrinterManager.PRINTER_MODEL_RP_E10)
                                    || (printerModel == PrinterManager.PRINTER_MODEL_RP_D10)) {
                                mPrinterManager.sendTextEx(
                                        text,
                                        bold,
                                        underline,
                                        reverse,
                                        font,
                                        scale,
                                        alignment);
                            } else {
                                mPrinterManager.sendTextEx(
                                        text,
                                        bold,
                                        underline,
                                        font,
                                        scale);
                            }

                            msg = "sendTextEx OK.";
                        } catch(PrinterException e) {
                            ret = e.getErrorCode();
                            msg = "sendTextEx NG.[" + ret + "]";
                        }
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();

                        writeLog("sendTextEx", false, ret, "");
                    }
                });
        alertDialog.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        return alertDialog.create();
    }


    private Dialog createDialogPrintBarcode() {
        LayoutInflater inflater = (LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.print_barcode_layout, (ViewGroup)findViewById(R.id.layout_print_barcode));

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setIcon(android.R.drawable.ic_dialog_info);
        alertDialog.setTitle(R.string.dialog_print_barcode_title);
        alertDialog.setView(layout);
        alertDialog.setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        EditText edtData = (EditText)layout.findViewById(R.id.edittext_data);
                        String data = edtData.getText().toString();
                        edtData.setText("");
                        writeLog("printBarcode", true, 0, data);

                        byte[] byteData = null;
                        Spinner spnrBarcodeSymbol = (Spinner)layout.findViewById(R.id.spinner_barcode_symbol);
                        String barcodeTypeItem = (String)spnrBarcodeSymbol.getSelectedItem();
                        BarcodeSymbol barcodeSymbol;
                        if(barcodeTypeItem.equals("BARCODE_SYMBOL_UPC_A")) {
                            barcodeSymbol = BarcodeSymbol.BARCODE_SYMBOL_UPC_A;
                        } else if(barcodeTypeItem.equals("BARCODE_SYMBOL_UPC_E")) {
                            barcodeSymbol = BarcodeSymbol.BARCODE_SYMBOL_UPC_E;
                        } else if(barcodeTypeItem.equals("BARCODE_SYMBOL_EAN13")) {
                            barcodeSymbol = BarcodeSymbol.BARCODE_SYMBOL_EAN13;
                        } else if(barcodeTypeItem.equals("BARCODE_SYMBOL_JAN13")) {
                            barcodeSymbol = BarcodeSymbol.BARCODE_SYMBOL_JAN13;
                        } else if(barcodeTypeItem.equals("BARCODE_SYMBOL_EAN8")) {
                            barcodeSymbol = BarcodeSymbol.BARCODE_SYMBOL_EAN8;
                        } else if(barcodeTypeItem.equals("BARCODE_SYMBOL_JAN8")) {
                            barcodeSymbol = BarcodeSymbol.BARCODE_SYMBOL_JAN8;
                        } else if(barcodeTypeItem.equals("BARCODE_SYMBOL_CODE39")) {
                            barcodeSymbol = BarcodeSymbol.BARCODE_SYMBOL_CODE39;
                        } else if(barcodeTypeItem.equals("BARCODE_SYMBOL_CODE93")) {
                            barcodeSymbol = BarcodeSymbol.BARCODE_SYMBOL_CODE93;
                            byteData = asByteArray(data);
                        } else if(barcodeTypeItem.equals("BARCODE_SYMBOL_CODE128")) {
                            barcodeSymbol = BarcodeSymbol.BARCODE_SYMBOL_CODE128;
                            byteData = asByteArray(data);
                        } else if(barcodeTypeItem.equals("BARCODE_SYMBOL_ITF")) {
                            barcodeSymbol = BarcodeSymbol.BARCODE_SYMBOL_ITF;
                        } else if(barcodeTypeItem.equals("BARCODE_SYMBOL_CODABAR")) {
                            barcodeSymbol = BarcodeSymbol.BARCODE_SYMBOL_CODABAR;
                        } else if(barcodeTypeItem.equals("BARCODE_SYMBOL_EAN13_ADDON")) {
                            barcodeSymbol = BarcodeSymbol.BARCODE_SYMBOL_EAN13_ADDON;
                        } else if(barcodeTypeItem.equals("BARCODE_SYMBOL_JAN13_ADDON")) {
                            barcodeSymbol = BarcodeSymbol.BARCODE_SYMBOL_JAN13_ADDON;
                        } else {
                            barcodeSymbol = BarcodeSymbol.BARCODE_SYMBOL_UPC_A;
                        }
                        spnrBarcodeSymbol.setSelection(0);

                        Spinner spnrModuleWidth = (Spinner)layout.findViewById(R.id.spinner_module_size);
                        String moduleWidthItem = (String)spnrModuleWidth.getSelectedItem();
                        ModuleSize moduleWidth;
                        if(moduleWidthItem.equals("BARCODE_MODULE_WIDTH_2")) {
                            moduleWidth = ModuleSize.BARCODE_MODULE_WIDTH_2;
                        } else if(moduleWidthItem.equals("BARCODE_MODULE_WIDTH_3")) {
                            moduleWidth = ModuleSize.BARCODE_MODULE_WIDTH_3;
                        } else if(moduleWidthItem.equals("BARCODE_MODULE_WIDTH_4")) {
                            moduleWidth = ModuleSize.BARCODE_MODULE_WIDTH_4;
                        } else if(moduleWidthItem.equals("BARCODE_MODULE_WIDTH_5")) {
                            moduleWidth = ModuleSize.BARCODE_MODULE_WIDTH_5;
                        } else if(moduleWidthItem.equals("BARCODE_MODULE_WIDTH_6")) {
                            moduleWidth = ModuleSize.BARCODE_MODULE_WIDTH_6;
                        } else if(moduleWidthItem.equals("QR_MODULE_SIZE_2")) {
                            moduleWidth = ModuleSize.QR_MODULE_SIZE_2;
                        } else if(moduleWidthItem.equals("QR_MODULE_SIZE_3")) {
                            moduleWidth = ModuleSize.QR_MODULE_SIZE_3;
                        } else if(moduleWidthItem.equals("QR_MODULE_SIZE_4")) {
                            moduleWidth = ModuleSize.QR_MODULE_SIZE_4;
                        } else if(moduleWidthItem.equals("QR_MODULE_SIZE_5")) {
                            moduleWidth = ModuleSize.QR_MODULE_SIZE_5;
                        } else if(moduleWidthItem.equals("QR_MODULE_SIZE_6")) {
                            moduleWidth = ModuleSize.QR_MODULE_SIZE_6;
                        } else if(moduleWidthItem.equals("QR_MODULE_SIZE_7")) {
                            moduleWidth = ModuleSize.QR_MODULE_SIZE_7;
                        } else if(moduleWidthItem.equals("QR_MODULE_SIZE_8")) {
                            moduleWidth = ModuleSize.QR_MODULE_SIZE_8;
                        } else if(moduleWidthItem.equals("QR_MODULE_SIZE_9")) {
                            moduleWidth = ModuleSize.QR_MODULE_SIZE_9;
                        } else if(moduleWidthItem.equals("QR_MODULE_SIZE_10")) {
                            moduleWidth = ModuleSize.QR_MODULE_SIZE_10;
                        } else if(moduleWidthItem.equals("QR_MODULE_SIZE_11")) {
                            moduleWidth = ModuleSize.QR_MODULE_SIZE_11;
                        } else if(moduleWidthItem.equals("PDF417_MODULE_WIDTH_2")) {
                            moduleWidth = ModuleSize.PDF417_MODULE_WIDTH_2;
                        } else if(moduleWidthItem.equals("PDF417_MODULE_WIDTH_3")) {
                            moduleWidth = ModuleSize.PDF417_MODULE_WIDTH_3;
                        } else if(moduleWidthItem.equals("PDF417_MODULE_WIDTH_4")) {
                            moduleWidth = ModuleSize.PDF417_MODULE_WIDTH_4;
                        } else if(moduleWidthItem.equals("PDF417_MODULE_WIDTH_5")) {
                            moduleWidth = ModuleSize.PDF417_MODULE_WIDTH_5;
                        } else if(moduleWidthItem.equals("PDF417_MODULE_WIDTH_6")) {
                            moduleWidth = ModuleSize.PDF417_MODULE_WIDTH_6;
                        } else if(moduleWidthItem.equals("PDF417_MODULE_WIDTH_7")) {
                            moduleWidth = ModuleSize.PDF417_MODULE_WIDTH_7;
                        } else if(moduleWidthItem.equals("PDF417_MODULE_WIDTH_8")) {
                            moduleWidth = ModuleSize.PDF417_MODULE_WIDTH_8;
                        } else {
                            moduleWidth = ModuleSize.BARCODE_MODULE_WIDTH_2;
                        }
                        spnrModuleWidth.setSelection(0);

                        EditText edtHeight = (EditText)layout.findViewById(R.id.edittext_height);
                        int moduleHeight = StringUtil.getInt(edtHeight.getText().toString(), PrinterManager.BARCODE_HEIGHT_DEFAULT);
                        edtHeight.setText(Integer.toString(PrinterManager.BARCODE_HEIGHT_DEFAULT));

                        Spinner spnrHriPosition = (Spinner)layout.findViewById(R.id.spinner_hri_position);
                        String hriPositionItem = (String)spnrHriPosition.getSelectedItem();
                        HriPosition hriPosition;
                        if(hriPositionItem.equals("HRI_NONE")) {
                            hriPosition = HriPosition.HRI_NONE;
                        } else if(hriPositionItem.equals("HRI_POSITION_ABOVE")) {
                            hriPosition = HriPosition.HRI_POSITION_ABOVE;
                        } else if(hriPositionItem.equals("HRI_POSITION_BELOW")) {
                            hriPosition = HriPosition.HRI_POSITION_BELOW;
                        } else if(hriPositionItem.equals("HRI_POSITION_ABOVE_BELOW")) {
                            hriPosition = HriPosition.HRI_POSITION_ABOVE_BELOW;
                        } else {
                            hriPosition = HriPosition.HRI_NONE;
                        }
                        spnrHriPosition.setSelection(0);

                        Spinner spnrHriFont = (Spinner)layout.findViewById(R.id.spinner_hri_font);
                        String hriFontItem = (String)spnrHriFont.getSelectedItem();
                        CharacterFont hriFont;
                        if(hriFontItem.equals("FONT_A")) {
                            hriFont = CharacterFont.FONT_A;
                        } else if(hriFontItem.equals("FONT_B")) {
                            hriFont = CharacterFont.FONT_B;
                        } else {
                            hriFont = CharacterFont.FONT_A;
                        }
                        spnrHriFont.setSelection(0);

                        Spinner spnrAliment = (Spinner)layout.findViewById(R.id.spinner_alignment);
                        String alignmentItem = (String)spnrAliment.getSelectedItem();
                        PrintAlignment alignment;
                        if(alignmentItem.equals("ALIGNMENT_LEFT")) {
                            alignment =PrintAlignment.ALIGNMENT_LEFT;
                        } else if(alignmentItem.equals("ALIGNMENT_CENTER")) {
                            alignment = PrintAlignment.ALIGNMENT_CENTER;
                        } else if(alignmentItem.equals("ALIGNMENT_RIGHT")) {
                            alignment = PrintAlignment.ALIGNMENT_RIGHT;
                        } else {
                            alignment =PrintAlignment.ALIGNMENT_LEFT;
                        }
                        spnrAliment.setSelection(0);

                        Spinner spnrNwRatio = (Spinner)layout.findViewById(R.id.spinner_nw_ratio);
                        String nwRatioItem = (String)spnrNwRatio.getSelectedItem();
                        NwRatio nwRatio;
                        if(nwRatioItem.equals("WIDE_WIDTH_1")) {
                            nwRatio = NwRatio.WIDE_WIDTH_1;
                        } else if(nwRatioItem.equals("WIDE_WIDTH_2")) {
                            nwRatio = NwRatio.WIDE_WIDTH_2;
                        } else if(nwRatioItem.equals("WIDE_WIDTH_3")) {
                            nwRatio = NwRatio.WIDE_WIDTH_3;
                        } else if(nwRatioItem.equals("WIDE_WIDTH_4")) {
                            nwRatio = NwRatio.WIDE_WIDTH_4;
                        } else if(nwRatioItem.equals("NWRATIO_1TO2")) {
                            nwRatio = NwRatio.NWRATIO_1TO2;
                        } else if(nwRatioItem.equals("NWRATIO_1TO2_5")) {
                            nwRatio = NwRatio.NWRATIO_1TO2_5;
                        } else if(nwRatioItem.equals("NWRATIO_1TO3")) {
                            nwRatio = NwRatio.NWRATIO_1TO3;
                        } else {
                            nwRatio = NwRatio.WIDE_WIDTH_1;
                        }
                        spnrNwRatio.setSelection(0);

                        int ret = 0;
                        String msg;
                        try {
                            if(byteData == null) {
                                switch(barcodeSymbol) {
                                    case BARCODE_SYMBOL_UPC_A:
                                    case BARCODE_SYMBOL_UPC_E:
                                    case BARCODE_SYMBOL_EAN13:
                                    case BARCODE_SYMBOL_JAN13:
                                    case BARCODE_SYMBOL_EAN8:
                                    case BARCODE_SYMBOL_JAN8:
                                    case BARCODE_SYMBOL_EAN13_ADDON:
                                    case BARCODE_SYMBOL_JAN13_ADDON:
                                         mPrinterManager.printBarcode(
                                                 barcodeSymbol,
                                                 data,
                                                 moduleWidth,
                                                 moduleHeight,
                                                 hriPosition,
                                                 hriFont,
                                                 alignment
                                                 );
                                         break;

                                    case BARCODE_SYMBOL_CODE39:
                                    case BARCODE_SYMBOL_ITF:
                                    case BARCODE_SYMBOL_CODABAR:
                                        mPrinterManager.printBarcode(
                                                barcodeSymbol,
                                                data,
                                                moduleWidth,
                                                moduleHeight,
                                                hriPosition,
                                                hriFont,
                                                alignment,
                                                nwRatio
                                                );
                                        break;
                                }

                            } else {
                                mPrinterManager.printBarcode(
                                        barcodeSymbol,
                                        byteData,
                                        moduleWidth,
                                        moduleHeight,
                                        hriPosition,
                                        hriFont,
                                        alignment
                                        );
                            }

                            msg = "printBarcode OK.";
                        } catch(PrinterException e) {
                            ret = e.getErrorCode();
                            msg = "printBarcode NG.[" + ret + "]";
                        }
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();

                        writeLog("printBarcode", false, ret, "");
                    }
                });
        alertDialog.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        return alertDialog.create();
    }


    private Dialog createDialogPrintPdf417() {
        LayoutInflater inflater = (LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.print_pdf417_layout, (ViewGroup)findViewById(R.id.layout_print_pdf417));

        final Spinner spnrErrorCorrection = (Spinner)layout.findViewById(R.id.spinner_error_correction);
        spnrErrorCorrection.setSelection(4);

        final Spinner spnrModuleWidth = (Spinner)layout.findViewById(R.id.spinner_module_width);
        spnrModuleWidth.setSelection(16);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setIcon(android.R.drawable.ic_dialog_info);
        alertDialog.setTitle(R.string.dialog_print_pdf417_title);
        alertDialog.setView(layout);
        alertDialog.setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        EditText edtData = (EditText)layout.findViewById(R.id.edittext_data);
                        String data = edtData.getText().toString();
                        edtData.setText("");
                        writeLog("printPDF417", true, 0, data);

                        Spinner spnrPDF417Symbol = (Spinner)layout.findViewById(R.id.spinner_pdf417_symbol);
                        String symbolModeItem = (String)spnrPDF417Symbol.getSelectedItem();
                        Pdf417Symbol pdf417Symbol;
                        if(symbolModeItem.equals("PDF417_STANDARD")) {
                            pdf417Symbol = Pdf417Symbol.PDF417_STANDARD;
                        } else if(symbolModeItem.equals("PDF417_COMPACT")) {
                            pdf417Symbol = Pdf417Symbol.PDF417_COMPACT;
                        } else {
                            pdf417Symbol = Pdf417Symbol.PDF417_STANDARD;
                        }
                        spnrPDF417Symbol.setSelection(0);

                        String errorCorrectionItem = (String)spnrErrorCorrection.getSelectedItem();
                        ErrorCorrection errorCorrection;
                        if(errorCorrectionItem.equals("PDF417_ERROR_CORRECTION_0")) {
                            errorCorrection = ErrorCorrection.PDF417_ERROR_CORRECTION_0;
                        } else if(errorCorrectionItem.equals("PDF417_ERROR_CORRECTION_1")) {
                            errorCorrection = ErrorCorrection.PDF417_ERROR_CORRECTION_1;
                        } else if(errorCorrectionItem.equals("PDF417_ERROR_CORRECTION_2")) {
                            errorCorrection = ErrorCorrection.PDF417_ERROR_CORRECTION_2;
                        } else if(errorCorrectionItem.equals("PDF417_ERROR_CORRECTION_3")) {
                            errorCorrection = ErrorCorrection.PDF417_ERROR_CORRECTION_3;
                        } else if(errorCorrectionItem.equals("PDF417_ERROR_CORRECTION_4")) {
                            errorCorrection = ErrorCorrection.PDF417_ERROR_CORRECTION_4;
                        } else if(errorCorrectionItem.equals("PDF417_ERROR_CORRECTION_5")) {
                            errorCorrection = ErrorCorrection.PDF417_ERROR_CORRECTION_5;
                        } else if(errorCorrectionItem.equals("PDF417_ERROR_CORRECTION_6")) {
                            errorCorrection = ErrorCorrection.PDF417_ERROR_CORRECTION_6;
                        } else if(errorCorrectionItem.equals("PDF417_ERROR_CORRECTION_7")) {
                            errorCorrection = ErrorCorrection.PDF417_ERROR_CORRECTION_7;
                        } else if(errorCorrectionItem.equals("PDF417_ERROR_CORRECTION_8")) {
                            errorCorrection = ErrorCorrection.PDF417_ERROR_CORRECTION_8;
                        } else if(errorCorrectionItem.equals("QR_ERROR_CORRECTION_L")) {
                            errorCorrection = ErrorCorrection.QR_ERROR_CORRECTION_L;
                        } else if(errorCorrectionItem.equals("QR_ERROR_CORRECTION_M")) {
                            errorCorrection = ErrorCorrection.QR_ERROR_CORRECTION_M;
                        } else if(errorCorrectionItem.equals("QR_ERROR_CORRECTION_Q")) {
                            errorCorrection = ErrorCorrection.QR_ERROR_CORRECTION_Q;
                        } else if(errorCorrectionItem.equals("QR_ERROR_CORRECTION_H")) {
                            errorCorrection = ErrorCorrection.QR_ERROR_CORRECTION_H;
                        } else {
                            errorCorrection = ErrorCorrection.PDF417_ERROR_CORRECTION_0;
                        }
                        spnrErrorCorrection.setSelection(4);

                        EditText edtRow = (EditText)layout.findViewById(R.id.edittext_row);
                        int row = StringUtil.getInt(edtRow.getText().toString(), PrinterManager.PDF417_ROW_AUTO);
                        edtRow.setText(Integer.toString(PrinterManager.PDF417_ROW_AUTO));

                        EditText edtColumn = (EditText)layout.findViewById(R.id.edittext_column);
                        int column = StringUtil.getInt(edtColumn.getText().toString(), PrinterManager.PDF417_COLUMN_AUTO);
                        edtColumn.setText(Integer.toString(PrinterManager.PDF417_COLUMN_AUTO));

                        String moduleWidthItem = (String)spnrModuleWidth.getSelectedItem();
                        ModuleSize moduleWidth;
                        if(moduleWidthItem.equals("PDF417_MODULE_WIDTH_2")) {
                            moduleWidth = ModuleSize.PDF417_MODULE_WIDTH_2;
                        } else if(moduleWidthItem.equals("PDF417_MODULE_WIDTH_3")) {
                            moduleWidth = ModuleSize.PDF417_MODULE_WIDTH_3;
                        } else if(moduleWidthItem.equals("PDF417_MODULE_WIDTH_4")) {
                            moduleWidth = ModuleSize.PDF417_MODULE_WIDTH_4;
                        } else if(moduleWidthItem.equals("PDF417_MODULE_WIDTH_5")) {
                            moduleWidth = ModuleSize.PDF417_MODULE_WIDTH_5;
                        } else if(moduleWidthItem.equals("PDF417_MODULE_WIDTH_6")) {
                            moduleWidth = ModuleSize.PDF417_MODULE_WIDTH_6;
                        } else if(moduleWidthItem.equals("PDF417_MODULE_WIDTH_7")) {
                            moduleWidth = ModuleSize.PDF417_MODULE_WIDTH_7;
                        } else if(moduleWidthItem.equals("PDF417_MODULE_WIDTH_8")) {
                            moduleWidth = ModuleSize.PDF417_MODULE_WIDTH_8;
                        } else if(moduleWidthItem.equals("BARCODE_MODULE_WIDTH_2")) {
                            moduleWidth = ModuleSize.BARCODE_MODULE_WIDTH_2;
                        } else if(moduleWidthItem.equals("BARCODE_MODULE_WIDTH_3")) {
                            moduleWidth = ModuleSize.BARCODE_MODULE_WIDTH_3;
                        } else if(moduleWidthItem.equals("BARCODE_MODULE_WIDTH_4")) {
                            moduleWidth = ModuleSize.BARCODE_MODULE_WIDTH_4;
                        } else if(moduleWidthItem.equals("BARCODE_MODULE_WIDTH_5")) {
                            moduleWidth = ModuleSize.BARCODE_MODULE_WIDTH_5;
                        } else if(moduleWidthItem.equals("BARCODE_MODULE_WIDTH_6")) {
                            moduleWidth = ModuleSize.BARCODE_MODULE_WIDTH_6;
                        } else if(moduleWidthItem.equals("QR_MODULE_SIZE_2")) {
                            moduleWidth = ModuleSize.QR_MODULE_SIZE_2;
                        } else if(moduleWidthItem.equals("QR_MODULE_SIZE_3")) {
                            moduleWidth = ModuleSize.QR_MODULE_SIZE_3;
                        } else if(moduleWidthItem.equals("QR_MODULE_SIZE_4")) {
                            moduleWidth = ModuleSize.QR_MODULE_SIZE_4;
                        } else if(moduleWidthItem.equals("QR_MODULE_SIZE_5")) {
                            moduleWidth = ModuleSize.QR_MODULE_SIZE_5;
                        } else if(moduleWidthItem.equals("QR_MODULE_SIZE_6")) {
                            moduleWidth = ModuleSize.QR_MODULE_SIZE_6;
                        } else if(moduleWidthItem.equals("QR_MODULE_SIZE_7")) {
                            moduleWidth = ModuleSize.QR_MODULE_SIZE_7;
                        } else if(moduleWidthItem.equals("QR_MODULE_SIZE_8")) {
                            moduleWidth = ModuleSize.QR_MODULE_SIZE_8;
                        } else if(moduleWidthItem.equals("QR_MODULE_SIZE_9")) {
                            moduleWidth = ModuleSize.QR_MODULE_SIZE_9;
                        } else if(moduleWidthItem.equals("QR_MODULE_SIZE_10")) {
                            moduleWidth = ModuleSize.QR_MODULE_SIZE_10;
                        } else if(moduleWidthItem.equals("QR_MODULE_SIZE_11")) {
                            moduleWidth = ModuleSize.QR_MODULE_SIZE_11;
                        } else {
                            moduleWidth = ModuleSize.PDF417_MODULE_WIDTH_3;
                        }
                        spnrModuleWidth.setSelection(16);

                        EditText edtModuleHeight = (EditText)layout.findViewById(R.id.edittext_module_height);
                        int moduleHeight = StringUtil.getInt(edtModuleHeight.getText().toString(), PrinterManager.PDF417_MODULE_HEIGHT_DEFAULT);
                        edtModuleHeight.setText(Integer.toString(PrinterManager.PDF417_MODULE_HEIGHT_DEFAULT));

                        Spinner spnrAliment = (Spinner)layout.findViewById(R.id.spinner_alignment);
                        String alignmentItem = (String)spnrAliment.getSelectedItem();
                        PrintAlignment alignment;
                        if(alignmentItem.equals("ALIGNMENT_LEFT")) {
                            alignment =PrintAlignment.ALIGNMENT_LEFT;
                        } else if(alignmentItem.equals("ALIGNMENT_CENTER")) {
                            alignment = PrintAlignment.ALIGNMENT_CENTER;
                        } else if(alignmentItem.equals("ALIGNMENT_RIGHT")) {
                            alignment = PrintAlignment.ALIGNMENT_RIGHT;
                        } else {
                            alignment =PrintAlignment.ALIGNMENT_LEFT;
                        }
                        spnrAliment.setSelection(0);

                        int ret = 0;
                        String msg;
                        try {
                            mPrinterManager.printPDF417(
                                    data,
                                    errorCorrection,
                                    row,
                                    column,
                                    moduleWidth,
                                    moduleHeight,
                                    alignment,
                                    pdf417Symbol
                                    );

                            msg = "printPDF417 OK.";
                        } catch(PrinterException e) {
                            ret = e.getErrorCode();
                            msg = "printPDF417 NG.[" + ret + "]";
                        }
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();

                        writeLog("printPDF417", false, ret, "");
                    }
                });
        alertDialog.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        return alertDialog.create();
    }


    private Dialog createDialogPrintQrcode() {
        LayoutInflater inflater = (LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.print_qrcode_layout, (ViewGroup)findViewById(R.id.layout_print_qrcode));

        final Spinner spnrModuleSize = (Spinner)layout.findViewById(R.id.spinner_module_size);
        spnrModuleSize.setSelection(9);

        final Spinner spnrModel = (Spinner)layout.findViewById(R.id.spinner_model);
        spnrModel.setSelection(1);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setIcon(android.R.drawable.ic_dialog_info);
        alertDialog.setTitle(R.string.dialog_print_qrcode_title);
        alertDialog.setView(layout);
        alertDialog.setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        EditText edtData = (EditText)layout.findViewById(R.id.edittext_data);
                        String data = edtData.getText().toString();
                        edtData.setText("");
                        writeLog("printQRcode", true, 0, data);

                        Spinner spnrErrorCorrection = (Spinner)layout.findViewById(R.id.spinner_error_correction);
                        String errorCorrectionItem = (String)spnrErrorCorrection.getSelectedItem();
                        ErrorCorrection errorCorrection;
                        if(errorCorrectionItem.equals("QR_ERROR_CORRECTION_L")) {
                            errorCorrection = ErrorCorrection.QR_ERROR_CORRECTION_L;
                        } else if(errorCorrectionItem.equals("QR_ERROR_CORRECTION_M")) {
                            errorCorrection = ErrorCorrection.QR_ERROR_CORRECTION_M;
                        } else if(errorCorrectionItem.equals("QR_ERROR_CORRECTION_Q")) {
                            errorCorrection = ErrorCorrection.QR_ERROR_CORRECTION_Q;
                        } else if(errorCorrectionItem.equals("QR_ERROR_CORRECTION_H")) {
                            errorCorrection = ErrorCorrection.QR_ERROR_CORRECTION_H;
                        } else if(errorCorrectionItem.equals("PDF417_ERROR_CORRECTION_0")) {
                            errorCorrection = ErrorCorrection.PDF417_ERROR_CORRECTION_0;
                        } else if(errorCorrectionItem.equals("PDF417_ERROR_CORRECTION_1")) {
                            errorCorrection = ErrorCorrection.PDF417_ERROR_CORRECTION_1;
                        } else if(errorCorrectionItem.equals("PDF417_ERROR_CORRECTION_2")) {
                            errorCorrection = ErrorCorrection.PDF417_ERROR_CORRECTION_2;
                        } else if(errorCorrectionItem.equals("PDF417_ERROR_CORRECTION_3")) {
                            errorCorrection = ErrorCorrection.PDF417_ERROR_CORRECTION_3;
                        } else if(errorCorrectionItem.equals("PDF417_ERROR_CORRECTION_4")) {
                            errorCorrection = ErrorCorrection.PDF417_ERROR_CORRECTION_4;
                        } else if(errorCorrectionItem.equals("PDF417_ERROR_CORRECTION_5")) {
                            errorCorrection = ErrorCorrection.PDF417_ERROR_CORRECTION_5;
                        } else if(errorCorrectionItem.equals("PDF417_ERROR_CORRECTION_6")) {
                            errorCorrection = ErrorCorrection.PDF417_ERROR_CORRECTION_6;
                        } else if(errorCorrectionItem.equals("PDF417_ERROR_CORRECTION_7")) {
                            errorCorrection = ErrorCorrection.PDF417_ERROR_CORRECTION_7;
                        } else if(errorCorrectionItem.equals("PDF417_ERROR_CORRECTION_8")) {
                            errorCorrection = ErrorCorrection.PDF417_ERROR_CORRECTION_8;
                        } else {
                            errorCorrection = ErrorCorrection.QR_ERROR_CORRECTION_L;
                        }
                        spnrErrorCorrection.setSelection(0);

                        String moduleSizeItem = (String)spnrModuleSize.getSelectedItem();
                        ModuleSize moduleSize;
                        if(moduleSizeItem.equals("QR_MODULE_SIZE_2")) {
                            moduleSize = ModuleSize.QR_MODULE_SIZE_2;
                        } else if(moduleSizeItem.equals("QR_MODULE_SIZE_3")) {
                            moduleSize = ModuleSize.QR_MODULE_SIZE_3;
                        } else if(moduleSizeItem.equals("QR_MODULE_SIZE_4")) {
                            moduleSize = ModuleSize.QR_MODULE_SIZE_4;
                        } else if(moduleSizeItem.equals("QR_MODULE_SIZE_5")) {
                            moduleSize = ModuleSize.QR_MODULE_SIZE_5;
                        } else if(moduleSizeItem.equals("QR_MODULE_SIZE_6")) {
                            moduleSize = ModuleSize.QR_MODULE_SIZE_6;
                        } else if(moduleSizeItem.equals("QR_MODULE_SIZE_7")) {
                            moduleSize = ModuleSize.QR_MODULE_SIZE_7;
                        } else if(moduleSizeItem.equals("QR_MODULE_SIZE_8")) {
                            moduleSize = ModuleSize.QR_MODULE_SIZE_8;
                        } else if(moduleSizeItem.equals("QR_MODULE_SIZE_9")) {
                            moduleSize = ModuleSize.QR_MODULE_SIZE_9;
                        } else if(moduleSizeItem.equals("QR_MODULE_SIZE_10")) {
                            moduleSize = ModuleSize.QR_MODULE_SIZE_10;
                        } else if(moduleSizeItem.equals("QR_MODULE_SIZE_11")) {
                            moduleSize = ModuleSize.QR_MODULE_SIZE_11;
                        } else if(moduleSizeItem.equals("BARCODE_MODULE_WIDTH_2")) {
                            moduleSize = ModuleSize.BARCODE_MODULE_WIDTH_2;
                        } else if(moduleSizeItem.equals("BARCODE_MODULE_WIDTH_3")) {
                            moduleSize = ModuleSize.BARCODE_MODULE_WIDTH_3;
                        } else if(moduleSizeItem.equals("BARCODE_MODULE_WIDTH_4")) {
                            moduleSize = ModuleSize.BARCODE_MODULE_WIDTH_4;
                        } else if(moduleSizeItem.equals("BARCODE_MODULE_WIDTH_5")) {
                            moduleSize = ModuleSize.BARCODE_MODULE_WIDTH_5;
                        } else if(moduleSizeItem.equals("BARCODE_MODULE_WIDTH_6")) {
                            moduleSize = ModuleSize.BARCODE_MODULE_WIDTH_6;
                        } else if(moduleSizeItem.equals("PDF417_MODULE_WIDTH_2")) {
                            moduleSize = ModuleSize.PDF417_MODULE_WIDTH_2;
                        } else if(moduleSizeItem.equals("PDF417_MODULE_WIDTH_3")) {
                            moduleSize = ModuleSize.PDF417_MODULE_WIDTH_3;
                        } else if(moduleSizeItem.equals("PDF417_MODULE_WIDTH_4")) {
                            moduleSize = ModuleSize.PDF417_MODULE_WIDTH_4;
                        } else if(moduleSizeItem.equals("PDF417_MODULE_WIDTH_5")) {
                            moduleSize = ModuleSize.PDF417_MODULE_WIDTH_5;
                        } else if(moduleSizeItem.equals("PDF417_MODULE_WIDTH_6")) {
                            moduleSize = ModuleSize.PDF417_MODULE_WIDTH_6;
                        } else if(moduleSizeItem.equals("PDF417_MODULE_WIDTH_7")) {
                            moduleSize = ModuleSize.PDF417_MODULE_WIDTH_7;
                        } else if(moduleSizeItem.equals("PDF417_MODULE_WIDTH_8")) {
                            moduleSize = ModuleSize.PDF417_MODULE_WIDTH_8;
                        } else {
                            moduleSize = ModuleSize.QR_MODULE_SIZE_6;
                        }
                        spnrModuleSize.setSelection(9);

                        Spinner spnrAliment = (Spinner)layout.findViewById(R.id.spinner_alignment);
                        String alignmentItem = (String)spnrAliment.getSelectedItem();
                        PrintAlignment alignment;
                        if(alignmentItem.equals("ALIGNMENT_LEFT")) {
                            alignment =PrintAlignment.ALIGNMENT_LEFT;
                        } else if(alignmentItem.equals("ALIGNMENT_CENTER")) {
                            alignment = PrintAlignment.ALIGNMENT_CENTER;
                        } else if(alignmentItem.equals("ALIGNMENT_RIGHT")) {
                            alignment = PrintAlignment.ALIGNMENT_RIGHT;
                        } else {
                            alignment =PrintAlignment.ALIGNMENT_LEFT;
                        }
                        spnrAliment.setSelection(0);

                        String modelItem = (String)spnrModel.getSelectedItem();
                        QrModel model;
                        if(modelItem.equals("QR_MODEL_1")) {
                            model = QrModel.QR_MODEL_1;
                        } else if(modelItem.equals("QR_MODEL_2")) {
                            model = QrModel.QR_MODEL_2;
                        } else {
                            model = QrModel.QR_MODEL_2;
                        }
                        spnrModel.setSelection(1);

                        int ret = 0;
                        String msg;
                        try {
                            mPrinterManager.printQRcode(
                                    data,
                                    errorCorrection,
                                    moduleSize,
                                    alignment,
                                    model
                                    );

                            msg = "printQRcode OK.";
                        } catch(PrinterException e) {
                            ret = e.getErrorCode();
                            msg = "printQRcode NG.[" + ret + "]";
                        }
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();

                        writeLog("printQRcode", false, ret, "");
                    }
                });
        alertDialog.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        return alertDialog.create();
    }


    private Dialog createDialogCutPaper() {
        LayoutInflater inflater = (LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.cut_paper_layout, (ViewGroup)findViewById(R.id.layout_cut_paper));

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setIcon(android.R.drawable.ic_dialog_info);
        alertDialog.setTitle(R.string.dialog_cut_paper_title);
        alertDialog.setView(layout);
        alertDialog.setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Spinner spnrCuttingMethod = (Spinner)layout.findViewById(R.id.spinner_cutting_method);
                        String cutTypeItem = (String)spnrCuttingMethod.getSelectedItem();
                        CuttingMethod cuttingMethod;
                        if(cutTypeItem.equals("CUT_FULL")) {
                            cuttingMethod = CuttingMethod.CUT_FULL;
                        } else if(cutTypeItem.equals("CUT_PARTIAL")) {
                            cuttingMethod = CuttingMethod.CUT_PARTIAL;
                        } else {
                            cuttingMethod = CuttingMethod.CUT_FULL;
                        }
                        spnrCuttingMethod.setSelection(0);
                        writeLog("cutPaper", true, 0, cutTypeItem);

                        int ret = 0;
                        String msg;
                        try {
                            mPrinterManager.cutPaper(cuttingMethod);

                            msg = "cutPaper OK.";
                        } catch(PrinterException e) {
                            ret = e.getErrorCode();
                            msg = "cutPaper NG.[" + ret + "]";
                        }
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();

                        writeLog("cutPaper", false, ret, "");
                    }
                });
        alertDialog.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        return alertDialog.create();
    }


    private Dialog createDialogOpenDrawer() {
        LayoutInflater inflater = (LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.open_drawer_layout, (ViewGroup)findViewById(R.id.layout_open_drawer));

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setIcon(android.R.drawable.ic_dialog_info);
        alertDialog.setTitle(R.string.dialog_open_drawer_title);
        alertDialog.setView(layout);
        alertDialog.setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Spinner spnrDrawerNum = (Spinner)layout.findViewById(R.id.spinner_drawer_num);
                String drawerNumItem = (String)spnrDrawerNum.getSelectedItem();
                DrawerNum drawerNum;
                if(drawerNumItem.equals("DRAWER_1")) {
                    drawerNum = DrawerNum.DRAWER_1;
                } else if(drawerNumItem.equals("DRAWER_2")) {
                    drawerNum = DrawerNum.DRAWER_2;
                } else {
                    drawerNum = DrawerNum.DRAWER_1;
                }
                spnrDrawerNum.setSelection(0);

                Spinner spnrPulseWidth = (Spinner)layout.findViewById(R.id.spinner_pulse_width);
                String pulseWidthItem = (String)spnrPulseWidth.getSelectedItem();
                PulseWidth pulseWidth;
                if(pulseWidthItem.equals("ON_OFF_TIME_100")) {
                    pulseWidth = PulseWidth.ON_OFF_TIME_100;
                } else if(pulseWidthItem.equals("ON_OFF_TIME_200")) {
                    pulseWidth = PulseWidth.ON_OFF_TIME_200;
                } else if(pulseWidthItem.equals("ON_OFF_TIME_300")) {
                    pulseWidth = PulseWidth.ON_OFF_TIME_300;
                } else if(pulseWidthItem.equals("ON_OFF_TIME_400")) {
                    pulseWidth = PulseWidth.ON_OFF_TIME_400;
                } else if(pulseWidthItem.equals("ON_OFF_TIME_500")) {
                    pulseWidth = PulseWidth.ON_OFF_TIME_500;
                } else if(pulseWidthItem.equals("ON_OFF_TIME_600")) {
                    pulseWidth = PulseWidth.ON_OFF_TIME_600;
                } else if(pulseWidthItem.equals("ON_OFF_TIME_700")) {
                    pulseWidth = PulseWidth.ON_OFF_TIME_700;
                } else if(pulseWidthItem.equals("ON_OFF_TIME_800")) {
                    pulseWidth = PulseWidth.ON_OFF_TIME_800;
                } else {
                    pulseWidth = PulseWidth.ON_OFF_TIME_100;
                }
                spnrPulseWidth.setSelection(0);

                writeLog("openDrawer", true, 0, drawerNumItem + "," + pulseWidthItem);

                int ret = 0;
                String msg;
                try {
                    mPrinterManager.openDrawer(drawerNum, pulseWidth);

                    msg = "openDrawer OK.";
                } catch(PrinterException e) {
                    ret = e.getErrorCode();
                    msg = "openDrawer NG.[" + ret + "]";
                }
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();

                writeLog("openDrawer", false, ret, "");
            }
                });
        alertDialog.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        return alertDialog.create();
    }


    private Dialog createDialogBuzzer() {
        LayoutInflater inflater = (LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.buzzer_layout, (ViewGroup)findViewById(R.id.layout_buzzer));

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setIcon(android.R.drawable.ic_dialog_info);
        alertDialog.setTitle(R.string.dialog_buzzer_title);
        alertDialog.setView(layout);
        alertDialog.setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                EditText edtOnTime = (EditText)layout.findViewById(R.id.edittext_on_time);
                int onTime = StringUtil.getInt(edtOnTime.getText().toString());
                edtOnTime.setText("");

                EditText edtOffTime = (EditText)layout.findViewById(R.id.edittext_off_time);
                int offTime = StringUtil.getInt(edtOffTime.getText().toString());
                edtOffTime.setText("");

                writeLog("buzzer", true, 0, Integer.toString(onTime) + ','+ Integer.toString(offTime));

                int ret = 0;
                String msg;
                try {
                    mPrinterManager.buzzer(onTime, offTime);

                    msg = "buzzer OK.";
                } catch(PrinterException e) {
                    ret = e.getErrorCode();
                    msg = "buzzer NG.[" + ret + "]";
                }
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();

                writeLog("buzzer", false, ret, "");
            }
                });
        alertDialog.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        return alertDialog.create();
    }


    private Dialog createDialogInputBinary() {
        final EditText editView = new EditText(MainActivity.this);
        editView.setInputType(InputType.TYPE_CLASS_TEXT);
        InputFilter[] inputFilter = new InputFilter[2];
        inputFilter[0] = new BinaryFilter();
        inputFilter[1] = new InputFilter.LengthFilter(256);
        editView.setFilters(inputFilter);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setIcon(android.R.drawable.ic_dialog_info);
        alertDialog.setTitle(R.string.dialog_send_binary_title);
        alertDialog.setView(editView);
        alertDialog.setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        byte[] binary = asByteArray(editView.getText().toString());
                        writeLog("sendBinary", true, 0, editView.getText().toString());

                        int ret = 0;
                        String msg;
                        try {
                            mPrinterManager.sendBinary(binary);
                            msg = "sendBinary OK.";
                        } catch(PrinterException e) {
                            ret = e.getErrorCode();
                            msg = "sendBinary NG.[" + ret + "]";
                        }
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();

                        writeLog("sendBinary", false, ret, "");

                        editView.setText("");
                    }
                });
        alertDialog.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        return alertDialog.create();
    }


    private Dialog createDialogSendDataFile() {
        LayoutInflater inflater = (LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.print_position_layout, (ViewGroup)findViewById(R.id.layout_print_position));

        final Spinner spnrAliment = (Spinner)layout.findViewById(R.id.spinner_alignment);
        spnrAliment.setSelection(0);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setIcon(android.R.drawable.ic_dialog_info);
        alertDialog.setTitle(R.string.dialog_print_alignment_title);
        alertDialog.setView(layout);
        alertDialog.setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String positionItem = (String)spnrAliment.getSelectedItem();
                        PrintAlignment alignment =PrintAlignment.ALIGNMENT_LEFT;;
                        if(positionItem.equals("ALIGNMENT_LEFT")) {
                            alignment =PrintAlignment.ALIGNMENT_LEFT;
                        } else if(positionItem.equals("ALIGNMENT_CENTER")) {
                            alignment = PrintAlignment.ALIGNMENT_CENTER;
                        } else if(positionItem.equals("ALIGNMENT_RIGHT")) {
                            alignment = PrintAlignment.ALIGNMENT_RIGHT;
                        } else {
                            alignment =PrintAlignment.ALIGNMENT_LEFT;
                        }
                        spnrAliment.setSelection(0);

                        writeLog("sendDataFile", true);

                        int ret = 0;
                        String msg;
                        try {
                            mPrinterManager.sendDataFile(mSelectFile.getAbsolutePath(), alignment);
                            msg = "sendDataFile OK.";
                        } catch(PrinterException e) {
                            ret = e.getErrorCode();
                            msg = "sendDataFile NG.[" + ret + "]";
                        }
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();

                        writeLog("sendDataFile", false, ret, "");

                    }
                });
        alertDialog.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        return alertDialog.create();
    }


    private Dialog createDialogSelectPrinterResponse() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(R.string.dialog_get_printer_response_title);
        alertDialog.setItems(R.array.printer_response_list, new DialogInterface.OnClickListener() {
            private String[] printerResponses = getResources().getStringArray(R.array.printer_response_values_list);
            public void onClick(DialogInterface dialog, int which) {
                writeLog("getPrinterResponse", true);

                int id = Integer.parseInt(printerResponses[which]);
                if(id == PrinterManager.PRINTER_RESPONSE_KEY_CODE) {
                    ArrayList<String> buf = new ArrayList<String>();

                    int ret = 0;
                    String msg;
                    StringBuffer keyCode = new StringBuffer(256);
                    try {
                        mPrinterManager.getPrinterResponse(id, buf);
                        int size = buf.size();
                        for(int index = 0; index < size; index++) {
                            if(index != 0) {
                                keyCode.append(",");
                            }
                            keyCode.append(buf.get(index));
                        }

                        msg = "getPrinterResponse OK. Key:" + keyCode.toString();
                    } catch(PrinterException e) {
                        ret = e.getErrorCode();
                        msg = "getPrinterResponse NG.[" + ret + "]";
                    }
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();

                    writeLog("getPrinterResponse", false, ret, "Key:" + keyCode.toString());

                } else {
                    int[] buf = new int[1];
                    if(id == 0) {
                        buf[0] = 0x0E;
                    }
                    int ret = 0;
                    String msg;
                    try {
                        mPrinterManager.getPrinterResponse(id, buf);
                        msg = "getPrinterResponse OK. Response:0x" + String.format("%08X", buf[0]);
                    } catch(PrinterException e) {
                        ret = e.getErrorCode();
                        msg = "getPrinterResponse NG.[" + ret + "]";
                    }
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();

                    writeLog("getPrinterResponse", false, ret, "Response:0x" + String.format("%08X", buf[0]));
                }

            }
        });

        return alertDialog.create();
    }


    private Dialog createDialogRegisterLogoID1() {
        final EditText editView = new EditText(MainActivity.this);
        editView.setInputType(InputType.TYPE_CLASS_NUMBER);
        InputFilter[] inputFilter = new InputFilter[1];
        inputFilter[0] = new InputFilter.LengthFilter(4);
        editView.setFilters(inputFilter);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setIcon(android.R.drawable.ic_dialog_info);
        alertDialog.setTitle(R.string.dialog_register_logo_title);
        alertDialog.setView(editView);
        alertDialog.setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        int logoID = StringUtil.getInt(editView.getText().toString(), 0);
                        writeLog("registerLogo", true, 0, editView.getText().toString());

                        int ret = 0;
                        String msg;
                        try {
                            mPrinterManager.registerLogo(mSelectFile.getAbsolutePath(), logoID);
                            msg = "registerLogo OK.";
                        } catch(PrinterException e) {
                            ret = e.getErrorCode();
                            msg = "registerLogo NG.[" + ret + "]";
                        }
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();

                        writeLog("registerLogo", false, ret, "");

                        editView.setText("");
                    }
                });
        alertDialog.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        return alertDialog.create();
    }


    private Dialog createDialogRegisterLogoID2() {
        final EditText editView = new EditText(MainActivity.this);
        editView.setInputType(InputType.TYPE_CLASS_TEXT);
        InputFilter[] inputFilter = new InputFilter[1];
        inputFilter[0] = new InputFilter.LengthFilter(2);
        editView.setFilters(inputFilter);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setIcon(android.R.drawable.ic_dialog_info);
        alertDialog.setTitle(R.string.dialog_register_logo_title);
        alertDialog.setView(editView);
        alertDialog.setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String logoID = editView.getText().toString();
                        writeLog("registerLogo", true, 0, editView.getText().toString());

                        int ret = 0;
                        String msg;
                        try {
                            mPrinterManager.registerLogo(mSelectFile.getAbsolutePath(), logoID);
                            msg = "registerLogo OK.";
                        } catch(PrinterException e) {
                            ret = e.getErrorCode();
                            msg = "registerLogo NG.[" + ret + "]";
                        }
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();

                        writeLog("registerLogo", false, ret, "");

                        editView.setText("");
                    }
                });
        alertDialog.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        return alertDialog.create();
    }


    private Dialog createDialogPrintLogoID1() {
        final EditText editView = new EditText(MainActivity.this);
        editView.setInputType(InputType.TYPE_CLASS_NUMBER);
        InputFilter[] inputFilter = new InputFilter[1];
        inputFilter[0] = new InputFilter.LengthFilter(4);
        editView.setFilters(inputFilter);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setIcon(android.R.drawable.ic_dialog_info);
        alertDialog.setTitle(R.string.dialog_print_logo_title);
        alertDialog.setView(editView);
        alertDialog.setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        int logoID = StringUtil.getInt(editView.getText().toString(), 0);
                        writeLog("printLogo", true, 0, editView.getText().toString());

                        int ret = 0;
                        String msg;
                        try {
                            mPrinterManager.printLogo(logoID);
                            msg = "printLogo OK.";
                        } catch(PrinterException e) {
                            ret = e.getErrorCode();
                            msg = "printLogo NG.[" + ret + "]";
                        }
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();

                        writeLog("printLogo", false, ret, "");

                        editView.setText("");
                    }
                });
        alertDialog.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        return alertDialog.create();
    }


    private Dialog createDialogPrintLogoID2() {
        LayoutInflater inflater = (LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.print_logo_layout, (ViewGroup)findViewById(R.id.layout_print_logo));

        final Spinner spnrAliment = (Spinner)layout.findViewById(R.id.spinner_alignment);
        spnrAliment.setSelection(0);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setIcon(android.R.drawable.ic_dialog_info);
        alertDialog.setTitle(R.string.dialog_print_logo_title);
        alertDialog.setView(layout);
        alertDialog.setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        EditText edtLogoID = (EditText)layout.findViewById(R.id.edittext_logo_id);
                        String logoID = edtLogoID.getText().toString();
                        edtLogoID.setText("");
                        writeLog("printLogo", true, 0, logoID);

                        String positionItem = (String)spnrAliment.getSelectedItem();
                        PrintAlignment alignment;
                        if(positionItem.equals("ALIGNMENT_LEFT")) {
                            alignment =PrintAlignment.ALIGNMENT_LEFT;
                        } else if(positionItem.equals("ALIGNMENT_CENTER")) {
                            alignment = PrintAlignment.ALIGNMENT_CENTER;
                        } else if(positionItem.equals("ALIGNMENT_RIGHT")) {
                            alignment = PrintAlignment.ALIGNMENT_RIGHT;
                        } else {
                            alignment =PrintAlignment.ALIGNMENT_LEFT;
                        }
                        spnrAliment.setSelection(0);

                        int ret = 0;
                        String msg;
                        try {
                            mPrinterManager.printLogo(
                                    logoID,
                                    alignment
                                    );

                            msg = "printLogo OK.";
                        } catch(PrinterException e) {
                            ret = e.getErrorCode();
                            msg = "printLogo NG.[" + ret + "]";
                        }
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();

                        writeLog("printLogo", false, ret, "");
                    }
                });
        alertDialog.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        return alertDialog.create();
    }


    private Dialog createDialogUnregisterLogoID1() {
        final EditText editView = new EditText(MainActivity.this);
        editView.setInputType(InputType.TYPE_CLASS_NUMBER);
        InputFilter[] inputFilter = new InputFilter[1];
        inputFilter[0] = new InputFilter.LengthFilter(4);
        editView.setFilters(inputFilter);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setIcon(android.R.drawable.ic_dialog_info);
        alertDialog.setTitle(R.string.dialog_unregister_logo_title);
        alertDialog.setView(editView);
        alertDialog.setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        int logoID = StringUtil.getInt(editView.getText().toString(), 0);
                        writeLog("unregisterLogo", true, 0, editView.getText().toString());

                        int ret = 0;
                        String msg;
                        try {
                            mPrinterManager.unregisterLogo(logoID);
                            msg = "unregisterLogo OK.";
                        } catch(PrinterException e) {
                            ret = e.getErrorCode();
                            msg = "unregisterLogo NG.[" + ret + "]";
                        }
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();

                        writeLog("unregisterLogo", false, ret, "");

                        editView.setText("");
                    }
                });
        alertDialog.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        return alertDialog.create();
    }


    private Dialog createDialogUnregisterLogoID2() {
        final EditText editView = new EditText(MainActivity.this);
        editView.setInputType(InputType.TYPE_CLASS_TEXT);
        InputFilter[] inputFilter = new InputFilter[1];
        inputFilter[0] = new InputFilter.LengthFilter(2);
        editView.setFilters(inputFilter);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setIcon(android.R.drawable.ic_dialog_info);
        alertDialog.setTitle(R.string.dialog_unregister_logo_title);
        alertDialog.setView(editView);
        alertDialog.setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String logoID = editView.getText().toString();
                        writeLog("unregisterLogo", true, 0, editView.getText().toString());

                        int ret = 0;
                        String msg;
                        try {
                            mPrinterManager.unregisterLogo(logoID);
                            msg = "unregisterLogo OK.";
                        } catch(PrinterException e) {
                            ret = e.getErrorCode();
                            msg = "unregisterLogo NG.[" + ret + "]";
                        }
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();

                        writeLog("unregisterLogo", false, ret, "");

                        editView.setText("");
                    }
                });
        alertDialog.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        return alertDialog.create();
    }


    private Dialog createDialogRegisterStyleSheetNo() {
        final EditText editView = new EditText(MainActivity.this);
        editView.setInputType(InputType.TYPE_CLASS_NUMBER);
        InputFilter[] inputFilter = new InputFilter[1];
        inputFilter[0] = new InputFilter.LengthFilter(1);
        editView.setFilters(inputFilter);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setIcon(android.R.drawable.ic_dialog_info);
        alertDialog.setTitle(R.string.dialog_register_style_sheet_title);
        alertDialog.setView(editView);
        alertDialog.setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        int styleSeetNo = StringUtil.getInt(editView.getText().toString(), 0);
                        writeLog("registerStyleSheet", true, 0, editView.getText().toString());

                        int ret = 0;
                        String msg;
                        try {
                            mPrinterManager.registerStyleSheet(mSelectFile.getAbsolutePath(), styleSeetNo);
                            msg = "registerStyleSheet OK.";
                        } catch(PrinterException e) {
                            ret = e.getErrorCode();
                            msg = "registerStyleSheet NG.[" + ret + "]";
                        }
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();

                        writeLog("registerStyleSheet", false, ret, "");

                        editView.setText("");
                    }
                });
        alertDialog.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        return alertDialog.create();
    }


    private Dialog createDialogUnregisterStyleSheetNo() {
        final EditText editView = new EditText(MainActivity.this);
        editView.setInputType(InputType.TYPE_CLASS_NUMBER);
        InputFilter[] inputFilter = new InputFilter[1];
        inputFilter[0] = new InputFilter.LengthFilter(1);
        editView.setFilters(inputFilter);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setIcon(android.R.drawable.ic_dialog_info);
        alertDialog.setTitle(R.string.dialog_unregister_style_sheet_title);
        alertDialog.setView(editView);
        alertDialog.setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        int styleSeetNo = StringUtil.getInt(editView.getText().toString(), 0);
                        writeLog("unregisterStyleSheet", true, 0, editView.getText().toString());

                        int ret = 0;
                        String msg;
                        try {
                            mPrinterManager.unregisterStyleSheet(styleSeetNo);
                            msg = "unregisterStyleSheet OK.";
                        } catch(PrinterException e) {
                            ret = e.getErrorCode();
                            msg = "unregisterStyleSheet NG.[" + ret + "]";
                        }
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();

                        writeLog("unregisterStyleSheet", false, ret, "");

                        editView.setText("");
                    }
                });
        alertDialog.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        return alertDialog.create();
    }


    private Dialog createDialogConfirmFinishApp() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setIcon(android.R.drawable.ic_dialog_info);
        alertDialog.setTitle(R.string.dialog_finish_app_title);
        alertDialog.setMessage(R.string.dialog_finish_app_message);
        alertDialog.setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finishApp();
                    }
                });
        alertDialog.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        return alertDialog.create();
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


    class BinaryFilter implements InputFilter {
        public CharSequence filter(CharSequence source, int start, int end,
                                Spanned dest, int dstart, int dend) {

            if(source.toString().matches("^[a-fA-F0-9]+$") ){
                return source;
            }else{
                return "";
            }
        }
    }


    private byte[] asByteArray(String hex) {
        byte[] bytes = new byte[hex.length() / 2];

        try {
            for(int index = 0; index < bytes.length; index++) {
                String byteStr = hex.substring(index * 2, (index + 1) * 2);
                bytes[index] = (byte)Integer.parseInt(byteStr, 16);
            }
        } catch(IndexOutOfBoundsException e) {
        } catch(NumberFormatException e) {
        }

        return bytes;
    }


    private void checkBluetooth() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null) {
            showDialog(DIALOG_BLUETOOTH_NO_SUPPORT);
            return;
        }

        if(!bluetoothAdapter.isEnabled()) {
            Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BLUETOOTH);
        }
    }


    private void setProperty() {
        if(mPrinterManager != null) {
            final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
            int sendTimeout = StringUtil.getInt(pref.getString(getString(R.string.key_send_timeout), "100000"));
            mPrinterManager.setSendTimeout(sendTimeout);

            int receiveTimeout = StringUtil.getInt(pref.getString(getString(R.string.key_receive_timeout), "100000"));
            mPrinterManager.setReceiveTimeout(receiveTimeout);

            int socketKeepingTime = StringUtil.getInt(pref.getString(getString(R.string.key_socket_keeping_time), "3000000"));
            mPrinterManager.setSocketKeepingTime(socketKeepingTime);

            int internationalCharacter;
            int codePage;
            if(Locale.JAPAN.equals(Locale.getDefault())) {
                internationalCharacter = StringUtil.getInt(pref.getString(getString(R.string.key_international_character), "8"));
                codePage = StringUtil.getInt(pref.getString(getString(R.string.key_code_page), "1"));
            } else {
                internationalCharacter = StringUtil.getInt(pref.getString(getString(R.string.key_international_character), "0"));
                codePage = StringUtil.getInt(pref.getString(getString(R.string.key_code_page), "16"));
            }
            mPrinterManager.setInternationalCharacter(internationalCharacter);
            mPrinterManager.setCodePage(codePage);
        }
    }

    private void writeLog(String command, boolean start) {
        writeLog(command, start, 0, "");
    }


    private void writeLog(String command, boolean start, int returnCode, String msg) {
        EditText edtLog = (EditText)findViewById(R.id.edittext_log);
        if(edtLog != null) {
            StringBuffer buf = new StringBuffer(128);
            buf.append("[")
                .append(StringUtil.getDateString("yyyy/MM/dd HH:mm:ss.SSS"))
                .append("]");

            buf.append(" ").append(command).append("()");
            if(start) {
                buf.append(" IN");
            } else {
                buf.append(" OUT");
                if(returnCode != 0) {
                    buf.append(" Result:").append(returnCode);
                }
            }

            if(!StringUtil.isEmpty(msg)) {
                buf.append(" ").append(msg);
            }

            buf.append("\n");

            edtLog.append(buf.toString());
        }
    }

}
