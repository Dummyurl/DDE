package pratham.dde.domain;

import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

public class DDE_RuleQuestion {

    @PrimaryKey
    @NonNull
    private int RuleQuestionId;
    private String RuleId;
    private String RuleQuestion;

    @Override
    public String toString() {
        return "DDE_RuleQuestion{" +
                "RuleQuestionId=" + RuleQuestionId +
                ", RuleId='" + RuleId + '\'' +
                ", RuleQuestion='" + RuleQuestion + '\'' +
                '}';
    }

    @NonNull
    public int getRuleQuestionId() {
        return RuleQuestionId;
    }

    public void setRuleQuestionId(@NonNull int ruleQuestionId) {
        RuleQuestionId = ruleQuestionId;
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
}