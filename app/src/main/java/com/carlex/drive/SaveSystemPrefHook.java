package com.carlex.drive;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class SaveSystemPrefHook implements IXposedHookLoadPackage, IXposedHookZygoteInit {

    private static final String TAG = "SaveSystemPrefHook";
    private static final String DIRECTORY_PATH = "/data/system/carlex/";
    private static Context moduleContext;
    private static ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        Log.d(TAG, "Initializing Zygote");
        ensureDirectoryExists(DIRECTORY_PATH);
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (lpparam.packageName.equals("com.carlex.drivre")) {
            try {
                final Class<?> activityClass = XposedHelpers.findClass("android.app.Activity", lpparam.classLoader);

                findAndHookMethod(activityClass, "onCreate", Bundle.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if (moduleContext == null) {
                            Context context = ((Activity) param.thisObject).getApplicationContext();
                            moduleContext = context.createPackageContext("com.carlex.drive", Context.CONTEXT_IGNORE_SECURITY);
                            // Grant the permission once
                            grantUriPermission(context);
                        }
                        Log.d(TAG, "Module context obtained and permission granted");
                    }
                });
            } catch (Exception e) {
                XposedBridge.log("Error in SaveSystemPrefHook module: " + e.getMessage());
            }

            hookSharedPreferencesMethods(lpparam);
        }
    }

    private void grantUriPermission(Context context) {
        try {
            XposedHelpers.callMethod(context, "grantUriPermission", "com.carlex.drive", Settings.Global.CONTENT_URI, PackageManager.PERMISSION_GRANTED);
        } catch (Exception e) {
            Log.e(TAG, "Error granting URI permission: " + e.getMessage());
        }
    }

    private void hookSharedPreferencesMethods(XC_LoadPackage.LoadPackageParam lpparam) {
        findAndHookMethod("android.app.SharedPreferencesImpl$EditorImpl", lpparam.classLoader, "putString", String.class, String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                String key = (String) param.args[0];
                String value = (String) param.args[1];
                executorService.submit(() -> savePreferenceToSystemDirectory(key, value));
            }
        });

        // Adicionar hooks para outros tipos de preferências
        findAndHookMethod("android.app.SharedPreferencesImpl$EditorImpl", lpparam.classLoader, "putBoolean", String.class, boolean.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                String key = (String) param.args[0];
                boolean value = (boolean) param.args[1];
                executorService.submit(() -> savePreferenceToSystemDirectory(key, String.valueOf(value)));
            }
        });

        findAndHookMethod("android.app.SharedPreferencesImpl$EditorImpl", lpparam.classLoader, "putInt", String.class, int.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                String key = (String) param.args[0];
                int value = (int) param.args[1];
                executorService.submit(() -> savePreferenceToSystemDirectory(key, String.valueOf(value)));
            }
        });

        findAndHookMethod("android.app.SharedPreferencesImpl$EditorImpl", lpparam.classLoader, "putLong", String.class, long.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                String key = (String) param.args[0];
                long value = (long) param.args[1];
                executorService.submit(() -> savePreferenceToSystemDirectory(key, String.valueOf(value)));
            }
        });

        findAndHookMethod("android.app.SharedPreferencesImpl$EditorImpl", lpparam.classLoader, "putFloat", String.class, float.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                String key = (String) param.args[0];
                float value = (float) param.args[1];
                executorService.submit(() -> savePreferenceToSystemDirectory(key, String.valueOf(value)));
            }
        });
    }

    private void savePreferenceToSystemDirectory(String key, String value) {
        if (moduleContext != null) {
            File dir = new File(DIRECTORY_PATH);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    Log.e(TAG, "Failed to create directory: " + DIRECTORY_PATH);
                    return;
                }
            }

            File file = new File(dir, key);
            try {
                Process su = Runtime.getRuntime().exec("su");
                OutputStreamWriter osw = new OutputStreamWriter(su.getOutputStream());
                osw.write("echo \"" + value + "\" > " + file.getAbsolutePath() + "\n");
                osw.write("chmod 666 " + file.getAbsolutePath() + "\n");
                osw.flush();
                osw.close();
                su.waitFor();
                Log.d(TAG, "Saved preference: " + key + " = " + value + " to " + file.getAbsolutePath());
            } catch (IOException | InterruptedException e) {
                Log.e(TAG, "Error saving preference to file with superuser permissions: " + e.getMessage(), e);
            }
        } else {
            Log.e(TAG, "Module context is null, cannot save preference");
        }
    }

    private void ensureDirectoryExists(String path) {
        executorService.submit(() -> {
            try {
                Process su = Runtime.getRuntime().exec("su");
                OutputStreamWriter osw = new OutputStreamWriter(su.getOutputStream());
                osw.write("mkdir -p " + path + "\n");
                osw.write("chmod 777 " + path + "\n");
                osw.flush();
                osw.close();
                su.waitFor();
                Log.d(TAG, "Ensured directory exists: " + path);
            } catch (IOException | InterruptedException e) {
                Log.e(TAG, "Error ensuring directory exists with superuser permissions: " + e.getMessage(), e);
            }
        });
    }
}
