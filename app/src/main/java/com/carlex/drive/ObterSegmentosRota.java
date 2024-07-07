package com.carlex.drive;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ObterSegmentosRota {

    private static final String TAG = "ObterSegmentosRota";

    // Chave da API visível
    private static final String API_KEY = "AIzaSyCgefVFOWLWzW4K6BngQoQgdWELwm2SlBI";

    public interface OnSegmentosRotaListener {
        void onSegmentosRota(List<Segmento> segmentos);
    }

    public static boolean obterSegmentosRota(@NonNull Context context, @NonNull LatLng origem, @NonNull LatLng destino, @NonNull OnSegmentosRotaListener listener) {
        // Criando contexto da API do Google Maps
        GeoApiContext geoApiContext = new GeoApiContext.Builder()
                .apiKey(API_KEY)
                .build();

        // Exibir ProgressDialog
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Carregando rota...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Usar ExecutorService para chamadas assíncronas
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                DirectionsResult result = DirectionsApi.newRequest(geoApiContext)
                        .origin(new com.google.maps.model.LatLng(origem.latitude, origem.longitude))
                        .destination(new com.google.maps.model.LatLng(destino.latitude, destino.longitude))
                        .await();

                if (result != null && result.routes != null && result.routes.length > 0) {
                    DirectionsRoute route = result.routes[0];
                    if (route.legs != null && route.legs.length > 0) {
                        DirectionsLeg leg = route.legs[0];
                        List<Segmento> segmentos = obterSegmentos(leg);
                        handler.post(() -> {
                            progressDialog.dismiss();
                            listener.onSegmentosRota(segmentos);
                        });
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Erro ao obter segmentos de rota", e);
                handler.post(() -> {
                    progressDialog.dismiss();
                    listener.onSegmentosRota(null);
                });
            }
        });

        return true;
    }

    private static List<Segmento> obterSegmentos(DirectionsLeg leg) {
        List<Segmento> segmentos = new ArrayList<>();

        for (DirectionsStep step : leg.steps) {
            double distanciaSegmento = step.distance.inMeters;
            long tempoSegmento = step.duration.inSeconds;
            double velocidadems = distanciaSegmento / tempoSegmento;
            List<LatLng> pontosSegmento = PolyUtil.decode(step.polyline.getEncodedPath());
            Segmento segmento = new Segmento(distanciaSegmento, tempoSegmento, pontosSegmento, velocidadems);
            Log.i(TAG, "Distância segmento: " + distanciaSegmento + " m");
            Log.i(TAG, "Tempo segmento: " + tempoSegmento + " s");
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
            this.tempo = tempo;
            this.pontos = pontos;
            this.velocidade = velocidade;
        }

        public double gettDistancia() {
            return tdistancia;
        }

        public long getTempo() {
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
