package com.pratham.dde.domain;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity
public class ErrorLog {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    private int Id;
    private int errorCode;
    String errorBody;
    String errorDetail;
    String activityMethod;

    @NonNull
    public int getId() {
        return Id;
    }

    public void setId(@NonNull int id) {
        Id = id;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorBody() {
        return errorBody;
    }

    public void setErrorBody(String errorBody) {
        this.errorBody = errorBody;
    }

    public String getErrorDetail() {
        return errorDetail;
    }

    public void setErrorDetail(String errorDetail) {
        this.errorDetail = errorDetail;
    }

    public String getActivityMethod() {
        return activityMethod;
    }

    public void setActivityMethod(String activityMethod) {
        this.activityMethod = activityMethod;
    }

    @Override
    public String toString() {
        return "ErrorLog{" +
                "Id=" + Id +
                ", errorCode=" + errorCode +
                ", errorBody='" + errorBody + '\'' +
                ", errorDetail='" + errorDetail + '\'' +
                ", activityMethod='" + activityMethod + '\'' +
                '}';
    }
}