package com.pratham.dde.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import com.pratham.dde.domain.DataSourceEntries;

@Dao
public interface DataSourceEntriesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertEntry(List<DataSourceEntries> dataSourceEntries);

    @Query("Select * from DataSourceEntries where formId IN (:formids) and users LIKE :userId")
    List<DataSourceEntries> getDatasourceOnline(List<String> formids, String userId);

    @Query("Select users from DataSourceEntries where entryId=:entryId")
    String getUsersAssociatedWithData(String entryId);
}
