package pratham.dde.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.androidnetworking.interfaces.JSONObjectRequestListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pratham.dde.BaseActivity;
import pratham.dde.R;
import pratham.dde.domain.Status;
import pratham.dde.domain.User;
import pratham.dde.services.SyncUtility;
import pratham.dde.utils.Utility;

import static pratham.dde.utils.Utility.isTokenValid;

public class MainActivity extends BaseActivity {

    @BindView(R.id.input_email)
    TextView input_email;
    @BindView(R.id.input_password)
    TextView input_password;
    Context mContext;
    Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = MainActivity.this;
        ButterKnife.bind(this);
        startApp();
    }

    private void startApp() {
        dialog = new ProgressDialog(mContext);
        if (appDatabase.getStatusDao().getValueByKey("LastPulledDate") == null)
            initialiseStatusTable();
     /*   input_email.setText("pk@pk.com");
        input_password.setText("Admin@1234");*/
    }

    @Override
    protected void onResume() {
        super.onResume();
        input_email.setText("");
        input_password.setText("");
        input_email.requestFocus();
        checkVersion();
    }

    private void checkVersion() {
        String playStoreVersion = "";
        String currentVersion = Utility.getCurrentVersion(MainActivity.this);
        Log.d("version::", "Current version = " + currentVersion);
        try {
            playStoreVersion  = new GetLatestVersion().execute().get();
//            latestVersion = "1.1";
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Force Update Code
        if ( playStoreVersion != null && currentVersion != null && compareCurrentAndPlayStoreVersion(currentVersion,playStoreVersion)) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Upgrade to a better version !");
            builder.setCancelable(false);
            builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //Click button action
                    if (SyncUtility.isDataConnectionAvailable(MainActivity.this)) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=pratham.dde")));
                    } else {
                        Toast.makeText(mContext, "No internet connection", Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();
                }
            });
            builder.show();
        }
    }

    private boolean compareCurrentAndPlayStoreVersion(String currentVersion, String playStoreVersion ) {
        if ((Float.parseFloat(currentVersion)- Float.parseFloat(playStoreVersion)) >= 0)
            return false;
        return true;
    }

    private void initialiseStatusTable() {
        Status[] statuses = new Status[3];
        Status status = new Status();
        status.setKeys("LastPulledDate");
        status.setValue("");
        statuses[0] = status;
        Status status1 = new Status();
        status1.setKeys("GPSLocation");
        status1.setValue("");
        statuses[1] = status1;
        Status status2 = new Status();
        status2.setKeys("DeviceId");
        status2.setValue(Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID));
        statuses[2] = status2;
        appDatabase.getStatusDao().initialiseAppStatus(statuses);
    }

    String userName;
    String password;

    @OnClick(R.id.btn_login)
    public void checkLogin() {
        userName = input_email.getText().toString();
        password = input_password.getText().toString();

        if (userName.equals("") || password.equals(""))
            Utility.showDialogue(this, "Insert Username and Password correctly");
        else if (!validateUserFromLocalDatabase())
            getNewTokenFromServer(Utility.getProperty("prodcheckCredentials", mContext));
        else startNextActivity();
    }

    User user;

    private boolean validateUserFromLocalDatabase() {
        user = appDatabase.getUserDao().getUserDetails(userName, password);
        if (user != null) {
            if (!isTokenValid(user.getExpiryDate())) {
                Toast.makeText(mContext, "Token for this user has expired. Getting new token.", Toast.LENGTH_SHORT).show();
                return false;
            }
            return true;
        }
        return false;
    }

    private void getNewTokenFromServer(String url) {
        //TODO checkNetwork
        if (SyncUtility.isDataConnectionAvailable(this)) {
            Utility.showDialogInApiCalling(dialog, mContext, "getting new token from server");
            AndroidNetworking.post(url).addBodyParameter("username", userName).addBodyParameter("password", password).addBodyParameter("grant_type", "password").setTag("test").setPriority(Priority.MEDIUM).build().getAsJSONObject(new JSONObjectRequestListener() {
                @Override
                public void onResponse(JSONObject response) {
                    Utility.dismissDialog(dialog);
                    validateResult(response);
                }

                @Override
                public void onError(ANError error) {
                    Utility.dismissDialog(dialog);
                    try {
                        String errorBody = error.getErrorBody();
                        Toast.makeText(MainActivity.this, "" + new JSONObject(errorBody).getString("error_description"), Toast.LENGTH_LONG).show();
                    } catch (JSONException e) {
                        Toast.makeText(mContext, "Problem with the server, Contact administrator.", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
            });
        } else {
            Toast.makeText(this, "Internet not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void validateResult(JSONObject response) {
        try {
            if (response.length() > 2) {
                String access_token = response.getString("access_token");
                Log.d("token", access_token);
                String Name = response.getString("Name");
                String userName = response.getString("userName");
                String token_type = response.getString("token_type");
                String expiryDate = response.getString(".expires");

                callAPIForPrograms(token_type + " " + access_token, Utility.getProperty("prodgetPrograms", mContext), expiryDate, Name, userName);
            } else {
                Utility.showDialogue(this, "Invalid User! Try registering.");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    JSONArray programsJson;

    private void callAPIForPrograms(final String access_token, String url, final String expiryDate, final String Name, final String userName) {
        //TODO checkNetwork

        if (SyncUtility.isDataConnectionAvailable(this)) {
            Utility.showDialogInApiCalling(dialog, mContext, "Getting programs");
            AndroidNetworking.get(url).addHeaders("Content-Type", "application/json").addHeaders("Authorization", access_token).build().getAsJSONArray(new JSONArrayRequestListener() {
                @Override
                public void onResponse(JSONArray response) {
                    // do anything with response
                    programsJson = response;
                    Utility.dismissDialog(dialog);
                    setUserEntries(access_token, expiryDate, Name, userName);
                }

                @Override
                public void onError(ANError error) {
                    // handle error
                    Utility.dismissDialog(dialog);
                    Toast.makeText(mContext, "Problem with the server, Contact administrator.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(mContext, "Internet not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void setUserEntries(String access_token, String expiryDate, String Name, String userName) {
        String programIds = getProgramIds();
        String programNames = getProgramNames();
        this.userName = userName;
        user = appDatabase.getUserDao().getUserDetails(userName, password);
        if (user != null) {
            appDatabase.getUserDao().UpdateTokenAndExpiry(access_token, expiryDate, userName, password);
        } else {
            user = new User();
            user.setUserName(userName);
            user.setUserToken(access_token);
            user.setProgramNames(programNames);
            user.setProgramIds(programIds);
            user.setPassword(password);
            user.setName(Name);
            user.setExpiryDate(expiryDate);
            appDatabase.getUserDao().insert(user);


        }
        startNextActivity();
    }

    private String getProgramIds() {
        String programIds = "";
        int i;
        try {
            for (i = 0; i < programsJson.length() - 1; i++) {
                programIds += programsJson.getJSONObject(i).getString("progid") + ",";
            }
            /* programIds +="1,";*/
            programIds += programsJson.getJSONObject(i).getString("progid");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return programIds;
    }

    private String getProgramNames() {
        String programNames = "";
        int i;
        try {
            for (i = 0; i < programsJson.length() - 1; i++) {
                programNames += programsJson.getJSONObject(i).getString("programname") + ",";
            }
            programNames += programsJson.getJSONObject(i).getString("programname");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return programNames;
    }

    private void startNextActivity() {
        Intent intent = new Intent(this, HomeScreen.class);
        intent.putExtra("userName", userName);
        intent.putExtra("password", password);
        startActivity(intent);
    }

    private class GetLatestVersion extends AsyncTask<String, String, String> {
        String latestVersion;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                //It retrieves the latest version by scraping the content of current version from play store at runtime
                // Document doc = w3cDom.fromJsoup(Jsoup.connect(urlOfAppFromPlayStore).get());
                //Log.d(TAG,"playstore doc "+getStringFromDoc(doc));
                latestVersion = Jsoup.connect("https://play.google.com/store/apps/details?id=pratham.dde&hl=en")
                        .timeout(30000)
                        .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                        .referrer("http://www.google.com")
                        .get()
                        .select("div.hAyfc:nth-child(4) > span:nth-child(2) > div:nth-child(1) > span:nth-child(1)")
                        .first()
                        .ownText();
                /*latestVersion = Jsoup.connect("https://play.google.com/store/apps/details?id=" + "pratham.dde" + "&hl=en")
                        .timeout(30000)
                        .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                        .referrer("http://www.google.com")
                        .get()
                        .select(".hAyfc .htlgb")
//                        .select("div[itemprop=softwareVersion]")
                        .get(7)
//                        .first()
                        .ownText();*/
                Log.d("latest::", latestVersion);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return latestVersion;
        }
    }

}