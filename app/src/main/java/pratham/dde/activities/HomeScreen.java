package pratham.dde.activities;

import android.app.Dialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import pratham.dde.R;
import pratham.dde.dao.GenericDao;
import pratham.dde.database.BackupDatabase;
import pratham.dde.domain.DDE_Forms;
import pratham.dde.domain.DDE_Questions;
import pratham.dde.domain.DDE_RuleCondition;
import pratham.dde.domain.DDE_RuleMaster;
import pratham.dde.domain.DDE_RuleQuestion;
import pratham.dde.domain.User;
import pratham.dde.fragments.FillFormsFragment;
import pratham.dde.fragments.OldFormsFragment;
import pratham.dde.fragments.SavedFormsFragment;
import pratham.dde.interfaces.FabInterface;
import pratham.dde.services.SyncUtility;
import pratham.dde.utils.Utility;

import static pratham.dde.BaseActivity.appDatabase;

public class HomeScreen extends AppCompatActivity implements FabInterface/* implements LocationLisner */ {

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
                    case R.id.nav_saved_forms:
                        //appDatabase.close();
                        SavedFormsFragment savedFormsFragment = new SavedFormsFragment();
                        FragmentManager fm = getFragmentManager();
                        fm.beginTransaction().replace(R.id.fragment, savedFormsFragment).commit();
                        break;

                    case R.id.nav_get_new_forms:
                        String token = appDatabase.getUserDao().getToken(userName, password);
                        DDE_Forms[] forms = appDatabase.getDDE_FormsDao().getAllForms();
                     //   Log.d("forms",forms.toString());
                        for (int i = 0; i < forms.length; i++) {
                            getQuestionsAndData(forms[i].getFormid(), token);
                        }
                        break;

                    case R.id.nav_fill_forms:
                        callFillforms();
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

    @Override
    protected void onResume() {
        super.onResume();
        updateFormEntries();
    }

    private void getQuestionsAndData(int formId, String token) {
        // TODO get questions and data if required
        String url = "http://www.ddeapi.prathamskills.org/api/ddeforms/getquestions?Id=" + formId;
        AndroidNetworking.get(url).addHeaders("Content-Type", "application/json").addHeaders("Authorization", token).build().getAsJSONObject(new JSONObjectRequestListener() {
            @Override
            public void onResponse(JSONObject response) {
                saveData(response);
            }

            @Override
            public void onError(ANError anError) {
                String s = anError.toString();

            }
        });
    }

    private void saveData(JSONObject response) {
        saveQuestion(response);
        //   saveRule(response);
    }

   /* private void saveRule(JSONObject response) {

        try {
            JSONObject data = response.getJSONObject("Data");
            JSONArray rules= data.getJSONArray("Rules");
            DDE_RuleMaster dde_ruleMaster=new DDE_RuleMaster();
            for (int i=0;i<rules.length();i++){
                dde_ruleMaster.setFormId();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }*/

