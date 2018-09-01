package pratham.dde.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import pratham.dde.domain.DDE_FormWiseDataSource;

@Dao
public interface DDE_FormWiseDataSourceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertEntry(DDE_FormWiseDataSource dde_formWiseDataSource);

    @Query("SELECT dsformid FROM DDE_FormWiseDataSource where formid=:formId")
    String getDSFormId(String formId);

    @Query("update DDE_FormWiseDataSource set updatedDate=:updateDate where dsformid=:dsFormId")
    String setUpdateDate(String dsFormId, String updateDate);

}
