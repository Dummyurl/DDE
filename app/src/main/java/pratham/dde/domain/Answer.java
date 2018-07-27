package pratham.dde.domain;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity
public class Answer {
//todo userID
    @PrimaryKey
    @NonNull
    private int AnswerId;
    private String EntryId;
    private int FormId;
    private String QuestionType;
    private String Answers;
    private String TableName;
    private String DestColumnName;

    @Override
    public String toString() {
        return "Answer{" +
                "AnswerId=" + AnswerId +
                ", EntryId='" + EntryId + '\'' +
                ", FormId=" + FormId +
                ", QuestionType='" + QuestionType + '\'' +
                ", Answers='" + Answers + '\'' +
                ", TableName='" + TableName + '\'' +
                ", DestColumnName='" + DestColumnName + '\'' +
                '}';
    }

    @NonNull
    public int getAnswerId() {
        return AnswerId;
    }

    public void setAnswerId(@NonNull int answerId) {
        AnswerId = answerId;
    }

    public String getEntryId() {
        return EntryId;
    }

    public void setEntryId(String entryId) {
        EntryId = entryId;
    }

    public int getFormId() {
        return FormId;
    }

    public void setFormId(int formId) {
        FormId = formId;
    }

    public String getQuestionType() {
        return QuestionType;
    }

    public void setQuestionType(String questionType) {
        QuestionType = questionType;
    }

    public String getAnswers() {
        return Answers;
    }

    public void setAnswers(String answers) {
        Answers = answers;
    }

    public String getTableName() {
        return TableName;
    }

    public void setTableName(String tableName) {
        TableName = tableName;
    }

    public String getDestColumnName() {
        return DestColumnName;
    }

    public void setDestColumnName(String destColumnName) {
        DestColumnName = destColumnName;
    }
}