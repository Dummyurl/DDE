package pratham.dde.activities;

import android.app.Dialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
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
import com.androidnetworking.interfaces.JSONObjectRequestListener;

import org.json.JSONArray;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import pratham.dde.R;
import pratham.dde.domain.User;
import pratham.dde.fragments.FillFormsFragment;
import pratham.dde.fragments.OldFormsFragment;
import pratham.dde.services.SyncUtility;
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
    Dialog dialog;
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
        dialog = new ProgressDialog(mContext);
        userName = this.getIntent().getStringExtra("userName");
        password = this.getIntent().getStringExtra("password");

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
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
                        Bundle bundle = new Bundle();
                        bundle.putString("userName", userName);
                        bundle.putString("password", password);
                        FillFormsFragment fillFormsFragment = new FillFormsFragment();
                        fillFormsFragment.setArguments(bundle);
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
        if (SyncUtility.isDataConnectionAvailable(this)) {
            Utility.showDialoginApiCalling(dialog, mContext, "getNewForms");
            AndroidNetworking.get(url).addHeaders("Content-Type", "application/json").addHeaders("Authorization", access_token).build().getAsJSONObject(new JSONObjectRequestListener() {
                @Override
                public void onResponse(JSONObject response) {
                    // do anything with response
                    Utility.dismissShownDialog(dialog);
                    Log.d("pk-log", "" + response.length());
                    updateFormsInDatabase(response);
                }

                @Override
                public void onError(ANError error) {
                    // handle error
                    Utility.dismissShownDialog(dialog);
                    Toast.makeText(mContext, "Problem with the server, Contact administrator.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(mContext, "Internet not available", Toast.LENGTH_SHORT);
        }

        AndroidNetworking.get(url)
                .addHeaders("Content-Type", "application/json")
                .addHeaders("Authorization", access_token)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("pk-log", "" + response.length());
                        updateFormsInDatabase(response);
                    }

                    @Override
                    public void onError(ANError error) {
                        // handle error
                        Toast.makeText(mContext, "Problem with the server, Contact administrator.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateFormsInDatabase(JSONObject response) {
        try {
            JSONArray formData = new JSONArray();
            if (response.length() > 1) {
                JSONObject result = response.getJSONObject("Result");
                if (result.getString("success").equals("true")) {
                    formData = result.getJSONArray("Data");
                } else {
                    Utility.showDialogue(this, "Problem with server");
                }
            } else {
                Utility.showDialogue(this, "Problem in updating Forms in database");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
