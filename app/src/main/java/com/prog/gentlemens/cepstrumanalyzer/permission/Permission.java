package com.prog.gentlemens.cepstrumanalyzer.permission;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

public class Permission {

    private static final int PERMISSION_WRITE_EXTERNAL_STORAGE_CODE = 1;
    private static boolean permissionWriteExternalStorage;
    private static final int PERMISSION_RECORD_AUDIO_CODE = 2;
    private static boolean permissionRecordAudio;

    public static boolean setPermissions(Activity activity) {
        if (!checkPermissionWriteExternalStorage(activity)) {
            // do sth
        } else {
            permissionWriteExternalStorage = true;
        }

        if (!checkPermissionRecordAudio(activity)) {
            requestRecordAudioPermission(activity);
        } else {
            permissionRecordAudio = true;
        }

        return (permissionRecordAudio && permissionWriteExternalStorage);
    }

    private void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_WRITE_EXTERNAL_STORAGE_CODE: {
                //allowed
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    permissionWriteExternalStorage = true;
                }
                //denied
                else {
                    permissionWriteExternalStorage = false;
                }
            }

            case PERMISSION_RECORD_AUDIO_CODE: {
                //allowed
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    permissionRecordAudio = true;
                }
                //denied
                else {
                    permissionRecordAudio = false;
                }
            }
        }
    }

    //DANGEROUS PERMISSION MARSHMALLOW OR HIGHER
    private static boolean checkPermissionWriteExternalStorage(Activity activity) {
        return (ContextCompat.checkSelfPermission(activity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != //
                PackageManager.PERMISSION_DENIED);
    }

    //DANGEROUS PERMISSION MARSHMALLOW OR HIGHER
    private static boolean checkPermissionRecordAudio(Activity activity) {
        return (ContextCompat.checkSelfPermission(activity, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_DENIED);
    }

    private static void requestRecordAudioPermission(Activity activity) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, android.Manifest.permission.RECORD_AUDIO)) {
            Toast.makeText(activity, "Record Audio permission allows us to do record. Please allow this permission in App Settings  .", Toast.LENGTH_LONG)
                    .show();
        } else {
            ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.RECORD_AUDIO}, PERMISSION_RECORD_AUDIO_CODE);
        }
    }

}
