package pratham.dde.domain;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity /*(foreignKeys = {@ForeignKey(entity = DDE_RuleMaster.class, parentColumns = "RuleId", childColumns = "ruleId",onDelete=CASCADE), @ForeignKey(entity = DDE_RuleCondition.class, parentColumns = "RuleQuestion", childColumns = "ruleQuestion",onDelete=CASCADE)})*/
public class DDE_RuleQuestion {
    @NonNull
    @PrimaryKey/*(*//*autoGenerate = true*//*)*/
    private String RuleQuestionId;
    @SerializedName("RuleId")
    private String ruleId;
    @SerializedName("ShowQuestionIdentifier")
    private String ruleQuestion;

    @Override
    public String toString() {
        return "DDE_RuleQuestion{" + "RuleQuestionId=" + RuleQuestionId + ", RuleId='" + ruleId + '\'' + ", RuleQuestion='" + ruleQuestion + '\'' + '}';
    }

    @NonNull
    public String getRuleQuestionId() {
        return RuleQuestionId;
    }

    public void setRuleQuestionId(@NonNull String ruleQuestionId) {
        RuleQuestionId = ruleQuestionId;
    }

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public String getRuleQuestion() {
        return ruleQuestion;
    }

    public void setRuleQuestion(String ruleQuestion) {
        this.ruleQuestion = ruleQuestion;
    }
}