package pratham.dde.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import pratham.dde.domain.User;

@Dao
public interface UserDao {

    @Insert
    long insert(User user);

    @Query("select * from User where UserName = :UserName and Password = :Password")
    User getUserDetails(String UserName, String Password);
}
