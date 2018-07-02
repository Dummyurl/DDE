package pratham.dde.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.UUID;

public class Utility{

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
    public UUID GetUniqueID() {
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
    public int ConvertBooleanToInt(Boolean val) {
        return (val) ? 1 : 0;
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

    public static void showDialogue(Activity act, String msg) {
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
    public static void setMessage(Dialog dialog, String message){
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

}
