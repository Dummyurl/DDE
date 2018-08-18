package pratham.dde.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import pratham.dde.domain.DDE_Forms;

@Dao
public interface DDE_FormsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertForms(DDE_Forms... dde_forms);

    @Query("SELECT * FROM DDE_Forms")
    DDE_Forms[] getAllForms();

    @Query("DELETE FROM DDE_Forms where formid=:formId")
    void deleteFormById(String formId);

    @Query("SELECT * FROM DDE_Forms where programid=:programId")
    List<DDE_Forms> getFormProgramIdWise(String programId);

    @Query("SELECT tablename FROM DDE_Forms where formid=:formId")
    String getTableName(String formId);

    @Query("SELECT formname FROM DDE_Forms where formid=:formId")
    String getFormName(String formId);

    @Query("UPDATE DDE_Forms SET PulledDateTime=:date  WHERE  formid=:formId1")
    void updatePulledDate(String formId1, String date);

    @Query("SELECT formpassword FROM DDE_Forms where formid=:formId")
    String getFormPassword(String formId);

    @Query("SELECT PulledDateTime FROM DDE_Forms where formid=:formId")
    String getPulledDateTimeByFormID(String formId);
}
