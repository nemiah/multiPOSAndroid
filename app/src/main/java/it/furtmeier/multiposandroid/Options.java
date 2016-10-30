package it.furtmeier.multiposandroid;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;

public class Options {
	public static String url = "http://192.168.7.77/ubiquitous/CustomerPage/?D=multiPOS/Kassen&I=1&T=";
	public static int orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
	
	public static void load(MultiPOSAndroid openWaWiAndroid) {
		SharedPreferences pref = openWaWiAndroid.getPreferences(Activity.MODE_PRIVATE);
		url = pref.getString("url", url);
		orientation = pref.getInt("orientation", orientation);
	}

	public static void save(MultiPOSAndroid openWaWiAndroid) {
		SharedPreferences pref = openWaWiAndroid.getPreferences(Activity.MODE_PRIVATE);
		Editor editor = pref.edit();
		editor.putString("url", url);
		editor.putInt("orientation", orientation);
		editor.commit();
	}
}
