package com.uascent.android.pethunting.ui;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.uascent.android.pethunting.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FristFlashActivity extends BaseActivity {
    private final static String TAG = "FristFlashActivity";
    private Handler mHandler = new Handler();

    BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
    private static final int REQUEST_ENABLE_BT = 1;
    public static final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 2;


    private boolean addPermission(List<String> permissionsList, String permission) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            // Check for Rationale Option
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission))
                return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                Map<String, Integer> perms = new HashMap<String, Integer>();
                // Initial
                perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.CAMERA, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.BLUETOOTH, PackageManager.PERMISSION_GRANTED);
                // Fill with results
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);
                // Check for ACCESS_FINE_LOCATION
                if (perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED) {
                    // All Permissions Granted
                    Log.e("hjq", "all permissions are granted.");
                    if (!btAdapter.isEnabled()) {
                        Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                    } else {
                        launchMainActivity();
                    }
                } else {
                    // Permission Denied
                    Toast.makeText(FristFlashActivity.this, R.string.str_permission_denied, Toast.LENGTH_SHORT)
                            .show();
                    finish();
                }

                break;
            }

            default: {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }

    private boolean checkMultiPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();

        final List<String> permissionsList = new ArrayList<String>();
        if (!addPermission(permissionsList, Manifest.permission.ACCESS_FINE_LOCATION)) {
            permissionsNeeded.add(getString(R.string.str_location));
        }

        if (!addPermission(permissionsList, Manifest.permission.BLUETOOTH)) {
            permissionsNeeded.add(getString(R.string.str_bluetooth));
        }

        if (permissionsList.size() == 0) {
            return false;
        }

        // Need Rationale
        String message = getString(R.string.str_permission_prompt) + " " + permissionsNeeded.get(0);
        for (int i = 1; i < permissionsNeeded.size(); i++) {
            message = message + ", " + permissionsNeeded.get(i);
        }

        DialogUtil.showNoTitleDialog(FristFlashActivity.this, message, getString(R.string.system_sure), getString(R.string.system_cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(FristFlashActivity.this, permissionsList.toArray(new String[permissionsList.size()]),
                                REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
                    }
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        });
                    }
                }, false);

        return true;
    }

    void launchMainActivity() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "postDelayed");
                startActivity(new Intent(FristFlashActivity.this, MainActivity.class));
                finish();
            }
        }, 800);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT: {
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    launchMainActivity();
                } else {
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            }

            default:
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_frist);

        super.onCreate(savedInstanceState);

        if (checkMultiPermissions()) {
            return;
        }

        if (!btAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {
            launchMainActivity();
        }
    }

}
