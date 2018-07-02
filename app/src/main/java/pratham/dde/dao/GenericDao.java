package pratham.dde.dao;

import android.database.sqlite.SQLiteDatabase;

import static pratham.dde.BaseActivity.appDatabase;

public class GenericDao {

    public void createTable(String tableName, String columnNames) {
        appDatabase.getOpenHelper().getWritableDatabase().execSQL("CREATE TABLE " + "aa"+ " (" +
                "cc"+ " INTEGER PRIMARY KEY," +
                "dd"+ " TEXT," +
                "ee" + " TEXT)");
    }
}
