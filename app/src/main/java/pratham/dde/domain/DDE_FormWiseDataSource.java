package pratham.dde.domain;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity
public class DDE_FormWiseDataSource {

    @PrimaryKey
    @NonNull
    private String formwisedsid;
    private String formid;
    private String dsformid;
    private String updatedDate;
    private String userId;

    @NonNull
    public String getFormwisedsid() {
        return formwisedsid;
    }

    public void setFormwisedsid(@NonNull String formwisedsid) {
        this.formwisedsid = formwisedsid;
    }

    public String getFormid() {
        return formid;
    }

    public void setFormid(String formid) {
        this.formid = formid;
    }

    public String getDsformid() {
        return dsformid;
    }

    public void setDsformid(String dsformid) {
        this.dsformid = dsformid;
    }

    public String getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(String updatedDate) {
        this.updatedDate = updatedDate;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}