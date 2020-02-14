package com.pratham.dde.database;

import android.content.Context;
import android.os.Environment;

import com.pratham.dde.utils.PermissionUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

/**
 * Created by pravin on 23 Feb 2018.
 */

public class BackupDatabase {

    public static void backup(Context mContext) {
        try {
            File sd = new File(Environment.getExternalStorageDirectory()+"/PrathamBackups");
            if(!sd.exists())
                sd.mkdir();
            if (sd.canWrite()) {
                File currentDB = mContext.getDatabasePath(AppDatabase.DB_NAME);
                File parentPath = currentDB.getParentFile();
                for (File f : parentPath.listFiles()) {
                    File temp = new File(sd, f.getName());
                    if (!temp.exists()) temp.createNewFile();
                    FileChannel src = new FileInputStream(f).getChannel();
                    FileChannel dst = new FileOutputStream(temp).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
            } else {
                EventBus.getDefault().post(PermissionUtils.WRITE_PERMISSION);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
