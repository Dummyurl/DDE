package com.pratham.dde.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import com.pratham.dde.domain.Status;


@Dao
public interface StatusDao {

    @Query("UPDATE Status SET value = :value where keys = :key")
    void updateValue(String key, String value);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void initialiseAppStatus(Status... statuses);

    @Query("Select * from Status")
    List<Status> getStatus();

    @Query("select value from Status where keys = :key")
    String getValueByKey(String key);
    /*@RawQuery
    Cursor getUserViaQuery(SupportSQLiteQuery query);*/
}
