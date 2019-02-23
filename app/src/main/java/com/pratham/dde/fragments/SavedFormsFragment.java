package com.pratham.dde.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pratham.dde.BaseActivity;
import com.pratham.dde.R;
import com.pratham.dde.activities.DisplayQuestions;
import com.pratham.dde.interfaces.FabInterface;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import com.pratham.dde.domain.AnswersSingleForm;

/**
 * Created by abc on 7/4/2018.
 */

public class SavedFormsFragment extends android.app.Fragment {

    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.linearlayout)
    LinearLayout linearlayout;
    @BindView(R.id.savedform)
    TextView title;

    FabInterface fabInterface;
    String userID;

    @Override
    public void onAttach(Activity context) {
        super.onAttach(context);
        fabInterface = (FabInterface) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userID = getArguments().getString("userID");
        }
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
        // pass user to verify
        List distinctEntrys = BaseActivity.appDatabase.getAnswerDao().getDistinctEntries(userID, 0);
        List partiallyPushed = BaseActivity.appDatabase.getAnswerDao().getDistinctEntries(userID, 1);
        if (partiallyPushed.size() > 0) {
            distinctEntrys.addAll(partiallyPushed);
        }

        if (distinctEntrys != null && distinctEntrys.size() > 0) {
            title.setText("Saved forms");
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(20, 10, 20, 10);
            LinearLayout.LayoutParams paramsLeft = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.FILL_PARENT, 6);
            LinearLayout.LayoutParams paramsRight = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2);
            LinearLayout.LayoutParams textViewParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 0, 1);
            LinearLayout.LayoutParams imageViewParam = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            AnswersSingleForm answersSingleForm;
            for (int entryID = 0; distinctEntrys.size() > entryID; entryID++) {
                answersSingleForm = (AnswersSingleForm) distinctEntrys.get(entryID);
                /*OUTER lINEAR LAYOUT*/
                final LinearLayout linLayoutSingleEntry = new LinearLayout(getActivity());
                linLayoutSingleEntry.setOrientation(LinearLayout.HORIZONTAL);
                linLayoutSingleEntry.setBackground(getResources().getDrawable(R.drawable.roundedcornertext));
                linLayoutSingleEntry.setPadding(20, 10, 20, 10);
                linLayoutSingleEntry.setLayoutParams(params);
                /*INNER LEFT SIDE FORM INFO*/
                LinearLayout left = new LinearLayout(getActivity());
                left.setOrientation(LinearLayout.VERTICAL);
                left.setLayoutParams(paramsLeft);
                /*SET FORM NAME*/
                TextView formName = new TextView(getActivity());
                formName.setLayoutParams(textViewParam);
                formName.setTextSize(1, 15);
                formName.setAllCaps(true);
                formName.setTypeface(null, Typeface.BOLD_ITALIC);
                String formNameText = BaseActivity.appDatabase.getDDE_FormsDao().getFormName(answersSingleForm.getFormId());
                if (formNameText == null) {
                    formName.setText("Form deleted from server");
                }
                else
                    formName.setText(formNameText);
                /*SET FORM DATE*/
                TextView formDate = new TextView(getActivity());
                formDate.setLayoutParams(textViewParam);
                formDate.setText(answersSingleForm.getDate());
                left.addView(formName);
                left.addView(formDate);

                /*INNER RIGHT LINEAR LAYOUT*/
                final LinearLayout right = new LinearLayout(getActivity());
                right.setOrientation(LinearLayout.HORIZONTAL);
                right.setLayoutParams(paramsRight);
                right.setTag(answersSingleForm.getEntryId());
                /*SET EDIT FORM OPTION*/
                if (answersSingleForm.getPushStatus() == 0) {
                    final ImageView editForm = new ImageView(getActivity());
                    editForm.setImageResource(R.drawable.edit_green);
                    editForm.setLayoutParams(imageViewParam);
                    editForm.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                            alertDialogBuilder.setTitle("Alert");
                            alertDialogBuilder.setCancelable(false);
                            alertDialogBuilder.setMessage("Do You Want To Edit Form ?");

                            alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String tagEntyId = right.getTag().toString();
                                    Intent intent = new Intent(getActivity(), DisplayQuestions.class);
                                    intent.putExtra("formId", BaseActivity.appDatabase.getAnswerDao().getFormIDByEntryID(tagEntyId));
                                    intent.putExtra("userId", BaseActivity.appDatabase.getAnswerDao().getUserIDByEntryID(tagEntyId));
                                    intent.putExtra("entryId", tagEntyId);
                                    intent.putExtra("formEdit", "true");
                                    startActivity(intent);

                                }
                            });

                            alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            alertDialogBuilder.show();
                        }
                    });
                    /*SET DELETE FORM OPTION*/
                    ImageView deleteForm = new ImageView(getActivity());
                    deleteForm.setImageResource(R.drawable.delete_red);
                    deleteForm.setLayoutParams(imageViewParam);
                    deleteForm.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                            alertDialogBuilder.setTitle("Alert");
                            alertDialogBuilder.setCancelable(false);
                            alertDialogBuilder.setMessage("Do You Want To Delete Form ?");

                            alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    BaseActivity.appDatabase.getAnswerDao().deleteAnswerEntryByEntryID(right.getTag().toString());
                                    Animation slide = AnimationUtils.loadAnimation(getActivity(), R.anim.slide);
                                    linLayoutSingleEntry.startAnimation(slide);
                                    Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            linLayoutSingleEntry.setVisibility(View.GONE);
                                        }
                                    }, 1000);
                                }
                            });

                            alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            alertDialogBuilder.show();
                        }
                    });
                    right.addView(editForm);
                    right.addView(deleteForm);
                } else {
                    TextView uploading = new TextView(getActivity());
                    uploading.setText("In Uploading Queue");
                    uploading.setLayoutParams(imageViewParam);
                    uploading.setTextSize(1, 20);
                    uploading.setTypeface(null, Typeface.ITALIC);
                    uploading.setTextColor(Color.parseColor("#FF5733"));
                  //  right.setBackgroundColor(Color.parseColor("#DAF7A6"));
                    right.addView(uploading);
                }
                linLayoutSingleEntry.addView(left);
                linLayoutSingleEntry.addView(right);
                linearlayout.addView(linLayoutSingleEntry);
            }
        } else {
            title.setText("No saved forms");
        }
    }

    @OnClick(R.id.fab)
    public void edit() {
        fabInterface.fabOnClick();
    }
}
