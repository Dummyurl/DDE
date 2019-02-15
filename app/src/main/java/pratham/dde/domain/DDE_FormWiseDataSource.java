package pratham.dde.domain;

import android.arch.persistence.room.Entity;
import android.support.annotation.NonNull;

@Entity(primaryKeys = {"formwisedsid", "userId"})
public class DDE_FormWiseDataSource {

    @NonNull
    private String formwisedsid;
    @NonNull
    private String userId;
    private String formid;
    private String dsformid;
    private String updatedDate;

    public String getFormwisedsid() {
        return formwisedsid;
    }

    public void setFormwisedsid(String formwisedsid) {
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