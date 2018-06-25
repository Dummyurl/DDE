package pratham.dde.domain;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity
public class User {

    @PrimaryKey
    @NonNull
    private int Id;
    private String Name;
    private String UserName;
    private String Password;
    private String ProgramIds;
    private String ProgramNames;

    @Override
    public String toString() {
        return "User{" +
                "Id=" + Id +
                ", Name='" + Name + '\'' +
                ", UserName='" + UserName + '\'' +
                ", Password='" + Password + '\'' +
                ", ProgramIds='" + ProgramIds + '\'' +
                ", ProgramNames='" + ProgramNames + '\'' +
                '}';
    }

    @NonNull
    public int getId() {
        return Id;
    }

    public void setId(@NonNull int id) {
        Id = id;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }

    public String getProgramIds() {
        return ProgramIds;
    }

    public void setProgramIds(String programIds) {
        ProgramIds = programIds;
    }

    public String getProgramNames() {
        return ProgramNames;
    }

    public void setProgramNames(String programNames) {
        ProgramNames = programNames;
    }
}