package pratham.dde.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import pratham.dde.domain.Answer;
import pratham.dde.domain.DDE_FormWiseDataSource;
import pratham.dde.domain.DDE_Forms;
import pratham.dde.domain.DDE_Questions;
import pratham.dde.domain.DDE_RuleCondition;
import pratham.dde.domain.DDE_RuleMaster;
import pratham.dde.domain.DDE_RuleQuestion;
import pratham.dde.domain.User;

@Database(entities = { User.class,
                       DDE_Forms.class,
                       DDE_Questions.class,
                       DDE_FormWiseDataSource.class,
                       DDE_RuleQuestion.class,
                       DDE_RuleMaster.class,
                       DDE_RuleCondition.class,
                       Answer.class}, version = 1)

public abstract class AppDatabase extends RoomDatabase {

    public static final String DB_NAME = "dynamic_data_entry";

    public abstract CrlDao getCrlDao();

    public abstract StudentDao getStudentDao();

    public abstract ScoreDao getScoreDao();

    public abstract SessionDao getSessionDao();

    public abstract AttendanceDao getAttendanceDao();

    public abstract StatusDao getStatusDao();

}
