package com.pratham.dde.activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.zxing.Result;
import com.pratham.dde.BaseActivity;
import com.pratham.dde.DDE_Application;
import com.pratham.dde.MapDialog;
import com.pratham.dde.R;
import com.pratham.dde.customViews.ChooseImageDialog;
import com.pratham.dde.customViews.previewFormDialog;
import com.pratham.dde.domain.AnswerJSonArrays;
import com.pratham.dde.domain.AnswersSingleForm;
import com.pratham.dde.domain.DDE_Questions;
import com.pratham.dde.domain.DDE_RuleTable;
import com.pratham.dde.domain.DataSourceEntries;
import com.pratham.dde.domain.User;
import com.pratham.dde.interfaces.CurrentLocationListener;
import com.pratham.dde.interfaces.FillAgainListner;
import com.pratham.dde.interfaces.PreviewFormListener;
import com.pratham.dde.interfaces.updateTokenListener;
import com.pratham.dde.services.LocationService;
import com.pratham.dde.services.SyncUtility;
import com.pratham.dde.utils.DisplayValue;
import com.pratham.dde.utils.PermissionUtils;
import com.pratham.dde.utils.UploadAnswerAndImageToServer;
import com.pratham.dde.utils.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class DisplayQuestions extends BaseActivity implements FillAgainListner, PreviewFormListener, updateTokenListener, CurrentLocationListener, ZXingScannerView.ResultHandler {
    @BindView(R.id.homeButton)
    ImageView homeButton;
    @BindView(R.id.formNameHeader)
    TextView formNameHeader;
    @BindView(R.id.renderAllQuestions)
    LinearLayout renderAllQuestionsLayout;
    @BindView(R.id.parentScroll)
    ScrollView parentScroll;

    // private static final String MAP_VIEW_BUNDLE_KEY = "AIzaSyCSs-ZUla2cMV_5XojPVtnVfquSttJzW9M";

    List depQueID;
    List<DDE_RuleTable> allRules;
    List<DDE_Questions> formIdWiseQuestions = new ArrayList<>();
    List checkBoxList;
    List checkBoxImageList;
    public static final int PICK_IMAGE_FROM_GALLERY = 1;
    public static final int CAPTURE_IMAGE = 0;
    public static final int UPLOAD = 1;
    private static final int VIDEO_CAPTURE = 101;
    ImageView selectedImage;
    ImageView selectedView;
    static String userId;
    static String formId;
    boolean editFormFlag = false;
    String imageName = "";
    String videoName = "";
    String entryID;
    JsonArray answerJsonArray;
    String path, videoPath;
    boolean firstRun = true, deletedAlertShown = false;
    Dialog dialog;
    List<DataSourceEntries> dataSourceEntriesOnline;
    Context mContext;
    previewFormDialog preview;
    String depForms = "";
    updateTokenListener tokenListener;
    LocationService locationService;
    //  MapView mapView;
    MediaController mediaController;
    EditText qrEdittext;
    DDE_Questions qr_dde_questions;
    MapDialog newFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_questions);
        ButterKnife.bind(this);
        tokenListener = (updateTokenListener) this;
        mediaController = new MediaController(DisplayQuestions.this);
        locationService = new LocationService(this);
        if (!locationService.checkLocationEnabled()) {
            locationService.checkLocation();
            // textView1.setText(locationService.getLocation().toString());
        }
     /*   Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }*/
     /*   LinearLayout.LayoutParams mapViewparam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 200, 0);
        mapViewparam.setMargins(10, 0, 0, 0);
        mapView = new MapView(this);
        mapView.onCreate(mapViewBundle);
        mapView.setLayoutParams(mapViewparam);*/
        proceedFurther();
    }

    private void proceedFurther() {
        mContext = DisplayQuestions.this;

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                //Utility.showDialogInApiCalling(dialog, mContext, "Getting form ready to edit");
                dialog = new ProgressDialog(DisplayQuestions.this);
                dialog.setTitle("Getting form ready to edit");
                dialog.setCancelable(false);
                dialog.show();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                formId = getIntent().getStringExtra("formId");
                userId = getIntent().getStringExtra("userId");
                entryID = getIntent().getStringExtra("entryId");
                path = Environment.getExternalStorageDirectory().toString() + "/.DDE/DDEImages";
                videoPath = Environment.getExternalStorageDirectory().toString() + "/.DDE/DDEVideos";
                if (!getIntent().getExtras().getBoolean("fillAgainFlag", false)) {
                    DDE_Application.setCashedDataSourceEntriesOnline(new ArrayList<DataSourceEntries>());
                    dataSourceEntriesOnline = appDatabase.getDataSourceEntriesDao().getDatasourceOnline(appDatabase.getDDE_FormWiseDataSourceDao().getDSFormId(formId), "%," + userId + ",%");
                } else {
                    dataSourceEntriesOnline = DDE_Application.getCashedDataSourceEntriesOnline();
                    if (dataSourceEntriesOnline == null)
                        dataSourceEntriesOnline = appDatabase.getDataSourceEntriesDao().getDatasourceOnline(appDatabase.getDDE_FormWiseDataSourceDao().getDSFormId(formId), "%," + userId + ",%");
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                // Utility.dismissDialog(dialog);
                dialog.dismiss();
                Log.d("pkpkpk", "doInBackground: " + dataSourceEntriesOnline.size());
                final String formEdit = getIntent().getStringExtra("formEdit");
                if (formEdit.equals("true")) {
                    editFormFlag = true;
                } else {
                    editFormFlag = false;
                }
                checkBoxList = new ArrayList();
                checkBoxImageList = new ArrayList();
                String formName = appDatabase.getDDE_FormsDao().getFormName(formId);
                if (formName != null) {
                    formNameHeader.setText(formName);
                }
                allRules = appDatabase.getDDE_RulesDao().getAllRules(formId);
                formIdWiseQuestions = appDatabase.getDDE_QuestionsDao().getFormIdWiseQuestions(formId);
                Collections.sort(formIdWiseQuestions, new SortQuestions());

                /* SET VISIBILITY TO QUESTIONS */
                setVisibilityToQuestions(formId);

                if (editFormFlag) {
                    AnswersSingleForm answersSingleForm = appDatabase.getAnswerDao().getAnswersByEntryId(entryID);
                    answerJsonArray = answersSingleForm.getAnswerArrayOfSingleForm();
                } else {
                    entryID = Utility.getUniqueID().toString();
                }

                // flag for reinitialisation for the forms which has been deleted and still using
                deletedAlertShown = false;

                for (int i = 0; i < formIdWiseQuestions.size(); i++) {
                    displaySingleQue(formIdWiseQuestions.get(i));
                }
                for (int i = 0; i < formIdWiseQuestions.size(); i++) {
                    DDE_Questions dde_que = formIdWiseQuestions.get(i);
                    checkRuleCondition(dde_que.getQuestionId(), dde_que.getAnswer(), dde_que.getQuestionType());
                }
                firstRun = false;
            }
        }.execute();
    }

    /* @Override
     public void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);

         Bundle mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
         if (mapViewBundle == null) {
             mapViewBundle = new Bundle();
             outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle);
         }
         mapView.onSaveInstanceState(mapViewBundle);
     }
 */
    private void setVisibilityToQuestions(String formId) {
        depQueID = appDatabase.getDDE_RulesDao().getDependantQuestion(formId);
    }

    /* Display A Single Questions One By One */
    private void displaySingleQue(final DDE_Questions dde_questions) {
        try {
            final View layout = LayoutInflater.from(this).inflate(R.layout.question_layout, renderAllQuestionsLayout, false);
//        final LinearLayout layout = new LinearLayout(this);
//        layout.setPadding(10, 10, 10, 50);
//        layout.setOrientation(LinearLayout.VERTICAL);
//todo changed            layout.setTag(dde_questions.getQuestionId());
            final LinearLayout rootQuestionLL = layout.findViewById(R.id.root);
            rootQuestionLL.setTag(dde_questions.getQuestionId());
//        TextView textView = new TextView(this);
            TextView textView = layout.findViewById(R.id.tv_Question);
            textView.setText(/*dde_questions.getFieldSeqNo() + ". " + */dde_questions.getQuestion());
            textView.setTag("que" + dde_questions.getQuestionId());
//        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25);
//        textView.setTextColor(getResources().getColor(R.color.black));
//        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0);
//        params.setMargins(10, 0, 0, 0);
//        textView.setLayoutParams(params);
//        layout.addView(textView);

            LinearLayout.LayoutParams paramsWrapContaint = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0);
            paramsWrapContaint.setMargins(10, 0, 0, 0);

            /*SET DEFAULT VALUE TO ANSWER FIELD*/
            String validationValue = "";
            JsonArray validationArray = dde_questions.getValidations();
            for (int i = 1; i < validationArray.size(); i++) {
                JsonObject jsonObject = validationArray.get(i).getAsJsonObject();
                String validation = jsonObject.get("validationName").getAsString();
                if (validation.equals("DEFAULTVAL")) {
                    if (jsonObject.get("ValidationValue").isJsonNull()) {
                        validationValue = "";
                    } else {
                        validationValue = jsonObject.get("ValidationValue").getAsString();
                    }
                }
            }


            switch (dde_questions.getQuestionType()) {
                case "text":
                case "email":
                    View queTextLayout = LayoutInflater.from(this).inflate(R.layout.layout_edittext, rootQuestionLL, false);
                    final EditText editText = queTextLayout.findViewById(R.id.edt_Answer);
                    rootQuestionLL.addView(editText);
//                editText.setBackground(ContextCompat.getDrawable(this, R.drawable.rectangular_box));
                    //editText.setPadding(15, 5, 5, 5);
//                editText.setSingleLine(true);
//                editText.setLayoutParams(params);
//                layout.addView(editText);
                    if (editFormFlag) {
                        String dest_column = dde_questions.getDestColumname();
                        for (int ansObjIndex = 0; ansObjIndex < answerJsonArray.size(); ansObjIndex++) {
                            JsonObject ansObject = answerJsonArray.get(ansObjIndex).getAsJsonObject();
                            if (ansObject.get("DestColumnName").getAsString().equalsIgnoreCase(dest_column)) {
                                String ans = ansObject.get("Answers").getAsString();
                                editText.setText(ans);
                                dde_questions.setAnswer(ans);
                            }
                        }
                    } else {
                        editText.setText(validationValue);
                        dde_questions.setAnswer(validationValue);
                    }
                    editText.addTextChangedListener(new TextWatcher() {

                        // the user's changes are saved here
                        public void onTextChanged(CharSequence c, int start, int before, int count) {
                            //  mCrime.setTitle(c.toString());
                        }

                        public void beforeTextChanged(CharSequence c, int start, int count, int after) {
                            // this space intentionally left blank
                        }

                        public void afterTextChanged(Editable c) {
                            dde_questions.setAnswer(c.toString());
                        }
                    });
                    break;
                case "qrcode":
                    View queQrLayout = LayoutInflater.from(this).inflate(R.layout.layout_qr, rootQuestionLL, false);
                    final LinearLayout qrLayout = queQrLayout.findViewById(R.id.image_ll);
                    final Button btn_qrScan = qrLayout.findViewById(R.id.btn_qrScan);
                    final EditText et_answer = qrLayout.findViewById(R.id.edt_ScanAnswer);
//                    final TextView time = new TextView(this);
//                    time.setPadding(10, 5, 5, 5);
//                    time.setBackground(ContextCompat.getDrawable(this, R.drawable.rectangular_box));
//                    time.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
//                    time.setText("Select Time");
//                    time.setLayoutParams(paramsWrapContaint);
                    qrLayout.setTag("ans" + dde_questions.getQuestionId());
                    rootQuestionLL.addView(qrLayout);
                    if (editFormFlag) {
                        String dest_column = dde_questions.getDestColumname();
                        for (int ansObjIndex = 0; ansObjIndex < answerJsonArray.size(); ansObjIndex++) {
                            JsonObject ansObject = answerJsonArray.get(ansObjIndex).getAsJsonObject();
                            if (ansObject.get("DestColumnName").getAsString().equalsIgnoreCase(dest_column)) {
                                String ans = ansObject.get("Answers").getAsString();
                                et_answer.setText(ans);
                                dde_questions.setAnswer(ans);
                            }
                        }
                        qrEdittext = et_answer;
                        qr_dde_questions = dde_questions;
                    } else {
                        String text = "";
                        if (!validationValue.isEmpty())
                            text = validationValue;
                        et_answer.setText(text);
                        dde_questions.setAnswer(text);
                    }
//                layout.addView(time);
                    btn_qrScan.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            qrEdittext = et_answer;
                            qr_dde_questions = dde_questions;
                            scanQR();
                        }
                    });
                    break;

                case "singlechoice":
                    JsonArray option = dde_questions.getQuestionOption();
                    View queRadioGrpLayout = LayoutInflater.from(this).inflate(R.layout.layout_radiogroup, rootQuestionLL, false);
                    RadioGroup radioGroup = queRadioGrpLayout.findViewById(R.id.rg_options);
//                    RadioGroup radioGroup = new RadioGroup(this);
//                    radioGroup.setBackground(ContextCompat.getDrawable(this, R.drawable.rectangular_box));
                    for (int i = 0; i < option.size(); i++) {
                        JsonElement jsonElement = option.get(i);
                        JsonObject jsonObject = jsonElement.getAsJsonObject();
                        View queRadioLayout = LayoutInflater.from(this).inflate(R.layout.layout_radiobutton, (ViewGroup) queRadioGrpLayout, false);
                        final RadioButton radioButton = queRadioLayout.findViewById(R.id.rb_option);
                        radioButton.setId(i);
                        radioButton.setLayoutParams(paramsWrapContaint);
                        String tag = jsonObject.get("value").getAsString();
                        radioButton.setTag(tag);
                        String text = jsonObject.get("display").getAsString();
                        radioButton.setText(text);
                        if (editFormFlag) {
                            String dest_column = dde_questions.getDestColumname();
                            for (int ansObjIndex = 0; ansObjIndex < answerJsonArray.size(); ansObjIndex++) {
                                JsonObject ansObject = answerJsonArray.get(ansObjIndex).getAsJsonObject();
                                if (ansObject.get("DestColumnName").getAsString().equalsIgnoreCase(dest_column)) {
                                    String ans = ansObject.get("Answers").getAsString();
                                    if (ans.equalsIgnoreCase(tag)) {
                                        radioButton.setChecked(true);
                                        dde_questions.setAnswer(ans);
                                    }
                                }
                            }
                        } else {
                            if (validationValue.equalsIgnoreCase(tag)) {
                                radioButton.setChecked(true);
                                dde_questions.setAnswer(validationValue);
                            }
                        }
                        radioGroup.addView(radioButton);
                        radioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton compoundButton, boolean isSelected) {
                                if (isSelected) {
                                    dde_questions.setAnswer(compoundButton.getTag().toString());
                                    LinearLayout layout = (LinearLayout) compoundButton.getParent().getParent();
                                    String tag = (String) layout.getTag();
                                    checkRuleCondition(tag, compoundButton.getTag().toString(), "singlechoice");
                                }
                            }
                        });
                    }
                    rootQuestionLL.addView(radioGroup);
