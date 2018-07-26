package pratham.dde.domain;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

@Entity(indices = {@Index(value = "RuleQuestionForWhichQue",unique = true)},foreignKeys = @ForeignKey(entity = DDE_RuleMaster.class, parentColumns = "RuleId", childColumns = "RuleId"))
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
    private String RuleQuestionForWhichQue;

    @SerializedName("RuleId")
    private String RuleId;

    @SerializedName("FormId")
    private String formID;


    @Override
    public String toString() {
        return "DDE_RuleCondition{" + "ConditionId='" + ConditionId + '\'' + ", Conditiontype='" + Conditiontype + '\'' + ", RuleId='" + RuleId + '\'' + ", RuleQuestion='" + RuleQuestionForWhichQue + '\'' + ", QuestionIdentifier='" + QuestionIdentifier + '\'' + ", SelectValue='" + SelectValue + '\'' + ", SelectValueQuestion='" + SelectValueQuestion + '\'' + '}';
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


    public String getRuleQuestionForWhichQue() {
        return RuleQuestionForWhichQue;
    }

    public void setRuleQuestionForWhichQue(String ruleQuestionForWhichQue) {
        RuleQuestionForWhichQue = ruleQuestionForWhichQue;
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

    public String getFormID() {
        return formID;
    }

    public void setFormID(String formID) {
        this.formID = formID;
    }
}