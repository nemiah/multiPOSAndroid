package it.furtmeier.multiposandroid;

//import jim.h.common.android.lib.zxing.integrator.IntentIntegrator;
//import jim.h.common.android.lib.zxing.integrator.IntentResult;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;

public class MultiPOSAndroid extends Activity {
	public static boolean running;
	public static MultiPOSAndroid instance;
	public static MultiPOSWebView webView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		running = true;
		instance = this;
		Options.load(this);
		setFullScreen();
		webView = new MultiPOSWebView(this);
		setContentView(webView);
		//setMenuButtonVisibility(true);
		setRequestedOrientation(Options.orientation);
		refreshWebView();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		new it.furtmeier.multiposandroid.Menu(this).show();
		return false;
	}

	public void refreshWebView() {
		webView.loadUrl(Options.url);
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
	/*
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		if (requestCode == IntentIntegrator.REQUEST_CODE) {
			IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
			if (webView != null) {
				if (scanResult != null && scanResult.getContents() != null) {
					webView.loadUrl("javascript:barcodeScanned('" + scanResult.getContents() + "', '" + scanResult.getFormatName() + "')");
				} else {
					webView.loadUrl("javascript:barcodeScannedFailed()");
				}
			}
		}
	}*/
}
