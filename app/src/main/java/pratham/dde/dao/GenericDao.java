package pratham.dde.dao;

import android.arch.persistence.db.SimpleSQLiteQuery;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import static pratham.dde.BaseActivity.appDatabase;

public class GenericDao {

    public void createTable(String tableName, String columnNames) {
        appDatabase.getOpenHelper().getWritableDatabase().execSQL("CREATE TABLE " + "aa"+ " (" +
                "cc"+ " INTEGER PRIMARY KEY," +
                "dd"+ " TEXT," +
                "ee" + " TEXT)");
    }

    public void getTableCount() {
        SimpleSQLiteQuery query = new SimpleSQLiteQuery("SELECT name FROM sqlite_master WHERE type='table'",
                null);
        Cursor c = appDatabase.getStatusDao().getUserViaQuery(query);
        if (c.moveToFirst()) {
            while ( !c.isAfterLast() ) {
                Log.d("pk-count", "Table Name=> "+c.getString(0));
                c.moveToNext();
            }
        }
    }
}
