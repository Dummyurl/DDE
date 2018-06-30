package pratham.dde.domain;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity
public class Status {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    private int EntryId;
    private String keys;
    private String value;

    @NonNull
    public int getEntryId() {
        return EntryId;
    }

    public void setEntryId(@NonNull int entryId) {
        EntryId = entryId;
    }

    public String getKeys() {
        return keys;
    }

    public void setKeys(String keys) {
        this.keys = keys;
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
                ", key='" + keys + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}