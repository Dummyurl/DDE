package pratham.dde.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import pratham.dde.domain.DDE_RuleTable;

@Dao
public interface DDE_RulesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllRule(List<DDE_RuleTable> ddd_formRuleTables);

    @Query("SELECT ShowQuestionIdentifier FROM DDE_RuleTable WHERE formID=:formId ")
    public List<String> getDependantQuestion(String formId);

    @Query("SELECT * FROM DDE_RuleTable")
    public List<DDE_RuleTable> getAllRules();

    @Query("DELETE FROM DDE_RuleTable WHERE formID =:formId")
    public void deleteRulesByFormID(String formId);
}
