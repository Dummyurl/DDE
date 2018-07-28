package pratham.dde.domain;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;
import android.support.annotation.NonNull;

import com.google.gson.JsonArray;

@Entity
public class AnswersSingleForm {


    // private String AnswerId;

    // private String QuestionType;
    // private String Answers;

//    private String DestColumnName;

    /* @Override
     public String toString() {
         return "AnswersSingleForm{" + "AnswerId=" + AnswerId + ", EntryId='" + EntryId + '\'' + ", FormId=" + FormId + ", QuestionType='" + QuestionType + '\'' + ", Answers='" + Answers + '\'' + ", TableName='" + TableName + '\'' + ", DestColumnName='" + DestColumnName + '\'' + '}';
     }*/

    @PrimaryKey
    @NonNull
    private String EntryId;
    private String userID;
    private String date;
    private String FormId;
    private String TableName;
    private boolean isPushed = false;
    @TypeConverters(JSONArrayToString.class)
    private JsonArray answerArrayOfSingleForm;


    @NonNull
    public String getEntryId() {
        return EntryId;
    }

    public void setEntryId(@NonNull String entryId) {
        EntryId = entryId;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getFormId() {
        return FormId;
    }

    public void setFormId(String formId) {
        FormId = formId;
    }

    public String getTableName() {
        return TableName;
    }

    public void setTableName(String tableName) {
        TableName = tableName;
    }

    public boolean isPushed() {
        return isPushed;
    }

    public void setPushed(boolean pushed) {
        isPushed = pushed;
    }

    public JsonArray getAnswerArrayOfSingleForm() {
        return answerArrayOfSingleForm;
    }

    public void setAnswerArrayOfSingleForm(JsonArray answerArrayOfSingleForm) {
        this.answerArrayOfSingleForm = answerArrayOfSingleForm;
    }
}