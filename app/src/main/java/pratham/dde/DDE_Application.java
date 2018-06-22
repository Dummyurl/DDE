package pratham.dde;

import android.app.Application;

import com.androidnetworking.AndroidNetworking;

public class DDE_Application extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AndroidNetworking.initialize(getApplicationContext());
    }

}
