package com.carlex.drive;

import android.util.Log;
import android.util.Pair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

public class ConnectivityHooks implements IXposedHookZygoteInit, IXposedHookLoadPackage {
    private static final String TAG = "ConnectivityHooks";
    private static final String PREFS_FILE_PATH = "/data/system/carlex/preferences.json";
    private Map<String, Object> hooksMap = new HashMap<>();

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        Log.d(TAG, "Zygote initialized");
        try {
            Log.d(TAG, "Reading preferences file...");
            String jsonData = readPreferencesFile();
            Log.d(TAG, "Preferences JSON data: " + jsonData);

            if (jsonData == null) {
                Log.d(TAG, "Preferences JSON data is null...");
                return;
            }

            Log.d(TAG, "Parsing JSON data...");
            JSONArray jsonArray = new JSONArray(jsonData);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Log.d(TAG, "Processing JSON object: " + jsonObject.toString());
                iterateAndHook(jsonObject);
            }
        } catch (IOException | JSONException e) {
            Log.e(TAG, "Error reading or parsing preferences JSON", e);
        }
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        Log.d(TAG, "handleLoadPackage: Loaded app: " + lpparam.packageName);
    }

    private String readPreferencesFile() throws IOException {
        File prefsFile = new File(PREFS_FILE_PATH);
        if (!prefsFile.exists()) {
            Log.d(TAG, "Preferences file not found");
            return null;
        }

        BufferedReader reader = new BufferedReader(new FileReader(prefsFile));
        StringBuilder content = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            content.append(line);
        }
        reader.close();
        Log.d(TAG, "Read preferences file content: " + content.toString());
        return content.toString();
    }

    private void iterateAndHook(JSONObject jsonObject) {
        Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            try {
                JSONObject valueObject = jsonObject.getJSONObject(key);
                String typeName = valueObject.keys().next();
                String returnValueStr = valueObject.getString(typeName);

                String[] parts = key.split("\\.");
                String methodName = parts[parts.length - 1];
                String className = String.join(".", parts[0], parts[1], parts[2]);

                Class<?> clazz = findClass(className);

                if (clazz != null) {
                    Method method = findMethod(clazz, methodName);

                    if (method != null) {
                        Object returnValue = convertFromString(typeName, returnValueStr);

                        Log.d(TAG, "Hooking method: " + key + " with return value: " + returnValue);
                        hooksMap.put(key, returnValue);
                        hookMethodWithLogging(clazz, methodName, returnValue);
                    } else {
                        Log.e(TAG, "Method not found: " + methodName + " in class " + className);
                    }
                } else {
                    Log.e(TAG, "Class not found: " + className);
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing JSON object: " + key, e);
            } catch (Exception e) {
                Log.e(TAG, "Error hooking method: " + key, e);
            }
        }
    }

    private Class<?> findClass(String className) {
        switch (className) {
            case "int":
                return int.class;
            case "boolean":
                return boolean.class;
            case "float":
                return float.class;
            case "double":
                return double.class;
            case "long":
                return long.class;
            case "short":
                return short.class;
            case "byte":
                return byte.class;
            case "char":
                return char.class;
            default:
                try {
                    return Class.forName(className);
                } catch (ClassNotFoundException e) {
                    Log.e(TAG, "Class not found: " + className, e);
                    return null;
                }
        }
    }

    private Method findMethod(Class<?> clazz, String methodName) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        return null;
    }

    private void hookMethodWithLogging(Class<?> clazz, String methodName, Object returnValue) {
        try {
            XposedHelpers.findAndHookMethod(clazz, methodName, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    Log.d(TAG, "Original method called: " + clazz.getName() + "." + methodName);
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    param.setResult(returnValue);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error hooking method: " + clazz.getName() + "." + methodName, e);
        }
    }

    private Object convertFromString(String typeName, String valueStr) throws Exception {
        switch (typeName) {
            case "java.lang.Boolean":
            case "boolean":
                return Boolean.parseBoolean(valueStr);
            case "java.lang.Integer":
            case "int":
                return Integer.parseInt(valueStr);
            case "java.lang.Long":
            case "long":
                return Long.parseLong(valueStr);
            case "java.lang.Float":
            case "float":
                return Float.parseFloat(valueStr);
            case "java.lang.Double":
            case "double":
                return Double.parseDouble(valueStr);
            case "java.lang.Short":
            case "short":
                return Short.parseShort(valueStr);
            case "java.lang.Byte":
            case "byte":
                return Byte.parseByte(valueStr);
            case "java.lang.Character":
            case "char":
                return valueStr.charAt(0);
            case "java.lang.String":
                return valueStr;
            case "java.util.List":
                return parseList(valueStr);
            case "java.util.Map":
                return parseMap(valueStr);
            case "android.util.Pair":
                return parsePair(valueStr);
            default:
                return parseComplexObject(typeName, valueStr);
        }
    }

    private List<Object> parseList(String valueStr) {
        List<Object> list = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(valueStr);
            for (int i = 0; i < jsonArray.length(); i++) {
                String itemValueStr = jsonArray.getString(i);
                list.add(convertFromString("java.lang.String", itemValueStr));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing list from string: " + valueStr, e);
        }
        return list;
    }

    private Map<String, Object> parseMap(String valueStr) {
        Map<String, Object> map = new HashMap<>();
        try {
            JSONObject jsonObject = new JSONObject(valueStr);
            Iterator<String> keys = jsonObject.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                String itemValueStr = jsonObject.getString(key);
                map.put(key, convertFromString("java.lang.String", itemValueStr));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing map from string: " + valueStr, e);
        }
        return map;
    }

    private Pair<Object, Object> parsePair(String valueStr) {
        try {
            String[] parts = valueStr.split(" ");
            Object first = convertFromString("java.lang.String", parts[0]);
            Object second = convertFromString("java.lang.String", parts[1]);
            return new Pair<>(first, second);
        } catch (Exception e) {
            Log.e(TAG, "Error parsing pair from string: " + valueStr, e);
            return null;
        }
    }

    private Object parseComplexObject(String typeName, String valueStr) {
        try {
            Class<?> clazz = findClass(typeName);
            Constructor<?> constructor = clazz.getDeclaredConstructor(String.class);
            return constructor.newInstance(valueStr);
        } catch (Exception e) {
            Log.e(TAG, "Error parsing complex object: " + typeName + " with value: " + valueStr, e);
            return null;
        }
    }
}
