// MainHook.java
package com.carlex.drive;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit.StartupParam;
import de.robv.android.xposed.XposedHelpers;
import android.content.Context;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;

public class MainHook implements IXposedHookZygoteInit, IXposedHookLoadPackage {
    private static final String TAG = "MainHook";

    private NmeaHook nmeaHook = new NmeaHook();
    private GnssStatusHook gnssStatusHook = new GnssStatusHook();
   // private GnssStatusHook1 gnssStatusHook1 = new GnssStatusHook1();

    private GnssMeasurementsHook gnssMeasurementsHook = new GnssMeasurementsHook();

    //private GnssNavigationMessageHook gnssNavigationMessageHook = new GnssNavigationMessageHook();

    private ConnectivityHooks connectivityHooks = new ConnectivityHooks();

    private Context systemContext;

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        try {
            // Inicialização do Zygote
            Log.i(TAG, "Zygote initialized");

            // Obter contexto do sistema
            systemContext = (Context) XposedHelpers.callStaticMethod(
                    XposedHelpers.findClass("android.app.ActivityThread", null),
                    "currentApplication"
            );
            Log.i(TAG, "System context obtained");

            //0 Conceder permissões de superusuário e de sistema ao pacotie
	    /*if (lpparam.packageName.equals("com.carlex.drive")) {           
            	grantPermissions("com.carlex.drive");
		Log.i(TAG, "Permissions granted to com.carlex.drive");
	    }*/
        } catch (Throwable t) {
            Log.e(TAG, "Error initializing Zygote: " + t);
            t.printStackTrace();
        }
    }

    private void grantPermissions(String packageName) {
        try {
            Process su = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(su.getOutputStream());

            os.writeBytes("pm grant " + packageName + " android.permission.SYSTEM_ALERT_WINDOW\n");
            os.writeBytes("pm grant " + packageName + " android.permission.WRITE_SECURE_SETTINGS\n");
            os.writeBytes("pm grant " + packageName + " android.permission.READ_PHONE_STATE\n");
            os.writeBytes("exit\n");
            os.flush();
            os.close();

            su.waitFor();
            Log.i(TAG, "Granted superuser and system permissions to " + packageName);
        } catch (IOException | InterruptedException e) {
            Log.e(TAG, "Error granting permissions: " + e);
            e.printStackTrace();
        }
    }

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        try {
		
		if (lpparam.packageName.equals("com.carlex.drive")) {
			grantPermissions("com.carlex.drive");      
			Log.i(TAG, "Permissions granted to com.carlex.drive");                                     
		}
	
	    gnssStatusHook.setSystemContext(systemContext);
        nmeaHook.setSystemContext(systemContext);
	    connectivityHooks.setSystemContext(systemContext);
        gnssMeasurementsHook.setSystemContext(systemContext);
	    
        gnssMeasurementsHook.handleLoadPackage(lpparam);
	    gnssStatusHook.handleLoadPackage(lpparam);
        nmeaHook.handleLoadPackage(lpparam);
        connectivityHooks.handleLoadPackage(lpparam);
            
        Log.i(TAG, "Hooks initialized for package: " + lpparam.packageName);
        } catch (Throwable t) {
            Log.e(TAG, "Error in MainHook: " + t);
            t.printStackTrace();
        }
    }
}

