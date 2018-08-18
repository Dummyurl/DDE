package pratham.dde.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.database.Cursor;

import java.util.List;

import pratham.dde.domain.AnswersSingleForm;

@Dao
public interface AnswerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public long insertAnswer(AnswersSingleForm answersSingleForm);

    @Query("SELECT distinct FormId FROM AnswersSingleForm")
    public Cursor getNoOfForms();

    @Query("SELECT * FROM AnswersSingleForm")
    public List<AnswersSingleForm> getAnswers();

    @Query("SELECT * FROM AnswersSingleForm where EntryId=:entryID")
    AnswersSingleForm getAnswersByEntryId(String entryID);

    @Query("SELECT * FROM AnswersSingleForm where FormId=:fId")
    List<AnswersSingleForm> getAllAnswersByFormId(String fId);

    @Query("SELECT * FROM AnswersSingleForm where pushStatus=:statusCode")
    List<AnswersSingleForm> getAllAnswersByStatus(int statusCode);

    @Query("SELECT * FROM AnswersSingleForm where pushStatus!=2")
    List<AnswersSingleForm> getAllAnswersByStatusUnuploaded();

    @Query("select distinct count(*) as cnt, FormId from AnswersSingleForm group by FormId")
    public Cursor getFormCount();

    @Query("select distinct * from AnswersSingleForm where userID=:uId and pushStatus=:statuscode")
    public List<AnswersSingleForm> getDistinctEntrys(String uId, int statuscode);

    @Query("select FormId  from AnswersSingleForm Where EntryId=:entryId")
    public String getFormIDByEntryID(String entryId);

    @Query("select userID  from AnswersSingleForm Where EntryId=:entryId")
    public String getUserIDByEntryID(String entryId);

    @Query("delete from AnswersSingleForm where  EntryId=:entryId")
    public void deleteAnswerEntryByEntryID(String entryId);

 /*   @Query("SELECT * FROM AnswersSingleForm where isPushed=0")
    public List<AnswersSingleForm> getAnswersToPush();*/

    @Query("UPDATE AnswersSingleForm SET pushStatus=:statusCode where EntryId=:EntryId")
    public void setPushedStatus(String EntryId, int statusCode);

    @Query("UPDATE AnswersSingleForm SET pushStatus=:statusCode where formId=:formId")
    public void setPushedStatusByFormId(String formId, int statusCode);


}
