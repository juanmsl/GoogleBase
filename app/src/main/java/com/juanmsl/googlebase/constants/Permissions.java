package com.juanmsl.googlebase.constants;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import java.util.Vector;

public class Permissions {
    public static final Vector<String> permissions;
    public static final int FINE_LOCATION = 0;
    public static final int COARSE_LOCATION = 1;
    public static final int CAMERA= 2;
    public static final int READ_EXTERNAL_STORAGE = 3;
    public static final int WRITE_EXTERNAL_STORAGE = 4;

    static {
        permissions = new Vector<>();
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        permissions.add(Manifest.permission.CAMERA);
        permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    public static boolean checkSelfPermission(Activity context, int permissionCode) {
        String permission = permissions.get(permissionCode);
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean askPermission(Activity context, int permissionCode, String explanation) {
        String permission = permissions.get(permissionCode);
        if(!Permissions.checkSelfPermission(context, permissionCode)) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(context, permission)) {
                Toast.makeText(context, explanation, Toast.LENGTH_LONG).show();
            }
            ActivityCompat.requestPermissions(context, new String[]{permission}, permissionCode);
            return false;
        }
        return true;
    }

    public static boolean permissionGranted(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        return grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
    }
}