    private void saveQuestion(JSONObject response) {
        try {
            JSONObject data = response.getJSONObject("Data");
            String questions = data.getString("Questions");
        //    Log.d("json","DAta==>  "+data.toString());
       //     Log.d("json","questions==>  "+questions.toString());
            String formId;
            Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<DDE_Questions>>() {
            }.getType();
            ArrayList<DDE_Questions> questionList = gson.fromJson(questions, listType);
            appDatabase.getDDE_QuestionsDao().insertAllQuestions(questionList);
            if (questionList.size() > 0) {
                formId = questionList.get(0).getFormId();
                appDatabase.getDDE_FormsDao().updatePulledDate(questionList.get(0).getFormId(), "" + Utility.getCurrentDateTime());

                //save Rules to database
                JSONArray rules = data.getJSONArray("Rules");
            //    Log.d("json","rules==>  "+rules.toString());

                for (int i = 0; i < rules.length(); i++) {
                    JSONObject singleRule = rules.getJSONObject(i);
                 //   Log.d("json","singleRule==>  "+singleRule.toString());

                    DDE_RuleMaster dde_ruleMaster = new DDE_RuleMaster();
                    dde_ruleMaster.setFormId(formId);
                    dde_ruleMaster.setRuleId(singleRule.getString("RuleId"));
                    JSONArray questionConditionArray = singleRule.getJSONArray("QuestionCondition");
              //      Log.d("json","questionConditionArray==>  "+questionConditionArray.toString());

                   long rm= appDatabase.getDDE_RuleMasterDao().insertRuleMaster(dde_ruleMaster);
                    for (int j = 0; j < questionConditionArray.length(); j++) {
                        JSONObject questionConditionSingleObj = questionConditionArray.getJSONObject(j);
                       // Log.d("json","questionConditionSingleObj==>  "+questionConditionSingleObj.toString());

                        DDE_RuleCondition dde_ruleCondition = new DDE_RuleCondition();
                        dde_ruleCondition.setConditionId(questionConditionSingleObj.getString("ConditionId"));
                        dde_ruleCondition.setQuestionIdentifier(questionConditionSingleObj.getString("QuestionIdentifier"));
                        dde_ruleCondition.setConditiontype(questionConditionSingleObj.getString("ConditionType"));
                        dde_ruleCondition.setSelectValue(questionConditionSingleObj.getString("SelectValue"));
                        dde_ruleCondition.setSelectValueQuestion(questionConditionSingleObj.getString("SelectValueQuestion"));
                        dde_ruleCondition.setQuestionIdentifier(singleRule.getString("ShowQuestionIdentifier"));
                        dde_ruleCondition.setRuleId(singleRule.getString("RuleId"));
                        long l=appDatabase.getDDE_RuleConditionDao().insertRuleCondition(dde_ruleCondition);
                    }
                    DDE_RuleQuestion dde_ruleQuestion = new DDE_RuleQuestion();
                    dde_ruleQuestion.setRuleQuestion(singleRule.getString("ShowQuestionIdentifier"));
                    dde_ruleQuestion.setRuleId(singleRule.getString("RuleId"));
                    long aa=appDatabase.getDDE_RuleQuestionDao().insertRuleQuestionDao(dde_ruleQuestion);
                    Log.d("s",""+aa);
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("error",e.getMessage()+"  deep"+e);
        }

    }

    private void updateFormEntries() {
        User user = appDatabase.getUserDao().getUserDetails(userName, password);
        if (user != null)
            getNewForms(Utility.getProperty("getForms", HomeScreen.this), user.getUserToken());
        /*else
            Toast.makeText(mContext, "Problem with the database, Contact administrator.", Toast.LENGTH_SHORT).show();*/
    }

    /* getFormsfromServer */
    private void getNewForms(String url, String access_token) {
        if (SyncUtility.isDataConnectionAvailable(this)) {
            Utility.showDialogInApiCalling(dialog, mContext, "Getting new forms... Please wait.");
            AndroidNetworking.get(url).addHeaders("Content-Type", "application/json").addHeaders("Authorization", access_token).build().getAsJSONObject(new JSONObjectRequestListener() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d("pk-log", "" + response.length());
                    Utility.setMessage(dialog, "Updating forms in Database... Please wait.");
                    updateFormsInDatabase(response);
                }

                @Override
                public void onError(ANError error) {
                    Utility.dismissDialog(dialog);
                    Toast.makeText(mContext, "Problem with the server, Contact administrator.", Toast.LENGTH_SHORT).show();
                }
            });
        } /*else {
            Toast.makeText(mContext, "Internet not available", Toast.LENGTH_SHORT);
        }*/
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
                    for (int i = 0; i < formData.length(); i++) {
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
                            appDatabase.getStatusDao().updateValue("LastPulledDate", Utility.getCurrentDateTime());
                            BackupDatabase.backup(mContext);
                            Utility.dismissDialog(dialog);
                            Log.d("pk-size", "pk-length:-" + appDatabase.getDDE_FormsDao().getAllForms().length);
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

                GenericDao genericDao = new GenericDao();
                genericDao.createTable("Employee", "name,age,work");
                genericDao.getTableCount();


                drawer_layout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void fabOnClick() {
        callFillforms();
    }

    private void callFillforms() {
        Bundle bundle = new Bundle();
        bundle.putString("userName", userName);
        bundle.putString("password", password);
        FillFormsFragment fillFormsFragment = new FillFormsFragment();
        fillFormsFragment.setArguments(bundle);
        FragmentManager manager = getFragmentManager();
        manager.beginTransaction().replace(R.id.fragment, fillFormsFragment).commit();
    }

   /* @Override
    public void onLocationFound(Location location) {
        if (location != null) {
            geo.setText("long" + location.getLongitude() + " /Lat " + location.getLatitude());
            fusedLocationAPI.stopLocationUpdates();
        }
    }*/
}
