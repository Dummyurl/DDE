package pratham.dde.utils;

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
    static String uploadDataUrl = "baseUrl/api/ddedataentry/savebulkdata";
    static String uploadImageUrl = "http://www.dde.prathamskills.org/content/files/";


    public static void uploadImageToServer(File file, String token, final Context context) {
        AndroidNetworking.upload(uploadImageUrl)
                .addMultipartFile("image", file)
            //    .setContentType("image/jpeg")
                .addHeaders("Authorization", token)
           //     .addHeaders("Content-Type", "image/jpeg")
                .addMultipartParameter("key", "value")
                //.setTag("uploadTest")
                .build()
                .setUploadProgressListener(new UploadProgressListener() {
                    @Override
                    public void onProgress(long bytesUploaded, long totalBytes) {
                        // do anything with progress
                        Log.d("bytesUploaded", String.valueOf(bytesUploaded));
                    }
                }).getAsString(new StringRequestListener() {
            @Override
            public void onResponse(String response) {
                //Toast.makeText(context, "NO Internet Connection", Toast.LENGTH_LONG).show();
                Log.d("imgresponse", response.toString());
            }

            @Override
            public void onError(ANError anError) {
                // handle error
                //         dialog.dismiss();
                Log.d("imgerror", anError.toString());
            }
        });
    }


    public static void uploadAnswer(final JsonArray data, String token, Context context) {
        try {
            String stringData = data.toString();
            JSONArray jsonArrayData = new JSONArray(stringData);

            final String entryId = data.get(0).getAsJsonObject().get("EntryId").getAsString();
            AndroidNetworking.post(uploadDataUrl)
                    .setContentType("application/json")
                    .addHeaders("Authorization", token)
                    .addHeaders("Content-Type", "application/json")
                    .addJSONArrayBody(jsonArrayData).build().getAsString(new StringRequestListener() {
                @Override
                public void onResponse(String response) {
                    Log.d("dataresponse", response.toString());
                    appDatabase.getAnswerDao().setIsPushed(entryId);
                }

                @Override
                public void onError(ANError anError) {
                    Log.d("dataError", "" + anError);

                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
