package pratham.dde.domain;

import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

public class User {

    @PrimaryKey
    @NonNull
    private String Id;
    private String UserName;
    private String PhoneNumber;
    private String programids;

    @Override
    public String toString() {
        return "User{" +
                "Id='" + Id + '\'' +
                ", UserName='" + UserName + '\'' +
                ", PhoneNumber='" + PhoneNumber + '\'' +
                ", programids='" + programids + '\'' +
                '}';
    }

    @NonNull
    public String getId() {
        return Id;
    }

    public void setId(@NonNull String id) {
        Id = id;
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }

    public String getPhoneNumber() {
        return PhoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        PhoneNumber = phoneNumber;
    }

    public String getProgramids() {
        return programids;
    }

    public void setProgramids(String programids) {
        this.programids = programids;
    }
}