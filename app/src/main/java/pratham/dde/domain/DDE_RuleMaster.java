package pratham.dde.domain;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity
public class DDE_RuleMaster {

    @PrimaryKey
    @NonNull
    private String RuleId;
    private int FormId;
    private String ConditionToBeMatched;

    @Override
    public String toString() {
        return "DDE_RuleMaster{" +
                "RuleId='" + RuleId + '\'' +
                ", FormId=" + FormId +
                ", ConditionToBeMatched='" + ConditionToBeMatched + '\'' +
                '}';
    }

    @NonNull
    public String getRuleId() {
        return RuleId;
    }

    public void setRuleId(@NonNull String ruleId) {
        RuleId = ruleId;
    }

    public int getFormId() {
        return FormId;
    }

    public void setFormId(int formId) {
        FormId = formId;
    }

    public String getConditionToBeMatched() {
        return ConditionToBeMatched;
    }

    public void setConditionToBeMatched(String conditionToBeMatched) {
        ConditionToBeMatched = conditionToBeMatched;
    }
}