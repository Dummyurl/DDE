package com.pratham.dde.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import pratham.dde.R;

/**
 * Created by abc on 6/26/2018.
 */

public class OldFormsFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_old_forms,container,false);
    }
}
