package pratham.dde.utils;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Properties;
import java.util.UUID;

public class Utility {

    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

    /**
     * Returns the current datetime as a string.
     * Needed for fields where current date time is being saved.
     * Datetime is returned as string because SQL Lite saves datetime as a string.
     *
     * @return Current Datetime as a string object.
     */
    public String GetCurrentDateTime() {
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
        try{
            Properties properties = new Properties();
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open("config.properties");
            properties.load(inputStream);
            return properties.getProperty(key);
        }
        catch (Exception ex){
            return null;
        }
    }
}
