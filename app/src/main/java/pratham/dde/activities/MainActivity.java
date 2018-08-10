package pratham.dde.activities;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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
        /*input_email.setText("prathamdde@dde.com");
        input_password.setText("Admin@1234");*/
    }

    @Override
    protected void onResume() {
        super.onResume();
        input_email.setText("");
        input_password.setText("");
        input_email.requestFocus();
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
            getNewTokenFromServer(Utility.getProperty("checkCredentials", mContext));
        else startNextActivity();
    }

    User user;

    private boolean validateUserFromLocalDatabase() {
        user = appDatabase.getUserDao().getUserDetails(userName, password);
        if (user != null) {
            if (!isTokenValid(user.getExpiryDate())) {
                Utility.showDialogue(this, "Token for this user has expired. Get new token by registering again.");
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

                callAPIForPrograms(token_type + " " + access_token, Utility.getProperty("getPrograms", mContext), expiryDate, Name, userName);
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

   /* @Override
    public void permissionGranted() {
        startApp();
    }

    @Override
    public void permissionDenied() {
        showPermissionWarningDilog();
    }

    @Override
    public void permissionForeverDenied() {
        showPermissionWarningDilog();
    }*/
}