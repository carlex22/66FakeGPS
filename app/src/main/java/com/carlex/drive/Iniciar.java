package com.carlex.drive;

import android.os.AsyncTask;
import com.google.android.gms.maps.model.LatLng;

public class Iniciar {
    private MainActivity mainActivity;

    public Iniciar(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void iniciar() {

        // Iniciar Servico FakeLocationService
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    // Limpar dados rotafake.db - Adicionar novo ponto fake
		    if (!FakeLocationService1.isServiceRunning()) {
			return null;
		    }
		    long cTime = System.currentTimeMillis() + 1000;
                    MyApp.getDatabase().rotaFakeDao().deleteAllExceptFirstFour();
                    RotaFake rotaFakeEntry = new RotaFake(
                            mainActivity.latLng.latitude, // latitude
                            mainActivity.latLng.longitude, // longitude
                            mainActivity.currentBearing, // bearing
                            mainActivity.currentSpeed, // Speed
                            cTime); // Tempo atual + 1 segundo
                    MyApp.getDatabase().rotaFakeDao().insert(rotaFakeEntry);
		    
                } catch (Exception e) {
                    // Lidar com exceções, se necessário
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
		if (!FakeLocationService1.isServiceRunning()) {
            		mainActivity.runOnUiThread(() -> {
				mainActivity.mToast("Localização Simulada Ligada");
			});
		} else {                                      
			mainActivity.startFakeLoc();
			mainActivity.runOnUiThread(() -> {
				mainActivity.mToast("Iniciando Localização Simulada");
			});
            	}
		mainActivity.runOnUiThread(() -> {
			mainActivity.iniciarFake = true;
			//mainActivity.mapaCentralizar = true;
		});
	    }

	}.execute();	
    }
}

