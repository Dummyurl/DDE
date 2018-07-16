package pratham.dde.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import pratham.dde.customViews.FormattedTextView;
import pratham.dde.domain.DDE_Forms;

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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userName = getArguments().getString("userName");
            password = getArguments().getString("password");
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
                forms.addAll(dde_forms);
            }
        }
    }

    /*Display forms names on screen*/
    private void updateUI() {

        for (int i = 0; i < forms.size(); i++) {
            LinearLayout layout = new LinearLayout(getActivity());
            layout.setPadding(20, 10, 20, 10);

            FormattedTextView textView = new FormattedTextView(getActivity());
            textView.setId(forms.get(i).getFormid());
            textView.setText(forms.get(i).getFormname());
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent questionIntent = new Intent(getActivity(), DisplayQuestions.class);
                    String formId = String.valueOf(view.getId());
                    questionIntent.putExtra("formId", formId);
                    startActivity(questionIntent);
                    Toast.makeText(getActivity(), "" + view.getId(), Toast.LENGTH_SHORT).show();
                }
            });
            layout.addView(textView);
            ImageButton imageButton = new ImageButton(getActivity());
            imageButton.setBackground(getResources().getDrawable(R.drawable.common_google_signin_btn_icon_dark_focused));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, 50, 1);
            params.setMargins(20, 0, 0, 0);
            imageButton.setLayoutParams(params);
            layout.addView(imageButton);
            linearLayout.addView(layout);
        }

    }

}
