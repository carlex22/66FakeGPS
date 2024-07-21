package com.carlex.drive;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;

public class DataContentProvider extends ContentProvider {

    public static final String AUTHORITY = "com.carlex.drive.fakesensor";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/data");
    private static final String JSON_DATA = "json_data";
    private JSONArray jsonDataArray;

    @Override
    public boolean onCreate() {
        jsonDataArray = new JSONArray(); // Inicialização do JSON
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        MatrixCursor cursor = new MatrixCursor(new String[]{JSON_DATA});
        cursor.addRow(new Object[]{jsonDataArray.toString()});
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return "application/json";
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        try {
            String jsonString = values.getAsString(JSON_DATA);
            jsonDataArray = new JSONArray(jsonString);
        } catch (JSONException e) {
            Log.e("DataContentProvider", "Erro ao inserir JSON", e);
        }
        return Uri.withAppendedPath(CONTENT_URI, "1");
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Delete não suportado");
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Update não suportado");
    }
}
