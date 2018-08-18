package pratham.dde.activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pratham.dde.BaseActivity;
import pratham.dde.R;
import pratham.dde.customViews.ChooseImageDialog;
import pratham.dde.domain.AnswerJSonArrays;
import pratham.dde.domain.AnswersSingleForm;
import pratham.dde.domain.DDE_Questions;
import pratham.dde.domain.DDE_RuleTable;
import pratham.dde.interfaces.FillAgainListner;
import pratham.dde.services.SyncUtility;
import pratham.dde.utils.DisplayValue;
import pratham.dde.utils.PermissionResult;
import pratham.dde.utils.PermissionUtils;
import pratham.dde.utils.UploadAnswerAndImageToServer;
import pratham.dde.utils.Utility;


public class DisplayQuestions extends BaseActivity implements FillAgainListner, PermissionResult {
    @BindView(R.id.homeButton)
    ImageView homeButton;
    @BindView(R.id.formNameHeader)
    TextView formNameHeader;
    @BindView(R.id.renderAllQuestions)
    LinearLayout renderAllQuestionsLayout;
    @BindView(R.id.parentScroll)
    ScrollView parentScroll;
    List depQueID;
    List<DDE_RuleTable> allRules;
    List<DDE_Questions> formIdWiseQuestions = new ArrayList<>();
    List checkBoxList;
    public static final int PICK_IMAGE_FROM_GALLERY = 1;
    public static final int CAPTURE_IMAGE = 0;
    ImageView selectedImage;
    static String userId;//logged UserId
    static String formId;
    boolean editFormFlag = false;
    String imageName = "";
    String entryID;
    JsonArray answerJsonArray;
    String path;
    boolean firstRun = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_questions);
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)) {
            String[] permissionArray = new String[]{PermissionUtils.Manifest_WRITE_EXTERNAL_STORAGE, PermissionUtils.Manifest_CAMERA};
            if (!isPermissionsGranted(DisplayQuestions.this, permissionArray)) {
                askCompactPermissions(permissionArray, this);
            } else proceedFurther();
        } else proceedFurther();
    }

    private void proceedFurther() {
        formId = getIntent().getStringExtra("formId");
        userId = getIntent().getStringExtra("userId");
        entryID = getIntent().getStringExtra("entryId");
        String formEdit = getIntent().getStringExtra("formEdit");
        path = Environment.getExternalStorageDirectory().toString() + "/DDEImages";
        if (formEdit.equals("true")) {
            editFormFlag = true;
        } else {
            editFormFlag = false;
        }
        ButterKnife.bind(this);
        checkBoxList = new ArrayList();
        String formName = appDatabase.getDDE_FormsDao().getFormName(formId);
        if (formName != null) {
            formNameHeader.setText(formName);
        }
        allRules = appDatabase.getDDE_RulesDao().getAllRules(formId);
        formIdWiseQuestions = appDatabase.getDDE_QuestionsDao().getFormIdWiseQuestions(formId);
        Collections.sort(formIdWiseQuestions, new Sortbyroll());

        /* SET VISIBILITY TO QUESTIONS */
        setVisibilityToQuestions(formId);


        if (editFormFlag) {
            AnswersSingleForm answersSingleForm = appDatabase.getAnswerDao().getAnswersByEntryId(entryID);
            answerJsonArray = answersSingleForm.getAnswerArrayOfSingleForm();
        } else {
            entryID = Utility.getUniqueID().toString();
        }
        for (int i = 0; i < formIdWiseQuestions.size(); i++) {
            displaySingleQue(formIdWiseQuestions.get(i));
        }
        //  if (editFormFlag) {
        for (int i = 0; i < formIdWiseQuestions.size(); i++) {
            DDE_Questions dde_que = formIdWiseQuestions.get(i);
            checkRuleCondition(dde_que.getQuestionId(), dde_que.getAnswer(), dde_que.getQuestionType());
        }
        firstRun = false;
        //   }
    }

    private void setVisibilityToQuestions(String formId) {
        depQueID = appDatabase.getDDE_RulesDao().getDependantQuestion(formId);
    }

    /*    Display A Single Questions One By One*/
    private void displaySingleQue(final DDE_Questions dde_questions) {
        final LinearLayout layout = new LinearLayout(this);
        layout.setPadding(10, 10, 10, 10);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setTag(dde_questions.getQuestionId());
        TextView textView = new TextView(this);
        textView.setText(/*dde_questions.getFieldSeqNo() + ". " + */dde_questions.getQuestion());
        textView.setTag("que" + dde_questions.getQuestionId());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25);
        textView.setTextColor(getResources().getColor(R.color.black));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0);
        params.setMargins(10, 0, 0, 0);
        textView.setLayoutParams(params);
        layout.addView(textView);

        LinearLayout.LayoutParams paramsWrapContaint = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0);
        paramsWrapContaint.setMargins(10, 0, 0, 0);

        /*SET DEFAULT VALUE TO ANSWWER FIELD*/
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
                final EditText editText = new EditText(this);
                editText.setBackground(ContextCompat.getDrawable(this, R.drawable.rectangular_box));
                editText.setPadding(15, 5, 5, 5);
                editText.setSingleLine(true);
                editText.setLayoutParams(params);
                layout.addView(editText);
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

            case "singlechoice":
                JsonArray option = dde_questions.getQuestionOption();
                RadioGroup radioGroup = new RadioGroup(this);
                radioGroup.setBackground(ContextCompat.getDrawable(this, R.drawable.rectangular_box));
                for (int i = 0; i < option.size(); i++) {
                    JsonElement jsonElement = option.get(i);
                    JsonObject jsonObject = jsonElement.getAsJsonObject();
                    RadioButton radioButton = new RadioButton(this);
                    radioButton.setId(i);
                    radioButton.setLayoutParams(paramsWrapContaint);
                    radioButton.setTag(jsonObject.get("value").getAsString());
                    String text = jsonObject.get("display").getAsString();
                    radioButton.setText(text);
                    if (editFormFlag) {
                        String dest_column = dde_questions.getDestColumname();
                        for (int ansObjIndex = 0; ansObjIndex < answerJsonArray.size(); ansObjIndex++) {
                            JsonObject ansObject = answerJsonArray.get(ansObjIndex).getAsJsonObject();
                            if (ansObject.get("DestColumnName").getAsString().equalsIgnoreCase(dest_column)) {
                                String ans = ansObject.get("Answers").getAsString();
                                if (ans.equalsIgnoreCase(text)) {
                                    radioButton.setChecked(true);
                                    dde_questions.setAnswer(ans);
                                }
                            }
                        }
                    } else {
                        if (validationValue.equalsIgnoreCase(text)) {
                            radioButton.setChecked(true);
                            dde_questions.setAnswer(validationValue);
                        }
                    }
                    radioGroup.addView(radioButton);
                    radioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean isSelected) {
                            if (isSelected) {
                                dde_questions.setAnswer(compoundButton.getText().toString());
                                LinearLayout layout = (LinearLayout) compoundButton.getParent().getParent();
                                String tag = (String) layout.getTag();
                                checkRuleCondition(tag, compoundButton.getTag().toString(), "singlechoice");
                            }
                        }
                    });
                }
                radioGroup.setLayoutParams(params);
                layout.addView(radioGroup);
                break;

            case "email":
                final EditText et_email = new EditText(this);
                et_email.setBackground(ContextCompat.getDrawable(this, R.drawable.rectangular_box));
                et_email.setPadding(15, 5, 5, 5);
                et_email.setSingleLine(true);
                et_email.setLayoutParams(params);
                layout.addView(et_email);
                et_email.setText(validationValue);
                if (editFormFlag) {
                    String dest_column = dde_questions.getDestColumname();
                    for (int ansObjIndex = 0; ansObjIndex < answerJsonArray.size(); ansObjIndex++) {
                        JsonObject ansObject = answerJsonArray.get(ansObjIndex).getAsJsonObject();
                        if (ansObject.get("DestColumnName").getAsString().equalsIgnoreCase(dest_column)) {
                            String ans = ansObject.get("Answers").getAsString();
                            et_email.setText(ans);
                            dde_questions.setAnswer(ans);
                        }
                    }
                } else {
                    et_email.setText(validationValue);
                    dde_questions.setAnswer(validationValue);
                }
                et_email.addTextChangedListener(new TextWatcher() {

                    // the user's changes are saved here
                    public void onTextChanged(CharSequence c, int start, int before, int count) {
                        //  mCrime.setTitle(c.toString());
                    }

                    public void beforeTextChanged(CharSequence c, int start, int count, int after) {
                        // this space intentionally left blank
                    }

                    public void afterTextChanged(Editable c) {
                        dde_questions.setAnswer(c.toString());
                        /*LinearLayout layout = (LinearLayout) et_email.getParent();
                        String tag = (String) layout.getTag();
                        //  checkRuleCondition(tag, c.toString());*/
                    }
                });
                break;

            case "number":
                final EditText number = new EditText(this);
                number.setBackground(ContextCompat.getDrawable(this, R.drawable.rectangular_box));
                number.setPadding(15, 5, 5, 5);

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
                number.setLayoutParams(params);
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
                layout.addView(number);
                break;

            case "multiple":
                String ans = "";
                JsonArray optionCheckBox = dde_questions.getQuestionOption();
                GridLayout gridLayout = new GridLayout(this);
                gridLayout.setBackground(ContextCompat.getDrawable(this, R.drawable.rectangular_box));
                gridLayout.setColumnCount(3);
                if (!validationValue.endsWith(",")) validationValue += ",";

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
                    if (!validationValue.endsWith(",")) validationValue += ",";
                    dde_questions.setAnswer(validationValue);
                }
                for (int i = 0; i < optionCheckBox.size(); i++) {
                    final CheckBox checkBox = new CheckBox(this);
                    checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean isSelected) {
                            String selectedAnswers = dde_questions.getAnswer();
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
                            String queParent = layout.getTag().toString();
                            checkRuleCondition(queParent, selectedAnswers, "multiple");
                        }
                    });
                    JsonElement jsonElement = optionCheckBox.get(i);
                    JsonObject jsonObject = jsonElement.getAsJsonObject();
                    checkBox.setTag(dde_questions.getQuestionId() + ":::" + jsonObject.get("value").getAsString());
                    String text = jsonObject.get("display").getAsString();
                    checkBox.setText(text);
                    if (editFormFlag) {
                        if (ans.contains(text + ",")) {
                            checkBox.setChecked(true);
                            dde_questions.setAnswer(ans);
                        }
                    } else {
                        if (validationValue.contains(text + ",")) {
                            checkBox.setChecked(true);
                            dde_questions.setAnswer(validationValue);
                        }
                    }
                    checkBoxList.add(checkBox);
                    GridLayout.LayoutParams paramGrid = new GridLayout.LayoutParams();
                    paramGrid.height = GridLayout.LayoutParams.WRAP_CONTENT;
                    paramGrid.width = GridLayout.LayoutParams.WRAP_CONTENT;
                    paramGrid.setGravity(Gravity.FILL_HORIZONTAL);
                    checkBox.setLayoutParams(paramGrid);
                    gridLayout.addView(checkBox);
                }

                layout.addView(gridLayout);

                break;

            case "image":
                LinearLayout outerLinearLayout = new LinearLayout(this);
                outerLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
                final TextView tv_img = new TextView(this);
                tv_img.setPadding(5, 5, 5, 5);
                tv_img.setBackground(ContextCompat.getDrawable(this, R.drawable.rectangular_box));
                tv_img.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
                tv_img.setText("Select Image");
                outerLinearLayout.addView(tv_img);
                final ImageView selectedImageTemp = new ImageView(this);
                // selectedImage.setLayoutParams(new android.view.ViewGroup.LayoutParams(150, 150));
                selectedImageTemp.setBackground(ContextCompat.getDrawable(this, R.drawable.rectangular_box));
                selectedImageTemp.setPadding(10, 5, 5, 5);
                LinearLayout.LayoutParams buttonLayoutParams = new LinearLayout.LayoutParams(200, 200);
                buttonLayoutParams.setMargins(50, 0, 0, 0);
                selectedImageTemp.setLayoutParams(buttonLayoutParams);
                outerLinearLayout.addView(selectedImageTemp);

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
                layout.addView(outerLinearLayout);

                tv_img.setOnClickListener(new View.OnClickListener() {
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

                        chooseImageDialog.btn_choose_from_gallery.setOnClickListener(new View.OnClickListener()

                        {
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
                final TextView date = new TextView(this);
                date.setPadding(10, 5, 5, 5);
                date.setBackground(ContextCompat.getDrawable(this, R.drawable.rectangular_box));
                date.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
                date.setText("Select Date");
                date.setTag("ans" + dde_questions.getQuestionId());
                date.setLayoutParams(paramsWrapContaint);
                layout.addView(date);
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
                    if (parseDate(validationValue) != null) {
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
                final TextView time = new TextView(this);
                time.setPadding(10, 5, 5, 5);
                time.setBackground(ContextCompat.getDrawable(this, R.drawable.rectangular_box));
                time.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
                time.setText("Select Time");
                time.setLayoutParams(paramsWrapContaint);
                time.setTag("ans" + dde_questions.getQuestionId());
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
                        final SimpleDateFormat sdf = new SimpleDateFormat("H:mm");
                        final Date dateObj = sdf.parse(validationValue);
                        text = new SimpleDateFormat("K:mm aa").format(dateObj);
                        time.setText(text);
                        dde_questions.setAnswer(text);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                layout.addView(time);
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
                List<DisplayValue> display = new ArrayList();
                // List value = new ArrayList();
                Spinner spinnerDropdown = new Spinner(this);
                spinnerDropdown.setBackground(ContextCompat.getDrawable(this, R.drawable.spinnerbg));
                JsonArray optionDropDown = dde_questions.getQuestionOption();
                display.add(new DisplayValue("select option","select option"));
                for (int i = 0; i < optionDropDown.size(); i++) {
                    JsonElement jsonElement = optionDropDown.get(i);
                    JsonObject jsonObject = jsonElement.getAsJsonObject();
                    display.add(new DisplayValue(jsonObject.get("display").getAsString(), jsonObject.get("value").getAsString()));
                }
                ArrayAdapter<DisplayValue> spinnerArrayAdapter = new ArrayAdapter<DisplayValue>(this, android.R.layout.simple_selectable_list_item, display);
                spinnerDropdown.setAdapter(spinnerArrayAdapter);
                spinnerDropdown.setLayoutParams(paramsWrapContaint);
                if (editFormFlag) {
                    String dest_column = dde_questions.getDestColumname();
                    for (int ansObjIndex = 0; ansObjIndex < answerJsonArray.size(); ansObjIndex++) {
                        JsonObject ansObject = answerJsonArray.get(ansObjIndex).getAsJsonObject();
                        if (ansObject.get("DestColumnName").getAsString().equalsIgnoreCase(dest_column)) {
                            ans = ansObject.get("Answers").getAsString();
                            int index = display.indexOf(ans);
                            if (index != -1) {
                                spinnerDropdown.setSelection(index);
                                dde_questions.setAnswer(ans);
                            }
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
                            dde_questions.setAnswer(adapterView.getSelectedItem().toString());
                            checkRuleCondition(tag, ((DisplayValue) adapterView.getSelectedItem()).getValue(), "dropdown");
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });
                layout.addView(spinnerDropdown);
                break;

            case "datasourcelist":
                showDataSource(layout, dde_questions, "", "");
                break;
        }
        renderAllQuestionsLayout.addView(layout);
        /*check dependency if depends then hide*/
        if (depQueID.contains(dde_questions.getQuestionId()))
        {
            layout.setVisibility(View.GONE);
        }
    }

    private void showDataSource(final LinearLayout layout, final DDE_Questions dde_questions, String answer, String destColumnParent) {

        LinearLayout.LayoutParams paramsWrapContent = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0);
        paramsWrapContent.setMargins(10, 0, 0, 0);
        LinearLayout layoutObj = renderAllQuestionsLayout.findViewWithTag(dde_questions.getQuestionId());
        Spinner spinnerDataSource = null;
        if (layoutObj != null) {
            spinnerDataSource = (Spinner) layoutObj.getChildAt(1);
        } else {
            spinnerDataSource = new Spinner(this);
        }
        //spinnerDataSource.setBackground(ContextCompat.getDrawable(this, R.drawable.rectangular_box));
        spinnerDataSource.setBackground(ContextCompat.getDrawable(this, R.drawable.spinnerbg));

        List answerList = new ArrayList();
        answerList.add("select options");
        String dataSourceQuestionIdentifier = dde_questions.getDataSourceQuestionIdentifier();
        //final String dependentQuestionIdentifier = dde_questions.getDependentQuestionIdentifier();
        /* FORMID,destination, of which cointains dataSourceQuestionIdentifier question */
        String formId = appDatabase.getDDE_QuestionsDao().getFormIdByQuestionID(dataSourceQuestionIdentifier);
        final String destCol = appDatabase.getDDE_QuestionsDao().getDestColumnByQid(dataSourceQuestionIdentifier);
        /* getting all forms from answer table */
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
                            showDataSource(layout, formIdWiseQuestions.get(depQueIndex), selectedOption, "");
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
            /* spinnerDataSource = (Spinner) layoutObj.getChildAt(1);*/
        } else {
            layout.addView(spinnerDataSource);
        }

    }

    private void checkRuleCondition(String tag, String ans, String queType) {
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
                            layout.setVisibility(View.VISIBLE);
                            DDE_Questions visibleTempQue = null;
                            for (int queCnt = 0; queCnt < formIdWiseQuestions.size(); queCnt++) {
                                if (formIdWiseQuestions.get(queCnt).getQuestionId().equals(showQueTag)) {
                                    visibleTempQue = formIdWiseQuestions.get(queCnt);
                                    switch (visibleTempQue.getQuestionType()) {
                                        case "singlechoice":
                                            visibleTempQue.setAnswer(visibleTempQue.getDefaultValue());
                                            RadioGroup tempGroup = (RadioGroup) layout.getChildAt(1);
                                            for (int radioButtonIndex = 0; radioButtonIndex < tempGroup.getChildCount(); radioButtonIndex++) {
                                                if (((RadioButton) tempGroup.getChildAt(radioButtonIndex)).getText().equals(visibleTempQue.getDefaultValue())) {
                                                    ((RadioButton) tempGroup.getChildAt(radioButtonIndex)).setChecked(true);
                                                } else {
                                                    ((RadioButton) tempGroup.getChildAt(radioButtonIndex)).setChecked(false);
                                                }
                                            }
                                            break;
                                        case "multiple":
                                            visibleTempQue.setAnswer(visibleTempQue.getDefaultValue());
                                            GridLayout tempGrid = (GridLayout) layout.getChildAt(1);
                                            String defaultval = visibleTempQue.getDefaultValue();
                                            if (!defaultval.endsWith(",")) defaultval += ",";
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
                                            visibleTempQue.setAnswer(visibleTempQue.getDefaultValue());
                                            EditText tempEditText = (EditText) layout.getChildAt(1);
                                            tempEditText.setText(visibleTempQue.getDefaultValue());
                                            break;
                                        case "number":
                                            visibleTempQue.setAnswer(visibleTempQue.getDefaultValue());
                                            EditText tempEditNumber = (EditText) layout.getChildAt(1);
                                            tempEditNumber.setText(visibleTempQue.getDefaultValue());
                                            break;
                                        case "email":
                                            visibleTempQue.setAnswer(visibleTempQue.getDefaultValue());
                                            EditText tempEditEmail = (EditText) layout.getChildAt(1);
                                            tempEditEmail.setText(visibleTempQue.getDefaultValue());
                                            break;

                                        case "date":
                                            String defaultDate = visibleTempQue.getDefaultValue();
                                            if (parseDate(defaultDate) != null) {
                                                visibleTempQue.setAnswer(visibleTempQue.getDefaultValue());
                                                TextView tempDate = (TextView) layout.getChildAt(1);
                                                tempDate.setText(parseDate(defaultDate));
                                            }
                                            break;
                                        case "time":
                                            String defaultTime = visibleTempQue.getDefaultValue();
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
                                            spinner.setSelection(getIndex(spinner, visibleTempQue.getDefaultValue()));
                                            visibleTempQue.setAnswer(visibleTempQue.getDefaultValue());
                                            break;
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

                            layout.setVisibility(View.GONE);
                            View view = layout.getChildAt(1);
                            layout.getTag();
                            if (view instanceof EditText) {
                                ((EditText) view).setText("");
                            } else if (view instanceof LinearLayout) {
                                View ansView = ((LinearLayout) view).getChildAt(1);
                                if (ansView instanceof ImageView) {
                                    ((ImageView) ansView).setImageResource(android.R.color.transparent);
                                }
                            }
                            if (hiddenTempQue != null) {
                                checkRuleCondition(showQueTag, "$$", hiddenTempQue.getQuestionType());
                            }
                        }
                    }
                }
            }
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
                        if (firstRun)
                            return false;

                        if (!firstRun && ans.equals("$$"))
                            return false;

                        if (!firstRun && ans.equals(""))
                            return true;

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
                        if (ans.equalsIgnoreCase("") || ans.equalsIgnoreCase("$$"))
                            return false;

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
                    }
                    answerJSonArrays.setAnswers(ans);
                    answerJSonArrays.setTableName(appDatabase.getDDE_FormsDao().getTableName(formId));
                    answerJSonArrays.setTransactionId(entryID);
                    answersList.add(answerJSonArrays);
                }
                Gson gson = new Gson();
                JsonArray jsonArray = gson.fromJson(gson.toJson(answersList), JsonArray.class);
                answersSingleForm.setAnswerArrayOfSingleForm(jsonArray);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setTitle("Alert");
                alertDialogBuilder.setMessage("Do you want to upload form to server?");

                alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        /*UPLOAD TO SERVER*/
                        dialog.dismiss();
                        appDatabase.getAnswerDao().insertAnswer(answersSingleForm);
                        if (SyncUtility.isDataConnectionAvailable(DisplayQuestions.this)) {
                            upload();
                        } else {
                            Toast.makeText(DisplayQuestions.this, "CHECK INTERNET CONNECTION", Toast.LENGTH_LONG).show();
                        }

                    }
                });

                alertDialogBuilder.setNegativeButton("Save Locally", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        appDatabase.getAnswerDao().insertAnswer(answersSingleForm);
                        fillAgain();
                    }
                });
                alertDialogBuilder.show();
            }
        } else {
            Toast.makeText(this, "Attempt at least one question", Toast.LENGTH_SHORT).show();
        }
    }

    private void upload() {
        /*UPLOAD TO SERVER*/
        String token = appDatabase.getUserDao().getUserTokenByUserID(userId);
        List tempAnswerList = new ArrayList();
        tempAnswerList.add(appDatabase.getAnswerDao().getAnswersByEntryId(entryID));
        new UploadAnswerAndImageToServer(this, tempAnswerList, token);
    }

    private boolean checkValidations() {
        try {
            for (int i = 0; i < formIdWiseQuestions.size(); i++) {
                JsonArray jsonArray = formIdWiseQuestions.get(i).getValidations();
                for (int j = 1; j < jsonArray.size(); j++) {
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
                                    parentScroll.scrollTo(0, linearLayout.getTop());
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

    @Override
    public void permissionGranted() {
        proceedFurther();
    }

    @Override
    public void permissionDenied() {
        showPermissionWarningDilog();
    }

    @Override
    public void permissionForeverDenied() {
        showPermissionWarningDilog();
    }


    class Sortbyroll implements Comparator<DDE_Questions> {
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
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                Intent questionIntent = new Intent(DisplayQuestions.this, DisplayQuestions.class);
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

    private void showPermissionWarningDilog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Alert");
        alertDialogBuilder.setMessage("Denying the permissions may cause in application failure." + "\nPermissions can also be given through app settings.");

        alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                /*UPLOAD TO SERVER*/
                dialog.dismiss();
                proceedFurther();
            }
        });
        alertDialogBuilder.show();
    }


}