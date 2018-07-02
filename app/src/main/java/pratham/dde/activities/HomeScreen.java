package pratham.dde.activities;

import android.app.Dialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
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
import pratham.dde.dao.GenericDao;
import pratham.dde.domain.DDE_Forms;
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
            Utility.showDialogInApiCalling(dialog, mContext, "Getting new forms... Please wait.");
            AndroidNetworking.get(url)
                    .addHeaders("Content-Type", "application/json")
                    .addHeaders("Authorization", access_token)
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d("pk-log", "" + response.length());
                            Utility.setMessage(dialog,"Updating forms in Database... Please wait.");
                            updateFormsInDatabase(response);
                        }

                        @Override
                        public void onError(ANError error) {
                            Utility.dismissDialog(dialog);
                            Toast.makeText(mContext, "Problem with the server, Contact administrator.", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(mContext, "Internet not available", Toast.LENGTH_SHORT);
        }
    }

    private void updateFormsInDatabase(JSONObject response) {
        try {
            JSONArray formData;
            JSONObject tempObj;
            DDE_Forms dde_form;
            final DDE_Forms[] dde_forms;

            if (response.length() > 1) {
                JSONObject result = response.getJSONObject("Result");
                if (result.getString("success").equals("true")) {
                    formData = result.getJSONArray("Data");
                    dde_forms = new DDE_Forms[formData.length()];
                    for (int i = 0; i < formData.length(); i++){
                        dde_form = new DDE_Forms();
                        tempObj = formData.getJSONObject(i);
                        dde_form.setFormid(tempObj.getInt("formid"));
                        dde_form.setFormname(tempObj.getString("formname"));
                        dde_form.setFormpassword(tempObj.getString("formpassword"));
                        dde_form.setProgramid(tempObj.getString("programid"));
                        dde_form.setTablename(tempObj.getString("tablename"));
                        dde_form.setUpdateddate(tempObj.getString("updateddate"));
                        dde_form.setPulledDateTime(Utility.getCurrentDateTime());
                        dde_forms[i] = dde_form;
                    }

                    new AsyncTask<Void, Void, String>() {
                        @Override
                        protected String doInBackground(Void... voids) {
                            appDatabase.getDDE_FormsDao().insertForms(dde_forms);
                            return null;
                        }

                        @Override
                        protected void onPostExecute(String s) {
                            Utility.dismissDialog(dialog);
                            Log.d("pk-size", "pk-length:-"+appDatabase.getDDE_FormsDao().getAllForms().length);
                        }

                        @Override
                        protected void onCancelled() {
                            Utility.dismissDialog(dialog);
                        }
                    }.execute();
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
                GenericDao genericDao=new GenericDao();
                 genericDao.createTable("Employee","name,age,work");
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
