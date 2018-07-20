package pratham.dde.activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pratham.dde.R;
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
    List depQueID;
    List<DDE_RuleTable> allRules;
    List<DDE_Questions> formIdWiseQuestions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_questions);
        String formId = getIntent().getStringExtra("formId");
        ButterKnife.bind(this);
        String formName = appDatabase.getDDE_FormsDao().getFormName(formId);
        if (formName != null) {
            formNameHeader.setText(formName);
        }
        allRules = appDatabase.getDDE_RulesDao().getAllRules();
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
        textView.setText(dde_questions.getQuestion());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25);
        textView.setTextColor(getResources().getColor(R.color.colorAccentDark));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0);
        params.setMargins(10, 0, 0, 0);
        textView.setLayoutParams(params);
        layout.addView(textView);

        LinearLayout.LayoutParams paramsWrapContaint = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0);
        paramsWrapContaint.setMargins(10, 0, 0, 0);
        switch (dde_questions.getQuestionType()) {
            case "text":
                final EditText editText = new EditText(this);
                editText.setBackground(ContextCompat.getDrawable(this, R.drawable.rectangular_box));
                editText.setPadding(15, 5, 5, 5);
                editText.setLayoutParams(params);
                layout.addView(editText);
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
                        checkRuleCondion(tag, c.toString());
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
                    radioButton.setText(jsonObject.get("display").getAsString());
                    radioGroup.addView(radioButton);
                    radioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            dde_questions.setAnswer(compoundButton.getText().toString());
                            LinearLayout layout = (LinearLayout) compoundButton.getParent().getParent();
                            String tag = (String) layout.getTag();
                            checkRuleCondion(tag, compoundButton.getTag().toString());
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
                et_email.setLayoutParams(params);
                layout.addView(et_email);
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
                        checkRuleCondion(tag, c.toString());
                    }
                });
                break;

            case "number":
                final EditText number = new EditText(this);
                number.setBackground(ContextCompat.getDrawable(this, R.drawable.rectangular_box));
                number.setInputType(InputType.TYPE_CLASS_NUMBER);
                number.setLayoutParams(params);
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
                        checkRuleCondion(tag, c.toString());
                    }
                });
                layout.addView(number);
                break;

            case "multiple":
                List checkBoxList = new ArrayList();
                JsonArray optionCheckBox = dde_questions.getQuestionOption();
                GridLayout gridLayout = new GridLayout(this);
                gridLayout.setBackground(ContextCompat.getDrawable(this, R.drawable.rectangular_box));
                gridLayout.setColumnCount(3);
                for (int i = 0; i < optionCheckBox.size(); i++) {
                    CheckBox checkBox = new CheckBox(this);
                    JsonElement jsonElement = optionCheckBox.get(i);
                    JsonObject jsonObject = jsonElement.getAsJsonObject();
                    checkBox.setTag(jsonObject.get("value").getAsString());
                    checkBox.setText(jsonObject.get("display").getAsString());
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
                break;

            case "date":
                final TextView date = new TextView(this);
                date.setPadding(10, 5, 5, 5);
                date.setBackground(ContextCompat.getDrawable(this, R.drawable.rectangular_box));
                date.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
                date.setText("Select Date");
                date.setLayoutParams(paramsWrapContaint);
                /*if (depQueID.contains(dde_questions.getQuestionId())) {
                    date.setEnabled(false);
                }*/
                layout.addView(date);


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
                                dde_questions.setAnswer("" + day + "/" + month + "/" + year);
                                date.setText("" + day + "/" + month + "/" + year);
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
                            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                                dde_questions.setAnswer("" + selectedHour + ":" + selectedMinute);
                                time.setText("" + selectedHour + ":" + selectedMinute);
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
                for (int i = 0; i < optionDropDown.size(); i++) {
                    JsonElement jsonElement = optionDropDown.get(i);
                    JsonObject jsonObject = jsonElement.getAsJsonObject();
                    value.add(jsonObject.get("value").getAsString());
                    display.add(jsonObject.get("display").getAsString());
                }
                ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_selectable_list_item, display);
                spinnerDropdown.setAdapter(spinnerArrayAdapter);
                spinnerDropdown.setLayoutParams(paramsWrapContaint);
                spinnerDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        dde_questions.setAnswer(adapterView.getSelectedItem().toString());
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

    private void checkRuleCondion(String tag, String ans) {
        for (int i = 0; i < allRules.size(); i++) {
            JsonArray questionCondition = allRules.get(i).getQuestionCondition();
            for (int j = 0; j < questionCondition.size(); j++) {
                JsonObject condition = questionCondition.get(i).getAsJsonObject();
                String QuestionIdentifier = condition.get("QuestionIdentifier").getAsString();
                if (QuestionIdentifier.equals(tag)) {
                    String ConditionType = condition.get("ConditionType").getAsString();
                    Set ContionsSatisfied = allRules.get(i).getContionsSatisfied();
                    if (checkConditionType(ConditionType, ans)) {
                        ContionsSatisfied.add(QuestionIdentifier);
                    } else {
                        ContionsSatisfied.remove(QuestionIdentifier);
                    }
                    String showQueTag = allRules.get(i).getShowQuestionIdentifier();
                    LinearLayout layout = renderAllQuestionsLayout.findViewWithTag(showQueTag);
                    if (conditionMatch(allRules.get(i).getConditionsMatch(), questionCondition.size(), ContionsSatisfied.size())) {
                        layout.setVisibility(View.VISIBLE);
                    } else {
                        layout.setVisibility(View.GONE);
                    }
                    break;
                }
            }
        }
    }

    private boolean conditionMatch(String conditionsMatch, int totalQue, int attemptedQue) {
        boolean flag = false;
        switch (conditionsMatch) {
            case "All of the conditions match":
                flag = totalQue == attemptedQue ? true : false;
                break;

        }
        return flag;
    }

    private boolean checkConditionType(String conditionType, String ans) {
        boolean flag = false;
        switch (conditionType) {
            case "greater than equal to":
                int answer = Integer.parseInt(ans);
                flag = answer >= 18 ? true : false;
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
        checkValidations();
    }

    private boolean checkValidations() {
        DDE_Questions dde_questions;
        for (int i = 0; i < formIdWiseQuestions.size(); i++) {
            JsonArray jsonArray = formIdWiseQuestions.get(i).getValidations();
            for (int j = 1; j < jsonArray.size(); j++) {
                JsonObject validationObject = jsonArray.get(j).getAsJsonObject();
                if (validationObject.get("ValidationApply").getAsBoolean()) {

                    checkValue(formIdWiseQuestions.get(i).getQuestionType(), validationObject.get("validationName").getAsString(), validationObject.get("ValidationValue").getAsString(), formIdWiseQuestions.get(i).getAnswer());
                }
            }
        }
        return true;

    }

    private boolean checkValue(String questionType, String validationName, String validationValue, String answer) {
        switch (validationName) {
            case "REQUIRED":
                return answer != "" ? true : false;
            case "MAXCHARACTERSALLOWED":
            case "MAXLENGTH":
                return answer.length() <= Integer.parseInt(validationValue) ? true : false;
            case "MINCHARACTERSALLOWED":
            case "MINLENGTH":
                return answer.length() >= Integer.parseInt(validationValue) ? true : false;
            case "MINRANGE":
                return Integer.parseInt(answer) >= Integer.parseInt(validationValue) ? true : false;
            case "MAXRANGE":
                return Integer.parseInt(answer) <= Integer.parseInt(validationValue) ? true : false;
            case "DEPENDSON":
                switch (questionType) {
                    case "number":
                        return Integer.parseInt(answer) <= Integer.parseInt(validationValue) ? true : false;
                    case "multiple":
                        //return Integer.parseInt(answer) <= Integer.parseInt(validationValue) ? true : false;
                    case "date":

                    case "time":


                }
            case "ALLOWDECIMAL":



            default:
                return true;
        }
    }

    class Sortbyroll implements Comparator<DDE_Questions> {
        // Used for sorting in ascending order of
        // roll number
        public int compare(DDE_Questions a, DDE_Questions b) {
            return a.getFieldSeqNo() - b.getFieldSeqNo();
        }
    }
}