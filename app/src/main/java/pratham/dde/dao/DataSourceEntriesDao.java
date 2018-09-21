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

    @Query("Select * from DataSourceEntries where formId=:formid and users LIKE :userId")
    public List<DataSourceEntries> getDatasourceOnline(String formid, String userId);

    @Query("Select users from DataSourceEntries where entryId=:entryId")
    String getUsersAssociatedWithData(String entryId);
}
