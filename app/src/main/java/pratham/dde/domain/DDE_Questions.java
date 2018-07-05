package pratham.dde.domain;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

@Entity
public class DDE_Questions {

    @PrimaryKey
    @NonNull
    @SerializedName("QuestionIdentifier")
    private String QuestionId;

    @SerializedName("QuestionSequenceNumber")
    private int FieldSeqNo;

    @SerializedName("QuestionType")
    private String QuestionType;

    @SerializedName("QuestionTitle")
    private String Question;

    @SerializedName("")
    private String Validations;


    @SerializedName("QuestionKeyword")
    private String DestColumname;

    @SerializedName("FormId")
    private int FormId;

    @SerializedName("DataSourceValue")
    private String DataSource;

    @SerializedName("QuestionDescription")
    private String QuestionDescription;

    @SerializedName("QuestionOption")
    private String QuestionOption;


    @SerializedName("QuestionIsRequired")
    private Boolean QuestionIsRequired;

    @SerializedName("QuestionAllowDecimal")
    private Boolean QuestionAllowDecimal;

    @SerializedName("QuestionValueDependsOn")
    private String QuestionValueDependsOn;

    @SerializedName("QuestionDependValueOperator")
    private String QuestionDependValueOperator;

    @SerializedName("IncludeNoneOfTheAbove")
    private Boolean IncludeNoneOfTheAbove;

    @SerializedName("NoneOfTheAboveVal")
    private String NoneOfTheAboveVal;

    @SerializedName("SelectFromDataSource")
    private Boolean SelectFromDataSource;

    @SerializedName("DataSourceValue")
    private String DataSourceValue;

    @SerializedName("ValidationJson")
    private String ValidationJson;

    @SerializedName("OptionJson")
    private String OptionJson;

    @SerializedName("DefaultValue")
    private String DefaultValue;

    @SerializedName("MAXCHARACTERSALLOWED")
    private String MAXCHARACTERSALLOWED;

    @SerializedName("MINCHARACTERSALLOWED")
    private String MINCHARACTERSALLOWED;

    @SerializedName("MINLENGTH")
    private String MINLENGTH;

    @SerializedName("MAXLENGTH")
    private String MAXLENGTH;

    @SerializedName("MINRANGE")
    private String MINRANGE;

    @SerializedName("MAXRANGE")
    private String MAXRANGE;

    @SerializedName("DEPENDESONVALUE")
    private String DEPENDESONVALUE;

    @SerializedName("DEPENDSONOPERATOR")
    private String DEPENDSONOPERATOR;

    @SerializedName("MINIMUMSELECT")
    private String MINIMUMSELECT;

    @SerializedName("MAXIMUMSELECT")
    private String MAXIMUMSELECT;

    @Override
    public String toString() {
        return "DDE_Questions{" +
                "QuestionId='" + QuestionId + '\'' +
                ", FieldSeqNo=" + FieldSeqNo +
                ", QuestionType='" + QuestionType + '\'' +
                ", Question='" + Question + '\'' +
                ", Validations='" + Validations + '\'' +
                ", DestColumname='" + DestColumname + '\'' +
                ", FormId=" + FormId +
                ", DataSource='" + DataSource + '\'' +
                ", QuestionDescription='" + QuestionDescription + '\'' +
                ", QuestionOption='" + QuestionOption + '\'' +
                ", QuestionIsRequired='" + QuestionIsRequired + '\'' +
                ", QuestionAllowDecimal='" + QuestionAllowDecimal + '\'' +
                ", QuestionValueDependsOn='" + QuestionValueDependsOn + '\'' +
                ", QuestionDependValueOperator='" + QuestionDependValueOperator + '\'' +
                ", IncludeNoneOfTheAbove='" + IncludeNoneOfTheAbove + '\'' +
                ", NoneOfTheAboveVal='" + NoneOfTheAboveVal + '\'' +
                ", SelectFromDataSource='" + SelectFromDataSource + '\'' +
                ", DataSourceValue='" + DataSourceValue + '\'' +
                ", ValidationJson='" + ValidationJson + '\'' +
                ", OptionJson='" + OptionJson + '\'' +
                ", DefaultValue='" + DefaultValue + '\'' +
                ", MAXCHARACTERSALLOWED='" + MAXCHARACTERSALLOWED + '\'' +
                ", MINCHARACTERSALLOWED='" + MINCHARACTERSALLOWED + '\'' +
                ", MINLENGTH='" + MINLENGTH + '\'' +
                ", MAXLENGTH='" + MAXLENGTH + '\'' +
                ", MINRANGE='" + MINRANGE + '\'' +
                ", MAXRANGE='" + MAXRANGE + '\'' +
                ", DEPENDESONVALUE='" + DEPENDESONVALUE + '\'' +
                ", DEPENDSONOPERATOR='" + DEPENDSONOPERATOR + '\'' +
                ", MINIMUMSELECT='" + MINIMUMSELECT + '\'' +
                ", MAXIMUMSELECT='" + MAXIMUMSELECT + '\'' +
                        '}';
    }

    @NonNull
    public String getQuestionId() {
        return QuestionId;
    }

    public void setQuestionId(@NonNull String questionId) {
        QuestionId = questionId;
    }

    public int getFieldSeqNo() {
        return FieldSeqNo;
    }

    public void setFieldSeqNo(int fieldSeqNo) {
        FieldSeqNo = fieldSeqNo;
    }

    public String getQuestionType() {
        return QuestionType;
    }

    public void setQuestionType(String questionType) {
        QuestionType = questionType;
    }

