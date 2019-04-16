package com.pratham.dde.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.util.Log;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.pratham.dde.database.AppDatabase;
import com.pratham.dde.domain.ErrorLog;
import com.pratham.dde.domain.User;
import com.pratham.dde.interfaces.updateTokenListener;
import com.pratham.dde.services.SyncUtility;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.UUID;

import static com.pratham.dde.BaseActivity.appDatabase;

public class Utility {

    //    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
    private static final DateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH);

    /**
     * Returns the current datetime as a string.
     * Needed for fields where current date time is being saved.
     * Datetime is returned as string because SQL Lite saves datetime as a string.
     *
     * @return Current Datetime as a string object.
     */
    public static String getCurrentDateTime() {
        Calendar cal = Calendar.getInstance();
        return dateFormat.format(cal.getTime());
    }

    /**
     * Method to generate a random GUID / UUID.
     * Needed when we are creating new IDs.
     *
     * @return A unique GUID / UUID.
     */
    public static UUID getUniqueID() {
        return UUID.randomUUID();
    }

    /**
     * Utility method which accepts a boolean variable and returns 1 if true and 0 if false.
     * Needed as flags which will be saved as boolean variables on central server are being saved as integers in local android database.
     * Boolean values are saved as integers in SQL Lite
     *
     * @param val Boolean value being passed
     * @return 1 if value passed is true, 0 if value passed is false.
     */
    public int convertBooleanToInt(Boolean val) {
        return (val) ? 1 : 0;
    }

    public static void updateToken(String userName, final String password, final int methodToCall, final updateTokenListener updateTokenListener, final Context mContext, final Dialog dialog) {
        if (SyncUtility.isDataConnectionAvailable(mContext)) {
            showDialogInApiCalling(dialog, mContext, "Getting new token from server");
            String url = Utility.getProperty("prodcheckCredentials", mContext);
            AndroidNetworking.post(url).addBodyParameter("username", userName).addBodyParameter("password", password).addBodyParameter("grant_type", "password").setTag("test").setPriority(Priority.MEDIUM).build().getAsJSONObject(new JSONObjectRequestListener() {
                @Override
                public void onResponse(JSONObject response) {
                    Utility.dismissDialog(dialog);
                    try {
                        if (response.length() > 2) {
                            String access_token = response.getString("access_token");
                            String userName = response.getString("userName");
                            String token_type = response.getString("token_type");
                            String expiryDate = response.getString(".expires");
                            User user = appDatabase.getUserDao().getUserDetails(userName, password);
                            Log.d("TokenChecker:***", "UserName: " + userName);
                            Log.d("TokenChecker:***", "Token: " + token_type + " " + access_token);
                            Log.d("TokenChecker:***", "Expiry: " + expiryDate);
                            if (user != null)
                                appDatabase.getUserDao().UpdateTokenAndExpiry(access_token, expiryDate, userName, password);
                            updateTokenListener.updateToken(methodToCall, token_type + " " + access_token);
                        } else {
                            Utility.showDialogue(mContext, "Invalid User! Try registering on website.");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(ANError error) {
                    Utility.dismissDialog(dialog);
                    updateErrorLog(error, appDatabase, "Utility : updateTokenFromServer");
                    Utility.showDialogue(mContext, "Problem in updating token!" + error.getErrorDetail());
                }
            });
        } else {
            Toast.makeText(mContext, "Internet not available", Toast.LENGTH_SHORT).show();
        }
    }


    public static String getProperty(String key, Context context) {
        try {
            Properties properties = new Properties();
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open("config.properties");
            properties.load(inputStream);
            return properties.getProperty(key);
        } catch (Exception ex) {
            return null;
        }
    }

    public static void showDialogue(Context act, String msg) {
        AlertDialog alertDialog = new AlertDialog.Builder(act).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage(msg);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    public static boolean isTokenValid(String expiryDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
            //   SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            Date strDate = sdf.parse(expiryDate);
            if (new Date().after(strDate))
                return false;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /*show loader */
    public static void showDialogInApiCalling(Dialog dialog, Context context, String msg) {
        if (dialog == null) {
            dialog = new ProgressDialog(context);
        }
        dialog.setTitle(msg);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    /* Set Message */
    public static void setMessage(Dialog dialog, String message) {
        if (dialog != null)
            dialog.setTitle(message);
    }

    /*Dismiss loader */
    public static void dismissDialog(Dialog dialog) {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }

    public static String getCurrentVersion(Context context) {
        PackageManager pm = context.getPackageManager();
        PackageInfo pInfo = null;
        try {
            pInfo = pm.getPackageInfo(context.getPackageName(), 0);

        } catch (PackageManager.NameNotFoundException e1) {
            e1.printStackTrace();
        }
        String currentVersion = pInfo.versionName;
        return currentVersion;
    }

    public static void updateErrorLog(ANError anError, AppDatabase appDatabase, String activityMethod) {
        try {
            ErrorLog errorLog = new ErrorLog();
            errorLog.setErrorBody(anError.getErrorBody());
            errorLog.setErrorCode(anError.getErrorCode());
            errorLog.setErrorDetail(anError.getErrorDetail());
            errorLog.setActivityMethod(activityMethod);
            Log.d("Error details", "onError: " + errorLog.toString());
            appDatabase.getErrorLogDao().insert(errorLog);
            Log.d("ErrorCount:", "updateErrorLog: " + appDatabase.getErrorLogDao().getAllErrorsLog().size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
