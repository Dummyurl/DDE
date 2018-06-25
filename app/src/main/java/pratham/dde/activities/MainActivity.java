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
import pratham.dde.utils.APIs;
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
        validateUser(Utility.getProperty("checkCredentials", mContext), userName, password);
    }

    public void validateUser(String Url, String userName, String password) {
        AndroidNetworking.post(Url)
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
            String token = response.getString(".access_token");

            Intent intent = new Intent(this, HomeScreen.class);
            startActivity(intent);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}