package pratham.dde.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.StringRequestListener;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.List;

import pratham.dde.domain.AnswersSingleForm;
import pratham.dde.interfaces.FillAgainListner;

import static pratham.dde.BaseActivity.appDatabase;

/**
 * Created by abc on 7/31/2018.
 */

public class UploadAnswerAndImageToServer {
    Context context;
    List<AnswersSingleForm> listAns;
    String uploadDataUrl, uploadImageUrl;
    static int imageFailCnt = 0;
    int uploadingIndex = 0;
    String token;
    FillAgainListner fillAgainListner;
    static int totalImages;
    static int imageUploadCnt = 0;
    static int IMAGEPUSHED = 1, ALLDATAPUSHED = 2;
    static int jsonIndex = 0;

    public UploadAnswerAndImageToServer(Context context, List<AnswersSingleForm> listAns, String token) {
        this.context = context;
        fillAgainListner = (FillAgainListner) context;
        this.listAns = listAns;
        this.token = token;
        uploadDataUrl = (Utility.getProperty("uploadDataUrl", context));
        uploadImageUrl = (Utility.getProperty("uploadImageUrl", context));
        uploadingIndex = 0;
        if (listAns.size() > 0) uploadData();
        else {
            Toast.makeText(context, "Nothing to upload ", Toast.LENGTH_SHORT).show();
        }
    }


    private void uploadData() {
        imageUploadCnt = 0;
        jsonIndex = 0;
        if (listAns.size() > uploadingIndex) {
            AnswersSingleForm answersSingleForm = listAns.get(uploadingIndex);
            JsonArray jsonArray = answersSingleForm.getAnswerArrayOfSingleForm().getAsJsonArray();
            String FormId = answersSingleForm.getFormId();
            totalImages = appDatabase.getDDE_QuestionsDao().getImageCountByFormID(FormId);
            if (totalImages == 0 || answersSingleForm.getPushStatus() > 0) {
                uploadingIndex++;
                uploadData();
            } else {
                uploadImageToServer(jsonArray);
            }
        } else {
            JsonArray wholeData = new JsonArray();
            //listAns = appDatabase.getAnswerDao().getAllAnswersByStatus(IMAGEPUSHED);
            String[] entryIdsOfAllForms = new String[listAns.size()];
            for (int listCount = 0; listCount < listAns.size(); listCount++) {
                JsonArray formAnsArray = listAns.get(listCount).getAnswerArrayOfSingleForm().getAsJsonArray();
                entryIdsOfAllForms[listCount] = listAns.get(listCount).getEntryId();
                wholeData.addAll(formAnsArray);
            }
            if (wholeData.size() > 0) {
                uploadAnswerDataToServer(wholeData, entryIdsOfAllForms);
            } else {
                Toast.makeText(context, "Data pushed successfully", Toast.LENGTH_SHORT).show();
                updateStatusOfAllForms(entryIdsOfAllForms, ALLDATAPUSHED);
            }
        }
    }

    public void uploadImageToServer(final JsonArray array) {

        if (array.size() > jsonIndex) {
            JsonObject jsonObject = array.get(jsonIndex).getAsJsonObject();
            String DestColumnName = jsonObject.get("DestColumnName").getAsString();
            String FormId = jsonObject.get("FormId").getAsString();
            String questionType = appDatabase.getDDE_QuestionsDao().getQueTypeByFormIDAndDestColName(FormId, DestColumnName);
            if (questionType.equalsIgnoreCase("image")) {
                String imgPath = jsonObject.get("Answers").getAsString();
                File file = new File(Environment.getExternalStorageDirectory().toString() + "/DDEImages/"+imgPath);
                if (file.exists() && !imgPath.equals("")) {
                    final ProgressDialog dialog = new ProgressDialog(context);
                    Utility.showDialogInApiCalling(dialog, context, "Uploading Image(s)..");
                    String fName = file.getName();
                    AndroidNetworking.upload(uploadImageUrl)
                            .addMultipartFile(fName, file)
                            .addHeaders("Authorization", token)
                            .build()
                            .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Utility.dismissDialog(dialog);
                            try {

                                if (response.getBoolean("success")) {
                                    jsonIndex++;
                                    imageUploadCnt++;
                                    if (totalImages == imageUploadCnt) {
                                        listAns.get(uploadingIndex).setPushStatus(IMAGEPUSHED);
                                        appDatabase.getAnswerDao().setPushedStatus(listAns.get(uploadingIndex).getEntryId(), IMAGEPUSHED);
                                        uploadingIndex++;
                                        uploadData();
                                    } else {
                                        uploadImageToServer(array);
                                    }
                                }
                            } catch (JSONException e) {
                                uploadingIndex++;
                                uploadData();
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(ANError anError) {
                            Toast.makeText(context, "Image push failed", Toast.LENGTH_SHORT).show();
                            uploadingIndex++;
                            uploadData();
                            Utility.dismissDialog(dialog);
                        }
                    });
                } else {
                    jsonIndex++;
                    uploadImageToServer(array);
                }
            } else {
                jsonIndex++;
                uploadImageToServer(array);
            }
        } else {
            uploadingIndex++;
            uploadData();
        }
    }

    public void uploadAnswerDataToServer(final JsonArray data, final String[] entryIdsOfAllForms) {
        try {
            String stringData = data.toString();
            JSONArray jsonArrayData = new JSONArray(stringData);
            //final String entryId = data.get(0).getAsJsonObject().get("EntryId").getAsString();

            final ProgressDialog dialog = new ProgressDialog(context);
            Utility.showDialogInApiCalling(dialog, context, "Uploading Data..");

            AndroidNetworking.post(uploadDataUrl)
                    .addHeaders("Content-Type", "application/json")
                    .addHeaders("Authorization", token)
                    .addJSONArrayBody(jsonArrayData)
                    .build()
                    .getAsString(new StringRequestListener() {

                @Override
                public void onResponse(String response) {
                    Toast.makeText(context, "Data pushed successfully", Toast.LENGTH_SHORT).show();

                    updateStatusOfAllForms(entryIdsOfAllForms, ALLDATAPUSHED);
                    Utility.dismissDialog(dialog);
                }

                @Override
                public void onError(ANError anError) {
                    Toast.makeText(context, "Data push failed", Toast.LENGTH_SHORT).show();
                    updateStatusOfAllForms(entryIdsOfAllForms, IMAGEPUSHED);
                    Utility.dismissDialog(dialog);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void updateStatusOfAllForms(String[] entryIdsOfAllForms, int statusCode) {
        for (int statusCnt = 0; statusCnt < entryIdsOfAllForms.length; statusCnt++)
            appDatabase.getAnswerDao().setPushedStatus(entryIdsOfAllForms[statusCnt], statusCode);
        fillAgainListner.fillAgainForm(true);
    }

}
