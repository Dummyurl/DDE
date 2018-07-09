package pratham.dde.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import pratham.dde.domain.DDE_Questions;
import pratham.dde.domain.DDE_RuleMaster;
import pratham.dde.domain.DDE_RuleQuestion;

@Dao
public interface DDE_RuleQuestionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public long insertRuleQuestionDao(DDE_RuleQuestion dde_ruleQuestion);

    @Query("SELECT * FROM DDE_RuleQuestion")
    public List<DDE_RuleQuestion> getAllRuleQuestionDao();
}
