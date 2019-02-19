package com.pratham.dde.customViews;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pratham.dde.R;
import com.pratham.dde.domain.AnswersSingleForm;
import com.pratham.dde.domain.DDE_Questions;
import com.pratham.dde.interfaces.PreviewFormListener;


public class previewFormDialog extends Dialog {
    List<DDE_Questions> ddeQuestionsList;
    @BindView(R.id.showQueAns)
    LinearLayout parent;
    AnswersSingleForm answersSingleForm;
    PreviewFormListener previewFormListener;

    Context context;

    public previewFormDialog(@NonNull Context context, List<DDE_Questions> ddeQuestions, AnswersSingleForm answersSingleForm) {
        super(context);
        this.context = context;
        this.ddeQuestionsList = ddeQuestions;
        this.answersSingleForm = answersSingleForm;
        this.previewFormListener = (PreviewFormListener) context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preview_form);
        ButterKnife.bind(this);
        setCanceledOnTouchOutside(false);
        setCancelable(false);
        showPreview();
    }

    private void showPreview() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(10, 5, 10, 5);
        LinearLayout.LayoutParams textViewParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1);

        DDE_Questions dde_questions;
        for (int queAns = 0; ddeQuestionsList.size() > queAns; queAns++) {
            dde_questions = ddeQuestionsList.get(queAns);
            if (!ddeQuestionsList.get(queAns).getAnswer().equals("")) {
                LinearLayout linLayoutSingleEntry = new LinearLayout(context);
                linLayoutSingleEntry.setOrientation(LinearLayout.VERTICAL);
                linLayoutSingleEntry.setPadding(20, 10, 20, 10);
                linLayoutSingleEntry.setLayoutParams(params);
                /*SET FORM NAME*/
                TextView que = new TextView(context);
                que.setLayoutParams(textViewParam);
                que.setTextSize(1, 25);
                que.setAllCaps(true);
                que.setTypeface(null, Typeface.BOLD_ITALIC);
                que.setText("Que :  " + dde_questions.getQuestion());
                linLayoutSingleEntry.addView(que);
                /*SET FORM DATE*/
                String queType = dde_questions.getQuestionType();
                if (queType.equals("image")) {
                    ImageView selectedImageTemp = new ImageView(context);
                    // selectedImage.setLayoutParams(new android.view.ViewGroup.LayoutParams(150, 150));
                    selectedImageTemp.setPadding(10, 5, 5, 5);
                    LinearLayout.LayoutParams buttonLayoutParams = new LinearLayout.LayoutParams(100, 100);
                    buttonLayoutParams.setMargins(50, 0, 0, 0);
                    selectedImageTemp.setLayoutParams(buttonLayoutParams);
                    String imgpath = dde_questions.getAnswer();
                    Bitmap bmp = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().toString() + "/DDEImages/" + imgpath);
                    selectedImageTemp.setImageBitmap(bmp);
                    linLayoutSingleEntry.addView(selectedImageTemp);
                } else {
                    TextView ans = new TextView(context);
                    ans.setLayoutParams(textViewParam);
                    ans.setTextSize(1, 20);
                    if (queType.equals("singlechoice") || queType.equals("multiple") || queType.equals("dropdown")) {
                        String value=getDisplayByValue(dde_questions);
                        ans.setText(value);
                    } else {
                        ans.setText("Ans :  " + dde_questions.getAnswer());
                    }
                    linLayoutSingleEntry.addView(ans);
                }

                parent.addView(linLayoutSingleEntry);

            }
        }
    }

    private String getDisplayByValue(DDE_Questions dde_questions) {
        String ans = "";
        JsonArray queOption = dde_questions.getQuestionOption();
        String answer = dde_questions.getAnswer();
        String values[] = answer.split(",");

        for (int jIndex = 0; jIndex < queOption.size(); jIndex++)
            for (int vIndex = 0; vIndex < values.length; vIndex++) {
                JsonObject obj = queOption.get(jIndex).getAsJsonObject();
                if (obj.get("value").getAsString().equals(values[vIndex])) {
                    ans += obj.get("display").getAsString() + ",";
                    break;
                }
            }
        if (ans.endsWith(",")) {
            ans = ans.substring(0, ans.length() - 1);
        }
        return ans;
    }

    @OnClick(R.id.btn_close_village)
    public void closeDialog() {
        dismiss();
    }

    @OnClick(R.id.txt_clear_changes_village)
    public void clearChanges() {
        dismiss();
    }

    @OnClick(R.id.txt_ok_village)
    public void ok() {
        previewFormListener.proceed(answersSingleForm);
    }


}