//                radioGroup.setLayoutParams(params);
//                layout.addView(radioGroup);
                    break;

//                case "email":
//                    View queEmailLayout = LayoutInflater.from(this).inflate(R.layout.layout_edittext, rootQuestionLL, false);
//                    final EditText et_email = queEmailLayout.findViewById(R.id.edt_Answer);
//                    rootQuestionLL.addView(et_email);
////                    final EditText et_email = new EditText(this);
////                    et_email.setBackground(ContextCompat.getDrawable(this, R.drawable.rectangular_box));
//                    //et_email.setPadding(15, 5, 5, 5);
////                    et_email.setSingleLine(true);
////                et_email.setLayoutParams(params);
////                layout.addView(et_email);
////                    et_email.setText(validationValue);
//                    if (editFormFlag) {
//                        String dest_column = dde_questions.getDestColumname();
//                        for (int ansObjIndex = 0; ansObjIndex < answerJsonArray.size(); ansObjIndex++) {
//                            JsonObject ansObject = answerJsonArray.get(ansObjIndex).getAsJsonObject();
//                            if (ansObject.get("DestColumnName").getAsString().equalsIgnoreCase(dest_column)) {
//                                String ans = ansObject.get("Answers").getAsString();
//                                et_email.setText(ans);
//                                dde_questions.setAnswer(ans);
//                            }
//                        }
//                    } else {
//                        et_email.setText(validationValue);
//                        dde_questions.setAnswer(validationValue);
//                    }
//                    et_email.addTextChangedListener(new TextWatcher() {
//
//                        // the user's changes are saved here
//                        public void onTextChanged(CharSequence c, int start, int before, int count) {
//                            //  mCrime.setTitle(c.toString());
//                        }
//
//                        public void beforeTextChanged(CharSequence c, int start, int count, int after) {
//                            // this space intentionally left blank
//                        }
//
//                        public void afterTextChanged(Editable c) {
//                            dde_questions.setAnswer(c.toString());
//                        /*LinearLayout layout = (LinearLayout) et_email.getParent();
//                        String tag = (String) layout.getTag();
//                        //  checkRuleCondition(tag, c.toString());*/
//                        }
//                    });
//                    break;

                case "number":
                    View queNumberLayout = LayoutInflater.from(this).inflate(R.layout.layout_edittext, rootQuestionLL, false);
                    final EditText number = queNumberLayout.findViewById(R.id.edt_Answer);
                    rootQuestionLL.addView(number);
//                    final EditText number = new EditText(this);
//                    number.setBackground(ContextCompat.getDrawable(this, R.drawable.rectangular_box));
                    //number.setPadding(15, 5, 5, 5);

                    for (int i = 1; i < validationArray.size(); i++) {
                        JsonObject jsonObject = validationArray.get(i).getAsJsonObject();
                        String validation = jsonObject.get("validationName").getAsString();
                        if (validation.equals("ALLOWDECIMAL")) {
                            if (jsonObject.get("ValidationApply").getAsBoolean()) {
                                if (jsonObject.get("ValidationValue").getAsString().equalsIgnoreCase("true"))
                                    number.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                                else number.setInputType(InputType.TYPE_CLASS_NUMBER);
                            } else {
                                number.setInputType(InputType.TYPE_CLASS_NUMBER);
                            }
                        }
                    }
                    number.setSingleLine(true);
//                number.setLayoutParams(params);
                    number.setTag("ans" + dde_questions.getQuestionId());
                    if (editFormFlag) {
                        String dest_column = dde_questions.getDestColumname();
                        for (int ansObjIndex = 0; ansObjIndex < answerJsonArray.size(); ansObjIndex++) {
                            JsonObject ansObject = answerJsonArray.get(ansObjIndex).getAsJsonObject();
                            if (ansObject.get("DestColumnName").getAsString().equalsIgnoreCase(dest_column)) {
                                String ans = ansObject.get("Answers").getAsString();
                                number.setText(ans);
                                dde_questions.setAnswer(ans);
                          /*  String tag = (String) layout.getTag();
                            checkRuleCondition(tag, ans, "number");*/
                            }
                        }
                    } else {
                        number.setText(validationValue);
                        dde_questions.setAnswer(validationValue);
                    }
                    number.addTextChangedListener(new TextWatcher() {

                        // the user's changes are saved here
                        public void onTextChanged(CharSequence c, int start, int before, int count) {
                            //  mCrime.setTitle(c.toString());
                        }

                        public void beforeTextChanged(CharSequence c, int start, int count, int after) {
                            // this space intentionally left blank
                        }

                        public void afterTextChanged(Editable c) {
                            dde_questions.setAnswer(c.toString());
                            LinearLayout layout = (LinearLayout) number.getParent();
                            String tag = (String) layout.getTag();
                            checkRuleCondition(tag, c.toString(), "number");
                        }
                    });
//                layout.addView(number);
                    break;

                case "multiple":
                    String ans = "";
                    JsonArray optionCheckBox = dde_questions.getQuestionOption();
                    View queMultipleLayout = LayoutInflater.from(this).inflate(R.layout.layout_imagecheckbox_group, rootQuestionLL, false);
                    final GridLayout gridLayout = queMultipleLayout.findViewById(R.id.gl_checkboxGroup);
//                    GridLayout gridLayout = new GridLayout(this);
//                    gridLayout.setBackground(ContextCompat.getDrawable(this, R.drawable.rectangular_box));
                    gridLayout.setColumnCount(1);
                    if (!validationValue.isEmpty() && !validationValue.endsWith(","))
                        validationValue += ",";

                    if (editFormFlag) {
                        String dest_column = dde_questions.getDestColumname();
                        for (int ansObjIndex = 0; ansObjIndex < answerJsonArray.size(); ansObjIndex++) {
                            JsonObject ansObject = answerJsonArray.get(ansObjIndex).getAsJsonObject();
                            if (ansObject.get("DestColumnName").getAsString().equalsIgnoreCase(dest_column)) {
                                ans = ansObject.get("Answers").getAsString();
                                if (!ans.endsWith(",")) ans += ",";
                            }
                        }
                    } else {
                        dde_questions.setAnswer(validationValue);
                    }
                    for (int i = 0; i < optionCheckBox.size(); i++) {
                        View queCheckBoxLayout = LayoutInflater.from(this).inflate(R.layout.layout_checkbox, gridLayout, false);
                        final CheckBox checkBox = queCheckBoxLayout.findViewById(R.id.cb_option);
//                        final CheckBox checkBox = new CheckBox(this);
                        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton compoundButton, boolean isSelected) {
                                String selectedAnswers = dde_questions.getAnswer();
                                if (selectedAnswers == null)
                                    selectedAnswers = "";
                                if (selectedAnswers.length() > 0) {
                                    if (!selectedAnswers.endsWith(",")) {
                                        selectedAnswers += ",";
                                    }
                                }
                                String tag = compoundButton.getTag().toString();
                                String[] splitted = tag.split(":::");
                                if (isSelected) {
                                    if (!selectedAnswers.contains(splitted[1] + ",")) {
                                        selectedAnswers += splitted[1] + ",";
                                    }
                                } else {
                                    Log.d("replace..", selectedAnswers + "//" + splitted[1]);
                                    selectedAnswers = selectedAnswers.replace(splitted[1] + ",", "");
                                }
                            /*if (selectedAnswers.endsWith(",")) {
                                selectedAnswers = selectedAnswers.substring(0, selectedAnswers.length() - 1);
                            }*/
                                dde_questions.setAnswer(selectedAnswers);
                                //String queParent = ((LinearLayout) compoundButton.getParent().getParent()).getTag().toString();
//                                String queParent = layout.getTag().toString();
                                String queParent = rootQuestionLL.getTag().toString();
                                checkRuleCondition(queParent, selectedAnswers, "multiple");
                            }
                        });
                        JsonElement jsonElement = optionCheckBox.get(i);
                        JsonObject jsonObject = jsonElement.getAsJsonObject();
                        checkBox.setTag(dde_questions.getQuestionId() + ":::" + jsonObject.get("value").getAsString());
                        String text = jsonObject.get("display").getAsString();
                        String value = jsonObject.get("value").getAsString();
                        checkBox.setText(text);
                        if (editFormFlag) {
                            if (ans.contains(value + ",")) {
                                checkBox.setChecked(true);
                                dde_questions.setAnswer(ans);
                            }
                        } else {
                            if (validationValue.contains(value + ",")) {
                                checkBox.setChecked(true);
                                dde_questions.setAnswer(validationValue);
                            }
                        }
                        checkBoxList.add(checkBox);
//                        GridLayout.LayoutParams paramGrid = new GridLayout.LayoutParams();
//                        paramGrid.height = GridLayout.LayoutParams.WRAP_CONTENT;
//                        paramGrid.width = GridLayout.LayoutParams.WRAP_CONTENT;
//                        paramGrid.setGravity(Gravity.FILL_HORIZONTAL);
//                        checkBox.setLayoutParams(paramGrid);
                        gridLayout.addView(checkBox);
                    }
                    rootQuestionLL.addView(gridLayout);
//                layout.addView(gridLayout);
                    break;
                case "image":
                    View queImageLayout = LayoutInflater.from(this).inflate(R.layout.layout_singleimage, rootQuestionLL, false);
                    final LinearLayout imageLayout = queImageLayout.findViewById(R.id.image_ll);
                    final Button imageBtn = imageLayout.findViewById(R.id.btn_imgPicker);
                    final ImageView selectedImageTemp = imageLayout.findViewById(R.id.iv_imageView);
                    rootQuestionLL.addView(imageLayout);
//                    LinearLayout outerLinearLayout = new LinearLayout(this);
//                    outerLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
//                    final TextView tv_img = new TextView(this);
//                    tv_img.setPadding(5, 5, 5, 5);
//                    tv_img.setBackground(ContextCompat.getDrawable(this, R.drawable.rectangular_box));
//                    tv_img.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
//                    tv_img.setText("Select Image");
//                    outerLinearLayout.addView(tv_img);
                    // selectedImage.setLayoutParams(new android.view.ViewGroup.LayoutParams(150, 150));
//                    selectedImageTemp.setBackground(ContextCompat.getDrawable(this, R.drawable.rectangular_box));
//                    selectedImageTemp.setPadding(10, 5, 5, 5);
//                    LinearLayout.LayoutParams buttonLayoutParams = new LinearLayout.LayoutParams(200, 200);
//                    buttonLayoutParams.setMargins(50, 0, 0, 0);
//                    selectedImageTemp.setLayoutParams(buttonLayoutParams);
//                    outerLinearLayout.addView(selectedImageTemp);

                    if (editFormFlag) {
                        String dest_column = dde_questions.getDestColumname();
                        for (int ansObjIndex = 0; ansObjIndex < answerJsonArray.size(); ansObjIndex++) {
                            JsonObject ansObject = answerJsonArray.get(ansObjIndex).getAsJsonObject();
                            if (ansObject.get("DestColumnName").getAsString().equalsIgnoreCase(dest_column)) {
                                ans = ansObject.get("Answers").getAsString();
                                Bitmap bmp = BitmapFactory.decodeFile(path + "/" + ans);
                                selectedImageTemp.setImageBitmap(bmp);
                                dde_questions.setAnswer(ans);
                            }
                        }
                    }
//                layout.addView(outerLinearLayout);

                    imageBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final ChooseImageDialog chooseImageDialog = new ChooseImageDialog(DisplayQuestions.this);
                            chooseImageDialog.btn_take_photo.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    chooseImageDialog.cancel();
                                    if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)) {
                                        String[] permissionArray = new String[]{PermissionUtils.Manifest_CAMERA};

                                        if (!isPermissionsGranted(DisplayQuestions.this, permissionArray)) {
                                            Toast.makeText(DisplayQuestions.this, "Give Camera permissions through settings and restart the app.", Toast.LENGTH_SHORT).show();
                                        } else {
                                            imageName = entryID + "_" + dde_questions.getQuestionId() + ".jpg";
                                            dde_questions.setAnswer(imageName);
                                            selectedImage = selectedImageTemp;
                                            Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                            startActivityForResult(takePicture, CAPTURE_IMAGE);
                                        }
                                    } else {
                                        imageName = entryID + "_" + dde_questions.getQuestionId() + ".jpg";
                                        dde_questions.setAnswer(imageName);
                                        selectedImage = selectedImageTemp;
                                        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                        startActivityForResult(takePicture, CAPTURE_IMAGE);
                                    }
                                }
                            });

                            chooseImageDialog.btn_choose_from_gallery.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    chooseImageDialog.cancel();
                                    if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)) {
                                        String[] permissionArray = new String[]{PermissionUtils.Manifest_WRITE_EXTERNAL_STORAGE};

                                        if (!isPermissionsGranted(DisplayQuestions.this, permissionArray)) {
                                            Toast.makeText(DisplayQuestions.this, "Give Storage permissions through settings and restart the app.", Toast.LENGTH_SHORT).show();
                                        } else {
                                            imageName = entryID + "_" + dde_questions.getQuestionId() + ".jpg";
                                            dde_questions.setAnswer(imageName);
                                            selectedImage = selectedImageTemp;
                                            Intent intent = new Intent();
                                            intent.setType("image/*");
                                            intent.setAction(Intent.ACTION_GET_CONTENT);
                                            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_FROM_GALLERY);
                                        }
                                    } else {
                                        imageName = entryID + "_" + dde_questions.getQuestionId() + ".jpg";
                                        dde_questions.setAnswer(imageName);
                                        selectedImage = selectedImageTemp;
                                        Intent intent = new Intent();
                                        intent.setType("image/*");
                                        intent.setAction(Intent.ACTION_GET_CONTENT);
                                        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_FROM_GALLERY);
                                    }
                                }
                            });

                            chooseImageDialog.show();
                        }
                    });
                    break;

                case "date":
                    View queDateLayout = LayoutInflater.from(this).inflate(R.layout.layout_date, rootQuestionLL, false);
                    final Button date = queDateLayout.findViewById(R.id.btn_datePicker);
