package pratham.dde.activities;

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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pratham.dde.BaseActivity;
import pratham.dde.R;
import pratham.dde.utils.APIs;

public class MainActivity extends BaseActivity {
    @BindView(R.id.input_email)
    TextView input_email;
    @BindView(R.id.input_password)
    TextView input_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        input_email.setText("prathamdde@dde.com");
        input_password.setText("Admin@1234");
    }

    @OnClick(R.id.btn_login)
    public void checkLogin() {
        String token;
        String userName = input_email.getText().toString();
        String password = input_password.getText().toString();
       loadUser(APIs.checkCredentials, userName, password);
    }

    public void loadUser(String Url, String userName, String password) {
       // final String[] responseJson = new String[1];
        AndroidNetworking.post(Url).addBodyParameter("username", userName).addBodyParameter("password", password).addBodyParameter("grant_type", "password").setTag("test").setPriority(Priority.MEDIUM).build().getAsJSONObject(new JSONObjectRequestListener() {
            @Override
            public void onResponse(JSONObject response) {
               // responseJson[0]= response.toString();
                validateResult(response);
            }

            @Override
            public void onError(ANError error) {
                // handle error
            }
        });
       // return responseJson[0];
    }

    private void validateResult(JSONObject response) {
        String expiresDate;
       /* Date currentTime = Calendar.getInstance().getTime();
        try {
            expiresDate = response.getString(".expires");
            SimpleDateFormat format=new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
            Date expier=format.parse(expiresDate);
            if(expier.compareTo(currentTime)>0){
                Toast.makeText(this, "InVAlid", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this, "valid", Toast.LENGTH_SHORT).show();

            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }*/

      /*  Intent intent=new Intent(this,HomeScreen.class);
        startActivity(intent);*/
    }
}