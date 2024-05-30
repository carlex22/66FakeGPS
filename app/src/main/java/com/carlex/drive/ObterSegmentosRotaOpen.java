package com.carlex.drive;

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class ObterSegmentosRotaOpen {

    private static final String TAG = "ObterSegmentosRota";
    private static final String API_KEY = "5b3ce3597851110001cf6248c8a113724e2a4ace9ca97d8082bf1942";
    private static final String BASE_URL = "https://api.openrouteservice.org/";

    public interface OnSegmentosRotaListener {
        void onSegmentosRota(List<Segmento> segmentos);
    }

    public interface OpenRouteServiceAPI {
        @POST("v2/directions/driving-car/json")
        Call<DirectionsResponse> getDirections(
            @Header("Authorization") String authorization,
            @Body Map<String, Object> requestBody
        );
    }

    public static void obterSegmentosRota(@NonNull LatLng origem, @NonNull LatLng destino, @NonNull OnSegmentosRotaListener listener) {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        httpClient.addInterceptor(logging);

        Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient.build())
            .build();

        OpenRouteServiceAPI service = retrofit.create(OpenRouteServiceAPI.class);

        Map<String, Object> requestBody = new HashMap<>();
        List<List<Double>> coordinates = new ArrayList<>();
        coordinates.add(Arrays.asList(origem.longitude, origem.latitude));
        coordinates.add(Arrays.asList(destino.longitude, destino.latitude));
        requestBody.put("coordinates", coordinates);

        Call<DirectionsResponse> call = service.getDirections("Bearer " + API_KEY, requestBody);
        call.enqueue(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Segmento> segmentos = obterSegmentos(response.body());
                    listener.onSegmentosRota(segmentos);
                } else {
                    Log.e(TAG, "Erro na resposta da API: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                Log.e(TAG, "Erro na solicitação da API", t);
            }
        });
    }

    private static List<Segmento> obterSegmentos(DirectionsResponse response) {
        List<Segmento> segmentos = new ArrayList<>();
        for (DirectionsResponse.Route route : response.routes) {
            List<LatLng> pontosTotais = decodeGeometry(route.geometry);
            double distanciaTotal = route.summary.distance;
            double duracaoTotal = route.summary.duration;
            double velocidadeTotal = distanciaTotal / duracaoTotal;
            Segmento segmento = new Segmento(distanciaTotal, duracaoTotal, pontosTotais, velocidadeTotal);
            Log.i(TAG, "distancia total: " + distanciaTotal + "m");
            Log.i(TAG, "duracao total: " + duracaoTotal + "s");
            segmentos.add(segmento);
        }
        return segmentos;
    }

    private static List<LatLng> decodeGeometry(String encodedGeometry) {
        List<LatLng> polyline = new ArrayList<>();
        int index = 0, len = encodedGeometry.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encodedGeometry.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encodedGeometry.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng(lat / 1E5, lng / 1E5);
            polyline.add(p);
        }
        return polyline;
    }

    public static class Segmento {
        private double distancia;
        private double duracao;
        private List<LatLng> pontos;
        private double velocidade;

        public Segmento(double distancia, double duracao, List<LatLng> pontos, double velocidade) {
            this.distancia = distancia;
            this.duracao = duracao;
            this.pontos = pontos;
            this.velocidade = velocidade;
        }

        public double gettDistancia() {
            return distancia;
        }

        public double getTempo() {
            return duracao;
        }

        public double getVelocidade() {
            return velocidade;
        }

        public List<LatLng> getPontos() {
            return pontos;
        }
    }

    public static class DirectionsResponse {
        public List<Route> routes;

        public static class Route {
            public Summary summary;
            public String geometry;
        }

        public static class Summary {
            public double distance;
            public double duration;
        }
    }
}

