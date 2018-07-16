package pratham.dde.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
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
    private void dispaySingleQue(DDE_Questions dde_questions) {
        LinearLayout layout = new LinearLayout(this);
        layout.setPadding(10, 10, 10, 10);
        layout.setOrientation(LinearLayout.VERTICAL);
        TextView textView = new TextView(this);
        textView.setText(dde_questions.getQuestion());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0);
        textView.setLayoutParams(params);
        layout.addView(textView);

        switch (dde_questions.getQuestionType()) {
            case "text":
                EditText editText = new EditText(this);
                editText.setBackground(ContextCompat.getDrawable(this,R.drawable.rectangular_box));
                editText.setLayoutParams(params);
                layout.addView(editText);
                break;

            case "singlechoice":
                JsonArray option = dde_questions.getQuestionOption();
                Log.d("option", option.toString());
                RadioGroup radioGroup = new RadioGroup(this);
                for (int i = 0; i < option.size(); i++) {
                    JsonElement jsonElement = option.get(i);
                    JsonObject jsonObject = jsonElement.getAsJsonObject();
                    RadioButton radioButton = new RadioButton(this);
                    radioButton.setTag("" + jsonObject.get("value"));
                    radioButton.setText("" + jsonObject.get("display"));
                    radioGroup.addView(radioButton);
                }
                radioGroup.setLayoutParams(params);
                layout.addView(radioGroup);
                break;

            case "email":
                EditText et_email = new EditText(this);
                et_email.setLayoutParams(params);
                layout.addView(et_email);
                break;

            case "number":
                EditText number = new EditText(this);
                number.setInputType(InputType.TYPE_CLASS_NUMBER);
                number.setLayoutParams(params);
                layout.addView(number);
                break;

            case "multiple":
                List checkBoxList = new ArrayList();
                JsonArray optionCheckBox = dde_questions.getQuestionOption();
                GridLayout linearLayout = new GridLayout(this);
                linearLayout.setColumnCount(4);
                for (int i = 0; i < optionCheckBox.size(); i++) {
                    CheckBox checkBox = new CheckBox(this);
                    JsonElement jsonElement = optionCheckBox.get(i);
                    JsonObject jsonObject = jsonElement.getAsJsonObject();
                    checkBox.setTag("" + jsonObject.get("value"));
                    checkBox.setText("" + jsonObject.get("display"));
                    checkBoxList.add(checkBox);
                    GridLayout.LayoutParams paramGrid = new GridLayout.LayoutParams();
                    paramGrid.height = GridLayout.LayoutParams.WRAP_CONTENT;
                    paramGrid.width = GridLayout.LayoutParams.WRAP_CONTENT;
                    paramGrid.setGravity(Gravity.FILL_HORIZONTAL);
                    checkBox.setLayoutParams(paramGrid);
                }
                break;

            case "image":
                break;

            case "date":
                EditText date = new EditText(this);
                date.setInputType(InputType.TYPE_CLASS_DATETIME);
                date.setLayoutParams(params);
                layout.addView(date);
                break;

            case "time":
                EditText time = new EditText(this);
                time.setInputType(InputType.TYPE_CLASS_DATETIME);
                time.setLayoutParams(params);
                layout.addView(time);
                break;

            case "dropdown":
                List display = new ArrayList();
                List value = new ArrayList();
                Spinner spinnerDropdown = new Spinner(this);
                JsonArray optionDropDown = dde_questions.getQuestionOption();
                for (int i = 0; i < optionDropDown.size(); i++) {
                    JsonElement jsonElement = optionDropDown.get(i);
                    JsonObject jsonObject = jsonElement.getAsJsonObject();
                    value.add("" + jsonObject.get("value"));
                    display.add("" + jsonObject.get("display"));
                }
                ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, display);
                spinnerDropdown.setAdapter(spinnerArrayAdapter);
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
