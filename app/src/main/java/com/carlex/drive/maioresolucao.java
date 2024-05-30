package com.carlex.drive;



import com.google.android.gms.maps.model.*;
import java.util.*;
import android.util.Log;


public class maioresolucao {

    /*	
    public static List<LatLng> processa(List<LatLng> lista) {
        List<LatLng> novaLista = suavizarCoordenadas(lista);
        return novaLista;
    }


    public static List<LatLng> maisresolucao(List<LatLng> lista) {
        List<LatLng> novaLista = maioresolucao(lista);            
	return (novaLista);      
    }

    */

    public static List<LatLng> maioresolucao(List<LatLng> wpoints) {
        List<LatLng> novaLista = new ArrayList<>();
        for (int i = 0; i < wpoints.size() - 1; i++) {
            LatLng pontoAtual = wpoints.get(i);
            novaLista.add(pontoAtual);
            LatLng proximoPonto = wpoints.get(i + 1);
            double distancia = calculaDistancia(pontoAtual, proximoPonto);
            if (distancia > 0.5) {
                int npontos = (int) Math.floor(distancia / 0.5);
                double ndistancia = distancia / npontos;
                for (int j = 1; j < npontos; j++) {
                    double lat = pontoAtual.latitude + (j * ndistancia) * (proximoPonto.latitude - pontoAtual.latitude) / distancia;
                    double lng = pontoAtual.longitude + (j * ndistancia) * (proximoPonto.longitude - pontoAtual.longitude) / distancia;
                    novaLista.add(new LatLng(lat, lng));
                }
            }
        }
        novaLista.add(wpoints.get(wpoints.size() - 1)); // Adiciona o último ponto
        return novaLista;
    }


    private static double calculaDistancia(LatLng ponto1, LatLng ponto2) {
        double earthRadius = 6371000; // Raio da Terra em metros
        double dLat = Math.toRadians(ponto2.latitude - ponto1.latitude);
        double dLng = Math.toRadians(ponto2.longitude - ponto1.longitude);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(ponto1.latitude)) * Math.cos(Math.toRadians(ponto2.latitude)) *
                   Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }


private double distanceBetween(LatLng p1, LatLng p2) {
    double earthRadius = 6371000; // Raio da Terra em metros
    double dLat = Math.toRadians(p2.latitude - p1.latitude);
    double dLng = Math.toRadians(p2.longitude - p1.longitude);
    double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
               Math.cos(Math.toRadians(p1.latitude)) * Math.cos(Math.toRadians(p2.latitude)) *
               Math.sin(dLng / 2) * Math.sin(dLng / 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return earthRadius * c;
}

private double bearingBetween(LatLng p1, LatLng p2) {
    double dLng = Math.toRadians(p2.longitude - p1.longitude);
    double lat1 = Math.toRadians(p1.latitude);
    double lat2 = Math.toRadians(p2.latitude);
    double y = Math.sin(dLng) * Math.cos(lat2);
    double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLng);
    return (Math.toDegrees(Math.atan2(y, x)) + 360) % 360;
}

public List<LatLng> suavizarCoordenadas(List<LatLng> waypoints) {
    List<LatLng> coordenadasSuavizadas = new ArrayList<>();
    LatLng way = new LatLng(0.0, 0.0);
    
    if (waypoints.size() < 3) {
        return waypoints;
    }
    
    for (int i = 1; i < waypoints.size() - 1; i++) {
        LatLng pontoAnterior = waypoints.get(i - 1);
        LatLng pontoAtual = waypoints.get(i);
        LatLng proximoPonto = waypoints.get(i + 1);

        double bearingAnterior = 
	getBearing(pontoAnterior, pontoAtual);
        double bearingProximo = 
	getBearing(pontoAtual, proximoPonto);

	//lbearing.add(bearingProximo);

	double dist = distanceBetween(pontoAnterior, 
		pontoAtual);

        double diferencaBearing = 
	Math.abs(bearingAnterior - bearingProximo);

        if (Math.abs(diferencaBearing) <= 10 || 
	    Math.abs(diferencaBearing) >= 350	) {
            	coordenadasSuavizadas.add(pontoAtual);
		//way = pontoAtual;
        } else {
		LatLng pontoIntermediario = 
		pontoIntermediario(pontoAnterior, 
		pontoAtual, proximoPonto);
            	coordenadasSuavizadas.add(pontoIntermediario);
		//way = pontoIntermediario;
        }
	//lista.add(new Object[]{way, dist});
    }
    return coordenadasSuavizadas;
}

private LatLng pontoIntermediario(LatLng pontoAnterior, LatLng pontoAtual, LatLng proximoPonto) {
    double distanciaAnterior = calcularDistancia(pontoAnterior, pontoAtual);
    double distanciaProximo = calcularDistancia(pontoAtual, proximoPonto);
    double metadeDistancia = (distanciaAnterior + distanciaProximo) / 2.0;

    double bearing = getBearing(pontoAnterior, proximoPonto);

    double lat1 = Math.toRadians(pontoAnterior.latitude);
    double lon1 = Math.toRadians(pontoAnterior.longitude);
    double dDivR = metadeDistancia / 6371.0;
    double bearingRad = Math.toRadians(bearing);

    double lat = Math.asin(Math.sin(lat1) * Math.cos(dDivR) +
                           Math.cos(lat1) * Math.sin(dDivR) * Math.cos(bearingRad));
    double lon = lon1 + Math.atan2(Math.sin(bearingRad) * Math.sin(dDivR) * Math.cos(lat1),
                                   Math.cos(dDivR) - Math.sin(lat1) * Math.sin(lat));

    return new LatLng(Math.toDegrees(lat), Math.toDegrees(lon));
}



private double calcularDistancia(LatLng ponto1, LatLng ponto2) {
    double lat1 = Math.toRadians(ponto1.latitude);
    double lon1 = Math.toRadians(ponto1.longitude);
    double lat2 = Math.toRadians(ponto2.latitude);
    double lon2 = Math.toRadians(ponto2.longitude);
    double dLat = lat2 - lat1;
    double dLon = lon2 - lon1;
    double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
               Math.cos(lat1) * Math.cos(lat2) *
               Math.sin(dLon/2) * Math.sin(dLon/2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    return 6371 * c; 
}

private double getBearing(LatLng ponto1, LatLng ponto2) {
    double lat1 = Math.toRadians(ponto1.latitude);
    double lon1 = Math.toRadians(ponto1.longitude);
    double lat2 = Math.toRadians(ponto2.latitude);
    double lon2 = Math.toRadians(ponto2.longitude);
    double y = Math.sin(lon2 - lon1) * Math.cos(lat2);
    double x = Math.cos(lat1) * Math.sin(lat2) -
               Math.sin(lat1) * Math.cos(lat2) * Math.cos(lon2 - lon1);
    double bearing = Math.toDegrees(Math.atan2(y, x));
    return (bearing + 360) % 360;
}


}






