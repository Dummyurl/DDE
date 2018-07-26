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
import java.util.Iterator;
import java.util.List;

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
import pratham.dde.domain.DDE_RuleTable;
import pratham.dde.domain.User;
import pratham.dde.fragments.FillFormsFragment;
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
    String dataSourceUrl;
    String tableName;
    Context mContext;
    String token, QuestionUrl;
    DDE_Forms[] forms;
    int formIndex = 0;

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
                        SavedFormsFragment savedFormsFragment = new SavedFormsFragment();
                        FragmentManager fm = getFragmentManager();
                        fm.beginTransaction().replace(R.id.fragment, savedFormsFragment).commit();
                        break;

                    case R.id.nav_get_new_forms:
                        formIndex = 0;
                        token = appDatabase.getUserDao().getToken(userName, password);
                        forms = appDatabase.getDDE_FormsDao().getAllForms();
                        if (SyncUtility.isDataConnectionAvailable(HomeScreen.this)) {
                            Utility.showDialogInApiCalling(dialog, mContext, "getQuestion");
                            QuestionUrl = Utility.getProperty("getQuestionsAndData", mContext);
                            getQuestionsAndData(forms[formIndex].getFormid());

                        } else {

                        }
                        break;

                    case R.id.nav_fill_forms:
                        callFillforms();
                        break;

                    case R.id.nav_old_forms:
                        /*OldFormsFragment oldFormsFragment = new OldFormsFragment();
                        FragmentManager fragmentManager = getFragmentManager();
                        fragmentManager.beginTransaction().replace(R.id.fragment, oldFormsFragment).commit();
                        */
                        break;
                }
                drawer_layout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
       /* fusedLocationAPI = new FusedLocationAPI(this);
        fusedLocationAPI.startLocationButtonClick();*/
    }

    /* load Question SourceDta */
    private void fetchQuestionsSourceData() {
        List<DDE_Questions> questions = appDatabase.getDDE_QuestionsDao().getAllQuestion();
        for (int i = 0; i < questions.size(); i++) {
            if (questions.get(i).getDataSource() != null) {
                String formId = questions.get(i).getFormId();
                tableName = appDatabase.getDDE_FormsDao().getTableName(formId);
                dataSourceUrl = Utility.getProperty("dataSource", mContext);
                loadSourceData(formId);
            }
        }
    }

    private void loadSourceData(String formId) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("FilterList", "null");
            jsonObject.put("FormId", formId);
            jsonObject.put("PageNumber", "1");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        AndroidNetworking.post(dataSourceUrl).addJSONObjectBody(jsonObject) // posting json
                .build().
                getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        saveSourceData(response);
                    }

                    @Override
                    public void onError(ANError anError) {

                    }
                });
    }

    private void saveSourceData(JSONObject response) {
        try {
            String columnNamesSourceData = "";
            JSONArray tablesArray = response.getJSONArray("Data");
            JSONObject table = tablesArray.getJSONObject(0);
            Iterator iterator = table.keys();
            while (iterator.hasNext()) {
                columnNamesSourceData = columnNamesSourceData + (String) iterator.next() + ", ";
            }
            if (columnNamesSourceData != null && columnNamesSourceData.length() > 0 && columnNamesSourceData.charAt(columnNamesSourceData.length() - 1) == ',') {
                columnNamesSourceData = columnNamesSourceData.substring(0, columnNamesSourceData.length() - 1);
            }
            GenericDao.createTable(tableName, columnNamesSourceData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateFormEntries();
       /* SavedFormsFragment savedFormsFragment = new SavedFormsFragment();
        FragmentManager fm = getFragmentManager();
        fm.beginTransaction().replace(R.id.fragment, savedFormsFragment).commit();*/
    }

    private void getQuestionsAndData(int formId) {
        // TODO get questions and data if required
        String url = QuestionUrl + formId;
        AndroidNetworking.get(url).addHeaders("Content-Type", "application/json").addHeaders("Authorization", token).build().getAsJSONObject(new JSONObjectRequestListener() {
            @Override
            public void onResponse(JSONObject response) {
                saveData(response);
                Utility.dismissDialog(dialog);
            }

            @Override
            public void onError(ANError anError) {
                Utility.dismissDialog(dialog);
                Log.d("responceError123", anError.toString());

            }
        });
    }

    private void saveData(JSONObject response) {
        saveQuestion(response);
        formIndex++;
        if (formIndex < forms.length) {
            getQuestionsAndData(forms[formIndex].getFormid());
        } else {
            fetchQuestionsSourceData();
        }
    }


    private void saveQuestion(JSONObject response) {
        try {
            JSONObject data = response.getJSONObject("Data");
            String questions = data.getString("Questions");
            String formId;
            Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<DDE_Questions>>() {
            }.getType();
            ArrayList<DDE_Questions> questionList = gson.fromJson(questions, listType);
            if (questionList.size() > 0) {
                String allRules = data.getString("Rules");
                Type listTypeRule = new TypeToken<ArrayList<DDE_RuleTable>>() {
                }.getType();
                ArrayList<DDE_RuleTable> rulesList = gson.fromJson(allRules, listTypeRule);

                /*ENTERING FORMID MANUALLY*/
                formId = questionList.get(0).getFormId();
                for (int i = 0; i < rulesList.size(); i++) {
                    rulesList.get(i).setFormID(formId);
                }
                appDatabase.getDDE_RulesDao().deleteRulesByFormID(formId);
                appDatabase.getDDE_RulesDao().insertAllRule(rulesList);


                appDatabase.getDDE_FormsDao().updatePulledDate(questionList.get(0).getFormId(), "" + Utility.getCurrentDateTime());
                appDatabase.getDDE_QuestionsDao().deleteQuestionsByFormID(formId);
                appDatabase.getDDE_QuestionsDao().insertAllQuestions(questionList);
                formId = response.getJSONObject("Formdata").getString("formid");
                appDatabase.getDDE_FormsDao().updatePulledDate(formId, "" + Utility.getCurrentDateTime());
                //save Rules to database
                JSONArray rules = data.getJSONArray("Rules");
                DDE_RuleMaster dde_ruleMaster;
                JSONObject singleRule;
                JSONArray questionConditionArray;
                for (int i = 0; i < rules.length(); i++) {
                    singleRule = rules.getJSONObject(i);
                    dde_ruleMaster = new DDE_RuleMaster();
                    dde_ruleMaster.setFormId(formId);
                    dde_ruleMaster.setRuleId(singleRule.getString("RuleId"));
                    dde_ruleMaster.setConditionToBeMatched(singleRule.getString("ConditionsMatch"));
                    questionConditionArray = singleRule.getJSONArray("QuestionCondition");

                    long rm = appDatabase.getDDE_RuleMasterDao().insertRuleMaster(dde_ruleMaster);
                    DDE_RuleCondition dde_ruleCondition;
                    JSONObject questionConditionSingleObj;
                    for (int j = 0; j < questionConditionArray.length(); j++) {
                        questionConditionSingleObj = questionConditionArray.getJSONObject(j);
                        dde_ruleCondition = new DDE_RuleCondition();
                        dde_ruleCondition.setFormID(formId);
                        dde_ruleCondition.setConditionId(questionConditionSingleObj.getString("ConditionId"));
                        dde_ruleCondition.setQuestionIdentifier(questionConditionSingleObj.getString("QuestionIdentifier"));
                        dde_ruleCondition.setConditiontype(questionConditionSingleObj.getString("ConditionType"));
                        dde_ruleCondition.setSelectValue(questionConditionSingleObj.getString("SelectValue"));
                        dde_ruleCondition.setSelectValueQuestion(questionConditionSingleObj.getString("SelectValueQuestion"));
                        dde_ruleCondition.setRuleQuestionForWhichQue(singleRule.getString("ShowQuestionIdentifier"));
                        dde_ruleCondition.setRuleId(singleRule.getString("RuleId"));
                        long l = appDatabase.getDDE_RuleConditionDao().insertRuleCondition(dde_ruleCondition);
                    }
                    DDE_RuleQuestion dde_ruleQuestion = new DDE_RuleQuestion();
                    dde_ruleQuestion.setRuleQuestionId("" + i);
                    dde_ruleQuestion.setRuleQuestion(singleRule.getString("ShowQuestionIdentifier"));
                    dde_ruleQuestion.setRuleId(singleRule.getString("RuleId"));
                    long aa = appDatabase.getDDE_RuleQuestionDao().insertRuleQuestionDao(dde_ruleQuestion);
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
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
                    String s = response.toString();
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

                // GenericDao genericDao = new GenericDao();
                //  genericDao.createTable("Employee", "name,age,work");
                //  genericDao.getTableCount();


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
