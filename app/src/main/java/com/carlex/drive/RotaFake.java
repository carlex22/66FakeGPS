package com.carlex.drive;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

@Entity(tableName = "rota_fake")
public class RotaFake {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "latitude")
    public double latitude;

    @ColumnInfo(name = "longitude")
    public double longitude;

    @ColumnInfo(name = "bearing")
    public float bearing;

    @ColumnInfo(name = "velocidade")
    public double velocidade;

    @ColumnInfo(name = "tempo")
    public long tempo;


    public RotaFake(double latitude, double longitude, float bearing, double velocidade, long tempo) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.bearing = bearing;
        this.velocidade = velocidade;
        this.tempo = tempo;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public float getBearing() {
        return bearing;
    }

    public double getVelocidade() {
        return velocidade;
    }

    public long getTempo() {
        return tempo;
    }

}

