package pratham.dde.activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pratham.dde.R;
import pratham.dde.domain.DDE_Questions;

import static pratham.dde.BaseActivity.appDatabase;

public class DisplayQuestions extends AppCompatActivity {
    @BindView(R.id.homeButton)
    ImageView homeButton;
    @BindView(R.id.formNameHeader)
    TextView formNameHeader;
    @BindView(R.id.renderAllQuestions)
    LinearLayout renderAllQuestions;

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
        formIdWiseQuestions = appDatabase.getDDE_QuestionsDao().getFormIdWiseQuestions(formId);
        for (int i = 0; i < formIdWiseQuestions.size(); i++) {
            dispaySingleQue(formIdWiseQuestions.get(i));
        }
    }

    /*    Display A Single Questions One By One*/
    private void dispaySingleQue(final DDE_Questions dde_questions) {
        LinearLayout layout = new LinearLayout(this);
        layout.setPadding(10, 10, 10, 10);
        layout.setOrientation(LinearLayout.VERTICAL);
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
                EditText editText = new EditText(this);
                editText.setBackground(ContextCompat.getDrawable(this, R.drawable.rectangular_box));
                editText.setPadding(15, 5, 5, 5);
                editText.setLayoutParams(params);
                layout.addView(editText);
                break;

            case "singlechoice":
                JsonArray option = dde_questions.getQuestionOption();
                RadioGroup radioGroup = new RadioGroup(this);
                radioGroup.setBackground(ContextCompat.getDrawable(this, R.drawable.rectangular_box));
                for (int i = 0; i < option.size(); i++) {
                    JsonElement jsonElement = option.get(i);
                    JsonObject jsonObject = jsonElement.getAsJsonObject();
                    RadioButton radioButton = new RadioButton(this);
                    radioButton.setLayoutParams(paramsWrapContaint);
                    radioButton.setTag(jsonObject.get("value").getAsString());
                    radioButton.setText(jsonObject.get("display").getAsString());
                    radioGroup.addView(radioButton);
                }
                radioGroup.setLayoutParams(params);
                layout.addView(radioGroup);
                break;

            case "email":
                EditText et_email = new EditText(this);
                et_email.setBackground(ContextCompat.getDrawable(this, R.drawable.rectangular_box));
                et_email.setPadding(15, 5, 5, 5);
                et_email.setLayoutParams(params);
                layout.addView(et_email);
                break;

            case "number":
                EditText number = new EditText(this);
                number.setBackground(ContextCompat.getDrawable(this, R.drawable.rectangular_box));
                number.setInputType(InputType.TYPE_CLASS_NUMBER);
                number.setLayoutParams(params);
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
                                date.setText("" + day + "/" + month + "/" + year);
                            }

                        }, mYear, mMonth, mDay);


                        /*GETTING RANGE OF DATE FROM QUESTION */
                        DatePicker datePicker = datePickerDialog.getDatePicker();
                        Calendar c = Calendar.getInstance();
                        String minDateString = dde_questions.getMINRANGE();
                        String maxDateString = dde_questions.getMAXRANGE();
                        if (minDateString != "null") {
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
                        if (maxDateString != "null") {
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
                layout.addView(spinnerDropdown);
                break;

            case "datasourcelist":
                break;
        }
        renderAllQuestions.addView(layout);
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
}
