package com.pratham.dde.services;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Pravin on 03/02/2018.
 */

public class SyncUtility {

    Context mContext;
    OkHttpClient client = new OkHttpClient();
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    public SyncUtility(Context context){
        this.mContext = context;
    }

    public String getData(String url) throws IOException {
        if (isDataConnectionAvailable(mContext)) {
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            Response response = client.newCall(request).execute();
            return response.body().string();
        } else
            return "failure";
    }

    public static boolean isDataConnectionAvailable(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }



    public String sendData(String url, String json) throws IOException {
        if (isDataConnectionAvailable(mContext)) {
            RequestBody body = RequestBody.create(JSON, json);
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            Response response = client.newCall(request).execute();
                return response.body().string();

/*            Response response = client.newCall(request).execute();
            return response.body().string();*/
        } else
            return "failure";
    }
}
