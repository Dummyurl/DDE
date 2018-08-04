package pratham.dde.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;

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

    static int totalImages;
    static int imageUploadCnt = 0;
    static int IMAGEPUSHED = 1, ALLDATAPUSHED = 2;
    static int jsonIndex = 0;

    public UploadAnswerAndImageToServer(Context context, List<AnswersSingleForm> listAns, String token) {
        this.context = context;
        this.listAns = listAns;
        this.token = token;
        uploadDataUrl = (Utility.getProperty("uploadDataUrl", context));
        uploadImageUrl = (Utility.getProperty("uploadImageUrl", context));
        uploadingIndex = 0;
        uploadData();
    }


    private void uploadData() {
        imageUploadCnt = 0;
        jsonIndex = 0;
        if (listAns.size() > uploadingIndex) {
            AnswersSingleForm answersSingleForm = listAns.get(uploadingIndex);
            JsonArray jsonArray = answersSingleForm.getAnswerArrayOfSingleForm().getAsJsonArray();
            String FormId = answersSingleForm.getFormId();
            totalImages = appDatabase.getDDE_QuestionsDao().getImageCountByFormID(FormId);
            uploadImageToServer(jsonArray);
        } else {
            JsonArray wholeData = new JsonArray();
            //listAns = appDatabase.getAnswerDao().getAllAnswersByStatus(IMAGEPUSHED);
            String[] entryIdsOfAllForms = new String[listAns.size()];
            for (int listCount=0;listCount<listAns.size();listCount++){
                    JsonArray formAnsArray = listAns.get(listCount).getAnswerArrayOfSingleForm().getAsJsonArray();
                    entryIdsOfAllForms[listCount] = listAns.get(listCount).getEntryId();
                    wholeData.addAll(formAnsArray);
                    /*for (int answerObjCount = 0 ; answerObjCount < formAnsArray.size(); answerObjCount++){
                        wholeData.addAll(formAnsArray.get(answerObjCount));
                    }*/
            }
            uploadAnswerDataToServer(wholeData,entryIdsOfAllForms);
            //todo upload answer data
            Log.d("answerData", "upload answer data");
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
                File file = new File(imgPath);
                if (file.exists()) {
                    final ProgressDialog dialog = new ProgressDialog(context);
                    Utility.showDialogInApiCalling(dialog, context, "Uploading Image(s)..");
                    String fName = file.getName();
                    AndroidNetworking.upload(uploadImageUrl).addMultipartFile(fName, file).addHeaders("Authorization", token).build().getAsJSONObject(new JSONObjectRequestListener() {
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
                            uploadingIndex++;
                            uploadData();
                            Utility.dismissDialog(dialog);
                        }
                    });
                }
            } else {
                jsonIndex++;
                uploadImageToServer(array);
            }
        }
    }

    public void uploadAnswerDataToServer(final JsonArray data, final String[] entryIdsOfAllForms){
            try {
                String stringData = data.toString();
                JSONArray jsonArrayData = new JSONArray(stringData);
                //final String entryId = data.get(0).getAsJsonObject().get("EntryId").getAsString();

                final ProgressDialog dialog = new ProgressDialog(context);
                Utility.showDialogInApiCalling(dialog, context, "Uploading Data..");

                AndroidNetworking.post(uploadDataUrl)
                        .addHeaders("Authorization", token)
                        .addHeaders("Content-Type", "application/json")
                        .addJSONArrayBody(jsonArrayData).build()
                        .getAsString(new StringRequestListener() {

                    @Override
                    public void onResponse(String response) {
                        Log.d("dataresponse", response.toString());
                        Utility.dismissDialog(dialog);
                        updateStatusOfAllForms(entryIdsOfAllForms,ALLDATAPUSHED);
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.d("dataError", "" + anError);
                        updateStatusOfAllForms(entryIdsOfAllForms,IMAGEPUSHED);
                        Utility.dismissDialog(dialog);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    private void updateStatusOfAllForms(String[] entryIdsOfAllForms, int statusCode) {
        for (int statusCnt = 0; statusCnt < entryIdsOfAllForms.length; statusCnt++)
            appDatabase.getAnswerDao().setPushedStatus(entryIdsOfAllForms[statusCnt], statusCode);
    }
}
