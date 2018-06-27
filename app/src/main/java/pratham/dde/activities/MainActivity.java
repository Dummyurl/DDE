package pratham.dde.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import pratham.dde.domain.User;
import pratham.dde.utils.Utility;

import static pratham.dde.utils.Utility.isTokenValid;

public class MainActivity extends BaseActivity {

    @BindView(R.id.input_email)
    TextView input_email;
    @BindView(R.id.input_password)
    TextView input_password;

    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = MainActivity.this;
        ButterKnife.bind(this);
        input_email.setText("prathamdde@dde.com");
        input_password.setText("Admin@1234");
    }

    String userName;
    String password;

    @OnClick(R.id.btn_login)
    public void checkLogin() {
        userName = input_email.getText().toString();
        password = input_password.getText().toString();

        if (!validateUserFromLocalDatabase())
            getNewTokenFromServer(Utility.getProperty("checkCredentials", mContext));
        else {
            // Move ahead
            startNextActivity();
        }
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
        AndroidNetworking.post(url)
                .addBodyParameter("username", userName)
                .addBodyParameter("password", password)
                .addBodyParameter("grant_type", "password")
                .setTag("test").setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        validateResult(response);
                    }

                    @Override
                    public void onError(ANError error) {
                        // handle error
                        Toast.makeText(mContext, "Problem with the server, Contact administrator.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void validateResult(JSONObject response) {
        try {
            if (response.length() > 2) {
                String access_token = response.getString("access_token");
                String Name = response.getString("Name");
                String userName = response.getString("userName");
                String token_type = response.getString("token_type");
                String expiryDate = response.getString(".expires");

                callAPIForPrograms(token_type+" "+access_token, Utility.getProperty("getPrograms", mContext),expiryDate,Name,userName);
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
        String token = "bearer NnssXop1Bel18qXNb0cWIFailO6wIPCGnuU2dqnTRGKlzFsmbhXCdkaNZIURnkdGFCwvVM0WI9FnoR7kCjvu5WDE-vUvaAMAsTsWRquvptazmCoLWhvaJWYIdcorMSfHi_SAK1j4SjG7WonvOL-8ecrG8qoDP7yZFavHr72WXXFKmOAOnlyj4jxujVXRctYyPO-UU6-bVLTHYxzTN3DxLExERxippbWoOUac6Uu0J8Be_kF__TdXqQ_r35oK9VjjtmAK-p62Aw_0z8HXimoMO51SU4JyCKTxYHjmv-Yz1iXrtCVAXxingEn9QlINGetWxVN1apM3AyHHtiU3ovxOehMDsWrHUS-DqyX3k3CfQCHOxOUFtwW0o4jiNPEjvd5yuktdYupBpfhi3lOg9a1Dk6-vIz0GmAo2kv9t738ue_-O8AsIGcS6LJHqiNFE7E3K5nGHaIX_9yCW4syidN4oxH7WbXtvg-z1c_y_v0Er1q66YDqHdybEzZBJhjyiJuTCO4VZVjEJ5VbeTI0BFW9rzs5bGvPBOC5plMXTO73qjvvxR-OfeebCgBWOLCmeEpKOcD_jWJf_oW_yeBvyvtSD9lsHpQW3OB1W0eXeSTCYK0A8KKcLv45ebMIam1O9DzdCLKhaiYrj7PzPe12e1OqlTOM7kp3Cj9vEEy5WQYpEVo1ss9r9HfpT60gRKE8yG90T6sDPeWW8Tak_A_kC_esG_LYa5Rh6bUKeoHm2tYrK3ag8RCUys3HGHv6I28hsH-mnk170OwiJPBkbAxguS_dbIfyHuL_ZZU2pIt6MVedU3d-w2I83Z_1Wtvo8Jo-g3PF9";
        AndroidNetworking.get(url)
                .addPathParameter("Content-Type","application/json")
                .addPathParameter("Authorization",/*access_*/token)
                .build()
                .getAsJSONArray(new JSONArrayRequestListener() {
                    @Override
                    public void onResponse(JSONArray response) {
                        // do anything with response
                        programsJson = response;
                        setUserEntries(access_token,expiryDate,Name,userName);
                    }

                    @Override
                    public void onError(ANError error) {
                        // handle error
                        Toast.makeText(mContext, "Problem with the server, Contact administrator.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setUserEntries(String access_token, String expiryDate, String Name, String userName) {
        String programIds = getProgramIds();
        String programNames = getProgramNames();
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
        startActivity(intent);
    }
}