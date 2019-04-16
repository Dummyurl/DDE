package com.pratham.dde.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.DownloadListener;
import com.androidnetworking.interfaces.DownloadProgressListener;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pratham.dde.BaseActivity;
import com.pratham.dde.R;
import com.pratham.dde.customViews.SelectQuestionDialog;
import com.pratham.dde.database.BackupDatabase;
import com.pratham.dde.domain.AnswersSingleForm;
import com.pratham.dde.domain.DDE_FormWiseDataSource;
import com.pratham.dde.domain.DDE_Forms;
import com.pratham.dde.domain.DDE_Questions;
import com.pratham.dde.domain.DDE_RuleTable;
import com.pratham.dde.domain.DataSourceEntries;
import com.pratham.dde.fragments.FillFormsFragment;
import com.pratham.dde.fragments.SavedFormsFragment;
import com.pratham.dde.interfaces.FabInterface;
import com.pratham.dde.interfaces.FillAgainListner;
import com.pratham.dde.interfaces.QuestionListLisner;
import com.pratham.dde.interfaces.updateTokenListener;
import com.pratham.dde.services.SyncUtility;
import com.pratham.dde.utils.UploadAnswerAndImageToServer;
import com.pratham.dde.utils.Utility;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

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

public class HomeScreen extends AppCompatActivity implements FabInterface, FillAgainListner, QuestionListLisner, updateTokenListener {

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
    public static String token;
    String QuestionUrl;
    List forms;
    int formIndex = 0, depformIndex = 0;
    int dataSourceIndex = 0;
    int PageNumber = 1;
    int imageCntDownload = 0;
    List<JSONObject> dataSourceForFormOnline;
    List<DDE_Forms> updatedFormsToPull;
    public static final int rowsPerPage = 10000, GETNEWFORMS = 1, GETQUESTIONS = 2;
    ProgressDialog progressDialog;
    int maxProgressCnt = 0;
    updateTokenListener tokenListener;

