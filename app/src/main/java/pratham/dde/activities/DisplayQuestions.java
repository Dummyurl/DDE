package pratham.dde.activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
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
import pratham.dde.R;
import pratham.dde.customViews.ChooseImageDialog;
import pratham.dde.domain.DDE_Questions;
import pratham.dde.domain.DDE_RuleTable;

import static pratham.dde.BaseActivity.appDatabase;

public class DisplayQuestions extends AppCompatActivity {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_questions);
        String formId = getIntent().getStringExtra("formId");
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

        for (int i = 0; i < formIdWiseQuestions.size(); i++) {
            displaySingleQue(formIdWiseQuestions.get(i));
        }
    }

    private void setVisibilityToQuestions(String formId) {
        depQueID = appDatabase.getDDE_RulesDao().getDependantQuestion(formId);
    }

    /*    Display A Single Questions One By One*/
    private void displaySingleQue(final DDE_Questions dde_questions) {
        LinearLayout layout = new LinearLayout(this);
        layout.setPadding(10, 10, 10, 10);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setTag(dde_questions.getQuestionId());
        TextView textView = new TextView(this);
        textView.setText(/*dde_questions.getFieldSeqNo() + ". " + */dde_questions.getQuestion());
        textView.setTag("que" + dde_questions.getQuestionId());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25);
        textView.setTextColor(getResources().getColor(R.color.colorAccentDark));
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
                editText.setText(validationValue);
                editText.setLayoutParams(params);
                layout.addView(editText);
                dde_questions.setAnswer(validationValue);
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
                        LinearLayout layout = (LinearLayout) editText.getParent();
                        String tag = (String) layout.getTag();
                        //checkRuleCondion(tag, c.toString());
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
                    final RadioButton radioButton = new RadioButton(this);
                    radioButton.setLayoutParams(paramsWrapContaint);
                    radioButton.setTag(jsonObject.get("value").getAsString());
                    String text = jsonObject.get("display").getAsString();
                    radioButton.setText(text);
                    if (validationValue.equalsIgnoreCase(text)) {
                        radioButton.setChecked(true);
                        dde_questions.setAnswer(validationValue);
                    }
                    radioGroup.addView(radioButton);
                    radioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            if (b) {
                                dde_questions.setAnswer(compoundButton.getText().toString());
                                LinearLayout layout = (LinearLayout) compoundButton.getParent().getParent();
                                String tag = (String) layout.getTag();
                                checkRuleCondion(tag, compoundButton.getTag().toString(), "singlechoice");
                            }
                        }
                    });
                  /*  radioButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String answerRadio = "";
                            if (view.isSelected()) {
                                view.setSelected(false);
                                answerRadio = "";
                            } else {
                                answerRadio = ((RadioButton) view).getText().toString();
                            }
                            dde_questions.setAnswer(answerRadio);
                            LinearLayout layout = (LinearLayout) view.getParent().getParent();
                            String tag = (String) layout.getTag();
                            checkRuleCondion(tag, view.getTag().toString(), "singlechoice");
                        }
                    });
*/
                }
                radioGroup.setLayoutParams(params);
                layout.addView(radioGroup);
                break;

            case "email":
                final EditText et_email = new EditText(this);
                et_email.setBackground(ContextCompat.getDrawable(this, R.drawable.rectangular_box));
                et_email.setPadding(15, 5, 5, 5);
                et_email.setSingleLine(true);
                et_email.setText(validationValue);
                et_email.setLayoutParams(params);
                layout.addView(et_email);
                dde_questions.setAnswer(validationValue);
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
                        LinearLayout layout = (LinearLayout) et_email.getParent();
                        String tag = (String) layout.getTag();
                        //  checkRuleCondion(tag, c.toString());
                    }
                });
                break;

            case "number":
                final EditText number = new EditText(this);
                number.setBackground(ContextCompat.getDrawable(this, R.drawable.rectangular_box));

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
                number.setText(validationValue);
                number.setLayoutParams(params);
                number.setTag("ans" + dde_questions.getQuestionId());
                dde_questions.setAnswer(validationValue);
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
                        checkRuleCondion(tag, c.toString(), "number");
                    }
                });
                layout.addView(number);
                break;

            case "multiple":
                JsonArray optionCheckBox = dde_questions.getQuestionOption();
                GridLayout gridLayout = new GridLayout(this);
                gridLayout.setBackground(ContextCompat.getDrawable(this, R.drawable.rectangular_box));
                gridLayout.setColumnCount(3);
                for (int i = 0; i < optionCheckBox.size(); i++) {
                    CheckBox checkBox = new CheckBox(this);
                    checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean isSelected) {
                            String selectedAnswers = dde_questions.getAnswer();
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
                            String queParent = ((LinearLayout) compoundButton.getParent().getParent()).getTag().toString();
                            checkRuleCondion(queParent, selectedAnswers, "multiple");

                        }
                    });
                    JsonElement jsonElement = optionCheckBox.get(i);
                    JsonObject jsonObject = jsonElement.getAsJsonObject();
                    checkBox.setTag(dde_questions.getQuestionId() + ":::" + jsonObject.get("value").getAsString());
                    String text = jsonObject.get("display").getAsString();
                    checkBox.setText(text);
                    if (validationValue.contains(text)) {
                        checkBox.setChecked(true);
                        dde_questions.setAnswer(text + ",");
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
                final TextView img = new TextView(this);
                img.setPadding(10, 5, 5, 5);
                img.setBackground(ContextCompat.getDrawable(this, R.drawable.rectangular_box));
                img.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
                img.setText("Select Image");

                img.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final ChooseImageDialog chooseImageDialog = new ChooseImageDialog(DisplayQuestions.this);
                        chooseImageDialog.btn_take_photo.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                chooseImageDialog.cancel();
                                Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                startActivityForResult(takePicture, 0);
                            }
                        });

                        chooseImageDialog.btn_choose_from_gallery.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                chooseImageDialog.cancel();
                                Intent intent = new Intent();
                                intent.setType("image/");
                                intent.setAction(Intent.ACTION_GET_CONTENT);
                                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_FROM_GALLERY);
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
                if (parseDate(validationValue) != null) {
                    date.setText(parseDate(validationValue));
                    dde_questions.setAnswer(validationValue);
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
                                Toast.makeText(DisplayQuestions.this, "Date changed", Toast.LENGTH_SHORT).show();
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
                try {
                    final SimpleDateFormat sdf = new SimpleDateFormat("H:mm");
                    final Date dateObj = sdf.parse(validationValue);
                    text = new SimpleDateFormat("K:mm aa").format(dateObj);
                    time.setText(text);
                    dde_questions.setAnswer(text);
                } catch (ParseException e) {
                    e.printStackTrace();
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
                List display = new ArrayList();
                List value = new ArrayList();
                Spinner spinnerDropdown = new Spinner(this);
                spinnerDropdown.setBackground(ContextCompat.getDrawable(this, R.drawable.rectangular_box));
                JsonArray optionDropDown = dde_questions.getQuestionOption();
                value.add("select option");
                display.add("select option");
                for (int i = 0; i < optionDropDown.size(); i++) {
                    JsonElement jsonElement = optionDropDown.get(i);
                    JsonObject jsonObject = jsonElement.getAsJsonObject();
                    value.add(jsonObject.get("value").getAsString());
                    display.add(jsonObject.get("display").getAsString());
                }
                ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_selectable_list_item, display);
                spinnerDropdown.setAdapter(spinnerArrayAdapter);
                spinnerDropdown.setLayoutParams(paramsWrapContaint);
                int index = display.indexOf(validationValue);
                if (index != -1) {
                    spinnerDropdown.setSelection(index);
                    dde_questions.setAnswer(validationValue);
                }
                spinnerDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        if (adapterView.getSelectedItem().toString().equals("select option")) {
                            dde_questions.setAnswer("");
                        } else {
                            dde_questions.setAnswer(adapterView.getSelectedItem().toString());
                        }
                        LinearLayout linearLayout = (LinearLayout) adapterView.getParent();
                        String tag = linearLayout.getTag().toString();
                        checkRuleCondion(tag, adapterView.getSelectedItem().toString(), "dropdown");

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });
                layout.addView(spinnerDropdown);
                break;

            case "datasourcelist":
                break;
        }
        renderAllQuestionsLayout.addView(layout);
        /*check dependency if depends then hide*/
        if (depQueID.contains(dde_questions.getQuestionId())) {
            layout.setVisibility(View.GONE);
        }
    }

    private void checkRuleCondion(String tag, String ans, String queType) {
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
                    if (conditionMatch(allRules.get(i).getConditionsMatch(), conditionId.size(), ConditionsSatisfied.size())) {
                        layout.setVisibility(View.VISIBLE);
                    } else {
                        layout.setVisibility(View.GONE);
                    }
                }
            }
        }
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
            answer = Integer.parseInt(ans);
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
                if (queType.equalsIgnoreCase("multiple")) {
                    /*ENTERED BY USER*/
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
        Log.d("tag111", formIdWiseQuestions.toString());
        if (checkValidations()) {
            //save answer to db


        }
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
                        if (!("null".equals(validationObject.get("ValidationValue").toString()))) {
                            ValidationValue = validationObject.get("ValidationValue").getAsString();
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
                // return answer.length() <= Integer.parseInt(validationValue) ? true : false;
                if (answer.length() > Integer.parseInt(validationValue)) {
                    Toast.makeText(this, "Maximum Characters Allowed " + Integer.parseInt(validationValue), Toast.LENGTH_LONG).show();
                    return false;
                }
                return true;
            case "MINCHARACTERSALLOWED":
            case "MINLENGTH":
                // return answer.length() >= Integer.parseInt(validationValue) ? true : false;
                if (answer.length() < Integer.parseInt(validationValue)) {
                    Toast.makeText(this, "Minimum Characters Required " + Integer.parseInt(validationValue), Toast.LENGTH_LONG).show();
                    return false;
                }
                return true;
            case "MINRANGE":
                switch (questionType) {
                    case "number":
                        if (Integer.parseInt(answer) < Integer.parseInt(validationValue)) {
                            Toast.makeText(this, "Minimum Value  Required" + Integer.parseInt(validationValue), Toast.LENGTH_LONG).show();
                            return false;
                        }
                        return true;
                    case "date":
                        int minYear, minMonth, minDay;
                        try {
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
                        } catch (Exception e) {
                            e.printStackTrace();
                            return false;

                            // return Integer.parseInt(answer) >= Integer.parseInt(validationValue) ? true : false;
                        }
                }
            case "MAXRANGE":
                switch (questionType) {
                    case "number":
                        // return Integer.parseInt(answer) <= Integer.parseInt(validationValue) ? true : false;
                        if (Integer.parseInt(answer) > Integer.parseInt(validationValue)) {
                            Toast.makeText(this, "Maximum Value Allowed " + Integer.parseInt(validationValue), Toast.LENGTH_LONG).show();
                            return false;
                        }
                        return true;
                    case "date":
                        try {
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
                        } catch (Exception e) {
                            e.printStackTrace();
                            return false;
                        }
                }

            case "DEPENDSON":
                switch (questionType) {
                    case "number":
                        EditText dependentQue = renderAllQuestionsLayout.findViewWithTag("ans" + validationValue);
                        int ans = Integer.parseInt(dependentQue.getText().toString());
                        switch (validationType) {
                            case "<":
                                if ((Integer.parseInt(answer) >= ans)) {
                                    Toast.makeText(this, "Answer Must be Smaller Than " + ((TextView) renderAllQuestionsLayout.findViewWithTag("que" + validationValue)).getText(), Toast.LENGTH_LONG).show();
                                    return false;
                                }
                                return true;
                            case ">":
                                if ((Integer.parseInt(answer) <= ans)) {
                                    Toast.makeText(this, "Answer Must be Greater Than " + ((TextView) renderAllQuestionsLayout.findViewWithTag("que" + validationValue)).getText(), Toast.LENGTH_LONG).show();
                                    return false;
                                }
                                return true;
                            case "=":
                                if ((Integer.parseInt(answer) != ans)) {
                                    Toast.makeText(this, "Answer Must be Equal To " + ((TextView) renderAllQuestionsLayout.findViewWithTag("que" + validationValue)).getText(), Toast.LENGTH_LONG).show();
                                    return false;
                                }
                                return true;
                            case "<=":
                                if ((Integer.parseInt(answer) > ans)) {
                                    Toast.makeText(this, "Answer Must be Smaller Than Or Equal To" + ((TextView) renderAllQuestionsLayout.findViewWithTag("que" + validationValue)).getText(), Toast.LENGTH_LONG).show();
                                    return false;
                                }
                                return true;
                            case ">=":
                                if ((Integer.parseInt(answer) < ans)) {
                                    Toast.makeText(this, "Answer Must be Greater Than Or Equal To" + ((TextView) renderAllQuestionsLayout.findViewWithTag("que" + validationValue)).getText(), Toast.LENGTH_LONG).show();
                                    return false;
                                }
                                return true;
                            default:
                                return true;
                        }
                    case "date":
                        TextView dependentDate = renderAllQuestionsLayout.findViewWithTag("ans" + validationValue);
                        try {
                            Date dependentAns = new SimpleDateFormat("yyyy/MM/DD").parse(dependentDate.getText().toString());
                            Date dependingAns = new SimpleDateFormat("yyyy/MM/DD").parse(answer);


                            switch (validationType) {
                                case "<":
                                    if (dependingAns.compareTo(dependentAns) >= 0) {
                                        Toast.makeText(this, "Answer Must be Before " + dependentDate.getText().toString(), Toast.LENGTH_LONG).show();
                                        return false;
                                    }
                                    return true;
                                case ">":
                                    if (dependingAns.compareTo(dependentAns) <= 0) {
                                        Toast.makeText(this, "Answer Must be After " + dependentDate.getText().toString(), Toast.LENGTH_LONG).show();
                                        return false;
                                    }
                                    return true;
                                case "=":
                                    if (dependingAns.compareTo(dependentAns) != 0) {
                                        Toast.makeText(this, "Answer Must be Equal to " + dependentDate.getText().toString(), Toast.LENGTH_LONG).show();
                                        return false;
                                    }
                                    return true;
                                case "<=":
                                    if (dependingAns.compareTo(dependentAns) > 0) {
                                        Toast.makeText(this, "Answer Must be Before Or equal TO " + dependentDate.getText().toString(), Toast.LENGTH_LONG).show();
                                        return false;
                                    }
                                    return true;
                                case ">=":
                                    if (dependingAns.compareTo(dependentAns) < 0) {
                                        Toast.makeText(this, "Answer Must be After Or equal TO " + dependentDate.getText().toString(), Toast.LENGTH_LONG).show();
                                        return false;
                                    }
                                    return true;
                                default:
                                    return true;
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                            return false;
                        }
                }
            case "time":
                TextView dependentTime = renderAllQuestionsLayout.findViewWithTag("ans" + validationValue);
                try {
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
                } catch (ParseException e) {
                    e.printStackTrace();
                    return false;
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
                // img.setImageURI(selectedImage);
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                createDirectoryAndSaveFile(bitmap, "g");

            } else if (requestCode == CAPTURE_IMAGE) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                //  img.setImageBitmap(photo);
                // String selectedImagePath = getPath(photo);
                createDirectoryAndSaveFile(photo, "c");
            }
        } catch (Exception e) {
        }
    }

    private void createDirectoryAndSaveFile(Bitmap imageToSave, String fileName) {

        File direct = new File(Environment.getExternalStorageDirectory() + "/DDEImages");

        if (!direct.exists()) {
            File imagesDirectory = new File("/sdcard/DDEImages/");
            imagesDirectory.mkdirs();
        }

        File file = new File(new File("/sdcard/DDEImages/"), fileName);
        if (file.exists()) {
            file.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            imageToSave.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}