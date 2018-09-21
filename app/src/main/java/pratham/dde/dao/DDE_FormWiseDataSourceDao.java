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

    @Query("SELECT distinct dsformid FROM DDE_FormWiseDataSource where formid=:formId")
    List<String> getDistinctAllDSFormId(String formId);

    @Query("SELECT updatedDate FROM DDE_FormWiseDataSource where dsformid=:dsformid")
    String getLastUpdateDateOfDSFormId(String dsformid);

    @Query("SELECT * FROM DDE_FormWiseDataSource where dsformid=:dsformid and userId=:userId")
    DDE_FormWiseDataSource getDataBYDSIdAndUserId(String dsformid,String userId);

    @Query("update DDE_FormWiseDataSource set updatedDate=:updateDate where dsformid=:dsFormId and userId=:userId")
    void setUpdateDate(String dsFormId, String updateDate, String userId);

    @Query("SELECT * FROM DDE_FormWiseDataSource where userId=:userId")
    List<DDE_FormWiseDataSource> getAllDSEntriesByUID(String userId);
}
