package com.carlex.drive;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Intent serviceFIntent = new Intent(context, FakeLocationService1.class);
            context.startForegroundService(serviceFIntent);
            Intent serviceIntent = new Intent(context, DataService.class);
            context.startForegroundService(serviceIntent);
        }
    }
}
