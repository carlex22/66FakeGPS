package com.carlex.drive;

import android.content.Context;
import android.content.res.Resources;
import android.location.GnssNavigationMessage;
import android.location.LocationManager;
import android.util.Log;
import androidx.annotation.NonNull;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

public class GnssNavigationMessageHook implements IXposedHookLoadPackage {
    private static final String TAG = "GnssNavigationMessageHook";
    private Context systemContext;

    public void setSystemContext(Context context) {
        this.systemContext = context;
    }

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        try {
            if (!isPackageInScope(lpparam.packageName)) {
               // return;
            }

            XposedBridge.log("Hooking package: " + lpparam.packageName);

            XposedHelpers.findAndHookMethod(LocationManager.class, "registerGnssNavigationMessageCallback", GnssNavigationMessage.Callback.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    final GnssNavigationMessage.Callback originalCallback = (GnssNavigationMessage.Callback) param.args[0];
                    try {
                        GnssNavigationMessage.Callback hookedCallback = new GnssNavigationMessage.Callback() {
                            @Override
                            public void onGnssNavigationMessageReceived(@NonNull GnssNavigationMessage event) {
                                // Log original message
                                Log.i(TAG, "Original GNSS navigation message: " + event);
                                // Modify the GNSS navigation message
                                GnssNavigationMessage fakeMessage = createFakeGnssNavigationMessage(event);
                                // Pass the modified message to the original callback
                                originalCallback.onGnssNavigationMessageReceived(fakeMessage);
                                Log.i(TAG, "Hooked GNSS navigation message: " + fakeMessage);
                            }

                            @Override
                            public void onStatusChanged(int status) {
                                Log.i(TAG, "GNSS navigation message status changed: " + status);
                                originalCallback.onStatusChanged(status);
                            }
                        };
                        param.args[0] = hookedCallback;
                        Log.i(TAG, "Hooked registerGnssNavigationMessageCallback");
                    } catch (Throwable t) {
                        Log.e(TAG, "Error: " + t);
                        t.printStackTrace();
                    }
                }
            });
        } catch (Throwable t) {
            Log.e(TAG, "Error: " + t);
            t.printStackTrace();
        }
    }

    private boolean isPackageInScope(String packageName) {
        try {
            if (systemContext == null) {
                Log.e(TAG, "System context is null");
                return false;
            }
            Resources res = systemContext.getResources();
            String[] scope = res.getStringArray(res.getIdentifier("scope", "array", systemContext.getPackageName()));

            for (String pkg : scope) {
                if (packageName.equals(pkg)) {
                    return true;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in isPackageInScope: " + e.getMessage(), e);
        }
        return false;
    }

    private GnssNavigationMessage createFakeGnssNavigationMessage(GnssNavigationMessage originalMessage) {
        try {
            // Use reflection to create a fake GnssNavigationMessage object
            Constructor<GnssNavigationMessage> constructor = GnssNavigationMessage.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            GnssNavigationMessage fakeMessage = constructor.newInstance();

            // Use reflection to set the fields
            Field svidField = GnssNavigationMessage.class.getDeclaredField("mSvid");
            svidField.setAccessible(true);
            svidField.setInt(fakeMessage, 123);

            Field typeField = GnssNavigationMessage.class.getDeclaredField("mType");
            typeField.setAccessible(true);
            typeField.setInt(fakeMessage, GnssNavigationMessage.TYPE_GPS_L1CA);

            Field messageIdField = GnssNavigationMessage.class.getDeclaredField("mMessageId");
            messageIdField.setAccessible(true);
            messageIdField.setInt(fakeMessage, 1);

            Field submessageIdField = GnssNavigationMessage.class.getDeclaredField("mSubmessageId");
            submessageIdField.setAccessible(true);
            submessageIdField.setInt(fakeMessage, 1);

            Field dataField = GnssNavigationMessage.class.getDeclaredField("mData");
            dataField.setAccessible(true);
            dataField.set(fakeMessage, new byte[]{1, 2, 3, 4, 5});

            return fakeMessage;
        } catch (Exception e) {
            Log.e(TAG, "Error creating fake GNSS navigation message: " + e.getMessage(), e);
            return originalMessage;
        }
    }
}

