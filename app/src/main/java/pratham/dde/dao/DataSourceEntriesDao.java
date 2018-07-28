package pratham.dde.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

@Dao
public interface DataSourceEntriesDao {
    @Query("Select answers from DataSourceEntries where formId=:formid and columnName=:clmnName")
    public String getAnswer(String formid, String clmnName);
}
