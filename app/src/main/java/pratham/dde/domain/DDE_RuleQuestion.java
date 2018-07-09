package pratham.dde.domain;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

@Entity/* (foreignKeys = {@ForeignKey(entity = DDE_RuleMaster.class, parentColumns = "RuleId", childColumns = "RuleId"), @ForeignKey(entity = DDE_RuleCondition.class, parentColumns = "RuleQuestion", childColumns = "RuleQuestion")})*/
public class DDE_RuleQuestion {
    @NonNull
    @PrimaryKey(autoGenerate = true)
    private int RuleQuestionId;
    @SerializedName("RuleId")
    private String RuleId;
    @SerializedName("ShowQuestionIdentifier")
    private String RuleQuestion;

    @Override
    public String toString() {
        return "DDE_RuleQuestion{" + "RuleQuestionId=" + RuleQuestionId + ", RuleId='" + RuleId + '\'' + ", RuleQuestion='" + RuleQuestion + '\'' + '}';
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