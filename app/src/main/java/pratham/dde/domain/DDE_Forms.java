package pratham.dde.domain;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity
public class DDE_Forms {

    @PrimaryKey
    @NonNull
    private int formid;
    private String formname;
    private String programid;
    private String tablename;
    private String formpassword;
    private String PulledDateTime;
    private String updateddate;

    @Override
    public String toString() {
        return "DDE_Forms{" +
                "formid=" + formid +
                ", formname='" + formname + '\'' +
                ", programid='" + programid + '\'' +
                ", tablename='" + tablename + '\'' +
                ", formpassword='" + formpassword + '\'' +
                ", PulledDateTime='" + PulledDateTime + '\'' +
                ", updateddate='" + updateddate + '\'' +
                '}';
    }

    @NonNull
    public int getFormid() {
        return formid;
    }

    public void setFormid(@NonNull int formid) {
        this.formid = formid;
    }

    public String getFormname() {
        return formname;
    }

    public void setFormname(String formname) {
        this.formname = formname;
    }

    public String getProgramid() {
        return programid;
    }

    public void setProgramid(String programid) {
        this.programid = programid;
    }

    public String getTablename() {
        return tablename;
    }

    public void setTablename(String tablename) {
        this.tablename = tablename;
    }

    public String getFormpassword() {
        return formpassword;
    }

    public void setFormpassword(String formpassword) {
        this.formpassword = formpassword;
    }

    public String getPulledDateTime() {
        return PulledDateTime;
    }

    public void setPulledDateTime(String pulledDateTime) {
        PulledDateTime = pulledDateTime;
    }

    public String getUpdateddate() {
        return updateddate;
    }

    public void setUpdateddate(String updateddate) {
        this.updateddate = updateddate;
    }
}