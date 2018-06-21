package pratham.dde.database;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

import static pratham.dde.database.AppDatabase.DB_NAME;

/**
 * Created by pravin on 23 Feb 2018.
 */

public class BackupDatabase {

    public static void backup(Context mContext) {
        try {
            File sd = Environment.getExternalStorageDirectory();

            if (sd.canWrite()) {
                File file = mContext.getDir("databases", Context.MODE_PRIVATE);

                String currentDBPath = file.getAbsolutePath().replace("app_databases","databases")+"/"+ DB_NAME;
                String backupDBPath = DB_NAME+".db";
                File currentDB = new File(currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
