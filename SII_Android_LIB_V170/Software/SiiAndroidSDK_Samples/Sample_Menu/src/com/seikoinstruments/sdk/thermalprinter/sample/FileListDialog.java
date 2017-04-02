package com.seikoinstruments.sdk.thermalprinter.sample;


import java.io.File;
import java.util.Arrays;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.*;


public class FileListDialog extends Activity
        implements View.OnClickListener, DialogInterface.OnClickListener {

    private Context mParent = null;
    private File[] mDialogFileList;
    private int mSelectFileIndex = -1;
    private onFileListDialogListener mListener = null;
    private boolean mIsDirectorySelect = false;


    public void setDirectorySelect(boolean is){
        mIsDirectorySelect = is;
    }
    public boolean isDirectorySelect(){
        return mIsDirectorySelect;
    }


    public String getSelectedFileName(){
        String ret = "";
        if(mSelectFileIndex < 0){

        }else{
            ret = mDialogFileList[mSelectFileIndex].getName();
        }
        return ret;
    }


    public FileListDialog(Context context){
        mParent = context;
    }

    public void onClick(View v) {
    }


    public void onClick(DialogInterface dialog, int which) {
        mSelectFileIndex = which;
        if((mDialogFileList == null) || (mListener == null)){
        }else{
            File file = mDialogFileList[which];

            if(file.isDirectory() && !isDirectorySelect()){
                show(file.getAbsolutePath(), file.getPath());
            }else{
                mListener.onClickFileList(file);
            }
        }
    }


    public void show(String path, String title){

        try{
            mDialogFileList = listSortedFiles(new File(path));
            if(mDialogFileList == null){
                //NG
                if(mListener != null){
                    mListener.onClickFileList(null);
                }
            }else{
                String[] list = new String[mDialogFileList.length];
                int count = 0;
                String name = "";

                for (File file : mDialogFileList) {
                    if(file.isDirectory()){
                        name = file.getName() + "/";
                    }else{
                        name = file.getName();
                    }
                    list[count] = name;
                    count++;
                }

                new AlertDialog.Builder(mParent).setTitle(title).setItems(list, this).show();
            }
        }catch(SecurityException se){
            //
        }catch(Exception e){
            //
        }

    }


    public File[] listSortedFiles(File file) {
        File[] sortedFiles;
        if(file.isDirectory()) {
            File[] files = file.listFiles();

            FileInformation [] fileInformations = new FileInformation[files.length];
            for (int i=0; i<files.length; i++) {
                fileInformations[i] = new FileInformation(files[i]);
            }

            Arrays.sort(fileInformations);

            sortedFiles = new File[files.length];
            for (int i=0; i<files.length; i++) {
                sortedFiles[i] = fileInformations[i].getFile();
            }
        } else {
            sortedFiles = null;
        }

        return sortedFiles;
    }


    public void setOnFileListDialogListener(onFileListDialogListener listener){
        mListener = listener;
    }


    public interface onFileListDialogListener{
        public void onClickFileList(File file);
    }

}
