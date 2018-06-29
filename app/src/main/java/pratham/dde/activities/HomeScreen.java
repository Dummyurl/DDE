package pratham.dde.activities;

import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.androidnetworking.interfaces.JSONObjectRequestListener;

import org.json.JSONArray;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import pratham.dde.R;
import pratham.dde.domain.User;
import pratham.dde.fragments.FillFormsFragment;
import pratham.dde.fragments.OldFormsFragment;
import pratham.dde.utils.Utility;

import static pratham.dde.BaseActivity.appDatabase;

public class HomeScreen extends AppCompatActivity/* implements LocationLisner */ {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawer_layout;
    /* @BindView(R.id.geo)
     TextView geo;*/
    @BindView(R.id.nav_view)
    NavigationView navigationView;

    //    FusedLocationAPI fusedLocationAPI;
    String userName, password;
    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        mContext = this;
        userName = this.getIntent().getStringExtra("userName");
        password = this.getIntent().getStringExtra("password");

        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {

                        switch (menuItem.getItemId()) {
                            case R.id.nav_get_new_forms:
                                User user = appDatabase.getUserDao().getUserDetails(userName, password);
                                if (user != null)
                                    getNewForms(Utility.getProperty("getForms", HomeScreen.this), user.getUserToken());
                                else
                                    Toast.makeText(mContext, "Problem with the database, Contact administrator.", Toast.LENGTH_SHORT).show();
                                break;

                            case R.id.nav_fill_forms:
                                FillFormsFragment fillFormsFragment = new FillFormsFragment();
                                FragmentManager manager = getFragmentManager();
                                manager.beginTransaction().replace(R.id.fragment, fillFormsFragment).commit();
                                break;

                            case R.id.nav_old_forms:
                                OldFormsFragment oldFormsFragment = new OldFormsFragment();
                                FragmentManager fragmentManager = getFragmentManager();
                                fragmentManager.beginTransaction().replace(R.id.fragment, oldFormsFragment).commit();
                                break;

                        }
                        drawer_layout.closeDrawer(GravityCompat.START);
                        return true;
                    }
                });


       /* fusedLocationAPI = new FusedLocationAPI(this);
        fusedLocationAPI.startLocationButtonClick();*/
    }

    /* getFormsfromServer */
    private void getNewForms(String url, String access_token) {
        //TODO checkNetwork

        AndroidNetworking.get(url)
                .addHeaders("Content-Type", "application/json")
                .addHeaders("Authorization", access_token)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        Log.d("pk-log",""+ response.length());
                    }

                    @Override
                    public void onError(ANError error) {
                        // handle error
                        Toast.makeText(mContext, "Problem with the server, Contact administrator.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawer_layout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

   /* @Override
    public void onLocationFound(Location location) {
        if (location != null) {
            geo.setText("long" + location.getLongitude() + " /Lat " + location.getLatitude());
            fusedLocationAPI.stopLocationUpdates();
        }
    }*/
}
