package pratham.dde.utils;

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
    static String uploadDataUrl="baseUrl/api/ddedataentry/savebulkdata";


    public static void uploadImageToServer(String url, File file) {
        AndroidNetworking.upload(url).addMultipartFile("image", file).addMultipartParameter("key", "value").setTag("uploadTest").build().setUploadProgressListener(new UploadProgressListener() {
            @Override
            public void onProgress(long bytesUploaded, long totalBytes) {
                // do anything with progress
            }
        }).getAsJSONObject(new JSONObjectRequestListener() {
            @Override
            public void onResponse(JSONObject response) {
                // do anything with response
            }

            @Override
            public void onError(ANError error) {
                // handle error
            }
        });
    }



    public static void uploadAnswer(JsonArray data,String token){
        AndroidNetworking.post(uploadDataUrl)
                .setContentType("application/json")
                .addHeaders("Authorization", token)
                .addHeaders("Content-Type", "application/json")
                .addStringBody("").build().getAsString(new StringRequestListener() {
            @Override
            public void onResponse(String response) {
                Log.d("response", response);
                //  dialog.dismiss();
            }

            @Override
            public void onError(ANError anError) {

                Log.d("anError", "" + anError);
            }
        });
}


}
