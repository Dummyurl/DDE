package pratham.dde.activities;

import android.location.Location;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import pratham.dde.R;
import pratham.dde.interfaces.LocationLisner;
import pratham.dde.utils.FusedLocationAPI;
import pratham.dde.fragments.FillFormsFragment;
import pratham.dde.fragments.OldFormsFragment;

public class HomeScreen extends AppCompatActivity/* implements LocationLisner */{
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawer_layout;
   /* @BindView(R.id.geo)
    TextView geo;*/
    @BindView(R.id.nav_view)
    NavigationView navigationView;
    FusedLocationAPI fusedLocationAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);

        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {

                        switch (menuItem.getItemId()) {
                            case R.id.nav_get_new_forms:
                                getNewFormsAsync();
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

    /*getFormsfromServer*/
    private void getNewFormsAsync() {

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
