package pratham.dde.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import pratham.dde.domain.DDE_FormWiseDataSource;

@Dao
public interface DDE_FormWiseDataSourceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertEntry(DDE_FormWiseDataSource dde_formWiseDataSource);

    @Query("SELECT dsformid FROM DDE_FormWiseDataSource where formid=:formId")
    String getDSFormId(String formId);

    @Query("SELECT * FROM DDE_FormWiseDataSource where dsformid=:dsformid")
    DDE_FormWiseDataSource getDataBYDSId(String dsformid);

    @Query("update DDE_FormWiseDataSource set updatedDate=:updateDate where dsformid=:dsFormId")
    void setUpdateDate(String dsFormId, String updateDate);

    @Query("SELECT * FROM DDE_FormWiseDataSource")
    List<DDE_FormWiseDataSource> getAllDSEntries();
}
