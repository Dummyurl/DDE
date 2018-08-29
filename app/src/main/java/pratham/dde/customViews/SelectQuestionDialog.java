package pratham.dde.customViews;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.widget.CheckBox;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pratham.dde.R;
import pratham.dde.domain.DDE_Forms;
import pratham.dde.interfaces.QuestionListLisner;

public class SelectQuestionDialog extends Dialog {

    @BindView(R.id.txt_clear_changes_village)
    TextView clear_changes;
    @BindView(R.id.btn_close_village)
    ImageButton btn_close;
    /*  @BindView(R.id.txt_count_village)
      TextView txt_count_village;*/
    @BindView(R.id.txt_message_village)
    TextView txt_message_village;
    @BindView(R.id.flowLayout)
    GridLayout flowLayout;

    Context context;
    List<DDE_Forms> formsList;
    List<CheckBox> checkBoxes = new ArrayList<>();
    QuestionListLisner questionListLisner;

    public SelectQuestionDialog(@NonNull Context context, List tempList) {
        super(context, android.R.style.Theme_DeviceDefault_NoActionBar_Fullscreen);
        this.formsList = tempList;
        this.context = context;
        this.questionListLisner = (QuestionListLisner) context;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_question_dialog);
        ButterKnife.bind(this);
        setCanceledOnTouchOutside(false);
        setCancelable(false);
        for (int i = 0; i < formsList.size(); i++) {
            CheckBox checkBox = new CheckBox(context);
            checkBox.setText(formsList.get(i).getFormname());
            checkBox.setTextSize(1, 25);
            checkBox.setTypeface(null, Typeface.BOLD);
            checkBox.setTag(formsList.get(i).getFormid());
            GridLayout.LayoutParams param = new GridLayout.LayoutParams();
            param.height = GridLayout.LayoutParams.WRAP_CONTENT;
            param.width = GridLayout.LayoutParams.WRAP_CONTENT;
            param.setGravity(Gravity.FILL_HORIZONTAL);
            checkBox.setLayoutParams(param);
            flowLayout.addView(checkBox);
            checkBoxes.add(checkBox);
        }
    }


    @OnClick(R.id.btn_close_village)
    public void closeDialog() {
        dismiss();
    }

    @OnClick(R.id.txt_clear_changes_village)
    public void clearChanges() {
        for (int i = 0; i < checkBoxes.size(); i++) {
            checkBoxes.get(i).setChecked(false);
        }
    }

    @OnClick(R.id.txt_ok_village)
    public void ok() {
        ArrayList<Integer> formIdList = new ArrayList();
        for (int i = 0; i < checkBoxes.size(); i++) {
            if (checkBoxes.get(i).isChecked()) {
                formIdList.add(Integer.parseInt(checkBoxes.get(i).getTag().toString()));
            }
        }
         questionListLisner.getSelectedForms(formIdList);
        dismiss();
    }

}

