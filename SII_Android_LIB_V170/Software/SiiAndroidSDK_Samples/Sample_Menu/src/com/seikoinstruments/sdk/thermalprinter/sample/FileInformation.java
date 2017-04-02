package com.seikoinstruments.sdk.thermalprinter.sample;

import java.io.File;
import java.util.Locale;


class FileInformation implements Comparable<FileInformation> {


    private File mFile;


    public FileInformation(File file) {
        mFile = file;
    }


    public int compareTo(FileInformation opponent) {
        if(mFile.isDirectory() && ! opponent.mFile.isDirectory()) {
            return -1;
        } else if(! mFile.isDirectory() && opponent.mFile.isDirectory()) {
            return 1;
        } else {
            return mFile.getName().toLowerCase(Locale.US).compareTo(opponent.mFile.getName().toLowerCase(Locale.US));
        }
    }


    public File getFile() {
        return mFile;
    }
}
