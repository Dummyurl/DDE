package pratham.dde.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import pratham.dde.domain.Status;


@Dao
public interface StatusDao {

    @Query("UPDATE Status SET value = :value where value= :value")
    void updateValue(String key, String value);

    @Insert
    void initialiseAppStatus(Status... statuses);
}
