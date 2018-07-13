package pratham.dde.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import pratham.dde.domain.DDE_Questions;

@Dao
public interface DDE_QuestionsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertAllQuestions(List<DDE_Questions> questions);

    @Query("SELECT * FROM DDE_Questions")
    public List<DDE_Questions> getAllQuestion();

    @Query("SELECT * FROM DDE_Questions WHERE formid=:formId")
    public List<DDE_Questions> getFormIdWiseQuestions(String formId);
}
