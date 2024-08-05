package com.carlex.drive;

import android.Manifest;
import android.view.*;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SplashActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final String TAG = "SplashActivity";
    private boolean verificandoPermissoes = false;
    private String[] permissions = {
            Manifest.permission.BODY_SENSORS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        verificarModoDesenvolvedorEPermissoes();
    }

    @Override
    protected void onResume() {
        super.onResume();
        verificarModoDesenvolvedorEPermissoes();
    }

    private void verificarModoDesenvolvedorEPermissoes() {
        if (verificandoPermissoes) return;

        verificandoPermissoes = true;
        if (isDeveloperModeEnabled()) {
            if (verificarMockLocation()) {
                verificarPermissoes();
            } else {
                tratarMockLocation();
            }
        } else {
            mostrarAlertaModoDesenvolvedor();
        }
    }

    private boolean isDeveloperModeEnabled() {
        int devSettingsEnabled = 0;
        try {
            devSettingsEnabled = Settings.Global.getInt(getContentResolver(), Settings.Global.DEVELOPMENT_SETTINGS_ENABLED);
            Log.d(TAG, "Modo desenvolvedor ativado: " + (devSettingsEnabled != 0));
        } catch (Settings.SettingNotFoundException e) {
            Log.e(TAG, "Configurações de desenvolvedor não encontradas: ", e);
        }
        return devSettingsEnabled != 0;
    }

    private boolean verificarMockLocation() {
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        if (appOps != null) {
            int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_MOCK_LOCATION, Process.myUid(), getPackageName());
            if (mode == AppOpsManager.MODE_ALLOWED) {
                Log.d(TAG, "Mock Location está habilitada.");
                return true;
            } else {
                Log.d(TAG, "Mock Location não está habilitada.");
            }
        } else {
            Log.e(TAG, "AppOpsManager é null.");
        }
        return false;
    }

    private void verificarPermissoes() {
        if (todasPermissoesConcedidas()) {
            iniciarMainActivity();
        } else {
            solicitarPermissoes();
        }
    }

    private boolean todasPermissoesConcedidas() {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permissão não concedida: " + permission);
                return false;
            }
        }
        Log.d(TAG, "Todas as permissões foram concedidas.");
        return true;
    }

    private void solicitarPermissoes() {
        List<String> permissoesAusentes = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissoesAusentes.add(permission);
            }
        }
        if (!permissoesAusentes.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissoesAusentes.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        } else {
            verificandoPermissoes = false;
            verificarModoDesenvolvedorEPermissoes();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            verificandoPermissoes = false;
            verificarModoDesenvolvedorEPermissoes();
        }
    }

    private void tratarMockLocation() {
        verificandoPermissoes = false;
        new AlertDialog.Builder(this)
                .setTitle("Permissão de Localização Falsa Necessária")
                .setMessage("O aplicativo requer a permissão de Localização Falsa. Por favor, ative a Localização Falsa nas Opções de Desenvolvedor.")
                .setPositiveButton("Abrir Opções de Desenvolvedor", (dialog, which) -> openDeveloperOptions())
                .setCancelable(false)
                .show();
    }

    private void mostrarAlertaModoDesenvolvedor() {
        verificandoPermissoes = false;
        new AlertDialog.Builder(this)
                .setTitle("Modo Desenvolvedor Necessário")
                .setMessage("O aplicativo requer que o Modo Desenvolvedor esteja ativado. Por favor, siga os passos abaixo para ativá-lo:\n\n" +
                        "1. Vá para 'Configurações'\n" +
                        "2. Role até 'Sobre o telefone'\n" +
                        "3. Toque em 'Número da versão' ou 'Número de compilação' 7 vezes até ver uma mensagem de que você agora é um desenvolvedor\n" +
                        "4. Volte para 'Configurações' e você verá a opção 'Opções do desenvolvedor'\n" +
                        "5. Ative o modo desenvolvedor\n")
                .setPositiveButton("Abrir Configurações", (dialog, which) -> openPhoneInfoSettings())
                .setCancelable(false)
                .show();
    }

    private void mostrarAlertaPermissoes() {
        verificandoPermissoes = false;
        new AlertDialog.Builder(this)
                .setTitle("Permissões Necessárias")
                .setMessage("Este aplicativo requer todas as permissões para funcionar corretamente. Por favor, conceda as permissões necessárias.")
                .setPositiveButton("Conceder", (dialog, which) -> openAppSettings())
                .setCancelable(false)
                .show();
    }

    private void openDeveloperOptions() {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Erro ao abrir opções de desenvolvedor: ", e);
            Toast.makeText(this, "Não foi possível abrir as opções de desenvolvedor.", Toast.LENGTH_SHORT).show();
        }
    }

    private void openPhoneInfoSettings() {
        try {
            Intent intent = new Intent(Settings.ACTION_DEVICE_INFO_SETTINGS);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Erro ao abrir configurações de telefone: ", e);
            Toast.makeText(this, "Não foi possível abrir as configurações de telefone.", Toast.LENGTH_SHORT).show();
        }
    }

    private void openAppSettings() {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Erro ao abrir configurações do aplicativo: ", e);
        }
    }

    private void iniciarMainActivity() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            try {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                handler.post(this::finish);
            } catch (Exception e) {
                Log.e(TAG, "Erro ao carregar a atividade principal em segundo plano: ", e);
            }
        });
    }
}
