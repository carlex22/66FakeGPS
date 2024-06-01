package com.carlex.drive;

import android.os.Handler;
import android.os.Looper;
import com.google.android.gms.maps.model.LatLng;

public class CentralizeHandler {
    private final Handler handler;
    private final xLocationManager locationManager;
    private final MainActivity mainActivity;

    public CentralizeHandler(MainActivity mainActivity, xLocationManager locationManager) {
        this.mainActivity = mainActivity;
        this.locationManager = locationManager;
        this.handler = new Handler(Looper.getMainLooper());
    }

    public void startCentralizing(long intervaloMillisegundos) {
        CentralizeRunnable centralizeRunnable = new CentralizeRunnable(mainActivity, handler, intervaloMillisegundos, locationManager);
        handler.postDelayed(centralizeRunnable, intervaloMillisegundos);
    }

    public void stopCentralizing() {
        handler.removeCallbacksAndMessages(null);
    }

    private static class CentralizeRunnable implements Runnable {
        private final Handler handler;
        private final long intervaloMillisegundos;
        private final xLocationManager locationManager;
        private final MainActivity mainActivity;

        public CentralizeRunnable(MainActivity mainActivity, Handler handler, long intervaloMillisegundos, xLocationManager locationManager) {
            this.mainActivity = mainActivity;
            this.handler = handler;
            this.intervaloMillisegundos = intervaloMillisegundos;
            this.locationManager = locationManager;
        }

        @Override
        public void run() {
            // Atualizar mapa
	    mainActivity.runOnUiThread(() -> {
            	mainActivity.centralizar();
	    });
            // Atualizar com dados GPS
	    

            mainActivity.latLng = locationManager.getLatLngFromLocation();
            mainActivity.currentSpeed = locationManager.getSpeed();
            mainActivity.currentBearing = locationManager.getBearing();
            mainActivity.currentAlt = locationManager.getAltitude();

            // Agendar próxima execução
            handler.postDelayed(this, intervaloMillisegundos);
        }
    }
}

