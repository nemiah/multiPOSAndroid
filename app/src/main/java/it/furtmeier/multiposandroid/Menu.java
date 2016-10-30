package it.furtmeier.multiposandroid;

import it.furtmeier.multiposandroid.R;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.text.InputType;
import android.widget.EditText;

public class Menu {
	private MultiPOSAndroid openWaWiAndroid;

	public Menu(MultiPOSAndroid openWaWiAndroid) {
		this.openWaWiAndroid = openWaWiAndroid;
	}
	
	public void show() {
		Builder builder = new AlertDialog.Builder(openWaWiAndroid);
		final Resources resources = openWaWiAndroid.getResources();
		final String[] menus = resources.getStringArray(R.array.Options);
		builder.setItems(menus, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (which == 0)
					openWaWiAndroid.refreshWebView();
				if (which == 1)
					editUrl(resources, menus[which], Options.url);
				if (which == 2)
					editOrientation(resources, menus[which], Options.orientation);
			}
		});
		builder.show();
	}

	private void editOrientation(Resources resources, String title, int orientation) {
	    AlertDialog.Builder builder = new AlertDialog.Builder(openWaWiAndroid);
	    builder.setTitle(title)
	           .setItems(R.array.Orientations, new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int which) {
	            	   int newValue = 0;
	            	   if(which == 0)
	            		   newValue = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
	            	   if(which == 1)
	            		   newValue = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
	            	   if(which == 2)
	            		   newValue = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
	            	   if(which == 3)
	            		   newValue = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
	            	   
						Options.orientation = newValue;
						Options.save(openWaWiAndroid);
						openWaWiAndroid.setRequestedOrientation(newValue);
	           }
	    });
	    builder.show();
	}
	
	private void editUrl(final Resources resources, final String title, final String url) {
		Builder builder = new AlertDialog.Builder(openWaWiAndroid);
		builder.setTitle(title);
		final EditText edit = new EditText(openWaWiAndroid);
		edit.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
		edit.setText(url);
		builder.setView(edit);
		builder.setPositiveButton(resources.getString(R.string.ok), new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				final String newUri = edit.getText().toString();
				try {
					URI.create(newUri);
					if (!newUri.toLowerCase(Locale.US).startsWith("http://") && !newUri.toLowerCase(Locale.US).startsWith("https://"))
						throw new MalformedURLException();
					Options.url = newUri;
					Options.save(openWaWiAndroid);
					openWaWiAndroid.refreshWebView();
				} catch (Exception e) {
					showError(resources.getString(R.string.madUri), new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							editUrl(resources, title, newUri);
						}
					});
				}
			}
		});
		builder.setNegativeButton(resources.getString(R.string.cancel), null);
		builder.show();
	}
	
	public void showError(String text, OnClickListener onClick) {
		final Resources resources = openWaWiAndroid.getResources();
		Builder builder = new AlertDialog.Builder(openWaWiAndroid);
		builder.setTitle(resources.getString(R.string.error));
		builder.setMessage(text);
		builder.setPositiveButton(resources.getString(R.string.ok), onClick);
		builder.show();
	}
}
