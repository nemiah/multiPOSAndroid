package it.furtmeier.multiposandroid;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import static it.furtmeier.multiposandroid.R.string.pref_default_server_url;

public class ActivityMain extends Activity {
	public static boolean running;
	public static ActivityMain instance;
	public static MultiPOSWebView webView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//Log.d("multiPOS",  "I'm here!");

		super.onCreate(savedInstanceState);
		running = true;
		instance = this;

		setFullScreen();

		webView = new MultiPOSWebView(this);
		setContentView(webView);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		refreshWebView();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}


	public void refreshWebView() {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		String url = sharedPrefs.getString("server_url", getResources().getString(R.string.pref_default_server_url))+"/ubiquitous/CustomerPage/?D=multiPOS/Kassen&I="+sharedPrefs.getString("server_kasse", getResources().getString(R.string.pref_default_server_kasse))+"&T=&cloud="+sharedPrefs.getString("server_cloud", getResources().getString(R.string.pref_default_server_cloud));
		//Log.d("multiPOS",  "Loading "+url);
		webView.loadUrl(url);
	}

	public void setFullScreen() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

	public void setMenuButtonVisibility(boolean visible) {
		try {
			int flag = WindowManager.LayoutParams.class.getField("FLAG_NEEDS_MENU_KEY").getInt(null);
			getWindow().setFlags(visible ? flag : 0, flag);
		} catch (Exception e) {
		}
    }

	public String getVersionName() {
		try {
			return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			return "";
		}
	}
}
