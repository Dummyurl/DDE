package pratham.dde.domain;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

@Entity(indices = {@Index(value = "RuleQuestion",unique = true)},foreignKeys = @ForeignKey(entity = DDE_RuleMaster.class, parentColumns = "RuleId", childColumns = "RuleId"))
public class DDE_RuleCondition {

    @PrimaryKey
    @NonNull
    @SerializedName("ConditionId")
    private String ConditionId;

    @SerializedName("QuestionIdentifier")
    private String QuestionIdentifier;

    @SerializedName("ConditionType")
    private String Conditiontype;

    @SerializedName("SelectValue")
    private String SelectValue;

    @SerializedName("SelectValueQuestion")
    private String SelectValueQuestion;

    @SerializedName("ShowQuestionIdentifier")
    private String RuleQuestion;

    @SerializedName("RuleId")
    private String RuleId;


    @Override
    public String toString() {
        return "DDE_RuleCondition{" + "ConditionId='" + ConditionId + '\'' + ", Conditiontype='" + Conditiontype + '\'' + ", RuleId='" + RuleId + '\'' + ", RuleQuestion='" + RuleQuestion + '\'' + ", QuestionIdentifier='" + QuestionIdentifier + '\'' + ", SelectValue='" + SelectValue + '\'' + ", SelectValueQuestion='" + SelectValueQuestion + '\'' + '}';
    }

    @NonNull
    public String getConditionId() {
        return ConditionId;
    }

    public void setConditionId(@NonNull String conditionId) {
        ConditionId = conditionId;
    }

    public String getConditiontype() {
        return Conditiontype;
    }

    public void setConditiontype(String conditiontype) {
        Conditiontype = conditiontype;
    }

    public String getRuleId() {
        return RuleId;
    }

    public void setRuleId(String ruleId) {
        RuleId = ruleId;
    }

    public String getRuleQuestion() {
        return RuleQuestion;
    }

    public void setRuleQuestion(String ruleQuestion) {
        RuleQuestion = ruleQuestion;
    }

    public String getQuestionIdentifier() {
        return QuestionIdentifier;
    }

    public void setQuestionIdentifier(String questionIdentifier) {
        QuestionIdentifier = questionIdentifier;
    }

    public String getSelectValue() {
        return SelectValue;
    }

    public void setSelectValue(String selectValue) {
        SelectValue = selectValue;
    }

    public String getSelectValueQuestion() {
        return SelectValueQuestion;
    }

    public void setSelectValueQuestion(String selectValueQuestion) {
        SelectValueQuestion = selectValueQuestion;
    }
}