package com.pratham.dde.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.StringRequestListener;
import com.androidnetworking.interfaces.UploadProgressListener;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.pratham.dde.BaseActivity;
import com.pratham.dde.database.BackupDatabase;
import com.pratham.dde.domain.AnswersSingleForm;
import com.pratham.dde.interfaces.FillAgainListner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.List;

/**
 * Created by abc on 7/31/2018.
 */

public class UploadAnswerAndImageToServer {
    Context context;
    List<AnswersSingleForm> listAns;
    String uploadDataUrl, uploadImageUrl;
    static int IMAGEPUSHFAILFLAG = -1;
    static int VIDEOPUSHFAILFLAG = -2;
    int uploadingIndex = 0;
    int uploadingVideoIndex = 0;
    String token;
    FillAgainListner fillAgainListner;
    static int totalImages, totalVideos;
    static int imageUploadCnt = 0;
    static int videoUploadCnt = 0;
    static int IMAGEPUSHED = 1, VIDEOPUDHED = 2, ALLDATAPUSHED = 3;
    static int jsonIndex = 0;

    public UploadAnswerAndImageToServer(Context context, List<AnswersSingleForm> listAns, String token) {
        //  demoUpload();
        this.context = context;
        fillAgainListner = (FillAgainListner) context;
        this.listAns = listAns;
        this.token = token;
        uploadDataUrl = (Utility.getProperty("produploadDataUrl", context));
        uploadImageUrl = (Utility.getProperty("produploadImageUrl", context));
       // uploadVideoUrl = (Utility.getProperty("produploadVideoUrl", context));
        uploadingIndex = 0;
        uploadingVideoIndex = 0;
        if (listAns.size() > 0) uploadData();
        else {
            Toast.makeText(context, "Nothing to upload ", Toast.LENGTH_SHORT).show();
        }
    }


    private void uploadData() {
        imageUploadCnt = 0;
        videoUploadCnt = 0;
        jsonIndex = 0;
        if (listAns.size() > uploadingIndex) {
            //uploading images
            AnswersSingleForm answersSingleForm = listAns.get(uploadingIndex);
            JsonArray jsonArray = answersSingleForm.getAnswerArrayOfSingleForm().getAsJsonArray();
            String FormId = answersSingleForm.getFormId();
            totalImages = BaseActivity.appDatabase.getDDE_QuestionsDao().getImageCountByFormID(FormId);
            if (totalImages == 0 || answersSingleForm.getPushStatus() > 0) {
                uploadingIndex++;
                uploadData();
            } else {
                uploadImageToServer(jsonArray);
            }
        } else if (listAns.size() > uploadingVideoIndex) {
            //uploading videos
            AnswersSingleForm answersSingleForm = listAns.get(uploadingVideoIndex);
            JsonArray jsonArray = answersSingleForm.getAnswerArrayOfSingleForm().getAsJsonArray();
            String FormId = answersSingleForm.getFormId();
            totalVideos = BaseActivity.appDatabase.getDDE_QuestionsDao().getVideoCountByFormID(FormId);
            if (totalVideos == 0 || answersSingleForm.getPushStatus() == VIDEOPUDHED) {
                uploadingVideoIndex++;
                uploadData();
            } else {
                uploadVideostoServer(jsonArray);
            }
        } else {
            //uploading json
            JsonArray wholeData = new JsonArray();
            //listAns = appDatabase.getAnswerDao().getAllAnswersByStatus(IMAGEPUSHED);
            String[] entryIdsOfAllForms = new String[listAns.size()];
            for (int listCount = 0; listCount < listAns.size(); listCount++) {
                if (listAns.get(listCount).getPushStatus() != IMAGEPUSHFAILFLAG && listAns.get(listCount).getPushStatus() != VIDEOPUSHFAILFLAG) {
                    JsonArray formAnsArray = listAns.get(listCount).getAnswerArrayOfSingleForm().getAsJsonArray();
                    entryIdsOfAllForms[listCount] = listAns.get(listCount).getEntryId();
                    wholeData.addAll(formAnsArray);
                }
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
            String questionType = BaseActivity.appDatabase.getDDE_QuestionsDao().getQueTypeByFormIDAndDestColName(FormId, DestColumnName);
            if (questionType.equalsIgnoreCase("image")) {
                String imgPath = jsonObject.get("Answers").getAsString();
                File file = new File(Environment.getExternalStorageDirectory().toString() + "/.DDE/DDEImages/" + imgPath);
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
                                                BaseActivity.appDatabase.getAnswerDao().setPushedStatus(listAns.get(uploadingIndex).getEntryId(), IMAGEPUSHED);
                                                uploadingIndex++;
                                                uploadData();
                                            } else {
                                                uploadImageToServer(array);
                                            }
                                        }
                                    } catch (JSONException e) {
                                        listAns.get(uploadingIndex).setPushStatus(IMAGEPUSHFAILFLAG);
                                        uploadingIndex++;
                                        uploadData();
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onError(ANError anError) {
                                    listAns.get(uploadingIndex).setPushStatus(IMAGEPUSHFAILFLAG);
                                    Toast.makeText(context, "Image push failed", Toast.LENGTH_SHORT).show();
                                    Utility.updateErrorLog(anError, BaseActivity.appDatabase, "MainActivity : uploadImageToServer");
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
                            Utility.dismissDialog(dialog);
                            updateStatusOfAllForms(entryIdsOfAllForms, IMAGEPUSHED);
                            Utility.updateErrorLog(anError, BaseActivity.appDatabase, "MainActivity : uploadAnswerDataToServer");
                            Utility.showDialogue(context, "Problem in pushing data due to: " + anError.getErrorDetail());
                        }
                    });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void updateStatusOfAllForms(String[] entryIdsOfAllForms, int statusCode) {
        for (int statusCnt = 0; statusCnt < entryIdsOfAllForms.length; statusCnt++)
            BaseActivity.appDatabase.getAnswerDao().setPushedStatus(entryIdsOfAllForms[statusCnt], statusCode);
        BackupDatabase.backup(context);
        fillAgainListner.fillAgainForm(true);
    }


