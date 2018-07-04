package pratham.dde.dao;

import android.arch.persistence.db.SimpleSQLiteQuery;
import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import static pratham.dde.BaseActivity.appDatabase;

public class GenericDao {

    public static void createTable(String tableName, String columnNames) {
        appDatabase.getOpenHelper().getWritableDatabase().execSQL("DROP TABLE IF EXISTS " + tableName);
        String CreateTable = "CREATE TABLE " + tableName + "(";
        String[] splitted = columnNames.split(",");
        for (int i = 0; i < splitted.length; i++) {
            CreateTable = CreateTable + splitted[i] + " TEXT ";
            if (i < splitted.length - 1) {
                CreateTable = CreateTable + ", ";
            }
        }
        CreateTable = CreateTable + ")";
        appDatabase.getOpenHelper().getWritableDatabase().execSQL(CreateTable);
    }

    public static void insert(String tableName, String columnValues) {
        String[] column={"name","age","work"};
        ContentValues contentValues = new ContentValues();
        String[] splittedValues = columnValues.split(",");
        for (int i = 0; i < splittedValues.length; i++) {
            contentValues.put(column[i], splittedValues[i]);
        }

       /* String insert = "INSERT INTO " + tableName + " VALUES (";
        String[] splitted = columnValues.split(",");
        for (int i = 0; i < splitted.length; i++) {
            insert = insert + splitted[i];
            if (i < splitted.length - 1) {
                insert = insert + ", ";
            }
        }
        insert = insert + " );";*/
            appDatabase.getOpenHelper().getWritableDatabase().insert(tableName,0,contentValues);
        }

    public static void getTableCount() {
        SimpleSQLiteQuery query = new SimpleSQLiteQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        Cursor c = appDatabase.getStatusDao().getUserViaQuery(query);
        if (c.moveToFirst()) {
            while (!c.isAfterLast()) {
                Log.d("pk-count", "Table Name=> " + c.getString(0));
                c.moveToNext();
            }
        }
    }
}
