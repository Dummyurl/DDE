package pratham.dde.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pratham.dde.BaseActivity;
import pratham.dde.R;
import pratham.dde.domain.User;
import pratham.dde.utils.Utility;

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

    @OnClick(R.id.btn_login)
    public void checkLogin() {
        String userName = input_email.getText().toString();
        String password = input_password.getText().toString();
       /*User user = new User();
        user.setId(1);
        user.setExpiryDate("date");
        user.setName("name");
        user.setPassword(password);
        user.setProgramIds("1,2,3");
        user.setProgramNames("one,two,three");
        user.setUserName(userName);
        user.setUserToken("token");*/
/*try {
    appDatabase.getUserDao().insert(user);
}catch (Exception e){
    e.printStackTrace();
}*/
        if (!validateUserFromLocalDatabase(userName, password))
            getNewTokenFromServer(Utility.getProperty("checkCredentials", mContext), userName, password);
        else {
            // Move ahead
        }
    }

    User user;
    private boolean validateUserFromLocalDatabase(String userName, String password) {
        user = appDatabase.getUserDao().getUserDetails(userName, password);
        if (user != null)
            return true;
        return false;
    }

    private void getNewTokenFromServer(String url, String userName, String password) {
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
            if(response.length()>2) {
                String access_token = response.getString("access_token");
                String Name = response.getString("Name");
                String token_type = response.getString("token_type");
                Intent intent = new Intent(this, HomeScreen.class);
                startActivity(intent);

            } else {

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}