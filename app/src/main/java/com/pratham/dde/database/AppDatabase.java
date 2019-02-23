package com.pratham.dde.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.pratham.dde.dao.AnswerDao;
import com.pratham.dde.dao.DDE_FormWiseDataSourceDao;
import com.pratham.dde.dao.DDE_FormsDao;
import com.pratham.dde.dao.DDE_QuestionsDao;
import com.pratham.dde.dao.DDE_RulesDao;
import com.pratham.dde.dao.DataSourceEntriesDao;
import com.pratham.dde.dao.ErrorLogDao;
import com.pratham.dde.dao.StatusDao;
import com.pratham.dde.dao.UserDao;
import com.pratham.dde.domain.DDE_FormWiseDataSource;
import com.pratham.dde.domain.AnswersSingleForm;
import com.pratham.dde.domain.DDE_Forms;
import com.pratham.dde.domain.DDE_Questions;
import com.pratham.dde.domain.DDE_RuleTable;
import com.pratham.dde.domain.DataSourceEntries;
import com.pratham.dde.domain.ErrorLog;
import com.pratham.dde.domain.Status;
import com.pratham.dde.domain.User;

@Database(entities = {User.class, Status.class, DDE_Questions.class, DDE_Forms.class, DDE_FormWiseDataSource.class, AnswersSingleForm.class, DDE_RuleTable.class, DataSourceEntries.class, ErrorLog.class}, version = 1, exportSchema = false)

public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase INSTANCE;

    public static final String DB_NAME = "dynamic_data";

    public abstract UserDao getUserDao();

    public abstract DDE_RulesDao getDDE_RulesDao();

    public abstract DataSourceEntriesDao getDataSourceEntriesDao();

    public abstract DDE_FormsDao getDDE_FormsDao();

    public abstract DDE_QuestionsDao getDDE_QuestionsDao();

    public abstract DDE_FormWiseDataSourceDao getDDE_FormWiseDataSourceDao();

    public abstract AnswerDao getAnswerDao();

    public abstract StatusDao getStatusDao();

    public abstract ErrorLogDao getErrorLogDao();

    public static AppDatabase getDatabaseInstance(final Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                    AppDatabase.class, DB_NAME)
                    .allowMainThreadQueries() // SHOULD NOT BE USED IN PRODUCTION !!!
                    .build();
        }
        return INSTANCE;
    }


}
