
package com.carlex.drive;

import android.content.Context;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Random;

public class GpsGnss implements IXposedHookLoadPackage {
    private SpaceMan spaceMan;
    private boolean devAlmanacAlwaysFalse = true;
    private boolean devEphemerisAlwaysFalse = true;
    private boolean devFixAlwaysFalse = true;
    private boolean devGpsOnly = true;
    private float fixDropRate = 0.1f;
    private Random rand = new Random();
    private boolean simulateNoise = true;

    private Handler handler;
    private Runnable gnssUpdateRunnable;
    private LocationManager locationManager;

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        Log.d("modulo", "Iniciado gpsgnss");

        hookGnssStatusBuilder(lpparam);
        hookGnssStatusCallback(lpparam);

        // Inicializar o LocationManager e o Handler para agendar atualizações periódicas
        locationManager = (LocationManager) XposedHelpers.callStaticMethod(LocationManager.class, "getService");
        handler = new Handler(Looper.getMainLooper());

        gnssUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                sendGnssStatusUpdate();
                handler.postDelayed(this, 10000); // Repetir a cada 10 segundos
            }
        };
        handler.post(gnssUpdateRunnable); // Iniciar as atualizações
    }

    private void hookGnssStatusBuilder(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedHelpers.findAndHookMethod("android.location.GnssStatus$Builder", lpparam.classLoader,
                "build", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Object gnssStatusBuilder = param.thisObject;
                XposedHelpers.callMethod(gnssStatusBuilder, "setConstellationType", 3); // GPS
                Log.d("modulo", "gnssstatusbuilder builder modificado");
            }
        });
    }

    private void hookGnssStatusCallback(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedHelpers.findAndHookMethod("android.location.GnssStatus$Callback", lpparam.classLoader,
                "onSatelliteStatusChanged", GnssStatus.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                GnssStatus status = (GnssStatus) param.args[0];
                Field numSatellites = GnssStatus.class.getDeclaredField("mNumSatellites");
                numSatellites.setAccessible(true);
                numSatellites.setInt(status, 6);
                Log.d("modulo", "gnssstatus callback modificado");
            }
        });
    }

    private void sendGnssStatusUpdate() {
        try {
            // Simular dados GNSS
            GnssStatus.Callback gnssCallback = new GnssStatus.Callback() {
                @Override
                public void onSatelliteStatusChanged(GnssStatus status) {
                    // Simular satélites visíveis
                    int satelliteCount = 6;
                    int[] prns = new int[satelliteCount];
                    float[] snrs = new float[satelliteCount];
                    float[] elevations = new float[satelliteCount];
                    float[] azimuths = new float[satelliteCount];
                    for (int i = 0; i < satelliteCount; i++) {
                        prns[i] = i + 1;
                        snrs[i] = 30 + rand.nextFloat() * 10;
                        elevations[i] = rand.nextFloat() * 90;
                        azimuths[i] = rand.nextFloat() * 360;
                    }
                    // Usar reflexão para definir os campos privados de GnssStatus
                    GnssStatus simulatedStatus = createGnssStatus(satelliteCount, prns, snrs, elevations, azimuths);
                    // Chamar o callback original com os dados simulados
                    super.onSatelliteStatusChanged(simulatedStatus);
                }
            };
            locationManager.registerGnssStatusCallback(gnssCallback);
            Log.d("modulo", "Atualização GNSS enviada");
        } catch (Exception e) {
            Log.e("modulo", "Erro ao enviar atualização GNSS", e);
        }
    }

    private GnssStatus createGnssStatus(int satelliteCount, int[] prns, float[] snrs, float[] elevations, float[] azimuths) {
        try {
            Class<?> clazz = Class.forName("android.location.GnssStatus");
            Method method = clazz.getDeclaredMethod("create", int.class, int[].class, float[].class, float[].class, float[].class);
            method.setAccessible(true);
            return (GnssStatus) method.invoke(null, satelliteCount, prns, snrs, elevations, azimuths);
        } catch (Exception e) {
            Log.e("modulo", "Erro ao criar GnssStatus simulado", e);
            return null;
        }
    }
}

