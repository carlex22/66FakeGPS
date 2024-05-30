package com.carlex.drive;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.EncodedPolyline;
import com.google.maps.android.PolyUtil;

import com.google.maps.*;



import java.util.ArrayList;
import java.util.List;

public class ObterSegmentosRota {

    private static final String TAG = "ObterSegmentosRota";
    //AIzaSyCgefVFOWLWzW4K6BngQoQgdWELwm2SlBI
    private static final String API_KEY = "AIzaSyCgefVFOWLWzW4K6BngQoQgdWELwm2SlBI";  

    public interface OnSegmentosRotaListener {
        void onSegmentosRota(List<Segmento> segmentos);
    }

    public static void obterSegmentosRota(@NonNull LatLng origem, @NonNull LatLng destino, @NonNull OnSegmentosRotaListener listener) {
        // Criando contexto da API do Google Maps
        GeoApiContext geoApiContext = new GeoApiContext.Builder()
                .apiKey(API_KEY)
                .build();

        // Solicitando rota de carro em uma AsyncTask
        new AsyncTask<Void, Void, List<Segmento>>() {
            @Override
            protected List<Segmento> doInBackground(Void... voids) {
                try {
                    DirectionsResult result = DirectionsApi.newRequest(geoApiContext)
                            .origin(new com.google.maps.model.LatLng(origem.latitude, origem.longitude))
                            .destination(new com.google.maps.model.LatLng(destino.latitude, destino.longitude))
                        //.mode(com.google.maps.DirectionsApi.Mode.driving)
                            .await();

                    if (result != null && result.routes != null && result.routes.length > 0) {
                        DirectionsRoute route = result.routes[0];
                        if (route.legs != null && route.legs.length > 0) {
                            DirectionsLeg leg = route.legs[0];
                            return obterSegmentos(leg);
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Erro ao obter segmentos de rota", e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(List<Segmento> segmentos) {
                if (segmentos != null) {
                    listener.onSegmentosRota(segmentos);
                } else {
                    Log.e(TAG, "Erro ao obter segmentos de rota");
                }
            }
        }.execute();
    }

    private static List<Segmento> obterSegmentos(DirectionsLeg leg) {
        List<Segmento> segmentos = new ArrayList<>();

	for (DirectionsStep step : leg.steps) {
	    double distanciaSegmento = step.distance.inMeters;
	    long tempoSegmento = step.duration.inSeconds;
	    double velocidadems = distanciaSegmento / tempoSegmento;
	    List<LatLng> pontosSegmento = PolyUtil.decode(step.polyline.getEncodedPath());
            Segmento segmento = new Segmento(distanciaSegmento, tempoSegmento, pontosSegmento, velocidadems);
	    Log.i(TAG, "distancia segmento" + distanciaSegmento + "m");
	    Log.i(TAG, "tempo segmento" +  tempoSegmento + "m");
            segmentos.add(segmento);
        }
        return segmentos;
    }



public static class Segmento {                                            
	private double tdistancia;                                         
	private long tempo;                                                
	private List<LatLng> pontos;                                     
	private double velocidade;                                                                                                                  
	public Segmento(double tdistancia, long tempo, List<LatLng> pontos, double velocidade) {     
		this.tdistancia = tdistancia;          
		this.tempo = (int) tempo;                    
		this.pontos = pontos;                               
		this.velocidade = velocidade;                 
	}                  

	public double gettDistancia() {       
		return tdistancia;       
	}                                                                                                                                              
	public long  getTempo() {            
		return tempo;                                             
	}

	public double getVelocidade() {             
		return velocidade;        
	}                         

	public List<LatLng> getPontos() {     
		return pontos;          
	}                                  
}

}

