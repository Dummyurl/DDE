package pratham.dde.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import pratham.dde.domain.DDE_Forms;

@Dao
public interface DDE_FormsDao {

    @Insert
    void insertForms(DDE_Forms... dde_forms);

    @Query("SELECT * FROM DDE_Forms")
    DDE_Forms[] getAllForms();

    @Query("SELECT * FROM DDE_Forms where programid=:programId")
    DDE_Forms getFormProgramIdWise(String programId);

}
