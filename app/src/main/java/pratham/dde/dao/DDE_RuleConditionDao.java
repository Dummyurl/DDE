package pratham.dde.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import pratham.dde.domain.DDE_Questions;
import pratham.dde.domain.DDE_RuleCondition;

@Dao
public interface DDE_RuleConditionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public long insertRuleCondition(DDE_RuleCondition dde_ruleCondition);

    @Query("SELECT * FROM DDE_RuleCondition")
    public List<DDE_RuleCondition> getAllRuleCondition();

   @Query("SELECT RuleQuestionForWhichQue FROM DDE_RuleCondition WHERE formID=:formId ")
    public List<String> getDependantQue(String formId);


}