//                    final TextView date = new TextView(this);
//                    date.setPadding(10, 5, 5, 5);
//                    date.setBackground(ContextCompat.getDrawable(this, R.drawable.rectangular_box));
//                    date.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
//                    date.setText("Select Date");
                    date.setTag("ans" + dde_questions.getQuestionId());
//                    date.setLayoutParams(paramsWrapContaint);
                    rootQuestionLL.addView(date);
                    if (editFormFlag) {
                        String dest_column = dde_questions.getDestColumname();
                        for (int ansObjIndex = 0; ansObjIndex < answerJsonArray.size(); ansObjIndex++) {
                            JsonObject ansObject = answerJsonArray.get(ansObjIndex).getAsJsonObject();
                            if (ansObject.get("DestColumnName").getAsString().equalsIgnoreCase(dest_column)) {
                                ans = ansObject.get("Answers").getAsString();
                                date.setText(ans);
                                dde_questions.setAnswer(ans);
                            }
                        }
                    } else {
                        if (!validationValue.isEmpty() && parseDate(validationValue) != null) {
                            date.setText(parseDate(validationValue));
                            dde_questions.setAnswer(validationValue);
                        }
                    }
                    date.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Calendar calendar = Calendar.getInstance();
                            int mYear = calendar.get(Calendar.YEAR);
                            int mMonth = calendar.get(Calendar.MONTH);
                            int mDay = calendar.get(Calendar.DAY_OF_MONTH);
                            DatePickerDialog datePickerDialog;

                            datePickerDialog = new DatePickerDialog(DisplayQuestions.this, new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                                    month++;
                                    dde_questions.setAnswer("" + year + "/" + month + "/" + day);
                                    date.setText("" + year + "/" + month + "/" + day);
                                }

                            }, mYear, mMonth, mDay);

                            /*GETTING RANGE OF DATE FROM QUESTION */
                            DatePicker datePicker = datePickerDialog.getDatePicker();
                            datePicker.init(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(), new DatePicker.OnDateChangedListener() {
                                @Override
                                public void onDateChanged(DatePicker datePicker, int i, int i1, int i2) {
                                    // Toast.makeText(DisplayQuestions.this, "Date changed", Toast.LENGTH_SHORT).show();
                                }
                            });
                            Calendar c = Calendar.getInstance();
                            String minDateString = dde_questions.getMINRANGE();
                            String maxDateString = dde_questions.getMAXRANGE();
                            if (minDateString != null) {
                                int minYear = 0, minMonth = 0, minDay = 0;
                                try {
                                    JSONObject mindateJsonObject = new JSONObject(minDateString);
                                    minYear = mindateJsonObject.getInt("year");
                                    minMonth = mindateJsonObject.getInt("month") - 1;
                                    minDay = mindateJsonObject.getInt("day");

                                    c.set(minYear, minMonth, minDay);
                                    datePicker.setMinDate(c.getTimeInMillis());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            if (maxDateString != null) {
                                int maxYear = 0, maxMonth = 0, maxDay = 0;

                                JSONObject maxdateJsonObject = null;
                                try {
                                    maxdateJsonObject = new JSONObject(maxDateString);
                                    maxYear = maxdateJsonObject.getInt("year");
                                    maxMonth = maxdateJsonObject.getInt("month") - 1;
                                    maxDay = maxdateJsonObject.getInt("day");

                                    c.set(maxYear, maxMonth, maxDay);
                                    datePicker.setMaxDate(c.getTimeInMillis());
                                } catch (JSONException e1) {
                                    e1.printStackTrace();
                                }
                            }

                            datePickerDialog.setTitle("Select Date");
                            datePickerDialog.show();

                        }
                    });
                    break;


                case "time":
                    View queTimeLayout = LayoutInflater.from(this).inflate(R.layout.layout_time, rootQuestionLL, false);
                    final Button time = queTimeLayout.findViewById(R.id.btn_timePicker);
//                    final TextView time = new TextView(this);
//                    time.setPadding(10, 5, 5, 5);
//                    time.setBackground(ContextCompat.getDrawable(this, R.drawable.rectangular_box));
//                    time.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
//                    time.setText("Select Time");
//                    time.setLayoutParams(paramsWrapContaint);
                    time.setTag("ans" + dde_questions.getQuestionId());
                    rootQuestionLL.addView(time);
                    String text = null;
                    if (editFormFlag) {
                        String dest_column = dde_questions.getDestColumname();
                        for (int ansObjIndex = 0; ansObjIndex < answerJsonArray.size(); ansObjIndex++) {
                            JsonObject ansObject = answerJsonArray.get(ansObjIndex).getAsJsonObject();
                            if (ansObject.get("DestColumnName").getAsString().equalsIgnoreCase(dest_column)) {
                                ans = ansObject.get("Answers").getAsString();
                                time.setText(ans);
                                dde_questions.setAnswer(ans);
                            }
                        }
                    } else {
                        try {
                            if (!validationValue.isEmpty()) {
                                final SimpleDateFormat sdf = new SimpleDateFormat("H:mm");
                                final Date dateObj = sdf.parse(validationValue);
                                text = new SimpleDateFormat("K:mm aa").format(dateObj);
                            } else text = /*Utility.getTimeForDateQuestion()*/"";
                            time.setText(text);
                            dde_questions.setAnswer(text);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
//                layout.addView(time);
                    time.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Calendar calendar = Calendar.getInstance();
                            int hour = calendar.get(Calendar.HOUR_OF_DAY);
                            int minute = calendar.get(Calendar.MINUTE);
                            TimePickerDialog mtimePickerDialog;
                            mtimePickerDialog = new TimePickerDialog(DisplayQuestions.this, new TimePickerDialog.OnTimeSetListener() {
                                @Override
                                public void onTimeSet(TimePicker timePicker, int hours, int mins) {
                                    String timeSet = "";
                                    if (hours > 12) {
                                        hours -= 12;
                                        timeSet = "PM";
                                    } else if (hours == 0) {
                                        hours += 12;
                                        timeSet = "AM";
                                    } else if (hours == 12) timeSet = "PM";
                                    else timeSet = "AM";


                                    String minutes = "";
                                    if (mins < 10) minutes = "0" + mins;
                                    else minutes = String.valueOf(mins);

                                    // Append in a StringBuilder
                                    String aTime = new StringBuilder().append(hours).append(':').append(minutes).append(" ").append(timeSet).toString();

                                    time.setText(aTime);
                                    dde_questions.setAnswer(aTime);
                                }
                            }, hour, minute, false);
                            mtimePickerDialog.setTitle("Select time");
                            mtimePickerDialog.show();
                        }
                    });

                    break;

                case "dropdown":
                    View queDropdownLayout = LayoutInflater.from(this).inflate(R.layout.layout_spinner, rootQuestionLL, false);
                    final Spinner spinnerDropdown = queDropdownLayout.findViewById(R.id.sp_Answer);
                    rootQuestionLL.addView(spinnerDropdown);
                    List<DisplayValue> display = new ArrayList();
                    // List value = new ArrayList();
//                    Spinner spinnerDropdown = new Spinner(this);
//                    spinnerDropdown.setBackground(ContextCompat.getDrawable(this, R.drawable.spinnerbg));
                    JsonArray optionDropDown = dde_questions.getQuestionOption();
                    display.add(new DisplayValue("select option", "select option"));
                    for (int i = 0; i < optionDropDown.size(); i++) {
                        JsonElement jsonElement = optionDropDown.get(i);
                        JsonObject jsonObject = jsonElement.getAsJsonObject();
                        display.add(new DisplayValue(jsonObject.get("display").getAsString(), jsonObject.get("value").getAsString()));
                    }
                    ArrayAdapter<DisplayValue> spinnerArrayAdapter = new ArrayAdapter<DisplayValue>(this, android.R.layout.simple_selectable_list_item, display);
                    spinnerDropdown.setAdapter(spinnerArrayAdapter);
//                spinnerDropdown.setLayoutParams(params);
                    if (editFormFlag) {
                        String dest_column = dde_questions.getDestColumname();
                        for (int ansObjIndex = 0; ansObjIndex < answerJsonArray.size(); ansObjIndex++) {
                            JsonObject ansObject = answerJsonArray.get(ansObjIndex).getAsJsonObject();
                            if (ansObject.get("DestColumnName").getAsString().equalsIgnoreCase(dest_column)) {
                                ans = ansObject.get("Answers").getAsString();
                                int index = -1;
                                for (int i = 0; i < display.size(); i++) {
                                    if (display.get(i).getValue().equals(ans)) {
                                        index = i;
                                        break;
                                    }
                                }
                                if (index != -1) {
                                    spinnerDropdown.setSelection(index);
                                    dde_questions.setAnswer(ans);
                                }
                                break;
                            }
                        }
                    } else {
                        int index = getIndex(spinnerDropdown, validationValue);
                        if (index != -1) {
                            spinnerDropdown.setSelection(index);
                            dde_questions.setAnswer(validationValue);
                        }
                    }
                    spinnerDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            LinearLayout linearLayout = (LinearLayout) adapterView.getParent();
                            String tag = linearLayout.getTag().toString();
                            if (adapterView.getSelectedItem().toString().equals("select option")) {
                                dde_questions.setAnswer("");
                                checkRuleCondition(tag, "", "dropdown");
                            } else {
                                DisplayValue displayValue = (DisplayValue) adapterView.getSelectedItem();

                                dde_questions.setAnswer(displayValue.getValue());

                                checkRuleCondition(tag, ((DisplayValue) adapterView.getSelectedItem()).getValue(), "dropdown");
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });
//                layout.addView(spinnerDropdown);
                    break;

                case "datasourcelist":
                    //todo
                    if (dde_questions.getDependentQuestionIdentifier() == null)
                        new ShowDataSources(DisplayQuestions.this, layout, rootQuestionLL, dde_questions, "", "").execute();
                    else
                        new ShowDataSources(DisplayQuestions.this, layout, rootQuestionLL, dde_questions, "", "firstInitializaion").execute();
                    break;
                case "singleimage":
                    View queSingleImageLayout = LayoutInflater.from(this).inflate(R.layout.layout_imageradiogroup, rootQuestionLL, false);
                    final HorizontalScrollView horizontalLayoutRadioImage = queSingleImageLayout.findViewById(R.id.horizontal_grid);
//                    gridLayoutRadioImage.setColumnCount(2);
                    final RadioGroup imageRadio = horizontalLayoutRadioImage.findViewById(R.id.rg_imgoptions);
//
                    String path = Environment.getExternalStorageDirectory().toString() + "/.DDE/DDEDownloadedImages/unzipped/" + dde_questions.getQuestionId() + "/file/";
                    Log.d("Files", "Path: " + path);
                    File directory = new File(path);
                    File[] files = directory.listFiles();
                    if (files != null && files.length > 0) {
                        Log.d("Files", "Size: " + files.length);
                        for (int i = 0; i < files.length; i++) {
                            Log.d("Files", "FileName:" + files[i].getName());
                            View queRadioImageLayout = LayoutInflater.from(this).inflate(R.layout.layout_imageradiobutton, imageRadio, false);
                            final RadioButton radioImageButton = queRadioImageLayout.findViewById(R.id.rb_imgoption);
//                            RadioButton radioButton = new RadioButton(this);
                            radioImageButton.setId(i);
                            // radioButton.setButtonDrawable(R.drawable.selector);
//                            radioButton.setLayoutParams(paramsWrapContaintRadio);
                            radioImageButton.setTag(files[i].getName());
                            // radioButton.setText(files[i].getName());
                            String pathName = path + files[i].getName();
                            Resources res = getResources();
                            Bitmap bitmap = BitmapFactory.decodeFile(pathName);
                            Drawable bd = new BitmapDrawable(res, bitmap);
                            radioImageButton.setBackground(bd);
                            radioImageButton.setButtonDrawable(R.drawable.selector);
//                        radioButton.setPadding(20,20,20,20);

                            //radioButton.setCompoundDrawablesWithIntrinsicBounds(null, null, bd, null);

                            if (editFormFlag) {
                                String dest_column = dde_questions.getDestColumname();
                                for (int ansObjIndex = 0; ansObjIndex < answerJsonArray.size(); ansObjIndex++) {
                                    JsonObject ansObject = answerJsonArray.get(ansObjIndex).getAsJsonObject();
                                    if (ansObject.get("DestColumnName").getAsString().equalsIgnoreCase(dest_column)) {
                                        String ansimage = ansObject.get("Answers").getAsString();
                                        radioImageButton.setChecked(true);
                                        dde_questions.setAnswer(ansimage);
                                    }
                                }
                            } else {
                                if (validationValue.equalsIgnoreCase(radioImageButton.getTag().toString())) {
                                    radioImageButton.setChecked(true);
                                    dde_questions.setAnswer(validationValue);
                                }
                            }
                            radioImageButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton compoundButton, boolean isSelected) {
                                    if (isSelected) {
                                        dde_questions.setAnswer(dde_questions.getQuestionId() + "/" + compoundButton.getTag().toString());
                                        LinearLayout layout = (LinearLayout) compoundButton.getParent().getParent().getParent();
                                        String tag = (String) layout.getTag();
                                        checkRuleCondition(tag, compoundButton.getTag().toString(), "singleimage");
                                    }
                                }
                            });
                            imageRadio.addView(radioImageButton);
                        }
                        rootQuestionLL.addView(queSingleImageLayout);
