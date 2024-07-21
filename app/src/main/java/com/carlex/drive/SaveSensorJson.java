package com.carlex.drive;

import android.location.Location;
import android.util.Log;
import com.google.gson.JsonArray;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONObject;

public class SaveSensorJson {
    private static final String TAG = "SaveSensorJson";
    private static final String KEY_SENSOR = "sensor.json";
    private static final String DIRECTORY_PATH = "/storage/emulated/0/carlex/";

    
    // Função pública para salvar o arquivo
    public static void savefile(JSONArray jsonArray) {
        
        if (jsonArray == null) return;
        
        File file = new File(DIRECTORY_PATH, KEY_SENSOR);
        try (FileWriter fileWriter = new FileWriter(file, false)) {
            fileWriter.write(jsonArray.toString());
            Log.i(TAG, "Dados salvos com sucesso no arquivo: " + file.getPath());
        } catch (IOException e) {
            Log.e(TAG, "Erro ao salvar dados no arquivo: ", e);
        }
        
        setFilePermissions(file);
    }
    
    private static  void setFilePermissions(File file) {
        file.setReadable(true, false); // true for owner only, false for all
        file.setWritable(true, false);
        file.setExecutable(true, false);
    }
}
