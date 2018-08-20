package pratham.dde.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import pratham.dde.domain.DataSourceEntries;

@Dao
public interface DataSourceEntriesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertEntry(List<DataSourceEntries> dataSourceEntries);


    @Query("Select * from DataSourceEntries where formId=:formid")
    public List<DataSourceEntries> getDatasourceOnline(String formid);
/*
   /* @Query("update DataSourceEntries set answers=:answers  where formId=:formid and columnName=:clmnName")
    public void updateAnswer(String formid, String clmnName, String answers);*/
}
