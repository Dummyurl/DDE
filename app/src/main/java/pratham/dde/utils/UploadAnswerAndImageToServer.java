package pratham.dde.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.androidnetworking.interfaces.UploadProgressListener;
import com.google.gson.JsonArray;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;

import static pratham.dde.BaseActivity.appDatabase;

/**
 * Created by abc on 7/31/2018.
 */

public class UploadAnswerAndImageToServer {
    static String uploadDataUrl = "http://www.dde.prathamskills.org/api/ddedataentry/savebulkdata";
    // static String uploadImageUrl = "http://www.dde.prathamskills.org/content/files/";
    static String uploadImageUrl = " http://www.ddeapi.prathamskills.org/api/ddedataentry/SaveImage";

    public static void uploadImageToServer(File file, String token, final Context context) {
        final ProgressDialog dialog = new ProgressDialog(context);
        Utility.showDialogInApiCalling(dialog, context, "Uploading Image(s)..");
        String fName = file.getName();
        AndroidNetworking.upload(uploadImageUrl)
                .addMultipartFile(fName, file)
                //    .setContentType("image/jpeg")
                .addHeaders("Authorization", token)
                //     .addHeaders("Content-Type", "image/jpeg")
                //.setTag("uploadTest")
                .build()
                /*.setUploadProgressListener(new UploadProgressListener() {
                    @Override
                    public void onProgress(long bytesUploaded, long totalBytes) {
                        // do anything with progress
                        Log.d("bytesUploaded", String.valueOf(bytesUploaded));
                    }
                })*/
                .getAsString(new StringRequestListener() {
            @Override
            public void onResponse(String response) {
                //Toast.makeText(context, "NO Internet Connection", Toast.LENGTH_LONG).show();
                Utility.dismissDialog(dialog);
                Log.d("imgresponse", response.toString());
            }

            @Override
            public void onError(ANError anError) {
                // handle error
                //         dialog.dismiss();
                Utility.dismissDialog(dialog);
                Log.d("imgerror", anError.toString());
            }
        });
    }


    public static void uploadAnswer(final JsonArray data, String token, Context context) {
        try {
            String stringData = data.toString();
            JSONArray jsonArrayData = new JSONArray(stringData);
            final String entryId = data.get(0).getAsJsonObject().get("EntryId").getAsString();

            final ProgressDialog dialog = new ProgressDialog(context);
            Utility.showDialogInApiCalling(dialog, context, "Uploading Data..");

            AndroidNetworking.post(uploadDataUrl)
                    .setContentType("application/json")
                    .addHeaders("Authorization", token)
                    .addHeaders("Content-Type", "application/json")
                    .addJSONArrayBody(jsonArrayData).build().getAsString(new StringRequestListener() {
                @Override
                public void onResponse(String response) {
                    Log.d("dataresponse", response.toString());
                    Utility.dismissDialog(dialog);
                    appDatabase.getAnswerDao().setIsPushed(entryId);
                }

                @Override
                public void onError(ANError anError) {
                    Log.d("dataError", "" + anError);
                    Utility.dismissDialog(dialog);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
