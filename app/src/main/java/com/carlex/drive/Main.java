package com.carlex.drive;

import java.util.Arrays;
import java.util.List;
import android.util.Log;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
//import com.carlex.drive.MockFakeKt;
//import com.carlex.drive.MockFake;
import org.json.JSONObject;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import org.json.JSONObject;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XC_MethodReplacement;

import java.lang.reflect.Method;


public class Main implements IXposedHookLoadPackage, IXposedHookZygoteInit {

    public static class Settings {
        static final List<String> hookedApps = Arrays.asList(
                "com.carlex.drive",
		//"com.google.android.gms",
		//"com.google.android.gms.location.sample.locationupdates",
		//"com.qualcomm.location",
		//"com.android.location.fused",
		"com.tananaev.gpsfix",
                "com.app99.drive",
                "flar2.devcheck",
                "com.google.android.apps.location.gps.gnsslogger",
		//"com.android.server.location",
		//"android.location.LocationManager",
                "fr.dvilleneuve.lockiito"
        );
    }

    //protected GpsGnss gpsgnss;
    //protected GPS gps;
    

    
    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        // Código de inicialização se necessário
	Log.d("Main", "initZygote");
	Log.d("Main", "startupParam "+startupParam.toString());
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
	Log.d("Main", "handleLoadPackage");
        if (Settings.hookedApps.contains(lpparam.packageName)) {
	    Log.d("Main", "hookedApps "+lpparam.packageName);


/*	    XposedHelpers.findAndHookMethod("com.carlex.drive.DriverInfo", lpparam.classLoader, "toJsonWithXposed", Object.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Object object = param.args[0];
                for (Method method : object.getClass().getDeclaredMethods()) {
                    //if (method.getName().startsWith("get")) {
                        method.setAccessible(true);
                    //}
                }
	    }});
*/
/*
	try {
            Class<?> targetClass = lpparam.classLoader.loadClass("android.telephony.TelephonyManager");
            XposedBridge.hookAllMethods(targetClass, "getAllCellInfo", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Object result = param.getResult();
                    if (result != null) {
                        JSONObject json = JsonCreator.createJson(result, "android.telephony.TelephonyManager");
                        XposedBridge.log("Generated JSON: " + json.toString());
                    }
                }
            });
        } catch (Exception e) {
            XposedBridge.log(e);
        }

*/

            GpsGnss gpsgnss = new GpsGnss();
            gpsgnss.handleLoadPackage(lpparam);
	    Log.d("GpsGnss", "GpsGnss loaded");

	    //gps = new GPS();
            //gps.handleLoadPackage(lpparam);
            //Log.d("Gps", "Gps loaded");

	    /*MockFake mockfake = new MockFake();
	    mockfake.handleLoadPackage(lpparam);
	    Log.d("MockFake", "MockFake loaded");*/

            //cellinfo = new Cellinfo();
            //cellinfo.handleLoadPackage(lpparam);
	    //Log.d("Cellinfo", "Cellinfo loaded");
        }
    }
}

