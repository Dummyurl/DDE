package pratham.dde.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import pratham.dde.domain.Status;


@Dao
public interface StatusDao {

    @Query("UPDATE Status SET value = :value where keys = :key")
    void updateValue(String key, String value);

    @Insert
    void initialiseAppStatus(Status... statuses);
}
