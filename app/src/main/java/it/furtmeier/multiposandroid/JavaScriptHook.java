package it.furtmeier.multiposandroid;

//import jim.h.common.android.lib.zxing.CaptureActivity;
//import jim.h.common.android.lib.zxing.integrator.IntentIntegrator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.util.Log;
import android.webkit.JavascriptInterface;

import com.epson.eposprint.*;
import com.epson.epsonio.*;

public class JavaScriptHook {
	private MultiPOSAndroid multiPOSAndroid;
	
	public JavaScriptHook(MultiPOSAndroid openWaWiAndroid) {
		this.multiPOSAndroid = openWaWiAndroid;
	}
	
	@JavascriptInterface
	public void search() {
		/*try {
			Finder.start(getBaseContext(),DevType.TCP, "255.255.255.255");
		
		
		} catch ( EpsonIoException e ) {
			
		}*/
	}
	
	Print printer = new Print();
	@JavascriptInterface
	public void print(String input) {
		
		int[] status = new int[1];
		status[0] = 0;
		
		try {
			JSONObject json = new JSONObject(input);
			JSONArray content = json.getJSONArray("content");
			//Initialize a Builder class instance
			Builder builder = new Builder("TM-T20II", Builder.MODEL_ANK);

			builder.addTextLang(Builder.LANG_EN);
			builder.addTextSmooth(Builder.TRUE);
			
			
			for (int i = 0; i < content.length(); ++i) {
			    JSONObject line = content.getJSONObject(i);
			    
			    int bold = Builder.FALSE;
			    int underline = Builder.FALSE;
			    int sizeW = 1;
			    int sizeH = 1;
			    int font = Builder.FONT_A;
			    
			    try {
				    if(line.getString("font") == "B")
				    	font = Builder.FONT_B;
				    
				    //Other fonts are not supported by all devices
			    } catch(JSONException e){}


			    try {
			    	if(line.getBoolean("bold"))
				    	bold = Builder.TRUE;
			    	
			    } catch(JSONException e){}

			    try {
			    	if(line.getBoolean("underline"))
			    		underline = Builder.TRUE;
			    	
			    } catch(JSONException e){}
			    
			    
			    try {
			    	JSONArray size = line.getJSONArray("size");
			    	sizeW = size.getInt(0);
			    	sizeH = size.getInt(1);
			    } catch(JSONException e){}

			    builder.addTextStyle(Builder.FALSE, underline, bold, Builder.PARAM_UNSPECIFIED);
			    builder.addTextFont(font);
			    builder.addTextSize(sizeW, sizeH);
			    
			    builder.addText(line.getString("text")+"\n");
			}
			
			//builder.addFeedPosition(Builder.FEED_PEELING);
			builder.addCut(Builder.CUT_FEED);
			
			printer.openPrinter(Print.DEVTYPE_TCP, "192.168.7.200");
			
			printer.sendData(builder, 10000, status);
			if((status[0] & Print.ST_PRINT_SUCCESS) == Print.ST_PRINT_SUCCESS) {
				builder.clearCommandBuffer();
			}
			printer.closePrinter();
			
		} catch (EposException e) {
			int errStatus = e.getErrorStatus();
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
	}
	
	/*
	@JavascriptInterface
	public void scan() {
		try {
	        Intent intent = new Intent(openWaWiAndroid, CaptureActivity.class);
	        openWaWiAndroid.startActivityForResult(intent, IntentIntegrator.REQUEST_CODE);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}*/
}