    public void uploadVideostoServer(final JsonArray array) {

        if (array.size() > jsonIndex) {
            JsonObject jsonObject = array.get(jsonIndex).getAsJsonObject();
            String DestColumnName = jsonObject.get("DestColumnName").getAsString();
            String FormId = jsonObject.get("FormId").getAsString();
            String questionType = BaseActivity.appDatabase.getDDE_QuestionsDao().getQueTypeByFormIDAndDestColName(FormId, DestColumnName);
            if (questionType.equalsIgnoreCase("video")) {
                String videoPath = jsonObject.get("Answers").getAsString();
                File file = new File(Environment.getExternalStorageDirectory().toString() + "/.DDE/DDEVideos/" + videoPath);
                if (file.exists() && !videoPath.equals("")) {
                   // String fName = file.getName();
                    final ProgressDialog dialogVideo = new ProgressDialog(context);
                    Utility.showDialogInApiCalling(dialogVideo, context, "Uploading video(s)..");
                    String fName = file.getName();
                    AndroidNetworking.upload(uploadImageUrl)
                            .addMultipartFile(fName, file)
                            .addHeaders("Authorization", token)
                            .build()
                            .getAsJSONObject(new JSONObjectRequestListener() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    Utility.dismissDialog(dialogVideo);
                                    try {

                                        if (response.getBoolean("success")) {
                                            jsonIndex++;
                                            videoUploadCnt++;
                                            if (totalVideos == videoUploadCnt) {
                                                listAns.get(uploadingVideoIndex).setPushStatus(VIDEOPUDHED);
                                                BaseActivity.appDatabase.getAnswerDao().setPushedStatus(listAns.get(uploadingVideoIndex).getEntryId(), VIDEOPUDHED);
                                                uploadingVideoIndex++;
                                                uploadData();
                                            } else {
                                                uploadVideostoServer(array);
                                            }
                                        }
                                    } catch (JSONException e) {
                                        listAns.get(uploadingVideoIndex).setPushStatus(VIDEOPUSHFAILFLAG);
                                        uploadingVideoIndex++;
                                        uploadData();
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onError(ANError anError) {
                                    listAns.get(uploadingVideoIndex).setPushStatus(VIDEOPUSHFAILFLAG);
                                    Toast.makeText(context, "video push failed", Toast.LENGTH_SHORT).show();
                                    Utility.updateErrorLog(anError, BaseActivity.appDatabase, "MainActivity : uploadVideostoServer");
                                    uploadingVideoIndex++;
                                    uploadData();
                                    Utility.dismissDialog(dialogVideo);
                                }
                            });
                } else {
                    jsonIndex++;
                    uploadVideostoServer(array);
                }
            } else {
                jsonIndex++;
                uploadVideostoServer(array);
            }
        } else {
            uploadingVideoIndex++;
            uploadData();
        }

      /*  AndroidNetworking.upload("videofilename ")
                .addMultipartFile("video", file)
                .setTag("uploadTest")
                .setPriority(Priority.HIGH)
                .build()
                .setUploadProgressListener(new UploadProgressListener() {
                    @Override
                    public void onProgress(long bytesUploaded, long totalBytes) {
                        // do anything with progress
                    }
                })
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                    }

                    @Override
                    public void onError(ANError error) {
                        // handle error
                    }
                });*/
    }


}
