package pratham.dde.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import pratham.dde.domain.ErrorLog;

@Dao
public interface ErrorLogDao {

    @Insert
    long insert(ErrorLog logObject);

    @Query("SELECT * FROM ErrorLog")
    List<ErrorLog> getAllErrorsLog();
}