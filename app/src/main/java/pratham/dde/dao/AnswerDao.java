package pratham.dde.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.database.Cursor;

import java.util.List;

import pratham.dde.domain.Answer;

@Dao
public interface AnswerDao {

    @Query("SELECT distinct FormId FROM Answer")
    public Cursor getNoOfForms();

    @Query("SELECT * FROM Answer")
    public List<Answer> getAnswers();

   /* @Insert()
    public void insertAnswers(Answer answers);
*/
    @Query("select distinct count(*) as cnt, FormId from Answer group by FormId")
    public Cursor getFormCount();


}
