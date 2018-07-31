package pratham.dde.activities;

import android.app.Dialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
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
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.StringRequestListener;
import com.androidnetworking.interfaces.UploadProgressListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import pratham.dde.R;
import pratham.dde.database.BackupDatabase;
import pratham.dde.domain.AnswersSingleForm;
import pratham.dde.domain.DDE_Forms;
import pratham.dde.domain.DDE_Questions;
import pratham.dde.domain.DDE_RuleCondition;
import pratham.dde.domain.DDE_RuleMaster;
import pratham.dde.domain.DDE_RuleQuestion;
import pratham.dde.domain.DDE_RuleTable;
import pratham.dde.domain.DataSourceEntries;
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
    String userId;
    String dataSourceUrl;
    String tableName;
    Context mContext;
    String token, QuestionUrl;
    DDE_Forms[] forms;
    int formIndex = 0;
    List<DDE_Questions> dataSourceQuestionsList;
    int dataSourceIndex = 0;
    int PageNumber = 1;

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
        userId = String.valueOf(appDatabase.getUserDao().getUserId(userName, password));

        View hView = navigationView.getHeaderView(0);
        TextView nav_user = (TextView) hView.findViewById(R.id.userName);
        nav_user.setText(userName);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.nav_saved_forms:
                        menuItem.setChecked(true);
                        showSavedOldForms();
                        break;

                    case R.id.nav_get_new_forms:
                        navigationView.getMenu().getItem(1).setChecked(false);
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
                        menuItem.setChecked(true);
                        callFillforms();
                        break;

                    case R.id.nav_old_forms:
                        List<AnswersSingleForm> allAnswersSingleForms = appDatabase.getAnswerDao().getAnswers();
                        if (allAnswersSingleForms.isEmpty()) {
                            Utility.showDialogue(HomeScreen.this, "Data is already Synced...");
                        } else {
                            if (SyncUtility.isDataConnectionAvailable(HomeScreen.this)) {
                                uploadOldFormsAsync();
                            } else {

                            }
                        }
                        break;
                }
                drawer_layout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
       /* fusedLocationAPI = new FusedLocationAPI(this);
        fusedLocationAPI.startLocationButtonClick();*/
    }


    private void uploadOldFormsAsync() {
        new AsyncTask<Void, Void, String>() {

            @Override
            protected void onPreExecute() {
                Dialog dialog = new ProgressDialog(HomeScreen.this);
                Utility.showDialogInApiCalling(dialog, HomeScreen.this, "Uploading Data to Server...");
            }

            @Override
            protected String doInBackground(Void... voids) {
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                // JSONObject metadataObj = getMetaData();
                String uploadDataUrl = Utility.getProperty("uploadData", mContext);

                // String requestString = "{ 'Metadata': "+metadataObj+", 'Answers': " + jsonArray.toString() + "}";
                //uploadData(uploadDataUrl,requestString);

            }

            @Override
            protected void onCancelled() {
                Utility.dismissDialog(dialog);
            }
        }.execute();

    }

    private JSONObject getMetaData() {
        JSONObject obj = new JSONObject();

       /* AnswersSingleForm answerObj = new AnswersSingleForm();
        answerObj.setAnswerId(5);
        answerObj.setEntryId("1");
        answerObj.setFormId(3);
        answerObj.setQuestionType("single Choice");
        answerObj.setAnswers("answers");
        answerObj.setTableName("table");
        answerObj.setDestColumnName("dest");
        appDatabase.getAnswerDao().insertAnswers(answerObj);
        Log.d("answerObj",answerObj.toString());*/

        Cursor noOfForms = appDatabase.getAnswerDao().getNoOfForms();
        try {
            obj.put("noOfForms", noOfForms.getCount());
            JSONArray jsonArray = new JSONArray();

            Cursor cursor = appDatabase.getAnswerDao().getFormCount();
            cursor.moveToFirst();
            while (cursor.isAfterLast() == false) {
                JSONObject jsonObject = new JSONObject();

                jsonObject.put("FormId", cursor.getInt(cursor.getColumnIndex("FormId")));
                jsonObject.put("Count", cursor.getInt(cursor.getColumnIndex("cnt")));
                jsonArray.put(jsonObject);
                cursor.moveToNext();
            }
            obj.put("RecCountPerForm", jsonArray);

            List<AnswersSingleForm> answersSingleForms = appDatabase.getAnswerDao().getAnswers();
            obj.put("TotalRecordCount", answersSingleForms.size());
            Log.d("answer", answersSingleForms.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj;
    }

    private void uploadData(String url, String json) {
      /*  final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setTitle("UPLOADING ... ");
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();*/
        AndroidNetworking.post(url).setContentType("application/json").addHeaders("Authorization", token).addHeaders("Content-Type", "application/json").addStringBody(json).build().getAsString(new StringRequestListener() {
            @Override
            public void onResponse(String response) {
                Log.d("response", response);
                //  dialog.dismiss();
                finish();
            }

            @Override
            public void onError(ANError anError) {

                Toast.makeText(HomeScreen.this, "NO Internet Connection", Toast.LENGTH_LONG).show();
                //Log.d("anError", "" + anError);
                dialog.dismiss();
            }
        });
    }


    /* load Question SourceDta */
    private void fetchQuestionsSourceData() {
        List<DDE_Questions> questions = appDatabase.getDDE_QuestionsDao().getAllQuestion();
        dataSourceQuestionsList = new ArrayList<>();
        dataSourceIndex = 0;
        PageNumber = 1;
        for (int i = 0; i < questions.size(); i++) {
            if (questions.get(i).getDataSource() != null) {
                dataSourceQuestionsList.add(questions.get(i));
            }
        }

        loadSourceData();

    }

    private void loadSourceData() {
        if (dataSourceQuestionsList.size() > dataSourceIndex) {
            String formId = dataSourceQuestionsList.get(dataSourceIndex).getFormId();
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("FilterList", "null");
                jsonObject.put("FormId", formId);
                jsonObject.put("PageNumber", PageNumber);

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
            PageNumber++;
        }
    }

    private void saveSourceData(JSONObject response) {
        if (response.length() > 1) {
            try {
                JSONObject jsonObjectData = response.getJSONObject("Data");
                JSONArray tableArray = jsonObjectData.getJSONArray("Table");
                for (int tableIndex = 0; tableIndex < tableArray.length(); tableIndex++) {
                    JSONObject tableObj = tableArray.getJSONObject(tableIndex);
                    Iterator iterator = tableObj.keys();
                    DataSourceEntries dataSourceEntries = new DataSourceEntries();
                    ;
                    while (iterator.hasNext()) {
                        if ((!iterator.next().equals("ROWNUMBER")) && (!iterator.next().equals("EntryId")) && (!iterator.next().equals("EntryId")) && (!iterator.next().equals("CreatedBy")) && (!iterator.next().equals("UpdatedBy")) && (!iterator.next().equals("Createdon")) && (!iterator.next().equals("Updatedon"))) {
                            String formId = dataSourceQuestionsList.get(dataSourceIndex).getFormId();
                            String ans = appDatabase.getDataSourceEntriesDao().getAnswer(formId, iterator.next().toString());
                            Log.d("ansre", ans);
                        }
                    }


                }
                jsonObjectData.length();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            PageNumber = 1;
            dataSourceIndex++;
        }
        loadSourceData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateFormEntries();
        showSavedOldForms();
    }

    private void showSavedOldForms() {
        navigationView.getMenu().getItem(0).setChecked(true);
        Bundle bundle = new Bundle();
        bundle.putString("userID", userId);
        SavedFormsFragment savedFormsFragment = new SavedFormsFragment();
        savedFormsFragment.setArguments(bundle);
        FragmentManager fm = getFragmentManager();
        fm.beginTransaction().replace(R.id.fragment, savedFormsFragment).commit();
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

    public void uploadImageToServer(String url, File file) {
        AndroidNetworking.upload(url).addMultipartFile("image", file).addMultipartParameter("key", "value").setTag("uploadTest").build().setUploadProgressListener(new UploadProgressListener() {
            @Override
            public void onProgress(long bytesUploaded, long totalBytes) {
                // do anything with progress
            }
        }).getAsJSONObject(new JSONObjectRequestListener() {
            @Override
            public void onResponse(JSONObject response) {
                // do anything with response
            }

            @Override
            public void onError(ANError error) {
                // handle error
            }
        });
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
        navigationView.getMenu().getItem(2).setChecked(true);
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
