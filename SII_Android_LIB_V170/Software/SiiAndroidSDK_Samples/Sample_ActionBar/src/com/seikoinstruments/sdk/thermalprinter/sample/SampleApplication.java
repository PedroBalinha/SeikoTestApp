package com.seikoinstruments.sdk.thermalprinter.sample;

import com.seikoinstruments.sdk.thermalprinter.PrinterManager;

import android.app.Application;


public class SampleApplication extends Application {

    /** PrinterManager（SDK） */
    private PrinterManager mPrinterManager = null;


    public void setPrinterManager(PrinterManager manager) {
        mPrinterManager = manager;
    }


    public PrinterManager getPrinterManager() {
        return mPrinterManager;
    }

}
