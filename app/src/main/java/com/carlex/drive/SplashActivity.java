package com.carlex.drive;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        getSupportActionBar().hide();
        // Executar a tarefa de carregamento em segundo plano
        new LoadContentTask().execute();
    }

    private class LoadContentTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            // Simular a tarefa de carregamento (por exemplo, 3 segundos de espera)
            try {
                Thread.sleep(3000); // Substitua com o seu método de carregamento real
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            // Iniciar a MainActivity quando o carregamento estiver completo
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Fechar a SplashActivity
        }
    }
}
