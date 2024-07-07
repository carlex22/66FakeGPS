package com.carlex.drive;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import java.util.List;

@Dao
public interface RotaFakeDao {

    @Insert
    void insert(RotaFake rotaFake);

    @Update
    void update(RotaFake rotaFake);

    @Delete
    void delete(RotaFake rotaFake);

    @Query("SELECT * FROM rota_fake")
    List<RotaFake> getAllRotaFake();


    @Query("DELETE FROM rota_fake WHERE id NOT IN (SELECT id FROM rota_fake ORDER BY id LIMIT 2)")
    void deleteAllExceptFirstFour();

    @Query("DELETE FROM rota_fake")   
    void deleteAll();


    @Query("DELETE FROM rota_fake WHERE tempo < :currentTimeMillis")
    void deleteRotaFakeWithTimeGreaterThan(long currentTimeMillis);


    @Query("SELECT * FROM rota_fake WHERE tempo >= :currentTimeMillis ORDER BY tempo ASC LIMIT 1")
    RotaFake getRotaFakeWithMinTime(long currentTimeMillis);

    @Query("SELECT * FROM rota_fake ORDER BY tempo DESC LIMIT 1")
    RotaFake getLastRotaFakeByTime();


}
