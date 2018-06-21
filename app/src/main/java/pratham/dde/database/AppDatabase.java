package pratham.dde.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import pratham.dde.dao.AnswerDao;
import pratham.dde.dao.DDE_FormWiseDataSourceDao;
import pratham.dde.dao.DDE_FormsDao;
import pratham.dde.dao.DDE_QuestionsDao;
import pratham.dde.dao.DDE_RuleConditionDao;
import pratham.dde.dao.DDE_RuleMasterDao;
import pratham.dde.dao.DDE_RuleQuestionDao;
import pratham.dde.dao.UserDao;
import pratham.dde.domain.Answer;
import pratham.dde.domain.DDE_FormWiseDataSource;
import pratham.dde.domain.DDE_Forms;
import pratham.dde.domain.DDE_Questions;
import pratham.dde.domain.DDE_RuleCondition;
import pratham.dde.domain.DDE_RuleMaster;
import pratham.dde.domain.DDE_RuleQuestion;
import pratham.dde.domain.User;

@Database(entities = {User.class,
        DDE_Forms.class,
        DDE_Questions.class,
        DDE_FormWiseDataSource.class,
        DDE_RuleQuestion.class,
        DDE_RuleMaster.class,
        DDE_RuleCondition.class,
        Answer.class}, version = 1)

public abstract class AppDatabase extends RoomDatabase {

    public static final String DB_NAME = "dynamic_data_entry";


    public abstract UserDao getUserDao();

    public abstract DDE_FormsDao getDDE_FormsDao();

    public abstract DDE_QuestionsDao getDDE_QuestionsDao();

    public abstract DDE_FormWiseDataSourceDao getDDE_FormWiseDataSourceDao();

    public abstract DDE_RuleQuestionDao getDDE_RuleQuestionDao();

    public abstract DDE_RuleMasterDao getDDE_RuleMasterDao();

    public abstract DDE_RuleConditionDao getDDE_RuleConditionDao();

    public abstract AnswerDao getAnswerDao();

}