    //store id of image questions for downloading images
    List<DDE_Questions> imageQuestionList = new ArrayList();

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
        tokenListener = (updateTokenListener) mContext;
        formLoaded = false;
        updatedFormsToPull = new ArrayList<>();
        userName = this.getIntent().getStringExtra("userName");
        password = this.getIntent().getStringExtra("password");
        userId = String.valueOf(BaseActivity.appDatabase.getUserDao().getUserId(userName, password));
        token = BaseActivity.appDatabase.getUserDao().getToken(userName, password);
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
                            getNewToken();
                        }
                        break;

                    case R.id.nav_fill_forms:
                        menuItem.setChecked(true);
                        callFillforms();
                        break;

                    case R.id.nav_old_forms:
                        List<AnswersSingleForm> allAnswersSingleForms = BaseActivity.appDatabase.getAnswerDao().getAllAnswersByStatusUnuploaded(userId);
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

    private void getNewToken() {
        //TODO updateToken
        Utility.updateToken(userName, password, GETQUESTIONS, tokenListener, mContext, dialog);
    }

    private void getQuestion() {
        if (updatedFormsToPull.size() > 0) {
            if (SyncUtility.isDataConnectionAvailable(HomeScreen.this)) {
                QuestionUrl = Utility.getProperty("prodgetQuestionsAndData", mContext);
                dataSourceForFormOnline = new ArrayList<>();
                SelectQuestionDialog selectQuestionDialog = new SelectQuestionDialog(HomeScreen.this, updatedFormsToPull);
                selectQuestionDialog.show();
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
        Cursor noOfForms = BaseActivity.appDatabase.getAnswerDao().getNoOfForms();
        try {
            obj.put("noOfForms", noOfForms.getCount());
            JSONArray jsonArray = new JSONArray();

            Cursor cursor = BaseActivity.appDatabase.getAnswerDao().getFormCount();
            cursor.moveToFirst();
            while (cursor.isAfterLast() == false) {
                JSONObject jsonObject = new JSONObject();

                jsonObject.put("FormId", cursor.getInt(cursor.getColumnIndex("FormId")));
                jsonObject.put("Count", cursor.getInt(cursor.getColumnIndex("cnt")));
                jsonArray.put(jsonObject);
                cursor.moveToNext();
            }
            obj.put("RecCountPerForm", jsonArray);

            List<AnswersSingleForm> answersSingleForms = BaseActivity.appDatabase.getAnswerDao().getAnswers();
            obj.put("TotalRecordCount", answersSingleForms.size());
            Log.d("answer", answersSingleForms.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj;
    }

    /*first load images and then load Question SourceDta  */
    private void fetchQuestionsSourceData() {
        Utility.dismissDialog(dialog);

        //load images


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
        for (int i = 0; i < imageQuestionList.size(); i++) {
            getImagePath(imageQuestionList.get(i));
        }
        if (imageQuestionList.size() == 0) {
            setDatasourceList();
            loadSourceData();
        }
    }

    private void getImagePath(final DDE_Questions dde_questions) {
        final String queID = dde_questions.getQuestionId();
        String formId = dde_questions.getFormId();
        String url = "http://www.ddeapi.prathamskills.org/api/ddeforms/GetQuestionOptions?Identifier=" + queID;
        AndroidNetworking.post(url).addHeaders("Content-Type", "application/json")
                .addHeaders("Content-Type", "application/x-www-form-urlencoded")
                .addHeaders("id", formId)
                .addHeaders("Authorization", token)
                .addBodyParameter("username", userName)
                .addBodyParameter("password", password)
                .addBodyParameter("grant_type", "password")
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String path = response.getString("ZipPath");
                            downloadImage(path, dde_questions);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        //todo delete form
                        BaseActivity.appDatabase.getDDE_FormsDao().deleteFormById(dde_questions.getFormId());
                        imageCntDownload++;
                        if (imageCntDownload == imageQuestionList.size()) {
                            setDatasourceList();
                            loadSourceData();
                        }
                       /* showErrorDialog(formId);
                        Utility.updateErrorLog(anError, BaseActivity.appDatabase, "HomeScreen : getQuestionsAndData");
                        Utility.dismissDialog(dialog);*/
                    }
                });
    }

    private void downloadImage(String path, final DDE_Questions dde_questions) {
        final ProgressDialog progressBarImage = new ProgressDialog(this);
        progressBarImage.setCancelable(false);
        progressBarImage.setTitle("downloading Image");
        progressBarImage.show();
        final String storagePath = Environment.getExternalStorageDirectory().toString() + "/.DDE/DDEDownloadedImages";
        AndroidNetworking.download("http://www.ddeapi.prathamskills.org/" + path, storagePath, dde_questions.getQuestionId() + ".zip")
                .setTag("downloadTest")
                .setPriority(Priority.MEDIUM)
                .build()
                .setDownloadProgressListener(new DownloadProgressListener() {
                    @Override
                    public void onProgress(long bytesDownloaded, long totalBytes) {
                        // do anything with progress
                        progressBarImage.setMessage("" + bytesDownloaded + "/" + totalBytes);
                    }
                })
                .startDownload(new DownloadListener() {
                    @Override
                    public void onDownloadComplete() {
                        // do anything after completion
                        progressBarImage.dismiss();
                        imageCntDownload++;
                        if (imageCntDownload == imageQuestionList.size()) {
                            setDatasourceList();
                            loadSourceData();
                        }
                        String source = storagePath + "/" + dde_questions.getQuestionId() + ".zip";
                        String destination = storagePath + "/unzipped" + "/" + dde_questions.getQuestionId();
                        try {
                            ZipFile zipFile = new ZipFile(source);
                            zipFile.extractAll(destination);
                        } catch (ZipException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onError(ANError error) {
                        // handle error
                        //todo delete form
                        progressBarImage.dismiss();
                        if (imageCntDownload == imageQuestionList.size()) {
                            setDatasourceList();
                            loadSourceData();
                        }
                    }
                });
    }


    private void setDatasourceList() {
        List<DDE_FormWiseDataSource> listOfDSEntries;
        try {
            listOfDSEntries = BaseActivity.appDatabase.getDDE_FormWiseDataSourceDao().getAllDSEntriesByUID(userId);
            if (listOfDSEntries != null) {
                // remove logic -----> form updated and datasource too
                for (int dsEntryIndex = 0; dsEntryIndex < dataSourceForFormOnline.size(); dsEntryIndex++) {
                    DDE_FormWiseDataSource dataObj = BaseActivity.appDatabase.getDDE_FormWiseDataSourceDao().getDataBYDSIdAndUserId(dataSourceForFormOnline.get(dsEntryIndex).getString("dsformid"), userId);
                    if (dataObj != null) {
                        String DSUpdateDate = BaseActivity.appDatabase.getDDE_FormsDao().getDataupdatedDateByFormID(dataObj.getDsformid());
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
                    String DSUpdateDate = BaseActivity.appDatabase.getDDE_FormsDao().getDataupdatedDateByFormID(dde_formWiseDataSourceObj.getDsformid());
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
                String lastPulledDate = BaseActivity.appDatabase.getDDE_FormWiseDataSourceDao().getLastUpdateDateOfDSFormId(dsFormId, userId);
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
                String formNameDownloading = BaseActivity.appDatabase.getDDE_FormsDao().getFormName(dsFormId);
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
                                Utility.updateErrorLog(anError, BaseActivity.appDatabase, "HomeScreen : loadSourceData");
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
                    String userNames = BaseActivity.appDatabase.getDataSourceEntriesDao().getUsersAssociatedWithData(entryId);
                    if (userNames != null) {
                        if (!userNames.contains("," + userId + ","))
                            userNames += userId + ",";
                        else
                            userNames = "," + userId + ",";
                    } else
                        userNames = "," + userId + ",";
                    dataSourceEntryObj.setUsers(userNames);
                    dataSourceEntries.add(dataSourceEntryObj);
                }
                BaseActivity.appDatabase.getDataSourceEntriesDao().insertEntry(dataSourceEntries);
                PageNumber++;
                if (maxProgressCnt < rowsPerPage) {
                    BaseActivity.appDatabase.getDDE_FormWiseDataSourceDao().setUpdateDate(dataSourceForFormOnline.get(dataSourceIndex).getString("dsformid"), Utility.getCurrentDateTime(), userId);
                    pstatus = 100;
                    PageNumber = 1;
                    dataSourceIndex++;
                } else {
                    pstatus = ((rowsPerPage * (PageNumber - 1)) * 100) / maxProgressCnt;
                    if (pstatus > 100) {
                        BaseActivity.appDatabase.getDDE_FormWiseDataSourceDao().setUpdateDate(dataSourceForFormOnline.get(dataSourceIndex).getString("dsformid"), Utility.getCurrentDateTime(), userId);
                        PageNumber = 1;
                        dataSourceIndex++;
                    }
                }
                Log.d("PagenoPKsaveSourceData", "PagenoPKsaveSourceData: " + PageNumber);
            } else {
                PageNumber = 1;
                progressDialog.setProgress(0);
                BaseActivity.appDatabase.getDDE_FormWiseDataSourceDao().setUpdateDate(dataSourceForFormOnline.get(dataSourceIndex).getString("dsformid"), Utility.getCurrentDateTime(), userId);
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
        AndroidNetworking.get(url).addHeaders("Content-Type", "application/json")
                .addHeaders("Authorization", token).build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        formIndex++;
                        saveData(response);
                    }

                    @Override
                    public void onError(ANError anError) {
                        showErrorDialog(formId);
                        Utility.updateErrorLog(anError, BaseActivity.appDatabase, "HomeScreen : getQuestionsAndData");
                        Utility.dismissDialog(dialog);
                    }
                });
    }

    private void showErrorDialog(int formId) {
        String formName = BaseActivity.appDatabase.getDDE_FormsDao().getFormName(String.valueOf(formId));
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
        String formName = BaseActivity.appDatabase.getDDE_FormsDao().getFormName(String.valueOf(formId));
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
                    BaseActivity.appDatabase.getDDE_FormWiseDataSourceDao().insertEntry(dde_formWiseDataSourceObj);
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

                for (DDE_Questions dde_questions : questionList) {
                    if ("singleimage".equals(dde_questions.getQuestionType()) || "multipleimage".equals(dde_questions.getQuestionType())) {
                        imageQuestionList.add(dde_questions);
                    }
                }
                String allRules = data.getString("Rules");
                Type listTypeRule = new TypeToken<ArrayList<DDE_RuleTable>>() {
                }.getType();
                ArrayList<DDE_RuleTable> rulesList = gson.fromJson(allRules, listTypeRule);

                /*ENTERING FORMID MANUALLY*/
                formId = questionList.get(0).getFormId();
                for (int i = 0; i < rulesList.size(); i++) {
                    rulesList.get(i).setFormID(formId);
                }
                BaseActivity.appDatabase.getDDE_RulesDao().deleteRulesByFormID(formId);
                BaseActivity.appDatabase.getDDE_RulesDao().insertAllRule(rulesList);


                BaseActivity.appDatabase.getDDE_FormsDao().updatePulledDate(questionList.get(0).getFormId(), "" + Utility.getCurrentDateTime());
                BaseActivity.appDatabase.getDDE_QuestionsDao().deleteQuestionsByFormID(formId);
                BaseActivity.appDatabase.getDDE_QuestionsDao().insertAllQuestions(questionList);
                formId = response.getJSONObject("Formdata").getString("formid");
                BaseActivity.appDatabase.getDDE_FormsDao().updatePulledDate(formId, "" + Utility.getCurrentDateTime());
            } else {
                BaseActivity.appDatabase.getDDE_FormsDao().updatePulledDate(response.getJSONObject("Formdata").getString("formid"), "" + Utility.getCurrentDateTime());
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
        //TODO updateToken
        Utility.updateToken(userName, password, GETNEWFORMS, tokenListener, mContext, dialog);
        updatedFormsToPull.clear();
    }

    /* getFormsfromServer */
    private void getNewForms(String url) {
        if (SyncUtility.isDataConnectionAvailable(this)) {
            Utility.showDialogInApiCalling(dialog, mContext, "Getting new forms... Please wait.");
            AndroidNetworking.get(url).addHeaders("Content-Type", "application/json").addHeaders("Authorization", token).build().getAsJSONObject(new JSONObjectRequestListener() {
                @Override
                public void onResponse(JSONObject response) {
                    /*  Log.d("pk-log", "" + response.length());*/
                    Utility.setMessage(dialog, "Updating forms in Database... Please wait.");
                    updateFormsInDatabase(response);
                }

                @Override
                public void onError(ANError error) {
                    Utility.dismissDialog(dialog);
                    Utility.updateErrorLog(error, BaseActivity.appDatabase, "HomeScreen : getNewForms");
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
            Boolean newUser;
            final DDE_Forms[] dde_forms;
            if (response.length() > 1) {
                //JSONObject result = response.getJSONObject("Result");
                if (response.getString("success").equalsIgnoreCase("true")) {
                    formData = response.getJSONArray("Data");
                    dde_forms = new DDE_Forms[formData.length()];
                    for (int i = 0; i < formData.length(); i++) {
                        newUser = false;
                        dde_form = new DDE_Forms();
                        tempObj = formData.getJSONObject(i);
                        dde_form.setFormid(tempObj.getInt("formid"));
                        dde_form.setFormname(tempObj.getString("formname"));
                        dde_form.setFormpassword(tempObj.getString("formpassword"));
                        dde_form.setProgramid(tempObj.getString("programid"));
                        dde_form.setTablename(tempObj.getString("tablename"));
                        // setting userIds for downloaded forms
                        String userIds = BaseActivity.appDatabase.getDDE_FormsDao().getUserIdsByFormId(String.valueOf(tempObj.getInt("formid")));
                        if (userIds == null) {
                            newUser = true;
                            userIds = "," + userId + ",";
                        } else if (!userIds.contains("," + userId + ",")) {
                            newUser = true;
                            userIds += userId + ",";
                        }
                        dde_form.setUserId(userIds);

                        if (newUser) {
                            if (!updatedFormsToPull.contains(dde_form))
                                updatedFormsToPull.add(dde_form);
                        } else {
                            if (!tempObj.getString("dataupdateddate").equalsIgnoreCase("null"))
                                dde_form.setDataupdateddate(tempObj.getString("dataupdateddate"));
                            String pulledDateString = BaseActivity.appDatabase.getDDE_FormsDao().getPulledDateTimeByFormID(tempObj.getString("formid"));
                            if (pulledDateString == null) {
                                // form not pulled
                                if (!updatedFormsToPull.contains(dde_form))
                                    updatedFormsToPull.add(dde_form);
                            } else {
                                // checking whether form questions are updated on server
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
                                Date pulledDateDate = simpleDateFormat.parse(pulledDateString);
                                String updateddate = tempObj.getString("updateddate");
                                if (updateddate.equals("null")) {
                                    updateddate = tempObj.getString("createddate");
                                }
                                Date update = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(updateddate);
                                // checking if pulled form has been updated or not
                                if (pulledDateDate.compareTo(update) < 0) {
                                    if (!updatedFormsToPull.contains(dde_form))
                                        updatedFormsToPull.add(dde_form);
                                }
                                dde_form.setPulledDateTime(pulledDateString);
                            }
                        }
                        dde_forms[i] = dde_form;
                    }

                    // deleting local forms which are updated on server.
                    DDE_Forms[] db_dde_forms;
                    List<String> dbFormIds = new ArrayList<>();
                    boolean flag;
                    db_dde_forms = BaseActivity.appDatabase.getDDE_FormsDao().getAllForms();
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
                        BaseActivity.appDatabase.getDDE_FormsDao().deleteFormById(dbFormIds.get(formCounter));
                    }

                    // inserting downloaded forms to database
                    new AsyncTask<Void, Void, String>() {
                        @Override
                        protected String doInBackground(Void... voids) {
                            BaseActivity.appDatabase.getDDE_FormsDao().insertForms(dde_forms);
                            return null;
                        }

                        @Override
                        protected void onPostExecute(String s) {
                            BaseActivity.appDatabase.getStatusDao().updateValue("LastPulledDate", Utility.getCurrentDateTime());
                            BackupDatabase.backup(mContext);
                            Utility.dismissDialog(dialog);
                          /*  if (!unUpdatedForms.equals("")) {
                               AlertDialog.Builder builder = new AlertDialog.Builder(HomeScreen.this);
                                builder.setTitle("Questions under below form has been updated");
                                builder.setMessage(unUpdatedForms + "\n\n\n Please pull the form(s) again.");
                                builder.setCancelable(true);
                                builder.show();
                            }*/

                            // Getting questions for updates forms
                            getNewToken();
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
                Utility.updateErrorLog(anError, BaseActivity.appDatabase, "HomeScreen : getQuestionsAndData");
                Utility.dismissDialog(dialog);
            }
        });

    }

    private void saveDependentFormDataData(JSONObject response) {
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
                BaseActivity.appDatabase.getDDE_RulesDao().deleteRulesByFormID(formId);
                BaseActivity.appDatabase.getDDE_RulesDao().insertAllRule(rulesList);


                BaseActivity.appDatabase.getDDE_FormsDao().updatePulledDate(questionList.get(0).getFormId(), "" + Utility.getCurrentDateTime());
                BaseActivity.appDatabase.getDDE_QuestionsDao().deleteQuestionsByFormID(formId);
                BaseActivity.appDatabase.getDDE_QuestionsDao().insertAllQuestions(questionList);
                formId = response.getJSONObject("Formdata").getString("formid");
                BaseActivity.appDatabase.getDDE_FormsDao().updatePulledDate(formId, "" + Utility.getCurrentDateTime());
            } else {
                BaseActivity.appDatabase.getDDE_FormsDao().updatePulledDate(response.getJSONObject("Formdata").getString("formid"), "" + Utility.getCurrentDateTime());
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

    @Override
    public void updateToken(int methodToCall, String updatedToken) {
        token = updatedToken;
        switch (methodToCall) {
            case 1:
                // 1 : getNewForms
                getNewForms(Utility.getProperty("prodgetForms", HomeScreen.this));
                break;
            case 2:
                // 2 : getQuestions
                getQuestion();
                break;
        }
    }
}
