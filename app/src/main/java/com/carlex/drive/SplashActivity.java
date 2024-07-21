package com.carlex.drive;

import android.Manifest;
import android.view.*;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class SplashActivity extends AppCompatActivity {

    public static Context context;
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final String TAG = "SplashActivity";
    private String[] permissions = {
            Manifest.permission.BODY_SENSORS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
            "android.permission.ACCESS_MOCK_LOCATION"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        context = getApplicationContext();
        
    }
    
   protected void loadapp() {         
       loadMainActivityInBackground();
   }
    
   protected void onResume() {         
       super.onResume();      
       
       try {
            if (arePermissionsGranted()) {
                if (isMockLocationsEnabled()) {
                    loadMainActivityInBackground();
                } else {
                    handleMockLocationPermission();
                }
            } else {
                requestPermissions();
            }
           
        } catch (Exception e) {
            Log.e(TAG, "Error during onCreate: ", e);
       }
      loadMainActivityInBackground();
    }

    private boolean arePermissionsGranted() {
        try {
            for (String permission : permissions) {
                if (!isPermissionGranted(this, permission)) {
                    Log.d(TAG, "Permission not granted: " + permission);
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error checking permissions: ", e);
            return false;
        }
    }

    private static boolean isPermissionGranted(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isMockLocationsEnabled() {
        boolean isMockLocationEnabled;
        try {
            AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);

            if (appOps == null) {
                Log.e(TAG, "AppOpsManager is null");
                return false;
            }

            int mockLocationResult = appOps.checkOpNoThrow(AppOpsManager.OPSTR_MOCK_LOCATION, Process.myUid(), context.getPackageName());
            isMockLocationEnabled = mockLocationResult == AppOpsManager.MODE_ALLOWED;
          
            if (!isMockLocationEnabled)
                MainActivity.carregando.setVisibility(View.VISIBLE);
            else 
                MainActivity.carregando.setVisibility(View.GONE);
     
            return isMockLocationEnabled;
        } catch (Exception e) {
            Log.e(TAG, "Error checking mock locations: ", e);
            return false;
        }
    }

    private void handleMockLocationPermission() {
        if (isDeveloperModeEnabled()) {
            showEnableMockLocationAlert();
        } else {
            showDeveloperModeAlert();
        }
    }

    private boolean isDeveloperModeEnabled() {
        int devSettingsEnabled = 0;
        try {
            devSettingsEnabled = Settings.Secure.getInt(getContentResolver(), Settings.Global.DEVELOPMENT_SETTINGS_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            Log.e(TAG, "Developer settings not found: ", e);
        }
        return devSettingsEnabled != 0;
    }

    private void showEnableMockLocationAlert() {
        new AlertDialog.Builder(this)
                .setTitle("Mock Location Required")
                .setMessage("The app requires Mock Location permission. Please enable Mock Location in Developer Options.")
                .setPositiveButton("Open Developer Options", (dialog, which) -> openDeveloperOptions())
                .setNegativeButton("Continue without Mock Location", (dialog, which) -> loadapp())
                .setCancelable(false)
                .show();
    }

    private void showDeveloperModeAlert() {
        new AlertDialog.Builder(this)
                .setTitle("Developer Mode Required")
                .setMessage("The app requires Developer Mode to be enabled. Please enable Developer Mode in Settings.")
                .setPositiveButton("Open Settings", (dialog, which) -> openDeveloperSettings())
                .setNegativeButton("Continue without Mock Location", (dialog, which) -> loadMainActivityInBackground())
                .setCancelable(false)
                .show();
    }

    private void openDeveloperOptions() {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error opening developer options: ", e);
            Toast.makeText(this, "Unable to open developer options.", Toast.LENGTH_SHORT).show();
        }
    }

    private void openDeveloperSettings() {
        try {
            Intent intent = new Intent(Settings.ACTION_DEVICE_INFO_SETTINGS);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error opening developer settings: ", e);
            Toast.makeText(this, "Unable to open developer settings.", Toast.LENGTH_SHORT).show();
        }
    }

    private void requestPermissions() {
        try {
            List<String> permissionsToRequest = new ArrayList<>();
            for (String permission : permissions) {
                if (!isPermissionGranted(this, permission)) {
                    permissionsToRequest.add(permission);
                }
            }
            if (!permissionsToRequest.isEmpty()) {
                ActivityCompat.requestPermissions(this, permissionsToRequest.toArray(new String[0]), PERMISSION_REQUEST_CODE);
            } else {
                showPermissionAlert();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error requesting permissions: ", e);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        try {
            if (requestCode == PERMISSION_REQUEST_CODE) {
                if (arePermissionsGranted()) {
                    if (isMockLocationsEnabled()) {
                        loadMainActivityInBackground();
                    } else {
                        handleMockLocationPermission();
                    }
                } else {
                    showPermissionAlert();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing permissions result: ", e);
        }
    }

    private void showPermissionAlert() {
        StringBuilder message = new StringBuilder("This app requires the following permissions to function properly:\n");

        try {
            if (!arePermissionsGranted()) {
                for (String permission : permissions) {
                    if (!isPermissionGranted(this, permission)) {
                        message.append("\n").append(permission);
                    }
                }
            }

            if (!isMockLocationsEnabled()) {
                message.append("\nMock Location Permission");
            }

            new AlertDialog.Builder(this)
                    .setTitle("Permissions Required")
                    .setMessage(message.toString() + "\nPlease grant the necessary permissions.")
                    .setPositiveButton("Grant", (dialog, which) -> openAppSettings())
                    .setNegativeButton("Continue without Permissions", (dialog, which) -> loadMainActivityInBackground())
                    .setCancelable(false)
                    .show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing permission alert: ", e);
        }
    }

    private void openAppSettings() {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error opening app settings: ", e);
        }
    }

    public static Intent inT;

    private void loadMainActivityInBackground() {
        try {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());
            executor.execute(() -> {
                try {
                     inT = new Intent(SplashActivity.this, MainActivity.class);
                     startActivity(inT);
                     handler.post(this::finish);
                     // Close the SplashActivity on the main thread
                } catch (Exception e) {
                    Log.e(TAG, "Error loading main activity in background: ", e);
                }
            });
           
        } catch (Exception e) {
            Log.e(TAG, "Error in loadMainActivityInBackground: ", e);
        }
    }
}
