package pratham.dde.domain;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity
public class Status {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    private int EntryId;
    private String key;
    private String value;

    @NonNull
    public int getEntryId() {
        return EntryId;
    }

    public void setEntryId(@NonNull int entryId) {
        EntryId = entryId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Status{" +
                "EntryId=" + EntryId +
                ", key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}