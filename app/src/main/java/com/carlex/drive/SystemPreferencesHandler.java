package com.carlex.drive;

import android.util.Log;
import com.topjohnwu.superuser.Shell;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class SystemPreferencesHandler {
    private static final String TAG = "SystemPreferencesHandler";
   // private static final String SYSTEM_PREFS_PATH = "/data/system/users/0/setglobal.xml";

    public SystemPreferencesHandler() {
        // Verificar se o acesso root está disponível
      //  if (!Shell.rootAccess()) {
          //  Log.e(TAG, "Root access not available");    }
    }

    public void savePreference(String key, String value) {
        /*try {
            File file = new File(SYSTEM_PREFS_PATH);
            if (!file.exists()) {
                Log.e(TAG, "System preferences file does not exist");
                return;
            }

            // Ler o conteúdo do arquivo
            StringBuilder fileContent = new StringBuilder();
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            boolean keyFound = false;

            while ((line = reader.readLine()) != null) {
                if (line.contains("name=\"" + key + "\"")) {
                    // Substituir a linha existente pela nova preferência
                    fileContent.append("<string name=\"").append(key).append("\" value=\"").append(value).append("\" />\n");
                    keyFound = true;
                } else {
                    fileContent.append(line).append("\n");
                }
            }
            reader.close();

            if (!keyFound) {
                // Adicionar a nova preferência ao final do arquivo
                fileContent.append("<string name=\"").append(key).append("\" value=\"").append(value).append("\" />\n");
            }

            // Escrever o conteúdo modificado de volta ao arquivo
            FileWriter writer = new FileWriter(file);
            writer.write(fileContent.toString());
            writer.close();

            Log.d(TAG, "Preference saved: " + key + " = " + value);
        } catch (IOException e) {
            Log.e(TAG, "Error saving preference", e);
        }*/
    }

    public String getPreference(String key) {
       /*" try {
            File file = new File(SYSTEM_PREFS_PATH);
            if (!file.exists()) {
                Log.e(TAG, "System preferences file does not exist");
                return null;
            }

            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("name=\"" + key + "\"")) {
                    reader.close();
                    return line; // Retornar a linha completa, incluindo chave e valor
                }
            }
            reader.close();
        } catch (IOException e) {
            Log.e(TAG, "Error getting preference", e);
        }*/
        return null;
    }
}