//                    imageRadio.setLayoutParams(params);
//                    horizontalScrollView.addView(imageRadio);
//                    layout.addView(horizontalScrollView);
                    } else {
                        Toast.makeText(this, "Image not found", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case "multipleimage":
                    View queMultipleImageLayout = LayoutInflater.from(this).inflate(R.layout.layout_imagecheckbox_group, rootQuestionLL, false);
                    final GridLayout gridLayoutImage = queMultipleImageLayout.findViewById(R.id.gl_checkboxGroup);
//                    GridLayout gridLayoutImage = new GridLayout(this);
//                    gridLayoutImage.setBackground(ContextCompat.getDrawable(this, R.drawable.rectangular_box));
                    gridLayoutImage.setColumnCount(2);
                    String ansImage = "";
                    String pathImageCheckBox = Environment.getExternalStorageDirectory().toString() + "/.DDE/DDEDownloadedImages/unzipped/" + dde_questions.getQuestionId() + "/file/";
                    Log.d("Files", "Path: " + pathImageCheckBox);
                    File directoryImageCheckBox = new File(pathImageCheckBox);
                    File[] filesImageCheckBox = directoryImageCheckBox.listFiles();
                    if (!validationValue.isEmpty() && !validationValue.endsWith("|"))
                        validationValue += "|";

                    if (editFormFlag) {
                        String dest_column = dde_questions.getDestColumname();
                        for (int ansObjIndex = 0; ansObjIndex < answerJsonArray.size(); ansObjIndex++) {
                            JsonObject ansObject = answerJsonArray.get(ansObjIndex).getAsJsonObject();
                            if (ansObject.get("DestColumnName").getAsString().equalsIgnoreCase(dest_column)) {
                                ansImage = ansObject.get("Answers").getAsString();
                                if (!ansImage.endsWith("|")) ansImage += "|";
                            }
                        }
                    } else {
                        dde_questions.setAnswer(validationValue);
                    }
                    if (filesImageCheckBox != null && filesImageCheckBox.length > 0) {
                        for (File imageCheckBox : filesImageCheckBox) {
                            View queImageCheckBoxLayout = LayoutInflater.from(this).inflate(R.layout.layout_imagecheckbox, gridLayoutImage, false);
                            final CheckBox imgCheckBox = queImageCheckBoxLayout.findViewById(R.id.cb_imgoptions);
//                            final CheckBox checkBox = new CheckBox(this);
//                            imgCheckBox.setButtonDrawable(R.drawable.selector);
                            imgCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton compoundButton, boolean isSelected) {
                                    String selectedAnswers = dde_questions.getAnswer();
                                    if (selectedAnswers.length() > 0) {
                                        if (!selectedAnswers.endsWith("|")) {
                                            selectedAnswers += "|";
                                        }
                                    }
                                    String tag = compoundButton.getTag().toString();
                                    String[] splitted = tag.split(":::");
                                    if (isSelected) {
                                        if (!selectedAnswers.contains(splitted[1] + "|")) {
                                            selectedAnswers += splitted[1] + "|";
                                        }
                                    } else {
                                        Log.d("replace..", selectedAnswers + "//" + splitted[1]);
                                        selectedAnswers = selectedAnswers.replace(splitted[1] + "|", "");
                                    }
                          /* if (selectedAnswers.endsWith(",")) {
                                selectedAnswers = selectedAnswers.substring(0, selectedAnswers.length() - 1);
                            }*/
                                    dde_questions.setAnswer(selectedAnswers);
                                    //String queParent = ((LinearLayout) compoundButton.getParent().getParent()).getTag().toString();
                                    String queParent = rootQuestionLL.getTag().toString();
                                    checkRuleCondition(queParent, selectedAnswers, "multipleimage");
                                }
                            });
                            String pathName = pathImageCheckBox + imageCheckBox.getName();
                            Resources res = getResources();
                            Bitmap bitmap = BitmapFactory.decodeFile(pathName);
                            Drawable bd = new BitmapDrawable(res, bitmap);
                            imgCheckBox.setBackground(bd);
                            imgCheckBox.setButtonDrawable(R.drawable.selector);
//                        checkBox.setButtonDrawable(bd);
                            //checkBox.setBackground(R.drawable.selector);
                            //checkBox.setCompoundDrawablesWithIntrinsicBounds(null, null, null, bd);
                            imgCheckBox.setTag(dde_questions.getQuestionId() + ":::" + dde_questions.getQuestionId() + "/" + imageCheckBox.getName());

                   /* JsonElement jsonElement = optionImageCheckBox.get(i);
                    JsonObject jsonObject = jsonElement.getAsJsonObject();

                    String text = jsonObject.get("display").getAsString();
                    String value = jsonObject.get("value").getAsString();*/
//                        checkBox.setText(pathImageCheckBox + filesImageCheckBox[i].getName());
                            if (editFormFlag) {
                                if (ansImage.contains(imageCheckBox.getName() + "|")) {
                                    imgCheckBox.setChecked(true);
                                    dde_questions.setAnswer(ansImage);
                                }
                            } else {
                                if (validationValue.contains(imageCheckBox.getName() + "|")) {
                                    imgCheckBox.setChecked(true);
                                    dde_questions.setAnswer(validationValue);
                                }
                            }
                            checkBoxImageList.add(imgCheckBox);
//                            GridLayout.LayoutParams paramGrid = new GridLayout.LayoutParams();
//                            paramGrid.height = getDp(130);
//                            paramGrid.width = getDp(130);
//                            paramGrid.setMargins(20, 20, 20, 20);
//                            imgCheckBox.setLayoutParams(paramGrid);
                            gridLayoutImage.addView(imgCheckBox);
                        }
                    }
                    rootQuestionLL.addView(gridLayoutImage);
//                layout.addView(gridLayoutImage);
                    break;

                case "rating":
//                    LinearLayout.LayoutParams paramsRating = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0);
//                    paramsRating.setMargins(10, 0, 0, 0);
                    View queRatingLayout = LayoutInflater.from(this).inflate(R.layout.layout_rating, rootQuestionLL, false);
                    final RatingBar ratingBar = queRatingLayout.findViewById(R.id.rb_Rating);
                    rootQuestionLL.addView(ratingBar);

//                    final RatingBar ratingBar = new RatingBar(this);
//                    ratingBar.setBackground(ContextCompat.getDrawable(this, R.drawable.rectangular_box));
                    ratingBar.setNumStars(5);
                    ratingBar.setMax(5);
//                    ratingBar.setLayoutParams(paramsRating);
//                layout.addView(ratingBar);

                    if (editFormFlag) {
                        String dest_column = dde_questions.getDestColumname();
                        for (int ansObjIndex = 0; ansObjIndex < answerJsonArray.size(); ansObjIndex++) {
                            JsonObject ansObject = answerJsonArray.get(ansObjIndex).getAsJsonObject();
                            if (ansObject.get("DestColumnName").getAsString().equalsIgnoreCase(dest_column)) {
                                String ansRating = ansObject.get("Answers").getAsString();
                                if (!ansRating.isEmpty())
                                    ratingBar.setRating(Float.parseFloat(ansRating));
                                else
                                    ratingBar.setRating(0.0f);
                                dde_questions.setAnswer(ansRating);
                            }
                        }
                    } else {
                        try {
                            if (!validationValue.isEmpty())
                                ratingBar.setRating(Float.parseFloat(validationValue));
                            else
                                ratingBar.setRating(0.0f);
                            dde_questions.setAnswer(validationValue);
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    }

                    ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                        @Override
                        public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                            dde_questions.setAnswer(String.valueOf(rating));
                        }
                    });

                    break;
                case "location":
                    View queLocationLayout = LayoutInflater.from(this).inflate(R.layout.layout_location, rootQuestionLL, false);
                    final LinearLayout locationLL = queLocationLayout.findViewById(R.id.locationLL);
                    rootQuestionLL.addView(locationLL);
                    final TextView latLong = locationLL.findViewById(R.id.tv_location);
//                    textView1.setBackground(ContextCompat.getDrawable(DisplayQuestions.this, R.drawable.rectangular_box));
//                textView1.setLayoutParams(params);
//                    latLong.setTextSize(1, 18);
                    final Button buttonLocation = locationLL.findViewById(R.id.btn_Location);
//                    button.setText("View Map");
                    final Button buttonMap = locationLL.findViewById(R.id.btn_viewMap);
//                    getLocation.setText("get location");
                    if (editFormFlag) {
                        String dest_column = dde_questions.getDestColumname();
                        for (int ansObjIndex = 0; ansObjIndex < answerJsonArray.size(); ansObjIndex++) {
                            JsonObject ansObject = answerJsonArray.get(ansObjIndex).getAsJsonObject();
                            if (ansObject.get("DestColumnName").getAsString().equalsIgnoreCase(dest_column)) {
                                String ansLocation = ansObject.get("Answers").getAsString();
                                latLong.setText(ansLocation);
                                dde_questions.setAnswer(ansLocation);
                            }
                        }
                    } else {
                        latLong.setText(validationValue);
                        dde_questions.setAnswer(validationValue);
                    }


                    buttonMap.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (locationService.checkLocationEnabled()) {
                                // textView1.setText(locationService.getLocation().toString());
                                Location location = locationService.mlocation;
                                if (location != null) {

                                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                                    Fragment prev = getFragmentManager().findFragmentByTag("dialog");
                                    if (prev != null) {
                                        ft.remove(prev);
                                    }
                                    ft.addToBackStack(null);

                                    // Create and show the dialog.
                                    newFragment = MapDialog.newInstance(locationService.mlocation.getLatitude(), locationService.mlocation.getLongitude());
                                    newFragment.show(ft, "dialog");

                                    //  textView1.setText("Latitude:" + location.getLatitude() + ",Longitude" + location.getLongitude());
                                /*dde_questions.setAnswer("Latitude:" + location.getLatitude() + ",Longitude" + location.getLongitude());
                                mapView.setCameraDistance(2);
                                mapView.getMapAsync(new OnMapReadyCallback() {
                                    @Override
                                    public void onMapReady(GoogleMap googleMap) {
                                        googleMap.addMarker(new MarkerOptions()
                                                .position(new LatLng(locationService.mlocation.getLatitude(), locationService.mlocation.getLongitude()))
                                                .title("Marker"));
                                    }
                                });*/

                                }

                            } else {
                                locationService.checkLocation();
                            }
                        }
                    });

                    buttonLocation.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            if (locationService.checkLocationEnabled()) {
                                //
                                Location location = locationService.mlocation;
                                if (location != null) {
                                    latLong.setText("Latitude:" + location.getLatitude() + ",Longitude" + location.getLongitude());
                                    dde_questions.setAnswer("Latitude:" + location.getLatitude() + ",Longitude" + location.getLongitude());
                                }
                            } else {
                                locationService.checkLocation();
                            }
                        }
                    });


                    // layout.addView(mapView);
                    // mapView.setActivated(true);
//                layout.addView(textView1);
//                layout.addView(getLocation);
//                layout.addView(button);
                    break;
                case "video":
                    View queVideoLayout = LayoutInflater.from(this).inflate(R.layout.layout_video, rootQuestionLL, false);
                    final LinearLayout videoLL = queVideoLayout.findViewById(R.id.videoLL);
                    rootQuestionLL.addView(videoLL);
                    final Button record = videoLL.findViewById(R.id.btn_Record);
//                    LinearLayout outerLinearLayoutVideo = new LinearLayout(this);
//                    outerLinearLayoutVideo.setOrientation(LinearLayout.HORIZONTAL);
//                    final TextView tv_video = new TextView(this);
//                    tv_video.setPadding(5, 5, 5, 5);
//                    tv_video.setBackground(ContextCompat.getDrawable(this, R.drawable.rectangular_box));
//                    tv_video.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
//                    tv_video.setText("Record video");
//                    outerLinearLayoutVideo.addView(tv_video);
                    final ImageView selectedVideo = videoLL.findViewById(R.id.iv_thumbnail);
//                    selectedVideo.setBackground(ContextCompat.getDrawable(this, R.drawable.rectangular_box));

                    // selectedVideo.setMediaController(mediaController);
               /* selectedVideo.setLayoutParams(new android.view.ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 150));
                selectedVideo.setPadding(10, 5, 5, 5);*/
//                    LinearLayout.LayoutParams buttonLayoutParamsVideo = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 200);
//                    buttonLayoutParamsVideo.setMargins(50, 0, 50, 0);
//                    selectedVideo.setLayoutParams(buttonLayoutParamsVideo);
//                    outerLinearLayoutVideo.addView(selectedVideo);

                    if (editFormFlag) {
                        String dest_column = dde_questions.getDestColumname();
                        for (int ansObjIndex = 0; ansObjIndex < answerJsonArray.size(); ansObjIndex++) {
                            JsonObject ansObject = answerJsonArray.get(ansObjIndex).getAsJsonObject();
                            if (ansObject.get("DestColumnName").getAsString().equalsIgnoreCase(dest_column)) {
                                ans = ansObject.get("Answers").getAsString();
                           /* Bitmap bmp = BitmapFactory.decodeFile(path + "/" + ans);
                            selectedImageTemp.setImageBitmap(bmp);*/
                                // selectedVideo.setVideoPath(videoPath + "/" + ans);

                                final String finalAns = ans;
                                BitmapFactory.Options options = new BitmapFactory.Options();
                                options.inSampleSize = 1;
                                Bitmap thumb = ThumbnailUtils.createVideoThumbnail(videoPath + "/" + finalAns, MediaStore.Images.Thumbnails.MICRO_KIND);
                                selectedVideo.setImageBitmap(thumb);

                                selectedVideo.setTag(R.id.path, videoPath + "/" + finalAns);
                                selectedVideo.setTag(R.id.name, finalAns);
                                selectedVideo.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        final Dialog dialog = new Dialog(DisplayQuestions.this);
                                        dialog.setContentView(R.layout.videoplayer);
                                        dialog.show();
                                        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                        lp.copyFrom(dialog.getWindow().getAttributes());
                                        dialog.getWindow().setAttributes(lp);
                                        final VideoView videoview = (VideoView) dialog.findViewById(R.id.videoView);
                                        videoview.setVideoPath(videoPath + "/" + finalAns);
                                        videoview.setZOrderOnTop(true);
                                        videoview.setZOrderMediaOverlay(true);
                                        videoview.setMediaController(mediaController);
                                        mediaController.setAnchorView(videoview);
                                        videoview.start();
                                    }
                                });
                                dde_questions.setAnswer(ans);
                            }
                        }
                    }
