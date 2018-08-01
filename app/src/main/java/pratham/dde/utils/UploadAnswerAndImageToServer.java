package pratham.dde.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.StringRequestListener;
import com.androidnetworking.interfaces.UploadProgressListener;
import com.google.gson.JsonArray;

import org.json.JSONObject;

import java.io.File;

/**
 * Created by abc on 7/31/2018.
 */

public class UploadAnswerAndImageToServer {
    static String uploadDataUrl = "baseUrl/api/ddedataentry/savebulkdata";
    static String uploadImageUrl = "http://www.dde.prathamskills.org/";


    public static void uploadImageToServer(File file, final Context context) {
   //     final ProgressDialog dialog = new ProgressDialog(context);
   //     Utility.showDialogInApiCalling(dialog, context, "Uploading Images To Server..");
        AndroidNetworking.upload(uploadImageUrl)
                .addMultipartFile("image", file)
                .addMultipartParameter("key", "value")
                //.setTag("uploadTest")
                .build()
                .setUploadProgressListener(new UploadProgressListener() {
                    @Override
                    public void onProgress(long bytesUploaded, long totalBytes) {
                        // do anything with progress
                    }
                }).getAsJSONObject(new JSONObjectRequestListener() {
            @Override
            public void onResponse(JSONObject response) {
                // do anything with response
      //          dialog.dismiss();
                Toast.makeText(context, "NO Internet Connection", Toast.LENGTH_LONG).show();
                Log.d("response", response.toString());
            }

            @Override
            public void onError(ANError error) {
                // handle error
       //         dialog.dismiss();
                Log.d("error", error.toString());
            }
        });
    }


    public static void uploadAnswer(JsonArray data, String token, Context context) {
     //   final ProgressDialog dialog = new ProgressDialog(context);
     //   Utility.showDialogInApiCalling(dialog, context, "Uploading Images To Server..");
        AndroidNetworking.post(uploadDataUrl)
                .setContentType("application/json")
                .addHeaders("Authorization", token)
                .addHeaders("Content-Type", "application/json")
                .addStringBody("").build().getAsString(new StringRequestListener() {
            @Override
            public void onResponse(String response) {
                Log.d("response", response);
        //        dialog.dismiss();
            }
            @Override
            public void onError(ANError anError) {
                Log.d("anError", "" + anError);
         //       dialog.dismiss();
            }
        });
    }


}