    public String getQuestion() {
        return Question;
    }

    public void setQuestion(String question) {
        Question = question;
    }

    public String getValidations() {
        return Validations;
    }

    public void setValidations(String validations) {
        Validations = validations;
    }

    public String getDestColumname() {
        return DestColumname;
    }

    public void setDestColumname(String destColumname) {
        DestColumname = destColumname;
    }

    public int getFormId() {
        return FormId;
    }

    public void setFormId(int formId) {
        FormId = formId;
    }

    public String getDataSource() {
        return DataSource;
    }

    public void setDataSource(String dataSource) {
        DataSource = dataSource;
    }

    public String getQuestionDescription() {
        return QuestionDescription;
    }

    public void setQuestionDescription(String questionDescription) {
        QuestionDescription = questionDescription;
    }

    public String getQuestionOption() {
        return QuestionOption;
    }

    public void setQuestionOption(String questionOption) {
        QuestionOption = questionOption;
    }

    public Boolean getQuestionIsRequired() {
        return QuestionIsRequired;
    }

    public void setQuestionIsRequired(Boolean questionIsRequired) {
        QuestionIsRequired = questionIsRequired;
    }

    public Boolean getQuestionAllowDecimal() {
        return QuestionAllowDecimal;
    }

    public void setQuestionAllowDecimal(Boolean questionAllowDecimal) {
        QuestionAllowDecimal = questionAllowDecimal;
    }

    public String getQuestionValueDependsOn() {
        return QuestionValueDependsOn;
    }

    public void setQuestionValueDependsOn(String questionValueDependsOn) {
        QuestionValueDependsOn = questionValueDependsOn;
    }

    public String getQuestionDependValueOperator() {
        return QuestionDependValueOperator;
    }

    public void setQuestionDependValueOperator(String questionDependValueOperator) {
        QuestionDependValueOperator = questionDependValueOperator;
    }

    public Boolean getIncludeNoneOfTheAbove() {
        return IncludeNoneOfTheAbove;
    }

    public void setIncludeNoneOfTheAbove(Boolean includeNoneOfTheAbove) {
        IncludeNoneOfTheAbove = includeNoneOfTheAbove;
    }

    public String getNoneOfTheAboveVal() {
        return NoneOfTheAboveVal;
    }

    public void setNoneOfTheAboveVal(String noneOfTheAboveVal) {
        NoneOfTheAboveVal = noneOfTheAboveVal;
    }

    public Boolean getSelectFromDataSource() {
        return SelectFromDataSource;
    }

    public void setSelectFromDataSource(Boolean selectFromDataSource) {
        SelectFromDataSource = selectFromDataSource;
    }

    public String getDataSourceValue() {
        return DataSourceValue;
    }

    public void setDataSourceValue(String dataSourceValue) {
        DataSourceValue = dataSourceValue;
    }

    public String getValidationJson() {
        return ValidationJson;
    }

    public void setValidationJson(String validationJson) {
        ValidationJson = validationJson;
    }

    public String getOptionJson() {
        return OptionJson;
    }

    public void setOptionJson(String optionJson) {
        OptionJson = optionJson;
    }

    public String getDefaultValue() {
        return DefaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        DefaultValue = defaultValue;
    }

    public String getMAXCHARACTERSALLOWED() {
        return MAXCHARACTERSALLOWED;
    }

    public void setMAXCHARACTERSALLOWED(String MAXCHARACTERSALLOWED) {
        this.MAXCHARACTERSALLOWED = MAXCHARACTERSALLOWED;
    }

    public String getMINCHARACTERSALLOWED() {
        return MINCHARACTERSALLOWED;
    }

    public void setMINCHARACTERSALLOWED(String MINCHARACTERSALLOWED) {
        this.MINCHARACTERSALLOWED = MINCHARACTERSALLOWED;
    }

    public String getMINLENGTH() {
        return MINLENGTH;
    }

    public void setMINLENGTH(String MINLENGTH) {
        this.MINLENGTH = MINLENGTH;
    }

    public String getMAXLENGTH() {
        return MAXLENGTH;
    }

    public void setMAXLENGTH(String MAXLENGTH) {
        this.MAXLENGTH = MAXLENGTH;
    }

    public String getMINRANGE() {
        return MINRANGE;
    }

    public void setMINRANGE(String MINRANGE) {
        this.MINRANGE = MINRANGE;
    }

    public String getMAXRANGE() {
        return MAXRANGE;
    }

    public void setMAXRANGE(String MAXRANGE) {
        this.MAXRANGE = MAXRANGE;
    }

    public String getDEPENDESONVALUE() {
        return DEPENDESONVALUE;
    }

    public void setDEPENDESONVALUE(String DEPENDESONVALUE) {
        this.DEPENDESONVALUE = DEPENDESONVALUE;
    }

    public String getDEPENDSONOPERATOR() {
        return DEPENDSONOPERATOR;
    }

    public void setDEPENDSONOPERATOR(String DEPENDSONOPERATOR) {
        this.DEPENDSONOPERATOR = DEPENDSONOPERATOR;
    }

    public String getMINIMUMSELECT() {
        return MINIMUMSELECT;
    }

    public void setMINIMUMSELECT(String MINIMUMSELECT) {
        this.MINIMUMSELECT = MINIMUMSELECT;
    }

    public String getMAXIMUMSELECT() {
        return MAXIMUMSELECT;
    }

    public void setMAXIMUMSELECT(String MAXIMUMSELECT) {
        this.MAXIMUMSELECT = MAXIMUMSELECT;
    }
}