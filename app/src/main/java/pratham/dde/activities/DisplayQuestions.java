package pratham.dde.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pratham.dde.R;
import pratham.dde.database.AppDatabase;
import pratham.dde.domain.DDE_Questions;

import static pratham.dde.BaseActivity.appDatabase;

public class DisplayQuestions extends AppCompatActivity {
    @BindView(R.id.homeButton)
    ImageView homeButton;

    List<DDE_Questions> formIdWiseQuestions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_questions);
        String formId = getIntent().getStringExtra("formId");
        ButterKnife.bind(this);

        formIdWiseQuestions=appDatabase.getDDE_QuestionsDao().getFormIdWiseQuestions(formId);
        Log.d("QQQ",formIdWiseQuestions.toString());
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

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        onHomeButtonClick();
    }
}
