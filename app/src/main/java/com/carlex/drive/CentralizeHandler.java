package com.carlex.drive;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import com.google.android.gms.maps.model.LatLng;
import android.os.Looper;
import android.content.SharedPreferences;

public class CentralizeHandler {
    private final Handler handler;
    private final Context context;
    private final MainActivity mainActivity;
    private static final String PREFS_NAME = "FakeLoc";

    public CentralizeHandler(MainActivity mainActivity, Context context) {
        this.mainActivity = mainActivity;
        this.context = context;
        this.handler = new Handler(Looper.getMainLooper());
    }

    public void startCentralizing(long intervaloMillisegundos) {
        CentralizeRunnable centralizeRunnable = new CentralizeRunnable(mainActivity, handler, intervaloMillisegundos, context);
        handler.postDelayed(centralizeRunnable, intervaloMillisegundos);
    }

    public void stopCentralizing() {
        handler.removeCallbacksAndMessages(null);
    }

    private static class CentralizeRunnable implements Runnable {
        private final Handler handler;
        private final long intervaloMillisegundos;
        private final Context context;
        private final MainActivity mainActivity;

        public CentralizeRunnable(MainActivity mainActivity, Handler handler, long intervaloMillisegundos, Context context) {
            this.mainActivity = mainActivity;
            this.handler = handler;
            this.intervaloMillisegundos = intervaloMillisegundos;
            this.context = context;
        }

        @Override
        public void run() {
            // Atualizar mapa
            mainActivity.runOnUiThread(() -> {
                mainActivity.centralizar();
            });

          /*  // Carregar dados de localização das preferências
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            double latitude =  (double)prefs.getFloat("latitude", -23.5879554f);
            double longitude = (double)prefs.getFloat( "longitude", 46.63816059f);
            float bearing = prefs.getFloat("bearing", 45f);
            float velocidade = prefs.getFloat("speed", 0f)*3.6f;
            double altitude = (double) prefs.getFloat("altitude", 750f);*/

   
            // Atualizar com dados carregados
            mainActivity.latLng = new LatLng(FakeLocationService1.latitude, FakeLocationService1.longitude);
            mainActivity.currentSpeed = (float) (  FakeLocationService1.velocidade*3.6);
            mainActivity.currentBearing = FakeLocationService1.bearing;
            mainActivity.currentAlt = FakeLocationService1.altitude;

            // Agendar próxima execução
            handler.postDelayed(this, 50l);
        }
    }
}
