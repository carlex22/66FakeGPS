package com.carlex.drive;

import java.util.ArrayList;
import java.util.List;
import java.util.List;


import com.google.android.gms.maps.model.LatLng;
import java.util.List;

public class SimuladorFrenagemSegura {

    // Método para calcular a distância entre dois pontos
    private static double calcularDistancia(LatLng ponto1, LatLng ponto2) {
        double dx = ponto2.latitude - ponto1.latitude;
        double dy = ponto2.longitude - ponto1.longitude;

        return Math.sqrt(dx * dx + dy * dy) * 1000000; // Converter para metros
    }

    // Método para calcular a distância de frenagem segura
    private static double calcularDistanciaFrenagemSegura(double velocidadeInicial, double anguloCurva) {
        double anguloEmRadianos = anguloCurva * Math.PI / 180;
        double aceleracaoDesaceleracao = 9.81 * Math.cos(anguloEmRadianos);

        return (velocidadeInicial * velocidadeInicial) / (2 * aceleracaoDesaceleracao);
    }

    // Método para calcular a velocidade segura
    private static double calcularVelocidadeSegura(double distanciaCurva, double velocidadeInicial, double anguloCurva) {
        double anguloEmRadianos = anguloCurva * Math.PI / 180;
        double aceleracaoDesaceleracao = 9.81 * Math.cos(anguloEmRadianos);

        return Math.sqrt(2 * aceleracaoDesaceleracao * distanciaCurva);
    }

    // Método principal para calcular a frenagem segura e atualizar a velocidade do veículo
    public void calcularFrenagemSegura(List<Object[]> rotaFake, double velocidadeAtual) {
        double distanciaFrenagemSegura = 0;
        double distanciaAteInicioCurva = Double.MAX_VALUE;
        double anguloCurva = 0;
        double velocidadeSegura;
        double novaVelocidade;
        int indicePontoInicioCurva = -1;

        for (int i = 0; i < rotaFake.size(); i++) {
            Object[] dadosSegmento = rotaFake.get(i);
            LatLng pontoAtual = (LatLng) dadosSegmento[1];
            float bearing = (float) dadosSegmento[2]; // Corrigido para Float
            double velocidadeSegmento = (double) dadosSegmento[3]; // Usado para atualizações se necessário

            // Calcular o ângulo da curva baseado no bearing
            anguloCurva = Math.abs(bearing); // Ajuste conforme necessário

            // Verificar se o ponto atual é o início de uma curva
            if (anguloCurva > 0) {
                indicePontoInicioCurva = i;
                distanciaAteInicioCurva = 0;
            }

            // Calcular a distância até o início da curva (se identificada)
            if (indicePontoInicioCurva != -1) {
                distanciaAteInicioCurva = calcularDistancia((LatLng) rotaFake.get(indicePontoInicioCurva)[1], pontoAtual);
            }

            // Calcular a distância segura de frenagem
            if (distanciaAteInicioCurva <= Double.MAX_VALUE) {
                distanciaFrenagemSegura = calcularDistanciaFrenagemSegura(velocidadeAtual, anguloCurva);
            }

            // Aplicar o fator de velocidade segura
            if (distanciaAteInicioCurva <= distanciaFrenagemSegura) {
                velocidadeSegura = calcularVelocidadeSegura(distanciaAteInicioCurva, velocidadeAtual, anguloCurva);
                novaVelocidade = Math.min(velocidadeAtual, velocidadeSegura);

                // Atualizar a velocidade do veículo na lista rotaFake
                atualizarVelocidadeRotaFake(rotaFake, i, novaVelocidade);

                // Reduzir a velocidade progressivamente até a velocidade segura
                if (novaVelocidade > velocidadeSegura) {
                    novaVelocidade = Math.max(velocidadeSegura, novaVelocidade - 0.1); // Reduzir em 0.1 m/s por ponto
                    atualizarVelocidadeRotaFake(rotaFake, i, novaVelocidade);
                }

                velocidadeAtual = novaVelocidade; // Atualizar a velocidade atual
            }
        }
    }

    // Método para atualizar a velocidade do veículo na lista rotaFake
    private void atualizarVelocidadeRotaFake(List<Object[]> rotaFake, int indice, double novaVelocidade) {
        Object[] segmento = rotaFake.get(indice);
        segmento[3] = novaVelocidade; // Atualizar a velocidade no segmento
        rotaFake.set(indice, segmento);
     //   System.out.println("Velocidade atualizada para: " + novaVelocidade + " m/s no segmento " + indice);
    }
}