//                layout.addView(outerLinearLayoutVideo);

                    record.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (hasCamera()) {
                                if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)) {
                                    String[] permissionArray = new String[]{PermissionUtils.Manifest_CAMERA};

                                    if (!isPermissionsGranted(DisplayQuestions.this, permissionArray)) {
                                        Toast.makeText(DisplayQuestions.this, "Give Camera permissions through settings and restart the app.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        videoName = entryID + "_" + dde_questions.getQuestionId() + ".mp4";
                                        dde_questions.setAnswer(videoName);
                                        selectedView = selectedVideo;
                                        Intent takePicture = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                                        takePicture.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 3 * 60);
                                        takePicture.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
                                        startActivityForResult(takePicture, VIDEO_CAPTURE);
                                    }
                                } else {
                                    videoName = entryID + "_" + dde_questions.getQuestionId() + ".mp4";
                                    dde_questions.setAnswer(videoName);
                                    selectedView = selectedVideo;
                                    Intent takePicture = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                                    takePicture.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 3 * 60);
                                    startActivityForResult(takePicture, VIDEO_CAPTURE);
                                }

                            } else {
                                Toast.makeText(DisplayQuestions.this, "Camera not found", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    break;
            }
            renderAllQuestionsLayout.addView(layout);
            /*check dependency if depends then hide*/
            if (depQueID.contains(dde_questions.getQuestionId())) {
                layout.setVisibility(View.GONE);
            } /*else {
             *//*LinearLayout.LayoutParams parameters = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2, 0);
            View view = new View(DisplayQuestions.this);
            view.setLayoutParams(parameters);
            renderAllQuestionsLayout.addView(view);*//*
        }
        LinearLayout.LayoutParams parameters = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 5, 0);
        parameters.setMargins(20, 20, 20, 20);
        View view = new View(DisplayQuestions.this);
        view.setLayoutParams(parameters);
        //view.setAlpha(0.3f);
        view.setBackgroundColor(getResources().getColor(R.color.black));
        renderAllQuestionsLayout.addView(view);*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    Dialog dialogQR;
    ZXingScannerView mScannerView;

    public void scanQR() {
        dialogQR = new Dialog(DisplayQuestions.this);
        dialogQR.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogQR.setContentView(R.layout.qr_scan_dialog);
        Objects.requireNonNull(dialogQR.getWindow()).setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialogQR.setCancelable(true);
        dialogQR.setCanceledOnTouchOutside(false);
        Button btn_reset = dialogQR.findViewById(R.id.dia_btn_cancel);
        ViewGroup content_frame = dialogQR.findViewById(R.id.content_frame);

        mScannerView = new ZXingScannerView(DisplayQuestions.this);
        mScannerView.setResultHandler(DisplayQuestions.this);
        mScannerView.startCamera();
        mScannerView.resumeCameraPreview(DisplayQuestions.this);
        content_frame.addView((mScannerView));
        dialogQR.show();
        btn_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mScannerView.resumeCameraPreview(DisplayQuestions.this);
            }
        });
    }

    @Override
    public void handleResult(Result result) {
        Toast.makeText(mContext, "" + result.getText(), Toast.LENGTH_SHORT).show();
        Log.d("RawResult:::", "****" + result.getText());
        if (qrEdittext != null && qr_dde_questions != null) {
            qrEdittext.setText(result.getText());
            qr_dde_questions.setAnswer(result.getText());
        }
        dialogQR.dismiss();
    }

    private boolean hasCamera() {
        return getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA);
    }

    private ArrayList<String> getDependentValues(ArrayList<String> answerList, String destColName, String conditionColName, String conditionColValue, NavigableMap<String, String> map) {

        for (DataSourceEntries dataSourceEntries : dataSourceEntriesOnline) {
            try {
                JSONObject jObject = new JSONObject(dataSourceEntries.getAnswers());
                if (conditionColName == null || conditionColName.isEmpty()) {
                    if (jObject.has(destColName)) {
                        String value = jObject.getString(destColName);
                        if (!answerList.contains(value)) answerList.add(value);
                    }
                } else {
                    boolean flag = true;
                    for (Map.Entry<String, String> mapEntry : map.entrySet()) {
                        if (jObject.has(mapEntry.getKey())) {
                            if (!jObject.getString(mapEntry.getKey()).equalsIgnoreCase(mapEntry.getValue())) {
                                flag = false;
                                break;
                            }
                        } else {
                            flag = false;
                            break;
                        }
                    }
                    if (flag) {
                        answerList.add(jObject.getString(destColName));
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return answerList;
    }

    @Override
    public void proceed(final AnswersSingleForm answersSingleForm) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Alert");
        alertDialogBuilder.setMessage("Do you want to Upload form to server?");

        alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                /*UPLOAD TO SERVER*/
                dialog.dismiss();
                preview.dismiss();
                appDatabase.getAnswerDao().insertAnswer(answersSingleForm);
                if (SyncUtility.isDataConnectionAvailable(DisplayQuestions.this)) {
                    getTokenAndUpload();
                } else {
                    Toast.makeText(DisplayQuestions.this, "CHECK INTERNET CONNECTION", Toast.LENGTH_LONG).show();
                }
            }
        });

        alertDialogBuilder.setNegativeButton("Save Locally", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                preview.dismiss();
                appDatabase.getAnswerDao().insertAnswer(answersSingleForm);
                fillAgain();
            }
        });
        alertDialogBuilder.show();
    }

    @Override
    public Location getLocation() {
        return null;
    }

    private class ShowDataSources extends AsyncTask<Void, Void, Void> {
        Context context;
        View layout;
        LinearLayout rootQuestionLL;
        DDE_Questions dde_questions;
        String answer;
        String destColumnParent;
        ArrayList<String> answerList;
        Spinner spinnerDataSource = null;
        LinearLayout.LayoutParams paramsWrapContent;
        LinearLayout layoutObj;
        String destCol;
        String selectedOption;
        private int index;
        ProgressDialog dialogForSpinners;

        public ShowDataSources(Context context, View layout, LinearLayout rootQuestionLL, DDE_Questions dde_questions, String answer, String destColumnParent) {
            this.context = context;
            this.layout = layout;
            this.rootQuestionLL = rootQuestionLL;
            this.dde_questions = dde_questions;
            this.answer = answer;
            this.destColumnParent = destColumnParent;
            dialogForSpinners = new ProgressDialog(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!dialog.isShowing() && !dialogForSpinners.isShowing()) {
                dialogForSpinners = new ProgressDialog(DisplayQuestions.this);
                dialogForSpinners.setTitle("Preparing form");
//                dialogForSpinners.getWindow().setDimAmount(1f);
                dialogForSpinners.setCancelable(false);
                dialogForSpinners.show();
            }
            paramsWrapContent = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0);
            paramsWrapContent.setMargins(10, 0, 0, 0);
            layoutObj = renderAllQuestionsLayout.findViewWithTag(dde_questions.getQuestionId());
            if (layoutObj != null) {
                spinnerDataSource = (Spinner) layoutObj.getChildAt(1);
            } else {
                View queDSDropdownLayout = LayoutInflater.from(DisplayQuestions.this).inflate(R.layout.layout_spinner, rootQuestionLL, false);
//                final Spinner spinnerDropdown = queDSDropdownLayout.findViewById(R.id.sp_Answer);
                spinnerDataSource = queDSDropdownLayout.findViewById(R.id.sp_Answer);
//                spinnerDataSource.setBackground(ContextCompat.getDrawable(context, R.drawable.spinnerbg));
                rootQuestionLL.addView(spinnerDataSource);
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            String dataSourceQuestionIdentifier = dde_questions.getDataSourceQuestionIdentifier();
            //destCol = dde_questions.getDataSourceColumnName();
            destCol = appDatabase.getDDE_QuestionsDao().getDestColumnByQid(dataSourceQuestionIdentifier);
            answerList = new ArrayList<>();
            if (destCol == null) {
                if (dialogForSpinners.isShowing()) {
                    dialogForSpinners.dismiss();
                }

                Log.d("ErrordestCol", "destCol: Linked form or question might be deleted. Spinner will not load. Contact administrator.");
                return null;
            } else {
                try {
                    NavigableMap<String, String> map = new TreeMap<String, String>();

                    answerList.add("select options");
                    if (destColumnParent.isEmpty()) {
                        answerList = getDependentValues(answerList, destCol, null, null, map);
                    } else if (!answer.isEmpty() && !answer.equalsIgnoreCase("select options")) {
                        String dep = dde_questions.getDependentQuestionIdentifier();
                        String depTemp = null;
                        String depDSTemp;
                        do {
                            DDE_Questions tempQuestion;
                            for (int k = 0; k < formIdWiseQuestions.size(); k++) {
                                tempQuestion = formIdWiseQuestions.get(k);
                                if (tempQuestion.getQuestionId().equals(dep)) {
                                    depTemp = tempQuestion.getDependentQuestionIdentifier();
                                    depDSTemp = tempQuestion.getDataSourceQuestionIdentifier();
                                    dep = depTemp;
//                                    map.put(tempQuestion.getDataSourceColumnName(), tempQuestion.getAnswer());
                                    map.put(appDatabase.getDDE_QuestionsDao().getDestColumnByQid(depDSTemp), formIdWiseQuestions.get(k).getAnswer());
                                    break;
                                }
                            }
                        } while (depTemp != null);

                        answerList = getDependentValues(answerList, destCol, destColumnParent, answer, map);
                    }

                    Log.d("pkpkpk", "Size: " + answerList.size());

                    String tempformId = appDatabase.getDDE_QuestionsDao().getFormIdByQuestionID(dataSourceQuestionIdentifier);
                    List<AnswersSingleForm> forms = appDatabase.getAnswerDao().getAllAnswersByFormId(tempformId);
                    for (int formIndex = 0; formIndex < forms.size(); formIndex++) {
                        JsonArray jsonArray = forms.get(formIndex).getAnswerArrayOfSingleForm();
                        for (int jsonArrayIndex = 0; jsonArrayIndex < jsonArray.size(); jsonArrayIndex++) {
                            JsonObject jsonObject = jsonArray.get(jsonArrayIndex).getAsJsonObject();
                            if (!answer.equals("") && (!answer.equals("select options"))) {
                                if (jsonObject.get("DestColumnName").getAsString().equals(destColumnParent) && jsonObject.get("Answers").getAsString().equals(answer)) {
                                    for (int dep = 0; dep < jsonArray.size(); dep++) {
                                        JsonObject depJsonObj = jsonArray.get(dep).getAsJsonObject();
                                        if (depJsonObj.get("DestColumnName").getAsString().equals(destCol)) {
                                            answerList.add(depJsonObj.get("Answers").getAsString());
                                        }
                                    }
                                }
                            } else {
                                if (answer.equals("")) {
                                    if (jsonObject.get("DestColumnName").getAsString().equals(destCol)) {
                                        answerList.add(jsonObject.get("Answers").getAsString());
                                    }
                                }
                            }
                        }
                    }
                    if (dialogForSpinners.isShowing()) dialogForSpinners.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            List tempList = new ArrayList();
            answerList.remove("select options");
            tempList.addAll(new TreeSet(answerList));
            answerList.clear();
            answerList.addAll(tempList);
            answerList.add(0, "select options");

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (answerList.size() == 0) {
                if (!deletedAlertShown) {
                    List<String> dependingForm = appDatabase.getDDE_FormWiseDataSourceDao().getDistinctAllDSFormId(dde_questions.getFormId(), userId);
                    for (int depIndex = 0; depIndex < dependingForm.size(); depIndex++) {
                        if (depForms.isEmpty())
                            depForms = appDatabase.getDDE_FormsDao().getFormName(dependingForm.get(depIndex));
                        else
                            depForms = depForms + ", " + appDatabase.getDDE_FormsDao().getFormName(dependingForm.get(depIndex));
                    }
                    AlertDialog builder = new AlertDialog.Builder(DisplayQuestions.this).create();
                    if (depForms == null)
                        builder.setMessage(Html.fromHtml("Linked form or question might be deleted / not present / datasource linked improperly. Download <b> dependent </b> form(s) again."));
                    else
                        builder.setMessage(Html.fromHtml("Linked form or question might be deleted / not present / datasource linked improperly. Download <b>" + depForms + "</b> form(s) again."));
                    builder.setCancelable(false);
                    builder.setButton(DialogInterface.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
                   /* builder.setButton(DialogInterface.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });*/
                    builder.show();
                    deletedAlertShown = true;
//                    Toast.makeText(context, "Linked form or question might be deleted/not present/datasource linked improperly. Download " + depForms + " form(s) again.", Toast.LENGTH_LONG).show();
                }
                answerList.add("select options");
            }
            //Collections.sort(answerList);
            //answerList.add(0,"select options");
            ArrayAdapter<String> spinnerArrayAdapterDS = new ArrayAdapter<String>(context, android.R.layout.simple_selectable_list_item, answerList);
            spinnerDataSource.setAdapter(spinnerArrayAdapterDS);
//            spinnerDataSource.setLayoutParams(paramsWrapContent);
            if (editFormFlag) {
                String dest_column = dde_questions.getDestColumname();
                for (int ansObjIndex = 0; ansObjIndex < answerJsonArray.size(); ansObjIndex++) {
                    JsonObject ansObject = answerJsonArray.get(ansObjIndex).getAsJsonObject();
                    if (ansObject.get("DestColumnName").getAsString().equalsIgnoreCase(dest_column)) {
                        String ans = ansObject.get("Answers").getAsString();
                        int index = answerList.indexOf(ans);
                        if (index != -1) {
                            spinnerDataSource.setSelection(index);
                            dde_questions.setAnswer(ans);
                        }
                        break;
                    }
                }
            }
            spinnerDataSource.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    selectedOption = adapterView.getSelectedItem().toString();
                    if (selectedOption.equals("select options")) {
                        dde_questions.setAnswer("");
                        for (int depQueIndex = 0; depQueIndex < formIdWiseQuestions.size(); depQueIndex++) {
                            if (dde_questions.getQuestionId().equals(formIdWiseQuestions.get(depQueIndex).getDependentQuestionIdentifier())) {
                                index = depQueIndex;
                                new ShowDataSources(DisplayQuestions.this, layout, rootQuestionLL, formIdWiseQuestions.get(index), selectedOption, "onClick").execute();
                                //mHandler.sendEmptyMessage(0);
                            }
                        }
                    } else {
                        dde_questions.setAnswer(selectedOption);
                        for (int depQueIndex = 0; depQueIndex < formIdWiseQuestions.size(); depQueIndex++) {
                            if (dde_questions.getQuestionId().equals(formIdWiseQuestions.get(depQueIndex).getDependentQuestionIdentifier())) {
                                index = depQueIndex;
                                new ShowDataSources(DisplayQuestions.this, layout, rootQuestionLL, formIdWiseQuestions.get(index), selectedOption, destCol).execute();
                                //mHandler.sendEmptyMessage(1);
                            }
                        }
                    }
                    LinearLayout linearLayout = (LinearLayout) adapterView.getParent();
                    String tag = linearLayout.getTag().toString();
                    checkRuleCondition(tag, adapterView.getSelectedItem().toString(), "dropdown");
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
//            if (layoutObj != null) {
//                /* spinnerDataSource = (Spinner) layoutObj.getChildAt(1);*/
//            } else {
//                layout.addView(spinnerDataSource);
//            }
        }

        Handler mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        new ShowDataSources(DisplayQuestions.this, layout, rootQuestionLL, formIdWiseQuestions.get(index), selectedOption, "onClick").execute();
                        break;
                    case 1:
                        new ShowDataSources(DisplayQuestions.this, layout, rootQuestionLL, formIdWiseQuestions.get(index), selectedOption, destCol).execute();
                        break;
                }
                return false;
            }
        });
    }


