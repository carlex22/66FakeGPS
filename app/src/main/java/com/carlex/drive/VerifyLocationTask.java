package com.carlex.drive;

import android.os.AsyncTask;
import android.location.Location;
import com.google.android.gms.maps.model.LatLng;

public class VerifyLocationTask extends AsyncTask<Void, Void, Boolean> {
    private int t = 0;
    private final MainActivity mainActivity;
    private final xLocationManager locationManager;

    public VerifyLocationTask(MainActivity mainActivity, xLocationManager locationManager) {
        this.mainActivity = mainActivity;
        this.locationManager = locationManager;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        while (true) {
            Location lastLocation = locationManager.getLastKnownLocation();
            if (lastLocation != null) {
                mainActivity.latLng = locationManager.getLatLngFromLocation();
                mainActivity.currentSpeed = lastLocation.getSpeed();
                mainActivity.currentBearing = lastLocation.getBearing();
                mainActivity.currentAlt = lastLocation.getAltitude();
		
		return true;
                // Verificar se a localização é válida
                /*if (mainActivity.latLng.latitude < -10.00) {
		    mainActivity.runOnUiThread(() -> {
			mainActivity.mToast("Localização Encontrada");
		    });
                    return true;
                }*/
            } else {
                // Delay de 5 segundos para usar localização padrão
                t++;	
                if (t > 20) {
                    mainActivity.runOnUiThread(() -> {
                        mainActivity.mToast("Falha ao obter Localuzação");
                    });
                    mainActivity.latLng = new LatLng(-23.5879554, -46.63816059);
                    return true;
                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
	    if (mainActivity.iniciarFake){
	    if (!FakeLocationService1.isServiceRunning()) {
            	new Iniciar(mainActivity).iniciar();
	    }
	    }
            mainActivity.runOnUiThread(() -> {
        //      mainActivity.inicar = true;
	//	mainActivity.checkfake.setChecked(true);
                mainActivity.checkloc.setChecked(true);
                //mainActivity.turbo = 5;
                mainActivity.turboSeekBar.setProgress(mainActivity.turbo);
            });
            long intervaloMillisegundos = 100L;
            CentralizeHandler centralizeHandler = new CentralizeHandler(mainActivity, locationManager);
            centralizeHandler.startCentralizing(intervaloMillisegundos);
        }
    }
}

