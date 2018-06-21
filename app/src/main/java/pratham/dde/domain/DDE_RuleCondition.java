package pratham.dde.domain;

import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

public class DDE_RuleCondition {

    @PrimaryKey
    @NonNull
    private String ConditionId;
    private String Conditiontype;
    private String RuleId;
    private String RuleQuestion;
    private String QuestionIdentifier;
    private String SelectValue;
    private String SelectValueQuestion;

    @Override
    public String toString() {
        return "DDE_RuleCondition{" +
                "ConditionId='" + ConditionId + '\'' +
                ", Conditiontype='" + Conditiontype + '\'' +
                ", RuleId='" + RuleId + '\'' +
                ", RuleQuestion='" + RuleQuestion + '\'' +
                ", QuestionIdentifier='" + QuestionIdentifier + '\'' +
                ", SelectValue='" + SelectValue + '\'' +
                ", SelectValueQuestion='" + SelectValueQuestion + '\'' +
                '}';
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