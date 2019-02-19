package com.pratham.dde.customViews;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.Button;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import pratham.dde.R;

/**
 * Created by abc on 7/14/2018.
 */

public class FormPasswordDialog extends Dialog {
    @BindView(R.id.btn_ok)
    public Button btn_ok;

    @BindView(R.id.btn_cancel)
    public Button btn_cancel;

    @BindView(R.id.et_password)
    public EditText et_password;


    public FormPasswordDialog(@NonNull Context context) {
        super(context, android.R.style.Theme_Holo_Light_Dialog_NoActionBar_MinWidth);
        setContentView(R.layout.formpassword_layout_dialog);
        ButterKnife.bind(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}

