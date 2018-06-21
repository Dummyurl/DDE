package pratham.dde.domain;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity
public class DDE_Questions {

    @PrimaryKey
    @NonNull
    private String QuestionId;
    private int FieldSeqNo;
    private String QuestionType;
    private String Question;
    private String Validations;
    private String DestColumname;
    private int FormId;
    private String Dependency;
    private String DataSource;
    private String Associate;
    private String ViewStyle;

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
                ", Dependency='" + Dependency + '\'' +
                ", DataSource='" + DataSource + '\'' +
                ", Associate='" + Associate + '\'' +
                ", ViewStyle='" + ViewStyle + '\'' +
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

    public String getDependency() {
        return Dependency;
    }

    public void setDependency(String dependency) {
        Dependency = dependency;
    }

    public String getDataSource() {
        return DataSource;
    }

    public void setDataSource(String dataSource) {
        DataSource = dataSource;
    }

    public String getAssociate() {
        return Associate;
    }

    public void setAssociate(String associate) {
        Associate = associate;
    }

    public String getViewStyle() {
        return ViewStyle;
    }

    public void setViewStyle(String viewStyle) {
        ViewStyle = viewStyle;
    }
}