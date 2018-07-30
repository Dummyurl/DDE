package pratham.dde.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pratham.dde.R;
import pratham.dde.interfaces.FabInterface;

import static pratham.dde.BaseActivity.appDatabase;

/**
 * Created by abc on 7/4/2018.
 */

public class SavedFormsFragment extends android.app.Fragment {

    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.linearlayout)
    LinearLayout linearlayout;

    FabInterface fabInterface;
    Context context;

    @Override
    public void onAttach(Activity context) {
        super.onAttach(context);
        fabInterface = (FabInterface) context;
        context = context;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_saved_forms, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        showOldSavedForm();
    }

    private void showOldSavedForm() {
        // pass user to veryfy
        List distinctEntrys = appDatabase.getAnswerDao().getDistinctEntrys();
        if (distinctEntrys != null && distinctEntrys.size() > 0) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            LinearLayout.LayoutParams paramsLeft = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 6);
            LinearLayout.LayoutParams paramsRight = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2);
            for (int entryID = 0; distinctEntrys.size() > entryID; entryID++) {
                /*OUTER lINEAR LAYOUT*/
                LinearLayout linLayoutSingleEntry = new LinearLayout(getActivity());
                linLayoutSingleEntry.setOrientation(LinearLayout.HORIZONTAL);
                /*INNER LEFT SIDE FORM INFO*/
                LinearLayout left = new LinearLayout(getActivity());
                left.setOrientation(LinearLayout.VERTICAL);
                TextView formName=new TextView(getActivity());
            }
        }
    }


    @OnClick(R.id.fab)
    public void edit() {
        fabInterface.fabOnClick();
    }


}
