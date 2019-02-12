package pratham.dde.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import pratham.dde.R;
import pratham.dde.customViews.SelectQuestionDialog;
import pratham.dde.database.BackupDatabase;
import pratham.dde.domain.AnswersSingleForm;
import pratham.dde.domain.DDE_FormWiseDataSource;
import pratham.dde.domain.DDE_Forms;
import pratham.dde.domain.DDE_Questions;
import pratham.dde.domain.DDE_RuleTable;
import pratham.dde.domain.DataSourceEntries;
import pratham.dde.domain.User;
import pratham.dde.fragments.FillFormsFragment;
import pratham.dde.fragments.SavedFormsFragment;
import pratham.dde.interfaces.FabInterface;
import pratham.dde.interfaces.FillAgainListner;
import pratham.dde.interfaces.QuestionListLisner;
import pratham.dde.services.SyncUtility;
import pratham.dde.utils.UploadAnswerAndImageToServer;
import pratham.dde.utils.Utility;

import static pratham.dde.BaseActivity.appDatabase;

public class HomeScreen extends AppCompatActivity implements FabInterface, FillAgainListner, QuestionListLisner/* implements LocationLisner */ {

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
    boolean formLoaded = false;
    String dataSourceUrl;
    Context mContext;
    String token, QuestionUrl;
    List forms;
    int formIndex = 0, depformIndex = 0;
    int dataSourceIndex = 0;
    int PageNumber = 1;
    List<JSONObject> dataSourceForFormOnline;
    List<DDE_Forms> updatedFormsToPull;
    static int rowsPerPage = 10000;
    ProgressDialog progressDialog;
    int maxProgressCnt = 0;

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
        formLoaded = false;
        updatedFormsToPull = new ArrayList<>();
        userName = this.getIntent().getStringExtra("userName");
        password = this.getIntent().getStringExtra("password");
        userId = String.valueOf(appDatabase.getUserDao().getUserId(userName, password));
        token = appDatabase.getUserDao().getToken(userName, password);
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
//                        forms = appDatabase.getDDE_FormsDao().getAllErrorsLog();
                        if (!formLoaded) {
                            if (SyncUtility.isDataConnectionAvailable(HomeScreen.this)) {
                                formLoaded = true;
                                updateFormEntries();
                            } else {
                                Toast.makeText(mContext, "Check your internet connection.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            getQuestions();
                        }
                        break;

                    case R.id.nav_fill_forms:
                        menuItem.setChecked(true);
                        callFillforms();
                        break;

                    case R.id.nav_old_forms:
                        List<AnswersSingleForm> allAnswersSingleForms = appDatabase.getAnswerDao().getAllAnswersByStatusUnuploaded();
                        if (allAnswersSingleForms.isEmpty()) {
                            Utility.showDialogue(HomeScreen.this, "Data is already Synced...");
                        } else {
                            if (SyncUtility.isDataConnectionAvailable(HomeScreen.this)) {
                                uploadOldFormsAsync(allAnswersSingleForms);
                            } else {
                                Toast.makeText(mContext, "CHECK INTERNET CONNECTION", Toast.LENGTH_LONG).show();
                            }
                        }
                        break;

                    case R.id.nav_logout:
                        onBackPressed();
                        //finish();
                        break;
                }
                drawer_layout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
        if (SyncUtility.isDataConnectionAvailable(HomeScreen.this)) updateFormEntries();
        else callFillforms();
        //else showSavedOldForms();
    }

