package pratham.dde.domain;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

@Entity(indices = {@Index(value = "RuleId", unique = true)}, foreignKeys = @ForeignKey(entity = DDE_Forms.class, parentColumns = "formid", childColumns = "FormId"))
public class DDE_RuleMaster {

    @PrimaryKey
    @NonNull
    @SerializedName("RuleId")
    private String RuleId;
    @SerializedName("FormId")
    private String FormId;
    @SerializedName("ConditionsMatch")
    private String ConditionToBeMatched;

    @Override
    public String toString() {
        return "DDE_RuleMaster{" + "RuleId='" + RuleId + '\'' + ", FormId=" + FormId + ", ConditionToBeMatched='" + ConditionToBeMatched + '\'' + '}';
    }

    @NonNull
    public String getRuleId() {
        return RuleId;
    }

    public void setRuleId(@NonNull String ruleId) {
        RuleId = ruleId;
    }

    public String getFormId() {
        return FormId;
    }

    public void setFormId(String formId) {
        FormId = formId;
    }

    public String getConditionToBeMatched() {
        return ConditionToBeMatched;
    }

    public void setConditionToBeMatched(String conditionToBeMatched) {
        ConditionToBeMatched = conditionToBeMatched;
    }
}