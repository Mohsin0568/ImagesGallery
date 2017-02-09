package gallery.images.com.imagesgallery.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import gallery.images.com.imagesgallery.ApplicationApp;

/**
 * Created by mohmurtu on 11/8/2015.
 */
public class SharedPrefUtil {

    public static SharedPreferences getPrefs(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ApplicationApp.context);
        return sharedPreferences;
    }

    public static SharedPreferences.Editor getPrefsEditor(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ApplicationApp.context);
        return sharedPreferences.edit();
    }

    public static boolean getBooleanPrefs(String prefsKey){
        SharedPreferences prefs = getPrefs();
        return prefs.getBoolean(prefsKey, false);
    }

    public static String getStringPrefs(String prefsKey){
        SharedPreferences prefs = getPrefs();
        return prefs.getString(prefsKey, "");
    }

    public static Integer getIntegerPrefs(String prefsKey){
        SharedPreferences prefs = getPrefs();
        return prefs.getInt(prefsKey,0);
    }

    public static void setBooleanPrefs(String prefkey, boolean prefValue){
        SharedPreferences.Editor editor = getPrefsEditor();
        editor.putBoolean(prefkey,prefValue);
        editor.commit();
    }

    public static  void setStringPrefs(String prefKey, String prefValue){
        SharedPreferences.Editor editor = getPrefsEditor();
        editor.putString(prefKey, prefValue);
        editor.commit();
    }

    public static void setIntegerPrefs(String prefKey, Integer prefValue){
        SharedPreferences.Editor editor = getPrefsEditor();
        editor.putInt(prefKey,prefValue);
        editor.commit();
    }
}
