package com.example.eyesyhopefyp.Shopping.DataBase;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

@Dao
public interface BarcodeDao {

    @Query("SELECT * FROM barcodes where ean = :barcodeNumber")
    List<Barcode> loadFullName(String barcodeNumber);
}
