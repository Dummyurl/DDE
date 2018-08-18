package pratham.dde.domain;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity
public class DataSourceEntries {
    @PrimaryKey(autoGenerate = true)
    @NonNull
    String indexData;
    String entryId;
    String formId;
    String columnName;
    String answers;

    @NonNull
    public String getIndexData() {
        return indexData;
    }

    public void setIndexData(@NonNull String indexData) {
        this.indexData = indexData;
    }

    public String getEntryId() {
        return entryId;
    }

    public void setEntryId(String entryId) {
        this.entryId = entryId;
    }

    public String getFormId() {
        return formId;
    }

    public void setFormId(String formId) {
        this.formId = formId;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getAnswers() {
        return answers;
    }

    public void setAnswers(String answers) {
        this.answers = answers;
    }
}
