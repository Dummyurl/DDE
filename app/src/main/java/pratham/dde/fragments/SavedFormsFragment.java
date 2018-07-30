package pratham.dde.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pratham.dde.R;
import pratham.dde.activities.DisplayQuestions;
import pratham.dde.domain.AnswersSingleForm;
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
    String userID;

    @Override
    public void onAttach(Activity context) {
        super.onAttach(context);
        fabInterface = (FabInterface) context;
        context = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments()!=null){
            userID=getArguments().getString("userID");
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
        // pass user to veryfy
        List distinctEntrys = appDatabase.getAnswerDao().getDistinctEntrys(userID);
        if (distinctEntrys != null && distinctEntrys.size() > 0) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(20, 10, 20, 10);
            LinearLayout.LayoutParams paramsLeft = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 6);
            LinearLayout.LayoutParams paramsRight = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2);
            LinearLayout.LayoutParams textViewParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1);
            LinearLayout.LayoutParams imageViewParam = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            for (int entryID = 0; distinctEntrys.size() > entryID; entryID++) {
                AnswersSingleForm answersSingleForm = (AnswersSingleForm) distinctEntrys.get(entryID);
                /*OUTER lINEAR LAYOUT*/
                LinearLayout linLayoutSingleEntry = new LinearLayout(getActivity());
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
                formName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30);
                formName.setTypeface(null, Typeface.BOLD);
                formName.setText(appDatabase.getDDE_FormsDao().getFormName(answersSingleForm.getFormId()));
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
                final ImageView editForm = new ImageView(getActivity());
                editForm.setImageResource(R.drawable.edit_black);
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
                                intent.putExtra("formId", appDatabase.getAnswerDao().getFormIDByEntryID(tagEntyId));
                                intent.putExtra("userId", appDatabase.getAnswerDao().getUserIDByEntryID(tagEntyId));
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

                linLayoutSingleEntry.addView(left);
                linLayoutSingleEntry.addView(right);
                linearlayout.addView(linLayoutSingleEntry);
            }
        }
    }


    @OnClick(R.id.fab)
    public void edit() {
        fabInterface.fabOnClick();
    }


}