    private void getQuestions() {
        if (updatedFormsToPull.size() > 0) {
            if (SyncUtility.isDataConnectionAvailable(HomeScreen.this)) {
                QuestionUrl = Utility.getProperty("prodgetQuestionsAndData", mContext);
                dataSourceForFormOnline = new ArrayList<>();
                SelectQuestionDialog selectVillageDialog = new SelectQuestionDialog(HomeScreen.this, updatedFormsToPull);
                selectVillageDialog.show();
            } else {
                Toast.makeText(mContext, "Check your internet connection.", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (SyncUtility.isDataConnectionAvailable(HomeScreen.this))
                Toast.makeText(mContext, "Forms and questions are up to date.", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(mContext, "Check your internet connection.", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadOldFormsAsync(List<AnswersSingleForm> allAnswersSingleForms) {
        /*UploadAnswerAndImageToServer uploadAnswerAndImageToServer=*/
        new UploadAnswerAndImageToServer(this, allAnswersSingleForms, token);
    }

    private JSONObject getMetaData() {
        JSONObject obj = new JSONObject();
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

    /* load Question SourceDta */
    private void fetchQuestionsSourceData() {
        Utility.dismissDialog(dialog);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Progress : " + (dataSourceIndex + 1) + "/" + dataSourceForFormOnline.size());
        progressDialog.setTitle("Downloading data please wait..");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setProgress(0);
        progressDialog.setMax(100);
        progressDialog.setCancelable(false);
        maxProgressCnt = 0;
        progressDialog.show();
        dataSourceUrl = Utility.getProperty("prodgetDataSource", mContext);
        dataSourceIndex = 0;
        PageNumber = 1;
        setDatasourceList();
        loadSourceData();
    }

    private void setDatasourceList() {
        List<DDE_FormWiseDataSource> listOfDSEntries;
        try {
            listOfDSEntries = appDatabase.getDDE_FormWiseDataSourceDao().getAllDSEntriesByUID(userId);
            if (listOfDSEntries != null) {
                // remove logic -----> form updated and datasource too
                for (int dsEntryIndex = 0; dsEntryIndex < dataSourceForFormOnline.size(); dsEntryIndex++) {
                    DDE_FormWiseDataSource dataObj = appDatabase.getDDE_FormWiseDataSourceDao().getDataBYDSIdAndUserId(dataSourceForFormOnline.get(dsEntryIndex).getString("dsformid"),userId);
                    if (dataObj != null) {
                        String DSUpdateDate = appDatabase.getDDE_FormsDao().getDataupdatedDateByFormID(dataObj.getDsformid());
                        String localUpdateDate = dataObj.getUpdatedDate();
                        if (localUpdateDate != null && DSUpdateDate != null) {
                            Date localUpdateDateDT = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z").parse(localUpdateDate);
                            Date DSUpdateDateDT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(DSUpdateDate);
                            if (localUpdateDateDT.compareTo(DSUpdateDateDT) >= 0) {
                                //remove from dataSourceForFormOnline
                                dataSourceForFormOnline.remove(dsEntryIndex);
                            }
                        }
                    }
                }

                // add logic ------> datasource updated but not forms
                DDE_FormWiseDataSource dde_formWiseDataSourceObj;
                JSONObject dsJsonObject;
                boolean containFlag;
                for (int entryIndex = 0; entryIndex < listOfDSEntries.size(); entryIndex++) {
                    containFlag = false;
                    dde_formWiseDataSourceObj = listOfDSEntries.get(entryIndex);
                    String DSUpdateDate = appDatabase.getDDE_FormsDao().getDataupdatedDateByFormID(dde_formWiseDataSourceObj.getDsformid());
                    String localUpdateDate = dde_formWiseDataSourceObj.getUpdatedDate();
                    if (DSUpdateDate != null && (!DSUpdateDate.equals("null"))) {
                        if (localUpdateDate == null) {
                            //add in dataSourceForFormOnline
                            dsJsonObject = new JSONObject();
                            dsJsonObject.put("formwisedsid", dde_formWiseDataSourceObj.getFormwisedsid());
                            dsJsonObject.put("formid", dde_formWiseDataSourceObj.getFormid());
                            dsJsonObject.put("dsformid", dde_formWiseDataSourceObj.getDsformid());
                            for (int index = 0; index < dataSourceForFormOnline.size(); index++) {
                                if (dataSourceForFormOnline.get(index).getString("dsformid").equalsIgnoreCase(dsJsonObject.getString("dsformid"))) {
                                    containFlag = true;
                                    break;
                                }
                            }
                            if (!containFlag) {
                                dataSourceForFormOnline.add(dsJsonObject);
                            }
                        } else {
                            Date localUpdateDateDT = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z").parse(localUpdateDate);
                            Date DSUpdateDateDT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(DSUpdateDate);
                            if (localUpdateDateDT.compareTo(DSUpdateDateDT) < 0) {
                                //add in dataSourceForFormOnline
                                dsJsonObject = new JSONObject();
                                dsJsonObject.put("formwisedsid", dde_formWiseDataSourceObj.getFormwisedsid());
                                dsJsonObject.put("dsformid", dde_formWiseDataSourceObj.getDsformid());
                                dsJsonObject.put("formid", dde_formWiseDataSourceObj.getFormid());
                                for (int index = 0; index < dataSourceForFormOnline.size(); index++) {
                                    if (dataSourceForFormOnline.get(index).getString("dsformid").equalsIgnoreCase(dsJsonObject.getString("dsformid"))) {
                                        containFlag = true;
                                        break;
                                    }
                                }
                                if (!containFlag) {
                                    dataSourceForFormOnline.add(dsJsonObject);
                                }
                            }
                        }
                    }
                }
            } else {
                // Take as it is
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadSourceData() {
        if (dataSourceForFormOnline.size() > dataSourceIndex) {
            try {
                JSONObject tempJsonObject = dataSourceForFormOnline.get(dataSourceIndex);
                String dsFormId = tempJsonObject.getString("dsformid");
                String lastPulledDate = appDatabase.getDDE_FormWiseDataSourceDao().getLastUpdateDateOfDSFormId(dsFormId,userId);
                //String lastPulledDate2 = "2018-08-15 17:56:37.000";
                JSONObject jsonObject = new JSONObject();
                if (lastPulledDate != null) {
                    Date localUpdateDateDT = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z").parse(lastPulledDate);
                    lastPulledDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(localUpdateDateDT);
                    lastPulledDate = "\"" + lastPulledDate + "\"";
                }
                String filterListString = "[{\"FilterKey\": \"convert(datetime,updateddate,106)\",\"FilterOperator\": \">=\",\"FilterValue\":" + lastPulledDate + "}]";
                JSONArray filterList = new JSONArray(filterListString);
                jsonObject.put("FilterList", filterList);
                jsonObject.put("FormId", dsFormId);
                jsonObject.put("PageNumber", PageNumber);
                jsonObject.put("PageSize", rowsPerPage);
                String formNameDownloading = appDatabase.getDDE_FormsDao().getFormName(dsFormId);
                if (formNameDownloading == null) {
                    formNameDownloading = " ";
                }
                progressDialog.setMessage("Progress : " + (dataSourceIndex + 1) + "/" + dataSourceForFormOnline.size() + "  Downloading " + formNameDownloading);

                AndroidNetworking.post(dataSourceUrl).addHeaders("Content-Type", "application/json").addHeaders("Authorization", token).addJSONObjectBody(jsonObject) // posting json
                        .build().
                        getAsJSONObject(new JSONObjectRequestListener() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.d("responsePKSERVER", "responsePKSERVER: " + dataSourceIndex);
                                saveSourceData(response);
                            }

                            @Override
                            public void onError(ANError anError) {
                                Utility.updateErrorLog(anError, appDatabase, "HomeScreen : loadSourceData");
                                if (dataSourceForFormOnline.size() > dataSourceIndex) {
                                    PageNumber = 1;
                                    dataSourceIndex++;
                                    loadSourceData();
                                } else {
                                    progressDialog.dismiss();
                                }
                            }
                        });
            } catch (Exception e) {
                e.printStackTrace();
                progressDialog.dismiss();
            }
        } else {
            progressDialog.dismiss();
            // TODO download dependent forms and set them as dependent
            progressDialog = new ProgressDialog(this);
            //progressDialog.setMessage("Progress : " + (dataSourceIndex + 1) + "/" + dataSourceForFormOnline.size());
            progressDialog.setTitle("Downloading dependent form data please wait..");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setProgress(0);
            progressDialog.setMax(100);
            progressDialog.setCancelable(false);
            progressDialog.show();
            depformIndex = 0;
            getDependentForms();
        }
    }

    private void getDependentForms() {
        try {
            if (dataSourceForFormOnline.size() == depformIndex) {
                progressDialog.setProgress(100);
                progressDialog.dismiss();
                callFillforms();
                Toast.makeText(mContext, "Downloaded successfully.", Toast.LENGTH_SHORT).show();
            } else {
                int currentDsFormId = dataSourceForFormOnline.get(depformIndex).getInt("dsformid");
                getQuestionsForDependentForms(currentDsFormId);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void saveSourceData(JSONObject response) {
        try {
            maxProgressCnt = response.getInt("Count");
            int pstatus = 0;
            if (response.length() > 1 && response.getJSONObject("Data").getJSONArray("Table").length() > 0) {
                JSONObject jsonObjectData = response.getJSONObject("Data");
                JSONArray tableArray = jsonObjectData.getJSONArray("Table");
                List<DataSourceEntries> dataSourceEntries = new ArrayList<>();
                DataSourceEntries dataSourceEntryObj;
                for (int tableIndex = 0; tableIndex < tableArray.length(); tableIndex++) {
                    JSONObject tableObj = tableArray.getJSONObject(tableIndex);
                    String entryId = tableObj.getString("EntryId");
                    String formId = dataSourceForFormOnline.get(dataSourceIndex).getString("dsformid");
                    String answers = tableObj.toString();
                    dataSourceEntryObj = new DataSourceEntries();
                    dataSourceEntryObj.setFormId(formId);
                    dataSourceEntryObj.setEntryId(entryId);
                    dataSourceEntryObj.setAnswers(answers);
                    String userNames = appDatabase.getDataSourceEntriesDao().getUsersAssociatedWithData(entryId);
                    if (userNames != null) {
                        if (!userNames.contains("," + userId + ","))
                            userNames += userId + ",";
                        else
                            userNames = "," +  userId + ",";
                    } else
                        userNames = "," + userId + ",";
                    dataSourceEntryObj.setUsers(userNames);
                    dataSourceEntries.add(dataSourceEntryObj);
                }
                appDatabase.getDataSourceEntriesDao().insertEntry(dataSourceEntries);
                PageNumber++;
                if (maxProgressCnt < rowsPerPage) {
                    appDatabase.getDDE_FormWiseDataSourceDao().setUpdateDate(dataSourceForFormOnline.get(dataSourceIndex).getString("dsformid"), Utility.getCurrentDateTime(),userId);
                    pstatus = 100;
                    PageNumber = 1;
                    dataSourceIndex++;
                } else {
                    pstatus = ((rowsPerPage * (PageNumber - 1)) * 100) / maxProgressCnt;
                    if (pstatus > 100) {
                        appDatabase.getDDE_FormWiseDataSourceDao().setUpdateDate(dataSourceForFormOnline.get(dataSourceIndex).getString("dsformid"), Utility.getCurrentDateTime(),userId);
                        PageNumber = 1;
                        dataSourceIndex++;
                    }
                }
                Log.d("PagenoPKsaveSourceData", "PagenoPKsaveSourceData: " + PageNumber);
            } else {
                PageNumber = 1;
                progressDialog.setProgress(0);
                appDatabase.getDDE_FormWiseDataSourceDao().setUpdateDate(dataSourceForFormOnline.get(dataSourceIndex).getString("dsformid"), Utility.getCurrentDateTime(),userId);
                dataSourceIndex++;
            }


            progressDialog.setProgress(pstatus);
            loadSourceData();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

 /*   @Override
    protected void onResume() {
        super.onResume();
        //callFillforms();
        showSavedOldForms();
    }*/

    private void showSavedOldForms() {
        navigationView.getMenu().getItem(0).setChecked(true);
        Bundle bundle = new Bundle();
        bundle.putString("userID", userId);
        SavedFormsFragment savedFormsFragment = new SavedFormsFragment();
        savedFormsFragment.setArguments(bundle);
        FragmentManager fm = getFragmentManager();
        fm.beginTransaction().replace(R.id.fragment, savedFormsFragment).commit();
    }

    private void getQuestionsAndData(final int formId) {
        String url = QuestionUrl + formId;
        AndroidNetworking.get(url).addHeaders("Content-Type", "application/json").addHeaders("Authorization", token).build().getAsJSONObject(new JSONObjectRequestListener() {
            @Override
            public void onResponse(JSONObject response) {
                formIndex++;
                saveData(response);
            }

            @Override
            public void onError(ANError anError) {
                showErrorDialog(formId);
                Utility.updateErrorLog(anError, appDatabase, "HomeScreen : getQuestionsAndData");
                Utility.dismissDialog(dialog);
            }
        });
    }

    private void showErrorDialog(int formId) {
        String formName = appDatabase.getDDE_FormsDao().getFormName(String.valueOf(formId));
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Error");
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setMessage("Problem in pulling " + formName + " !");

        alertDialogBuilder.setPositiveButton("Reload", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogHere, int which) {
                dialogHere.dismiss();
                Utility.showDialogInApiCalling(dialog, mContext, "Getting Questions");
                getQuestionsAndData((int) forms.get(formIndex));
            }
        });

        alertDialogBuilder.setNegativeButton("Skip", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogHere, int which) {
                dialogHere.dismiss();
                Utility.showDialogInApiCalling(dialog, mContext, "Getting Questions");
                formIndex++;
                getQuestionsAndData((int) forms.get(formIndex));
            }
        });
        alertDialogBuilder.show();
    }

    private void showErrorDialogForDependent(final int formId) {
        String formName = appDatabase.getDDE_FormsDao().getFormName(String.valueOf(formId));
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Error");
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setMessage("Problem in pulling " + formName + " !");

        alertDialogBuilder.setPositiveButton("Reload", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogHere, int which) {
                dialogHere.dismiss();
                Utility.showDialogInApiCalling(dialog, mContext, "Getting Questions");
                getQuestionsForDependentForms(formId);
            }
        });

        alertDialogBuilder.setNegativeButton("Skip", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogHere, int which) {
                dialogHere.dismiss();
                Utility.showDialogInApiCalling(dialog, mContext, "Getting Questions");
                depformIndex++;
                getQuestionsForDependentForms(formId);
            }
        });
        alertDialogBuilder.show();
    }

    private void saveData(JSONObject response) {
        saveQuestion(response);
        if (formIndex < forms.size()) {
            getQuestionsAndData((int) forms.get(formIndex));
        } else {
//            updatedFormsToPull.clear();
            fetchQuestionsSourceData();
        }
    }

    private void saveQuestion(JSONObject response) {
        try {
            boolean containFlag;
            JSONObject data = response.getJSONObject("Data");
            String questions = data.getString("Questions");
            JSONArray datasourceList = response.getJSONArray("DatasourceList");
            if (datasourceList.length() > 0) {
                // For unique datasources
                JSONObject dsJsonObject;
                DDE_FormWiseDataSource dde_formWiseDataSourceObj = new DDE_FormWiseDataSource();
                for (int dsIndex = 0; dsIndex < datasourceList.length(); dsIndex++) {
                    dsJsonObject = datasourceList.getJSONObject(dsIndex);
                    dde_formWiseDataSourceObj.setFormwisedsid(dsJsonObject.getString("formwisedsid"));
                    dde_formWiseDataSourceObj.setUserId(userId);
                    dde_formWiseDataSourceObj.setDsformid(dsJsonObject.getString("dsformid"));
                    dde_formWiseDataSourceObj.setFormid(dsJsonObject.getString("formid"));
                    appDatabase.getDDE_FormWiseDataSourceDao().insertEntry(dde_formWiseDataSourceObj);
                    containFlag = false;
                    for (int dsOnlineIndex = 0; dsOnlineIndex < dataSourceForFormOnline.size(); dsOnlineIndex++) {
                        if (dsJsonObject.getString("dsformid").equalsIgnoreCase(dataSourceForFormOnline.get(dsOnlineIndex).getString("dsformid")))
                            containFlag = true;
                    }
                    if (!containFlag) dataSourceForFormOnline.add(dsJsonObject);
                }
            }
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
            } else {
                appDatabase.getDDE_FormsDao().updatePulledDate(response.getJSONObject("Formdata").getString("formid"), "" + Utility.getCurrentDateTime());
            }
            for (int formIndex = 0; formIndex < updatedFormsToPull.size(); formIndex++) {
                if (updatedFormsToPull.get(formIndex).getFormid() == response.getJSONObject("Formdata").getInt("formid")) {
                    updatedFormsToPull.remove(formIndex);
                    break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateFormEntries() {
        updatedFormsToPull.clear();
        User user = appDatabase.getUserDao().getUserDetails(userName, password);
        if (user != null)
            getNewForms(Utility.getProperty("prodgetForms", HomeScreen.this), user.getUserToken());
    }

    /* getFormsfromServer */
    private void getNewForms(String url, String access_token) {
        if (SyncUtility.isDataConnectionAvailable(this)) {
            Utility.showDialogInApiCalling(dialog, mContext, "Getting new forms... Please wait.");
            AndroidNetworking.get(url).addHeaders("Content-Type", "application/json").addHeaders("Authorization", access_token).build().getAsJSONObject(new JSONObjectRequestListener() {
                @Override
                public void onResponse(JSONObject response) {
                    /*  Log.d("pk-log", "" + response.length());*/
                    Utility.setMessage(dialog, "Updating forms in Database... Please wait.");
                    updateFormsInDatabase(response);
                }

                @Override
                public void onError(ANError error) {
                    Utility.dismissDialog(dialog);
                    Utility.updateErrorLog(error, appDatabase, "HomeScreen : getNewForms");
                    Utility.showDialogue(HomeScreen.this, "Problem in getting new forms due to: " + error.getErrorDetail());
                }
            });
        }
    }

    private void updateFormsInDatabase(JSONObject response) {
        try {
            JSONArray formData;
            JSONObject tempObj;
            DDE_Forms dde_form;
            final DDE_Forms[] dde_forms;
            if (response.length() > 1) {
                //JSONObject result = response.getJSONObject("Result");
                if (response.getString("success").equalsIgnoreCase("true")) {
                    formData = response.getJSONArray("Data");
                    dde_forms = new DDE_Forms[formData.length()];
                    for (int i = 0; i < formData.length(); i++) {
                        dde_form = new DDE_Forms();
                        tempObj = formData.getJSONObject(i);
                        dde_form.setFormid(tempObj.getInt("formid"));
                        dde_form.setFormname(tempObj.getString("formname"));
                        dde_form.setFormpassword(tempObj.getString("formpassword"));
                        dde_form.setProgramid(tempObj.getString("programid"));
                        dde_form.setTablename(tempObj.getString("tablename"));
                        if (!tempObj.getString("dataupdateddate").equalsIgnoreCase("null"))
                            dde_form.setDataupdateddate(tempObj.getString("dataupdateddate"));
                        String pulledDateString = appDatabase.getDDE_FormsDao().getPulledDateTimeByFormID(tempObj.getString("formid"));
                        if (pulledDateString == null) {
                            if (!updatedFormsToPull.contains(dde_form))
                                updatedFormsToPull.add(dde_form);
                        } else {
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
                            Date pulledDateDate = simpleDateFormat.parse(pulledDateString);
                            String updateddate = tempObj.getString("updateddate");
                            if (updateddate.equals("null")) {
                                updateddate = tempObj.getString("createddate");
                            }
                            Date update = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(updateddate);
                            if (pulledDateDate.compareTo(update) < 0) {
                                if (!updatedFormsToPull.contains(dde_form))
                                    updatedFormsToPull.add(dde_form);
                            }
                            dde_form.setPulledDateTime(pulledDateString);
                        }
                        dde_forms[i] = dde_form;
                    }
                    DDE_Forms[] db_dde_forms;
                    List<String> dbFormIds = new ArrayList<>();
                    boolean flag;
                    db_dde_forms = appDatabase.getDDE_FormsDao().getAllForms();
                    if (db_dde_forms != null && db_dde_forms.length > 0) {
                        for (int dbFormNo = 0; dbFormNo < db_dde_forms.length; dbFormNo++) {
                            flag = false;
                            for (int formNo = 0; formNo < dde_forms.length; formNo++) {
                                if (db_dde_forms[dbFormNo].getFormid() == dde_forms[formNo].getFormid()) {
                                    flag = true;
                                    break;
                                }
                            }
                            if (flag) {
                                if (!dbFormIds.contains(String.valueOf(db_dde_forms[dbFormNo].getFormid())))
                                    dbFormIds.add(String.valueOf(db_dde_forms[dbFormNo].getFormid()));
                            }
                        }
                    }
                    for (int formCounter = 0; formCounter < dbFormIds.size(); formCounter++) {
                        //  appDatabase.getDDE_QuestionsDao().deleteQuestionsByFormID(dbFormIds.get(formCounter));
//                        appDatabase.getAnswerDao().setPushedStatusByFormId(dbFormIds.get(formCounter), 1);
                        appDatabase.getDDE_FormsDao().deleteFormById(dbFormIds.get(formCounter));
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
                          /*  if (!unUpdatedForms.equals("")) {
                               AlertDialog.Builder builder = new AlertDialog.Builder(HomeScreen.this);
                                builder.setTitle("Questions under below form has been updated");
                                builder.setMessage(unUpdatedForms + "\n\n\n Please pull the form(s) again.");
                                builder.setCancelable(true);
                                builder.show();
                            }*/
                            getQuestions();
                            formLoaded = true;
                            callFillforms();
                            //showSavedOldForms();
                        }

                        @Override
                        protected void onCancelled() {
                            Utility.dismissDialog(dialog);
                        }
                    }.execute();
                } else {
                    Utility.dismissDialog(dialog);
                    Toast.makeText(mContext, "Problem with server", Toast.LENGTH_SHORT).show();
                    callFillforms();
                }
            } else {
                Utility.dismissDialog(dialog);
                Toast.makeText(mContext, "Empty Response", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(mContext, "Error ...", Toast.LENGTH_SHORT).show();
            Utility.dismissDialog(dialog);
            e.printStackTrace();
        }
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

    @Override
    public void onBackPressed() {
        //super.onBackPressed();

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Warning..!!");
        alertDialog.setMessage("Do you really want to logout?");
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });

        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alertDialog.show();
    }

    @Override
    public void fillAgainForm(boolean value) {
        showSavedOldForms();
    }

    @Override
    public void getSelectedForms(ArrayList list) {
        if (!list.isEmpty()) {
            forms = list;
            Utility.showDialogInApiCalling(dialog, mContext, "Getting Questions");
            getQuestionsAndData((int) forms.get(formIndex));
        } else {
            fetchQuestionsSourceData();
            Toast.makeText(HomeScreen.this, "Nothing Selected", Toast.LENGTH_SHORT).show();
        }
    }

   /* public int stringToInt(String s) {
        try{
            return Integer.parseInt(s);
        }catch (Exception e){
            return 0;
        }
    }*/

    private void getQuestionsForDependentForms(final int formId) {
        String url = QuestionUrl + formId;
        AndroidNetworking.get(url).addHeaders("Content-Type", "application/json").addHeaders("Authorization", token).build().getAsJSONObject(new JSONObjectRequestListener() {
            @Override
            public void onResponse(JSONObject response) {
                depformIndex++;
                saveDependentFormDataData(response);
            }

            @Override
            public void onError(ANError anError) {
                showErrorDialogForDependent(formId);
                Utility.updateErrorLog(anError, appDatabase, "HomeScreen : getQuestionsAndData");
                Utility.dismissDialog(dialog);
            }
        });

    }

    private void saveDependentFormDataData(JSONObject response) {
        //TODO save questions in db with flag datasource
        //saveQuestion(response);
        saveDependentFormQuestion(response);
        getDependentForms();
    }

    private void saveDependentFormQuestion(JSONObject response) {
            try {
//                boolean containFlag;
                JSONObject data = response.getJSONObject("Data");
                String questions = data.getString("Questions");
                //JSONArray datasourceList = response.getJSONArray("DatasourceList");
//                if (datasourceList.length() > 0) {
//                    // For unique datasources
//                    JSONObject dsJsonObject;
//                    DDE_FormWiseDataSource dde_formWiseDataSourceObj = new DDE_FormWiseDataSource();
//                    for (int dsIndex = 0; dsIndex < datasourceList.length(); dsIndex++) {
//                        dsJsonObject = datasourceList.getJSONObject(dsIndex);
//                        dde_formWiseDataSourceObj.setFormwisedsid(dsJsonObject.getString("formwisedsid") + userId);
//                        dde_formWiseDataSourceObj.setDsformid(dsJsonObject.getString("dsformid"));
//                        dde_formWiseDataSourceObj.setFormid(dsJsonObject.getString("formid"));
//                        appDatabase.getDDE_FormWiseDataSourceDao().insertEntry(dde_formWiseDataSourceObj);
//                        containFlag = false;
//                        for (int dsOnlineIndex = 0; dsOnlineIndex < dataSourceForFormOnline.size(); dsOnlineIndex++) {
//                            if (dsJsonObject.getString("dsformid").equalsIgnoreCase(dataSourceForFormOnline.get(dsOnlineIndex).getString("dsformid")))
//                                containFlag = true;
//                        }
//                        if (!containFlag) dataSourceForFormOnline.add(dsJsonObject);
//                    }
//                }
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
                } else {
                    appDatabase.getDDE_FormsDao().updatePulledDate(response.getJSONObject("Formdata").getString("formid"), "" + Utility.getCurrentDateTime());
                }
//                for (int formIndex = 0; formIndex < updatedFormsToPull.size(); formIndex++) {
//                    if (updatedFormsToPull.get(formIndex).getFormid() == response.getJSONObject("Formdata").getInt("formid")) {
//                        updatedFormsToPull.remove(formIndex);
//                        break;
//                    }
//                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

    }

}
