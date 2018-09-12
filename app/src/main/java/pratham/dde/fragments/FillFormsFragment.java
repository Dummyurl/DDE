package pratham.dde.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import pratham.dde.R;
import pratham.dde.activities.DisplayQuestions;
import pratham.dde.customViews.FormPasswordDialog;
import pratham.dde.customViews.FormattedTextView;
import pratham.dde.domain.DDE_Forms;
import pratham.dde.domain.DDE_Questions;
import pratham.dde.services.SyncUtility;

import static pratham.dde.BaseActivity.appDatabase;

/**
 * Created by abc on 6/26/2018.
 */

public class FillFormsFragment extends Fragment {
    String programId;
    List<DDE_Forms> forms;
    private String userName, password;
    @BindView(R.id.forms)
    LinearLayout linearLayout;
    String formPassword;
    DisplayMetrics displaymetrics;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        displaymetrics = new DisplayMetrics();
        if (getArguments() != null) {
            userName = getArguments().getString("userName");
            password = getArguments().getString("password");
            if (appDatabase!=null)
                programId = appDatabase.getUserDao().getProgramIDs(userName, password);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_fill_forms, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        getFormsfromDb();
        updateUI();
    }

    /*fetching  forms according to programid*/
    private void getFormsfromDb() {
        // DDE_Forms[] dde = appDatabase.getDDE_FormsDao().getAllForms();

        String[] programIdArray = programId.split(",");
        forms = new ArrayList();
        for (int i = 0; i < programIdArray.length; i++) {
            List<DDE_Forms> dde_forms = appDatabase.getDDE_FormsDao().getFormProgramIdWise(programIdArray[i]);
            if (dde_forms != null) {
                for (int formIndex = 0; formIndex < dde_forms.size(); formIndex++) {
                    List<DDE_Questions> questions = appDatabase.getDDE_QuestionsDao().getFormIdWiseQuestions(String.valueOf(dde_forms.get(formIndex).getFormid()));
                    if (questions.size() > 0)
                        forms.add(dde_forms.get(formIndex));
                }
            }
        }
    }

    /*Display forms names on screen*/
    private void updateUI() {
        int height = 0;
        int parentHeight = 0;

        Configuration config = getResources().getConfiguration();
        if (config.smallestScreenWidthDp < 320) {
            height = 155;
            parentHeight = 185;
        } else if (config.smallestScreenWidthDp > 320 && config.smallestScreenWidthDp < 480) {
            height =120;
            parentHeight = 150;
        } else if (config.smallestScreenWidthDp >= 480 && config.smallestScreenWidthDp < 600) {
            height = 85;
            parentHeight = 105;
        } else if (config.smallestScreenWidthDp >= 600)
        {
            height = 50;
            parentHeight =60;
        }


        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, height, 1);
        params.setMargins(20, 0, 20, 0);
        LinearLayout.LayoutParams paramsParent = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, parentHeight);
        paramsParent.setMargins(0, 5, 0, 5);
        for (int i = 0; i < forms.size(); i++) {
            LinearLayout layout = new LinearLayout(getActivity());
            layout.setPadding(20, 5, 20, 5);
            layout.setLayoutParams(paramsParent);
            FormattedTextView textView = new FormattedTextView(getActivity());
            textView.setId(forms.get(i).getFormid());
            textView.setText(forms.get(i).getFormname());
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String formId = String.valueOf(view.getId());
                    checkFormPassword(formId);
                }
            });
            layout.addView(textView);
            final ImageButton imageButton = new ImageButton(getActivity());
            imageButton.setBackground(getResources().getDrawable(R.drawable.ic_insert_chart_green));
            if (forms.get(i).getPbreporturl() != null)
                imageButton.setTag(forms.get(i).getPbreporturl());
         /*   else
                imageButton.setTag("");*/
            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (SyncUtility.isDataConnectionAvailable(getActivity())) {
                        if (imageButton.getTag() != null) {
                            Uri uri = Uri.parse(imageButton.getTag().toString());
                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                            startActivity(intent);
                        } else {
                            Toast.makeText(getActivity(), "Uri Not available", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getActivity(), "Internet not available", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            imageButton.setLayoutParams(params);
            layout.addView(imageButton);
            linearLayout.addView(layout);
        }

    }

    private void checkFormPassword(final String formId) {
        final int UserId = appDatabase.getUserDao().getUserId(userName, password);
        List<DDE_Questions> formIdWiseQuestions = new ArrayList<>();
        formIdWiseQuestions = appDatabase.getDDE_QuestionsDao().getFormIdWiseQuestions(formId);
        if (!formIdWiseQuestions.isEmpty()) {

            formPassword = appDatabase.getDDE_FormsDao().getFormPassword(formId);
            if (!formPassword.equals("null")) {
                final FormPasswordDialog formPasswordDialog;
                formPasswordDialog = new FormPasswordDialog(getActivity());
                formPasswordDialog.setCancelable(false);
                formPasswordDialog.btn_ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String enteredPassword = formPasswordDialog.et_password.getText().toString();

                        if (formPassword.equals(enteredPassword) && enteredPassword.equals("null")) {
                            Intent questionIntent = new Intent(getActivity(), DisplayQuestions.class);
                            questionIntent.putExtra("formId", formId);
                            questionIntent.putExtra("userId", String.valueOf(UserId));
                            questionIntent.putExtra("formEdit", "false");

                            startActivity(questionIntent);
                            formPasswordDialog.dismiss();
                        } else {
                            formPasswordDialog.dismiss();
                            Toast.makeText(getActivity(), "Incorrect Password..", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                formPasswordDialog.btn_cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        formPasswordDialog.dismiss();
                    }
                });

                formPasswordDialog.show();
            } else {
                Intent questionIntent = new Intent(getActivity(), DisplayQuestions.class);
                questionIntent.putExtra("formId", formId);
                questionIntent.putExtra("userId", String.valueOf(UserId));
                questionIntent.putExtra("formEdit", "false1");
                startActivity(questionIntent);
            }
        } else {
            Log.d("QQQ", formIdWiseQuestions.toString());
            Toast.makeText(getActivity(), "Form Is Empty..", Toast.LENGTH_SHORT).show();
        }


    }

    public static boolean isDataConnectionAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    public float pxToDp(int pixels) {
        Resources resource = this.getResources();

        float dp = pixels / (resource.getDisplayMetrics().densityDpi / 160f);
        return dp;
    }
}
