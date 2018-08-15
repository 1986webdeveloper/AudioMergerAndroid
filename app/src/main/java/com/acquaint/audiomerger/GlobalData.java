package com.acquaint.audiomerger;

import android.Manifest;
import android.app.Activity;
import android.util.Log;

import com.master.permissionhelper.PermissionHelper;

import java.util.Random;

/**
 * Created by acquaint on 15/8/18.
 */

public class GlobalData {

    private static final String TAG = GlobalData.class.getSimpleName() ;
    public static Boolean FLAG_PERMISSION=false;
    public static String random() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(9);
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }
    public static Boolean askPermission(Activity activity) {
        final PermissionHelper permissionHelper;
        //   final Boolean permission;

        permissionHelper = new PermissionHelper(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
        if (permissionHelper.checkSelfPermission(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE})) {
            GlobalData.FLAG_PERMISSION = true;

        } else {
            final int flag = 2;
            permissionHelper.request(new PermissionHelper.PermissionCallback() {

                @Override
                public void onPermissionGranted() {
                    Log.d(TAG, "onPermissionGranted() called");
                    if (permissionHelper.checkSelfPermission(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE})) {
                        GlobalData.FLAG_PERMISSION = true;

                    }
                }

                @Override
                public void onPermissionDenied() {
                    Log.d(TAG, "onPermissionDenied() called");
                    // permission=false;
                    GlobalData.FLAG_PERMISSION = false;
                }

                @Override
                public void onPermissionDeniedBySystem() {
                    Log.d(TAG, "onPermissionDeniedBySystem() called");
                    /*permission=false;*/
                    GlobalData.FLAG_PERMISSION = false;
                }
            });
        }
        return GlobalData.FLAG_PERMISSION;
    }
}
