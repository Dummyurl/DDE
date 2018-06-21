package pratham.dde.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import com.example.pravin.angreziok.dao.AttendanceDao;
import com.example.pravin.angreziok.dao.CrlDao;
import com.example.pravin.angreziok.dao.ScoreDao;
import com.example.pravin.angreziok.dao.SessionDao;
import com.example.pravin.angreziok.dao.StatusDao;
import com.example.pravin.angreziok.dao.StudentDao;
import com.example.pravin.angreziok.domain.Attendance;
import com.example.pravin.angreziok.domain.Crl;
import com.example.pravin.angreziok.domain.Score;
import com.example.pravin.angreziok.domain.Session;
import com.example.pravin.angreziok.domain.Status;
import com.example.pravin.angreziok.domain.Student;

@Database(entities = {Crl.class, Student.class, Score.class, Session.class, Attendance.class, Status.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    public static final String DB_NAME = "dynamic_data_entry";

    public abstract CrlDao getCrlDao();

    public abstract StudentDao getStudentDao();

    public abstract ScoreDao getScoreDao();

    public abstract SessionDao getSessionDao();

    public abstract AttendanceDao getAttendanceDao();

    public abstract StatusDao getStatusDao();

}