/*
    private void showDataSource(final LinearLayout layout, final DDE_Questions dde_questions, String answer, String destColumnParent) {
        LinearLayout.LayoutParams paramsWrapContent = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0);
        paramsWrapContent.setMargins(10, 0, 0, 0);
        LinearLayout layoutObj = renderAllQuestionsLayout.findViewWithTag(dde_questions.getQuestionId());
        Spinner spinnerDataSource = null;
        if (layoutObj != null) {
            spinnerDataSource = (Spinner) layoutObj.getChildAt(1);
        } else {
            spinnerDataSource = new Spinner(this);
            spinnerDataSource.setBackground(ContextCompat.getDrawable(this, R.drawable.spinnerbg));
        }

        // Adding Data to dropdown list which is filled locally
        String dataSourceQuestionIdentifier = dde_questions.getDataSourceQuestionIdentifier();
        final String destCol = appDatabase.getDDE_QuestionsDao().getDestColumnByQid(dataSourceQuestionIdentifier);
        ArrayList<String> answerList = new ArrayList<>();
        try {
            answerList.add("select options");
            if (destColumnParent.isEmpty()) {
                answerList = getDependentValues(answerList, destCol, null, null);
            } else if (!answer.isEmpty() && !answer.equalsIgnoreCase("select options")) {
                answerList = getDependentValues(answerList, destCol, destColumnParent, answer);
            }

            Log.d("pkpkpk", "Size: " + answerList.size());

            formId = appDatabase.getDDE_QuestionsDao().getFormIdByQuestionID(dataSourceQuestionIdentifier);
            List<AnswersSingleForm> forms = appDatabase.getAnswerDao().getAllAnswersByFormId(formId);
            for (int formIndex = 0; formIndex < forms.size(); formIndex++) {
                JsonArray jsonArray = forms.get(formIndex).getAnswerArrayOfSingleForm();
                for (int jsonArrayIndex = 0; jsonArrayIndex < jsonArray.size(); jsonArrayIndex++) {
                    JsonObject jsonObject = jsonArray.get(jsonArrayIndex).getAsJsonObject();
                    if (!answer.equals("") && (!answer.equals("select options"))) {
                        if (jsonObject.get("DestColumnName").getAsString().equals(destColumnParent) && jsonObject.get("Answers").getAsString().equals(answer)) {
                            for (int dep = 0; dep < jsonArray.size(); dep++) {
                                JsonObject depJsonObj = jsonArray.get(dep).getAsJsonObject();
                                if (depJsonObj.get("DestColumnName").getAsString().equals(destCol)) {
                                    answerList.add(depJsonObj.get("Answers").getAsString());
                                }
                            }
                        }
                    } else {
                        if (answer.equals("")) {
                            if (jsonObject.get("DestColumnName").getAsString().equals(destCol)) {
                                answerList.add(jsonObject.get("Answers").getAsString());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        List tempList = new ArrayList();
        tempList.addAll(new LinkedHashSet(answerList));
        answerList.clear();
        answerList.addAll(tempList);
        ArrayAdapter<String> spinnerArrayAdapterDS = new ArrayAdapter<String>(this, android.R.layout.simple_selectable_list_item, answerList);
        spinnerDataSource.setAdapter(spinnerArrayAdapterDS);
        spinnerDataSource.setLayoutParams(paramsWrapContent);
        spinnerDataSource.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedOption = adapterView.getSelectedItem().toString();
                if (selectedOption.equals("select options")) {
                    dde_questions.setAnswer("");
                    for (int depQueIndex = 0; depQueIndex < formIdWiseQuestions.size(); depQueIndex++) {
                        if (dde_questions.getQuestionId().equals(formIdWiseQuestions.get(depQueIndex).getDependentQuestionIdentifier())) {
                            showDataSource(layout, formIdWiseQuestions.get(depQueIndex), selectedOption, "onClick");
                        }
                    }
                } else {
                    dde_questions.setAnswer(selectedOption);
                    for (int depQueIndex = 0; depQueIndex < formIdWiseQuestions.size(); depQueIndex++) {
                        if (dde_questions.getQuestionId().equals(formIdWiseQuestions.get(depQueIndex).getDependentQuestionIdentifier())) {
                            showDataSource(layout, formIdWiseQuestions.get(depQueIndex), selectedOption, destCol);
                        }
                    }
                }
                LinearLayout linearLayout = (LinearLayout) adapterView.getParent();
                String tag = linearLayout.getTag().toString();
                checkRuleCondition(tag, adapterView.getSelectedItem().toString(), "dropdown");
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        if (layoutObj != null) {
            */
    /* spinnerDataSource = (Spinner) layoutObj.getChildAt(1);*//*

        } else {
            layout.addView(spinnerDataSource);
        }

    }
*/

    private void checkRuleCondition(String tag, String ans, String queType) {
        try {
            for (int i = 0; i < allRules.size(); i++) {
                JsonArray questionCondition = allRules.get(i).getQuestionCondition();
                Set conditionId = new LinkedHashSet();
                for (int cntCondId = 0; cntCondId < questionCondition.size(); cntCondId++) {
                    JsonObject jsonObjectTemp = questionCondition.get(cntCondId).getAsJsonObject();
                    conditionId.add(jsonObjectTemp.get("ConditionId").getAsString());
                }
                for (int j = 0; j < questionCondition.size(); j++) {
                    JsonObject condition = questionCondition.get(j).getAsJsonObject();
                    String QuestionIdentifier = condition.get("QuestionIdentifier").getAsString();
                    String ConditionId = condition.get("ConditionId").getAsString();
                    if (QuestionIdentifier.equals(tag)) {
                        String answerFromJsonToMatch = "";
                        String ConditionType = condition.get("ConditionType").getAsString();
                        Set ConditionsSatisfied = allRules.get(i).getContionsSatisfied();
                        if (queType.equalsIgnoreCase("number")) {
                            if (!condition.get("SelectValueQuestion").isJsonNull()) {
                                answerFromJsonToMatch = condition.get("SelectValueQuestion").getAsString();
                            } else {
                                answerFromJsonToMatch = "";
                            }
                        } else {
                            JsonArray ja = condition.get("SelectValue").getAsJsonArray();
                            for (int cnt = 0; cnt < ja.size(); cnt++) {
                                answerFromJsonToMatch += ja.get(cnt).getAsString() + ",";
                            }
                        }

                        if (checkConditionType(ConditionType, ans, answerFromJsonToMatch, queType)) {
                            ConditionsSatisfied.add(ConditionId);
                        } else {
                            ConditionsSatisfied.remove(ConditionId);
                        }
                        String showQueTag = allRules.get(i).getShowQuestionIdentifier();
                        LinearLayout layout = renderAllQuestionsLayout.findViewWithTag(showQueTag);
                        if (layout != null) {
                            if (conditionMatch(allRules.get(i).getConditionsMatch(), conditionId.size(), ConditionsSatisfied.size())) {
                                ((CardView) layout.getParent()).setVisibility(View.VISIBLE);
//                                layout.setVisibility(View.VISIBLE);
                                DDE_Questions visibleTempQue = null;
                                for (int queCnt = 0; queCnt < formIdWiseQuestions.size(); queCnt++) {
                                    if (formIdWiseQuestions.get(queCnt).getQuestionId().equals(showQueTag)) {
                                        visibleTempQue = formIdWiseQuestions.get(queCnt);
                                        String defaultValue = visibleTempQue.getDefaultValue();
                                        if (defaultValue != null) {
                                            //setting default values
                                            switch (visibleTempQue.getQuestionType()) {
                                                case "singlechoice":
                                                    visibleTempQue.setAnswer(defaultValue);
                                                    RadioGroup tempGroup = (RadioGroup) layout.getChildAt(1);
                                                    for (int radioButtonIndex = 0; radioButtonIndex < tempGroup.getChildCount(); radioButtonIndex++) {
                                                        if (((RadioButton) tempGroup.getChildAt(radioButtonIndex)).getText().equals(defaultValue)) {
                                                            ((RadioButton) tempGroup.getChildAt(radioButtonIndex)).setChecked(true);
                                                        } else {
                                                            ((RadioButton) tempGroup.getChildAt(radioButtonIndex)).setChecked(false);
                                                        }
                                                    }
                                                    break;
                                                case "multiple":
                                                    visibleTempQue.setAnswer(defaultValue);
                                                    GridLayout tempGrid = (GridLayout) layout.getChildAt(1);
                                                    String defaultval = defaultValue;
                                                    if (!defaultval.isEmpty() && !defaultval.equalsIgnoreCase("null") && !defaultval.endsWith(","))
                                                        defaultval += ",";
                                                    for (int checkBoxIndex = 0; checkBoxIndex < tempGrid.getChildCount(); checkBoxIndex++) {
                                                        String checkBoxTag = tempGrid.getChildAt(checkBoxIndex).getTag().toString();
                                                        String[] splitted = checkBoxTag.split(":::");
                                                        if (defaultval.contains(splitted[1] + ",")) {
                                                            ((CheckBox) tempGrid.getChildAt(checkBoxIndex)).setChecked(true);
                                                        } else {
                                                            ((CheckBox) tempGrid.getChildAt(checkBoxIndex)).setChecked(false);
                                                        }
                                                    }
                                                    break;
                                                case "text":
                                                case "number":
                                                case "email":
                                                    visibleTempQue.setAnswer(defaultValue);
                                                    EditText tempEditText = (EditText) layout.getChildAt(1);
                                                    tempEditText.setText(defaultValue);
                                                    break;
                                                case "date":
                                                    if (!defaultValue.isEmpty() && parseDate(defaultValue) != null) {
                                                        visibleTempQue.setAnswer(defaultValue);
                                                        TextView tempDate = (TextView) layout.getChildAt(1);
                                                        tempDate.setText(parseDate(defaultValue));
                                                    }
                                                    break;
                                                case "time":
                                                    String defaultTime = defaultValue;
                                                    try {
                                                        final SimpleDateFormat sdf = new SimpleDateFormat("H:mm");
                                                        final Date dateObj;
                                                        dateObj = sdf.parse(defaultTime);
                                                        defaultTime = new SimpleDateFormat("K:mm aa").format(dateObj);
                                                        TextView tempTime = (TextView) layout.getChildAt(1);
                                                        tempTime.setText(defaultTime);
                                                        visibleTempQue.setAnswer(defaultTime);
                                                    } catch (ParseException e) {
                                                        e.printStackTrace();
                                                    }
                                                    break;
                                                case "dropdown":
                                                    Spinner spinner = (Spinner) layout.getChildAt(1);
                                                    spinner.setSelection(getIndex(spinner, defaultValue));
                                                    visibleTempQue.setAnswer(defaultValue);
                                                    break;
                                                case "video":
                                                    LinearLayout linearLayout = (LinearLayout) layout.getChildAt(1);
                                                    ImageView view = (ImageView) linearLayout.getChildAt(1);
                                                    String path = (String) view.getTag(R.id.path);
                                                    if (path != null) {
                                                        BitmapFactory.Options options = new BitmapFactory.Options();
                                                        options.inSampleSize = 1;
                                                        Bitmap thumb = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Images.Thumbnails.MICRO_KIND);
                                                        view.setImageBitmap(thumb);
                                                        visibleTempQue.setAnswer(view.getTag(R.id.name).toString());
                                                    }

                                            }
                                        }
                                    }
                                }
                                checkRuleCondition(showQueTag, visibleTempQue.getAnswer(), visibleTempQue.getQuestionType());
                            } else {
                                DDE_Questions hiddenTempQue = null;
                                for (int queCnt = 0; queCnt < formIdWiseQuestions.size(); queCnt++) {
                                    if (formIdWiseQuestions.get(queCnt).getQuestionId().equals(showQueTag)) {
                                        hiddenTempQue = formIdWiseQuestions.get(queCnt);
                                        hiddenTempQue.setAnswer("");
                                        break;
                                    }
                                }

                                ((CardView) layout.getParent()).setVisibility(View.GONE);
//                                layout.setVisibility(View.GONE);
                                try {
                                    View view = layout.getChildAt(1);
                                    if (view instanceof Spinner) {
                                        ((Spinner) view).setSelection(0);
                                    } else if (view instanceof RatingBar) {
                                        ((RatingBar) view).setRating(0.0f);
                                    } else if (view instanceof RadioGroup) {
                                        ((RadioGroup) view).clearCheck();
                                    } else if (view instanceof EditText) {
                                        ((EditText) view).setText("");
                                    } else if (view instanceof Button) {
                                        ((Button) view).setText("");
                                    } else if (view instanceof GridLayout) {
                                        GridLayout layTemp = ((GridLayout) view);
                                        int childCount = layTemp.getChildCount();
                                        for (int cntr = 0; cntr < childCount; cntr++) {
                                            View v = layTemp.getChildAt(i);
                                            if (v instanceof CheckBox)
                                                ((CheckBox) v).setChecked(false);
                                        }
                                    } else if (view instanceof HorizontalScrollView) {
                                        View ansView = ((HorizontalScrollView) view).getChildAt(0);
                                        if (ansView instanceof RadioGroup) {
                                            ((RadioGroup) ansView).clearCheck();
                                        }
                                    } else if (view instanceof LinearLayout) {
                                        View textView = ((LinearLayout) view).getChildAt(0);
                                        if (textView instanceof TextView) {
                                            ((TextView) textView).setText("");
                                        }
                                        View ansView = ((LinearLayout) view).getChildAt(1);
                                        if (ansView instanceof ImageView) {
                                            ((ImageView) ansView).setImageResource(android.R.color.transparent);
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                if (hiddenTempQue != null) {
                                    checkRuleCondition(showQueTag, "$$", hiddenTempQue.getQuestionType());
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getIndex(Spinner spinner, String string) {
        int index = 0;
        for (int i = 0; i < spinner.getCount(); i++) {
            DisplayValue displayValue = (DisplayValue) spinner.getItemAtPosition(i);
            if (displayValue.getValue().equals(string)) {
                index = i;
            }
        }
        return index;
    }

    private boolean conditionMatch(String conditionsMatch, int totalConditions, int attemptedQue) {
        boolean flag = false;
        switch (conditionsMatch) {
            case "All of the conditions match":
                flag = totalConditions == attemptedQue ? true : false;
                break;
            case "Any one of the conditions match":
                flag = attemptedQue > 0 ? true : false;
                break;
            default:
                Toast.makeText(this, "Problem with the rule conditions check rules again", Toast.LENGTH_SHORT).show();
                return false;

        }
        return flag;
    }

    private boolean checkConditionType(String conditionType, String ans, String answerFromJsonToMatch, String queType) {
        boolean flag = false;
        int answer = 0;
        int answerMatch = 0;
        String[] splittedAnswer = null;
        if (queType.equalsIgnoreCase("number")) {
            if (ans.equals("") || ans.equals("$$")) {
                return false;
            } else {
                answer = Integer.parseInt(ans);
            }
            answerMatch = Integer.parseInt(answerFromJsonToMatch);
        } else {
            if (answerFromJsonToMatch.startsWith("[")) {
                answerFromJsonToMatch = answerFromJsonToMatch.replace('[', ' ');
            }
            if (answerFromJsonToMatch.endsWith("]")) {
                answerFromJsonToMatch = answerFromJsonToMatch.replace(']', ' ');
            }
            splittedAnswer = answerFromJsonToMatch.trim().split(",");
        }
        switch (conditionType) {
            case "less than":
                flag = answer < answerMatch ? true : false;
                break;
            case "greater than":
                flag = answer > answerMatch ? true : false;
                break;
            case "equal to":
                flag = answer == answerMatch ? true : false;
                break;
            case "less than equal to":
                flag = answer <= answerMatch ? true : false;
                break;
            case "greater than equal to":
                flag = answer >= answerMatch ? true : false;
                break;
            case "match one":
                if (queType.equalsIgnoreCase("multiple")) {
                    /*ENTERED BY USER*/
                    String[] spittedGivenAns = ans.split(",");
                    for (int i = 0; i < splittedAnswer.length; i++) {
                        for (int j = 0; j < spittedGivenAns.length; j++) {
                            String answerTemp = splittedAnswer[i];
                            if (spittedGivenAns[j].equalsIgnoreCase(answerTemp)) {
                                flag = true;
                                break;
                            }
                        }
                        if (flag) break;
                    }
                } else {
                    for (int i = 0; i < splittedAnswer.length; i++) {
                        if (ans.equalsIgnoreCase(splittedAnswer[i])) {
                            flag = true;
                        }
                    }
                }
                break;
            case "match none":
                flag = true;

                    /*if (!firstRun && ans.equalsIgnoreCase("") && queType.equalsIgnoreCase("multiple"))
                        return false;
                    *//*if (ans.equalsIgnoreCase("") && !queType.equalsIgnoreCase("multiple")) {
                        return false;
                    }*/

                   /* if (ans.equalsIgnoreCase("") && queType.equalsIgnoreCase("multiple")) {
                        return true;
                    }*/

                if (queType.equalsIgnoreCase("multiple")) {
                    /*ENTERED BY USER*/
                    if (firstRun) return false;

                    if (!firstRun && ans.equals("$$")) return false;

                    if (!firstRun && ans.equals("")) return true;

                    String[] spittedGivenAns = ans.split(",");
                    for (int i = 0; i < splittedAnswer.length; i++) {
                        for (int j = 0; j < spittedGivenAns.length; j++) {
                            if (spittedGivenAns[j].equalsIgnoreCase(splittedAnswer[i])) {
                                flag = false;
                                break;
                            }
                        }
                        if (!flag) break;
                    }
                } else {
                    if (ans.equalsIgnoreCase("") || ans.equalsIgnoreCase("$$")) return false;

                    for (int i = 0; i < splittedAnswer.length; i++) {
                        if (ans.equalsIgnoreCase(splittedAnswer[i])) {
                            flag = false;
                        }
                    }
                }
                break;
        }
        return flag;
    }

    @Override
    public void onBackPressed() {
        onHomeButtonClick();
    }

    @OnClick(R.id.homeButton)
    public void onHomeButtonClick() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Alert");
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setMessage("All your changes to this form will be discarded. \n Do you want to continue?");

        alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alertDialogBuilder.show();
    }

    @OnClick(R.id.saveAllQuestions)
    public void save() {
        boolean queAttemptFlag = false;
        for (int queAns = 0; formIdWiseQuestions.size() > queAns; queAns++) {
            if (!formIdWiseQuestions.get(queAns).getAnswer().equals("")) {
                queAttemptFlag = true;
                break;
            }
        }
        if (queAttemptFlag) {
            if (checkValidations()) {
                final List<AnswerJSonArrays> answersList = new ArrayList<>();
                final AnswersSingleForm answersSingleForm = new AnswersSingleForm();
                answersSingleForm.setEntryId(entryID);
                answersSingleForm.setUserID(userId);
                answersSingleForm.setDate(Utility.getCurrentDateTime());
                answersSingleForm.setFormId(formId);
                answersSingleForm.setTableName(appDatabase.getDDE_FormsDao().getTableName(formId));
                AnswerJSonArrays answerJSonArrays;
                for (int ansIndex = 0; ansIndex < formIdWiseQuestions.size(); ansIndex++) {
                    answerJSonArrays = new AnswerJSonArrays();
                    String ansId = Utility.getUniqueID().toString();
                    answerJSonArrays.setAnswerId(ansId);
                    answerJSonArrays.setEntryId(entryID);
                    answerJSonArrays.setFormId(formId);
                    answerJSonArrays.setDestColumnName(formIdWiseQuestions.get(ansIndex).getDestColumname());
                    String ans = formIdWiseQuestions.get(ansIndex).getAnswer();
                    if (formIdWiseQuestions.get(ansIndex).getQuestionType().equalsIgnoreCase("multiple")) {
                        if (ans.endsWith(",")) ans = ans.substring(0, ans.length() - 1);
                        formIdWiseQuestions.get(ansIndex).setAnswer(ans);
                    }
                    if (formIdWiseQuestions.get(ansIndex).getQuestionType().equalsIgnoreCase("multipleimage")) {
                        if (ans.endsWith("|")) ans = ans.substring(0, ans.length() - 1);
                        formIdWiseQuestions.get(ansIndex).setAnswer(ans);
                    }
                    answerJSonArrays.setAnswers(ans);
                    answerJSonArrays.setTableName(appDatabase.getDDE_FormsDao().getTableName(formId));
                    answerJSonArrays.setTransactionId(entryID);
                    answersList.add(answerJSonArrays);
                }
                Gson gson = new Gson();
                JsonArray jsonArray = gson.fromJson(gson.toJson(answersList), JsonArray.class);
                answersSingleForm.setAnswerArrayOfSingleForm(jsonArray);

                preview = new previewFormDialog(this, formIdWiseQuestions, answersSingleForm);
                preview.show();

            }
        } else {
            Toast.makeText(this, "Attempt at least one question", Toast.LENGTH_SHORT).show();
        }
    }

    private void getTokenAndUpload() {
        /*UPLOAD TO SERVER*/
        User user = appDatabase.getUserDao().getUserDetailsById(userId);
        Utility.updateToken(user.getUserName(), user.getPassword(), UPLOAD, tokenListener, mContext, dialog);
    }

    private void upload(String token) {
        List tempAnswerList = new ArrayList();
        tempAnswerList.add(appDatabase.getAnswerDao().getAnswersByEntryId(entryID));
        new UploadAnswerAndImageToServer(this, tempAnswerList, token);
    }

    private boolean checkValidations() {
        try {
            for (int i = 0; i < formIdWiseQuestions.size(); i++) {
                JsonArray jsonArray = formIdWiseQuestions.get(i).getValidations();
                for (int j = 0; j < jsonArray.size(); j++) {
                    String queId = formIdWiseQuestions.get(i).getQuestionId();
                    JsonObject validationObject = jsonArray.get(j).getAsJsonObject();
                    if (validationObject.get("ValidationApply").getAsBoolean()) {
                        String ValidationValue = "";
                        String getQuestionType = formIdWiseQuestions.get(i).getQuestionType();
                        String validationName = validationObject.get("validationName").getAsString();
                        String ValidationType = validationObject.get("ValidationType").getAsString();

                        if ((!validationObject.get("ValidationValue").isJsonNull()) && (!("null".equals(validationObject.get("ValidationValue").toString())))) {
                            ValidationValue = validationObject.get("ValidationValue").getAsString();
                        } else {
                            Toast.makeText(this, "Validation values may be null or empty.. ", Toast.LENGTH_SHORT).show();
                            //return false;
                        }
                        String answerQQ = formIdWiseQuestions.get(i).getAnswer();
                        boolean flag = checkValue(queId, getQuestionType, validationName, ValidationType, ValidationValue, answerQQ);
                        if (!flag) {
                            final LinearLayout linearLayout = renderAllQuestionsLayout.findViewWithTag(queId);
                            linearLayout.getChildAt(1).requestFocus();
                            GradientDrawable border = new GradientDrawable();
                            border.setShape(GradientDrawable.RECTANGLE);
                            border.setStroke(3, Color.RED);
                            border.setCornerRadius(5);

                            linearLayout.setBackground(border);
                            /*Animation animation1 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade);
                            linearLayout.startAnimation(animation1);*/

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    linearLayout.setBackgroundResource(0);
                                }
                            }, 2000);

                            parentScroll.post(new Runnable() {
                                @Override
                                public void run() {
                                    linearLayout.getParent().requestChildFocus(linearLayout, linearLayout);
//                                    parentScroll.scrollTo(0, linearLayout.getTop());
                                }
                            });
                            return false;
                        }
                    }
                }
            }
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean checkValue(String questionId, String questionType, String validationName, String validationType, String validationValue, String answer) {
        try {
            if (((CardView) renderAllQuestionsLayout.findViewWithTag(questionId).getParent()).getVisibility() == View.VISIBLE) {
                if (validationValue.isEmpty())
                    return true;
                switch (validationName) {
                    case "REQUIRED":
                        // return answer != "" ? true : false;
                        if (answer.equals("")) {
                            Toast.makeText(this, "This Field Is Mandatory..", Toast.LENGTH_LONG).show();
                            return false;
                        }
                        return true;
                    case "MAXCHARACTERSALLOWED":
                    case "MAXLENGTH":
                        if (answer.length() > Integer.parseInt(validationValue)) {
                            Toast.makeText(this, "Maximum Characters Allowed " + Integer.parseInt(validationValue), Toast.LENGTH_LONG).show();
                            return false;
                        }
                        return true;
                    case "MINCHARACTERSALLOWED":
                    case "MINLENGTH":
                        if (answer.length() < Integer.parseInt(validationValue)) {
                            Toast.makeText(this, "Minimum Characters Required " + Integer.parseInt(validationValue), Toast.LENGTH_LONG).show();
                            return false;
                        }
                        return true;
                    case "MINRANGE":
                        switch (questionType) {
                            case "number":
                                if (answer.equals("")) {
                                    return true;
                                } else if (Integer.parseInt(answer) < Integer.parseInt(validationValue)) {
                                    Toast.makeText(this, "Minimum Value  Required" + Integer.parseInt(validationValue), Toast.LENGTH_LONG).show();
                                    return false;
                                }
                                return true;
                            case "date":
                                int minYear, minMonth, minDay;
                                try {
                                    if (answer.equals("")) {
                                        return true;
                                    } else {
                                        JSONObject mindateJsonObject = new JSONObject(validationValue);
                                        minYear = mindateJsonObject.getInt("year");
                                        minMonth = mindateJsonObject.getInt("month");
                                        minDay = mindateJsonObject.getInt("day");
                                        String minRange = "" + minYear + "/" + minMonth + "/" + minDay;

                                        Date minRangeDate = new SimpleDateFormat("yyyy/mm/dd").parse(minRange);
                                        Date dateAns = new SimpleDateFormat("yyyy/mm/dd").parse(answer);
                                        if (minRangeDate.compareTo(dateAns) > 0) {
                                            Toast.makeText(this, "Date Must Be After " + minRange, Toast.LENGTH_SHORT).show();
                                            return false;
                                        }
                                        return true;
                                    }
                                } catch (Exception e) {
                                    Toast.makeText(this, "Check Minimum date validations", Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                    return false;
                                }
                        }
                    case "MAXRANGE":
                        switch (questionType) {
                            case "number":
                                if (answer.equals("")) {
                                    return true;
                                } else if (Integer.parseInt(answer) > Integer.parseInt(validationValue)) {
                                    Toast.makeText(this, "Maximum Value Allowed " + Integer.parseInt(validationValue), Toast.LENGTH_LONG).show();
                                    return false;
                                }
                                return true;
                            case "date":
                                try {
                                    if (answer.equals("")) {
                                        return true;
                                    } else {
                                        int maxYear, maxMonth, maxDay;
                                        JSONObject maxdateJsonObject = new JSONObject(validationValue);
                                        maxYear = maxdateJsonObject.getInt("year");
                                        maxMonth = maxdateJsonObject.getInt("month");
                                        maxDay = maxdateJsonObject.getInt("day");

                                        String maxRange = "" + maxYear + "/" + maxMonth + "/" + maxDay;

                                        Date maxRangeDate = new SimpleDateFormat("yyyy/dd/mm").parse(maxRange);
                                        Date dateAns = new SimpleDateFormat("yyyy/dd/mm").parse(answer);
                                        if (maxRangeDate.compareTo(dateAns) < 0) {
                                            Toast.makeText(this, "Date Must Be Before " + maxRange, Toast.LENGTH_SHORT).show();
                                            return false;
                                        }
                                        return true;
                                    }
                                } catch (Exception e) {
                                    Toast.makeText(this, "Check Minimum date validations", Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                    return false;
                                }
                        }
                    case "DEPENDSON":
                        switch (questionType) {
                            case "number":
                                EditText ansOfDependentQue = renderAllQuestionsLayout.findViewWithTag("ans" + validationValue);
                                if (answer.equals("")) {
                                    return true;
                                } else if (ansOfDependentQue.getText().toString().equals("")) {
                                    Toast.makeText(this, "Answer of " + ((TextView) renderAllQuestionsLayout.findViewWithTag("que" + validationValue)).getText() + " is empty", Toast.LENGTH_SHORT).show();
                                    return false;
                                } else {
                                    int ans = Integer.parseInt(ansOfDependentQue.getText().toString());
                                    switch (validationType) {
                                        case "<":
                                            if ((Integer.parseInt(answer) >= ans)) {
                                                Toast.makeText(this, "Must be Smaller Than " + ((TextView) renderAllQuestionsLayout.findViewWithTag("que" + validationValue)).getText(), Toast.LENGTH_LONG).show();
                                                return false;
                                            }
                                            return true;
                                        case ">":
                                            if ((Integer.parseInt(answer) <= ans)) {
                                                Toast.makeText(this, "Must be Greater Than " + ((TextView) renderAllQuestionsLayout.findViewWithTag("que" + validationValue)).getText(), Toast.LENGTH_LONG).show();
                                                return false;
                                            }
                                            return true;
                                        case "=":
                                            if ((Integer.parseInt(answer) != ans)) {
                                                Toast.makeText(this, "Must be Equal To " + ((TextView) renderAllQuestionsLayout.findViewWithTag("que" + validationValue)).getText(), Toast.LENGTH_LONG).show();
                                                return false;
                                            }
                                            return true;
                                        case "<=":
                                            if ((Integer.parseInt(answer) > ans)) {
                                                Toast.makeText(this, "Must be Smaller Than Or Equal To" + ((TextView) renderAllQuestionsLayout.findViewWithTag("que" + validationValue)).getText(), Toast.LENGTH_LONG).show();
                                                return false;
                                            }
                                            return true;
                                        case ">=":
                                            if ((Integer.parseInt(answer) < ans)) {
                                                Toast.makeText(this, "Must be Greater Than Or Equal To" + ((TextView) renderAllQuestionsLayout.findViewWithTag("que" + validationValue)).getText(), Toast.LENGTH_LONG).show();
                                                return false;
                                            }
                                            return true;
                                        default:
                                            return true;
                                    }
                                }
                            case "date":
                                TextView dependentDate = renderAllQuestionsLayout.findViewWithTag("ans" + validationValue);
                                try {
                                    if (answer.equals("")) {
                                        return true;
                                    } else if (dependentDate.getText().toString().equals("Select Date")) {
                                        Toast.makeText(this, "Answer of " + ((TextView) renderAllQuestionsLayout.findViewWithTag("que" + validationValue)).getText() + " is empty", Toast.LENGTH_SHORT).show();
                                        return false;
                                    } else {
                                        Date dependentAns = new SimpleDateFormat("yyyy/MM/DD").parse(dependentDate.getText().toString());
                                        Date dependingAns = new SimpleDateFormat("yyyy/MM/DD").parse(answer);

                                        switch (validationType) {
                                            case "<":
                                                if (dependingAns.compareTo(dependentAns) >= 0) {
                                                    Toast.makeText(this, "Must be Before " + dependentDate.getText().toString(), Toast.LENGTH_LONG).show();
                                                    return false;
                                                }
                                                return true;
                                            case ">":
                                                if (dependingAns.compareTo(dependentAns) <= 0) {
                                                    Toast.makeText(this, "Must be After " + dependentDate.getText().toString(), Toast.LENGTH_LONG).show();
                                                    return false;
                                                }
                                                return true;
                                            case "=":
                                                if (dependingAns.compareTo(dependentAns) != 0) {
                                                    Toast.makeText(this, "Must be Equal to " + dependentDate.getText().toString(), Toast.LENGTH_LONG).show();
                                                    return false;
                                                }
                                                return true;
                                            case "<=":
                                                if (dependingAns.compareTo(dependentAns) > 0) {
                                                    Toast.makeText(this, "Must be Before Or equal TO " + dependentDate.getText().toString(), Toast.LENGTH_LONG).show();
                                                    return false;
                                                }
                                                return true;
                                            case ">=":
                                                if (dependingAns.compareTo(dependentAns) < 0) {
                                                    Toast.makeText(this, "Must be After Or equal TO " + dependentDate.getText().toString(), Toast.LENGTH_LONG).show();
                                                    return false;
                                                }
                                                return true;
                                            default:
                                                return true;
                                        }
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                    return false;
                                }

                            case "time":
                                TextView dependentTime = renderAllQuestionsLayout.findViewWithTag("ans" + validationValue);
                                try {
                                    if (answer.equals("")) {
                                        return true;
                                    } else if (dependentTime.getText().toString().equals("Select Time")) {
                                        Toast.makeText(this, "Answer of " + ((TextView) renderAllQuestionsLayout.findViewWithTag("que" + validationValue)).getText() + " is empty", Toast.LENGTH_SHORT).show();
                                        return false;
                                    } else {
                                        Date dependentAns = new SimpleDateFormat("hh:mm aa").parse(dependentTime.getText().toString());
                                        Date dependingAns = new SimpleDateFormat("hh:mm aa").parse(answer);


                                        switch (validationType) {
                                            case "<":
                                                if (dependingAns.compareTo(dependentAns) >= 0) {
                                                    Toast.makeText(this, "Time must be before " + dependentTime.getText().toString(), Toast.LENGTH_SHORT).show();
                                                    return false;
                                                }
                                                return true;
                                            case ">":
                                                if (dependingAns.compareTo(dependentAns) <= 0) {
                                                    Toast.makeText(this, "Time must be after " + dependentTime.getText().toString(), Toast.LENGTH_SHORT).show();
                                                    return false;
                                                }
                                                return true;
                                            case "=":
                                                if (dependingAns.compareTo(dependentAns) != 0) {
                                                    Toast.makeText(this, "Time must be equal to " + dependentTime.getText().toString(), Toast.LENGTH_SHORT).show();
                                                    return false;
                                                }
                                                return true;
                                            case "<=":
                                                if (dependingAns.compareTo(dependentAns) > 0) {
                                                    Toast.makeText(this, "Time must be before or equal to " + dependentTime.getText().toString(), Toast.LENGTH_SHORT).show();
                                                    return false;
                                                }
                                                return true;
                                            case ">=":
                                                if (dependingAns.compareTo(dependentAns) < 0) {
                                                    Toast.makeText(this, "Time must be after or equal to " + dependentTime.getText().toString(), Toast.LENGTH_SHORT).show();
                                                    return false;
                                                }
                                                return true;
                                            default:
                                                return true;
                                        }
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                    return false;
                                }
                        }
                    case "ALLOWDECIMAL":
                        int answerDecimal = Integer.parseInt(answer);
                        if (answerDecimal % 1 != 0) {
                            Toast.makeText(this, "Decimal values are not allowed" + ((TextView) renderAllQuestionsLayout.findViewWithTag("que" + validationValue)).getText(), Toast.LENGTH_LONG).show();
                            return false;
                        }
                        return true;
                    case "MINIMUMSELECT":
                        int min = Integer.parseInt(validationValue);
                        int count = 0;
                        for (int i = 0; i < checkBoxList.size(); i++) {
                            CheckBox checkBox = (CheckBox) checkBoxList.get(i);
                            if (checkBox.isChecked()) {
                                String[] queTag = checkBox.getTag().toString().split(":::");
                                if (queTag[0].equals(questionId)) {
                                    count++;
                                }
                            }
                        }
                        if (count < min) {
                            Toast.makeText(this, "Minimum Checkboxes Requered " + min, Toast.LENGTH_LONG).show();
                            return false;
                        }
                        return true;
                    case "MAXIMUMSELECT":
                        int max = Integer.parseInt(validationValue);
                        int count1 = 0;
                        for (int i = 0; i < checkBoxList.size(); i++) {
                            CheckBox checkBox = (CheckBox) checkBoxList.get(i);
                            if (checkBox.isChecked()) {
                                String[] queTag = checkBox.getTag().toString().split(":::");
                                if (queTag[0].equals(questionId)) {
                                    count1++;
                                }
                            }
                        }
                        if (count1 > max) {
                            Toast.makeText(this, "Maximum Checkboxes Can Be Selected " + max, Toast.LENGTH_LONG).show();
                            return false;
                        }
                        return true;
                    default:
                        return true;
                }
            } else return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public String parseDate(String json) {
        int Year, Month, Day;
        JSONObject dateJsonObject = null;
        try {
            dateJsonObject = new JSONObject(json);
            Year = dateJsonObject.getInt("year");
            Month = dateJsonObject.getInt("month") - 1;
            Day = dateJsonObject.getInt("day");
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return "" + Year + "/" + Month + "/" + Day;
    }


    class SortQuestions implements Comparator<DDE_Questions> {
        // Used for sorting in ascending order of
        public int compare(DDE_Questions a, DDE_Questions b) {
            return a.getFieldSeqNo() - b.getFieldSeqNo();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("codes", String.valueOf(requestCode) + resultCode);
        try {
            if (requestCode == PICK_IMAGE_FROM_GALLERY) {
                Uri selectedImage = data.getData();
                this.selectedImage.setImageURI(selectedImage);
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                createDirectoryAndSaveFile(bitmap, imageName);

            } else if (requestCode == CAPTURE_IMAGE) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                selectedImage.setImageBitmap(photo);
                // String selectedImagePath = getPath(photo);
                createDirectoryAndSaveFile(photo, imageName);
            } else if (requestCode == VIDEO_CAPTURE) {

                Uri selectedVideo = data.getData();
               /* this.selectedView.setVideoURI(selectedVideo);
                selectedView.setZOrderOnTop(true);
                selectedView.setZOrderMediaOverlay(true);*/

                AssetFileDescriptor videoAsset = getContentResolver().openAssetFileDescriptor(data.getData(), "r");
                FileInputStream in = videoAsset.createInputStream();
                final File dir = new File(videoPath + "/");
                if (!dir.exists()) {
                    dir.mkdir();
                }
                File fileName = new File(dir, videoName);
                if (fileName.exists()) fileName.delete();


                OutputStream out = new FileOutputStream(fileName);

                byte[] buf = new byte[1024];
                int len;

                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }

                in.close();
                out.close();

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 1;
                Bitmap thumb = ThumbnailUtils.createVideoThumbnail(dir + "/" + videoName, MediaStore.Images.Thumbnails.MICRO_KIND);
                selectedView.setImageBitmap(thumb);
                selectedView.setTag(R.id.path, dir + "/" + videoName);
                selectedView.setTag(R.id.name, videoName);
                selectedView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final Dialog dialog = new Dialog(DisplayQuestions.this);
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setContentView(R.layout.videoplayer);
                        dialog.show();
                        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        lp.copyFrom(dialog.getWindow().getAttributes());
                        dialog.getWindow().setAttributes(lp);
                        final VideoView videoview = (VideoView) dialog.findViewById(R.id.videoView);
                        videoview.setVideoPath(dir + "/" + videoName);
                        mediaController.setAnchorView(videoview);
                        videoview.setMediaController(mediaController);
                        videoview.start();
                    }
                });
            }
        } catch (Exception e) {
        }
    }

    private void createDirectoryAndSaveFile(Bitmap imageToSave, String fileName) {
        try {
            File direct = new File(path);
            if (!direct.exists()) direct.mkdir();

            File file = new File(direct, fileName);

            FileOutputStream out = new FileOutputStream(file);
            imageToSave.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void fillAgainForm(boolean value) {
        fillAgain();
    }

    public void fillAgain() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Do you want to fill this form again?");
        alertDialog.setCancelable(false);
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                Intent questionIntent = new Intent(DisplayQuestions.this, DisplayQuestions.class);
                DDE_Application.setCashedDataSourceEntriesOnline(dataSourceEntriesOnline);
                questionIntent.putExtra("fillAgainFlag", true);
                questionIntent.putExtra("formId", formId);
                questionIntent.putExtra("userId", String.valueOf(userId));
                questionIntent.putExtra("formEdit", "false");
                finish();
                startActivity(questionIntent);
            }
        });
        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                finish();
            }
        });
        alertDialog.show();
    }

    @Override
    public void updateToken(int methodToCall, String updatedToken) {
        switch (methodToCall) {
            case 1:
                // 1 : upload
                upload(updatedToken);
                break;
        }
    }


    public int getDp(int value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mScannerView != null)
            mScannerView.resumeCameraPreview(DisplayQuestions.this);
    }

   /* @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }*/
}