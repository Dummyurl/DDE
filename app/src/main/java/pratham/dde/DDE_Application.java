package pratham.dde;

import android.app.Application;

import com.androidnetworking.AndroidNetworking;

import java.util.ArrayList;
import java.util.List;

import pratham.dde.domain.DataSourceEntries;

public class DDE_Application extends Application {

    public static List<DataSourceEntries> cashedDataSourceEntriesOnline;

    @Override
    public void onCreate() {
        super.onCreate();
        AndroidNetworking.initialize(getApplicationContext());
    }

    public static List<DataSourceEntries> getCashedDataSourceEntriesOnline() {
        return cashedDataSourceEntriesOnline;
    }

    public static void setCashedDataSourceEntriesOnline(List<DataSourceEntries> cashedDataSourceEntriesOnline) {
        if (DDE_Application.cashedDataSourceEntriesOnline != null) {
            DDE_Application.cashedDataSourceEntriesOnline.clear();
            DDE_Application.cashedDataSourceEntriesOnline.addAll(cashedDataSourceEntriesOnline);
        } else {
            DDE_Application.cashedDataSourceEntriesOnline = new ArrayList<>();
            DDE_Application.cashedDataSourceEntriesOnline.addAll(cashedDataSourceEntriesOnline);
        }
    }
}
