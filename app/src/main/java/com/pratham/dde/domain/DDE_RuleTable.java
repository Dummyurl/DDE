package com.pratham.dde.domain;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;
import android.support.annotation.NonNull;

import com.google.gson.JsonArray;
import com.google.gson.annotations.SerializedName;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
public class DDE_RuleTable {
    @PrimaryKey
    @NonNull
    @SerializedName("RuleId")
    String RuleId;
    @SerializedName("ShowQuestionIdentifier")
    String ShowQuestionIdentifier;
    @SerializedName("ConditionsMatch")
    String ConditionsMatch;
    @SerializedName("QuestionCondition")
    @TypeConverters(JSONArrayToString.class)
    JsonArray QuestionCondition;
    @SerializedName("CreatedBy")
    String CreatedBy;
    @SerializedName("CreatedDate")
    String CreatedDate;
    @SerializedName("UpdatedBy")
    String UpdatedBy;
    @SerializedName("UpdatedDate")
    String UpdatedDate;

    String formID;
    @Ignore
    Set contionsSatisfied = new LinkedHashSet();

    public String getRuleId() {
        return RuleId;
    }

    public void setRuleId(String ruleId) {
        RuleId = ruleId;
    }

    public String getShowQuestionIdentifier() {
        return ShowQuestionIdentifier;
    }

    public void setShowQuestionIdentifier(String showQuestionIdentifier) {
        ShowQuestionIdentifier = showQuestionIdentifier;
    }

    public String getConditionsMatch() {
        return ConditionsMatch;
    }

    public void setConditionsMatch(String conditionsMatch) {
        ConditionsMatch = conditionsMatch;
    }

    public JsonArray getQuestionCondition() {
        return QuestionCondition;
    }

    public void setQuestionCondition(JsonArray questionCondition) {
        QuestionCondition = questionCondition;
    }

    public String getCreatedBy() {
        return CreatedBy;
    }

    public void setCreatedBy(String createdBy) {
        CreatedBy = createdBy;
    }

    public String getCreatedDate() {
        return CreatedDate;
    }

    public void setCreatedDate(String createdDate) {
        CreatedDate = createdDate;
    }

    public String getUpdatedBy() {
        return UpdatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        UpdatedBy = updatedBy;
    }

    public String getUpdatedDate() {
        return UpdatedDate;
    }

    public void setUpdatedDate(String updatedDate) {
        UpdatedDate = updatedDate;
    }

    public String getFormID() {
        return formID;
    }

    public void setFormID(String formID) {
        this.formID = formID;
    }

    public Set getContionsSatisfied() {
        return contionsSatisfied;
    }

    public void setContionsSatisfied(Set contionsSatisfied) {
        this.contionsSatisfied = contionsSatisfied;
    }
}
