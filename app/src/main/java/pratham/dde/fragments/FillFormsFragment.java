package pratham.dde.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import pratham.dde.R;
import pratham.dde.customViews.FormattedTextView;
import pratham.dde.database.BackupDatabase;

import static pratham.dde.BaseActivity.appDatabase;

/**
 * Created by abc on 6/26/2018.
 */

public class FillFormsFragment extends Fragment {
    String programId;
    List forms;
    private String userName,password;
    @BindView(R.id.forms)
    LinearLayout linearLayout;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments()!=null){
            userName=getArguments().getString("userName");
            password=getArguments().getString("password");
            programId=appDatabase.getUserDao().getProgramIDs(userName,password);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_fill_forms,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this,view);
        getFormsfromDb();
        updateUI();
    }


    /*fetching  forms according to programid*/
    private void getFormsfromDb() {
        String[] programIdArray=programId.split(",");
        forms = new ArrayList();
        for (int i=0;i<programIdArray.length;i++){
            forms.add(appDatabase.getDDE_FormsDao().getFormProgramIdWise(programIdArray[i]));
        }
    }

    /*Display forms names on screen*/
    private void updateUI() {
        BackupDatabase.backup(getActivity());
        FormattedTextView textView=new FormattedTextView(getActivity());
        textView.setText("Amar");
        linearLayout.addView(textView);
        ImageButton imageButton=new ImageButton(getActivity());
        imageButton.setBackground(getResources().getDrawable(R.drawable.common_google_signin_btn_icon_dark_focused));
        imageButton.setLayoutParams(new LinearLayout.LayoutParams(0,50,1));
        linearLayout.addView(imageButton);
    }

}
