package pratham.dde.domain;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity
public class DDE_FormWiseDataSource {

    @PrimaryKey
    @NonNull
    private int formwisedsid;
    private int formid;
    private int dsformid;

    @Override
    public String toString() {
        return "DDE_FormWiseDataSource{" +
                "formwisedsid=" + formwisedsid +
                ", formid=" + formid +
                ", dsformid=" + dsformid +
                '}';
    }

    @NonNull
    public int getFormwisedsid() {
        return formwisedsid;
    }

    public void setFormwisedsid(@NonNull int formwisedsid) {
        this.formwisedsid = formwisedsid;
    }

    public int getFormid() {
        return formid;
    }

    public void setFormid(int formid) {
        this.formid = formid;
    }

    public int getDsformid() {
        return dsformid;
    }

    public void setDsformid(int dsformid) {
        this.dsformid = dsformid;
    }
}