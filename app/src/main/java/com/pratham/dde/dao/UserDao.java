package com.pratham.dde.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.pratham.dde.domain.User;

@Dao
public interface UserDao {

    @Insert
    long insert(User user);

    @Query("select * from User where UserName = :UserName and Password = :Password")
    User getUserDetails(String UserName, String Password);

    @Query("UPDATE User SET UserToken = :UserToken , ExpiryDate = :ExpiryDate where UserName = :UserName and Password = :Password")
    void UpdateTokenAndExpiry(String UserToken, String ExpiryDate, String UserName, String Password);

    @Query("SELECT ProgramIds from User where UserName = :UserName and Password = :Password")
    String getProgramIDs(String UserName, String Password);

    @Query("SELECT UserToken from User where UserName = :UserName and Password = :Password")
    String getToken(String UserName, String Password);

    @Query("SELECT Id from User where UserName = :UserName and Password = :Password")
    int getUserId(String UserName, String Password);

    @Query("SELECT UserToken from User where Id=:userId")
    String getUserTokenByUserID(String userId);

    @Query("SELECT * from User where Id=:userId")
    User getUserDetailsById(String userId);
}