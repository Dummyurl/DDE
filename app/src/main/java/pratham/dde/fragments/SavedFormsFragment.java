package pratham.dde.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pratham.dde.R;
import pratham.dde.interfaces.FabInterface;

/**
 * Created by abc on 7/4/2018.
 */

public class SavedFormsFragment extends android.app.Fragment {

    @BindView(R.id.fab)
    FloatingActionButton fab;

    FabInterface fabInterface;

    @Override
    public void onAttach(Activity context) {
        super.onAttach(context);
        fabInterface = (FabInterface) context;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_saved_forms,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this,view);

    }


    @OnClick(R.id.fab)
    public void edit() {
        fabInterface.fabOnClick();
    }


}
