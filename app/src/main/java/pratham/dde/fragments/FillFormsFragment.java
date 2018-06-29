package pratham.dde.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import pratham.dde.R;

import static pratham.dde.BaseActivity.appDatabase;

/**
 * Created by abc on 6/26/2018.
 */

public class FillFormsFragment extends Fragment {
    String programId="25,26,27";
    List forms;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_fill_forms,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getFormsfromDb();
    }
    /*fetching  forms according to programid*/
    private void getFormsfromDb() {
        String[] programIdArray=programId.split(",");
        forms = new ArrayList();
        for (int i=0;i<programIdArray.length;i++){
            forms.add(appDatabase.getDDE_FormsDao().getFormProgramIdWise(programIdArray[i]));
        }
    }
}
