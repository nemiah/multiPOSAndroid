package it.furtmeier.multiposandroid;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.webkit.JavascriptInterface;

import com.epson.epos2.Epos2Exception;
import com.epson.epos2.printer.Printer;
import com.epson.epos2.printer.PrinterStatusInfo;
import com.epson.epos2.printer.ReceiveListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;


public class JavaScriptHook implements ReceiveListener {
	private Context mContext = null;
	//private EditText mEditTarget = null;
	private Printer  mPrinter = null;
	private MultiPOSAndroid multiPOSAndroid;
	
	public JavaScriptHook(MultiPOSAndroid multiPOSAndroid) {
		this.multiPOSAndroid = multiPOSAndroid;
		mContext = multiPOSAndroid;
	}

	private boolean initializeObject() {
		try {
			mPrinter = new Printer(Printer.TM_T20, Printer.MODEL_ANK, mContext);
		}
		catch (Exception e) {
			ShowMsg.showException(e, "Printer", mContext);
			return false;
		}

		mPrinter.setReceiveEventListener(this);

		return true;
	}

	@JavascriptInterface
	public boolean printReceipt(String JSON) {
		if (!initializeObject()) {
			return false;
		}

		JSONObject data = null;
		try {
			data = new JSONObject(JSON);


			if (!createReceiptData(data)) {
				finalizeObject();
				return false;
			}

			if (!printData(data)) {
				finalizeObject();
				return false;
			}

			return true;

		} catch (JSONException e) {
			e.printStackTrace();

			return false;
		}
	}

	private boolean connectPrinter(String printerPath) {
		boolean isBeginTransaction = false;

		if (mPrinter == null)
			return false;


		try {
			mPrinter.connect(printerPath, Printer.PARAM_DEFAULT);
		}
		catch (Exception e) {
			ShowMsg.showException(e, "connect", mContext);
			return false;
		}

		try {
			mPrinter.beginTransaction();
			isBeginTransaction = true;
		}
		catch (Exception e) {
			ShowMsg.showException(e, "beginTransaction", mContext);
		}

		if (isBeginTransaction == false) {
			try {
				mPrinter.disconnect();
			}
			catch (Epos2Exception e) {
				// Do nothing
				return false;
			}
		}

		return true;
	}

	private boolean printData(JSONObject JSON) throws JSONException {
		if (mPrinter == null) {
			return false;
		}

		if (!connectPrinter(JSON.getString("printer"))) {
			return false;
		}

		PrinterStatusInfo status = mPrinter.getStatus();

		dispPrinterWarnings(status);

		if (!isPrintable(status)) {
			ShowMsg.showMsg(makeErrorMessage(status), mContext);
			try {
				mPrinter.disconnect();
			}
			catch (Exception ex) {
				// Do nothing
			}
			return false;
		}

		try {
			mPrinter.sendData(Printer.PARAM_DEFAULT);
		}
		catch (Exception e) {
			ShowMsg.showException(e, "sendData", mContext);
			try {
				mPrinter.disconnect();
			}
			catch (Exception ex) {
				// Do nothing
			}
			return false;
		}

		return true;
	}

	private boolean createReceiptData(JSONObject JSON) {
		if (mPrinter == null)
			return false;

		StringBuilder textData = new StringBuilder();
		try {


			/*if(true)
				throw new Exception("BUUH");*/

			//mPrinter.addFeedLine(1);
			mPrinter.addTextAlign(Printer.ALIGN_CENTER);

			String logopath = JSON.getString("logo");
			if(logopath != "") {
				File file = new File(multiPOSAndroid.getFilesDir(), "bonlogo.png");
				downloadFile(logopath, file);

				Bitmap logoData = BitmapFactory.decodeFile(file.getAbsolutePath());

				mPrinter.addImage(logoData, 0, 0,
						logoData.getWidth(),
						logoData.getHeight(),
						Printer.COLOR_1,
						Printer.MODE_MONO,
						Printer.HALFTONE_DITHER,
						Printer.PARAM_DEFAULT,
						Printer.COMPRESS_AUTO);
			}

			String oben = JSON.getString("oben");
			if(oben != "")
				textData.append(oben+"\n");

			JSONArray positionen = JSON.getJSONArray("positionen");
			for (int i = 0; i < positionen.length(); i++) {
				try {
					JSONObject oneObject = positionen.getJSONObject(i);

					String text = oneObject.getString("text");
					String format = oneObject.getString("format");
					Log.d("multiPOSWebView", "Print: text: "+text+" format: "+format);

					textData.append(text+"\n");

				} catch (JSONException e) {
					// Oops
				}

			}

			String unten = JSON.getString("unten");
			if(unten != "")
				textData.append(unten+"\n");

			mPrinter.addText(textData.toString());
			mPrinter.addFeedLine(1);

			textData.delete(0, textData.length());
			mPrinter.addCut(Printer.CUT_FEED);

		}
		catch (Exception e) {
			ShowMsg.showException(e, "unknown", mContext);
			return false;
		}

		textData = null;

		return true;
	}

	private static boolean downloadFile(String url, File outputFile) throws IOException {
		Log.d("multiPOS", url);

		URL u = new URL(url);
		URLConnection conn = u.openConnection();
		int contentLength = conn.getContentLength();

		DataInputStream stream = new DataInputStream(u.openStream());

		byte[] buffer = new byte[contentLength];
		stream.readFully(buffer);
		stream.close();

		DataOutputStream fos = new DataOutputStream(new FileOutputStream(outputFile));
		fos.write(buffer);
		fos.flush();
		fos.close();

		return true;
	}

	private void disconnectPrinter() {
		if (mPrinter == null) {
			return;
		}

		try {
			mPrinter.endTransaction();
		}
		catch (final Exception e) {
			multiPOSAndroid.runOnUiThread(new Runnable() {
				@Override
				public synchronized void run() {
					ShowMsg.showException(e, "endTransaction", mContext);
				}
			});
		}

		try {
			mPrinter.disconnect();
		}
		catch (final Exception e) {
			multiPOSAndroid.runOnUiThread(new Runnable() {
				@Override
				public synchronized void run() {
					ShowMsg.showException(e, "disconnect", mContext);
				}
			});
		}

		finalizeObject();
	}

	private void finalizeObject() {
		if (mPrinter == null) {
			return;
		}

		mPrinter.clearCommandBuffer();

		mPrinter.setReceiveEventListener(null);

		mPrinter = null;
	}

	private boolean isPrintable(PrinterStatusInfo status) {
		if (status == null) {
			return false;
		}

		if (status.getConnection() == Printer.FALSE) {
			return false;
		}
		else if (status.getOnline() == Printer.FALSE) {
			return false;
		}
		else {
			;//print available
		}

		return true;
	}

	private String makeErrorMessage(PrinterStatusInfo status) {
		String msg = "";

		if (status.getOnline() == Printer.FALSE) {
			msg += R.string.handlingmsg_err_offline;
		}
		if (status.getConnection() == Printer.FALSE) {
			msg += R.string.handlingmsg_err_no_response;
		}
		if (status.getCoverOpen() == Printer.TRUE) {
			msg += R.string.handlingmsg_err_cover_open;
		}
		if (status.getPaper() == Printer.PAPER_EMPTY) {
			msg += R.string.handlingmsg_err_receipt_end;
		}
		if (status.getPaperFeed() == Printer.TRUE || status.getPanelSwitch() == Printer.SWITCH_ON) {
			msg += R.string.handlingmsg_err_paper_feed;
		}
		if (status.getErrorStatus() == Printer.MECHANICAL_ERR || status.getErrorStatus() == Printer.AUTOCUTTER_ERR) {
			msg += R.string.handlingmsg_err_autocutter;
			msg += R.string.handlingmsg_err_need_recover;
		}
		if (status.getErrorStatus() == Printer.UNRECOVER_ERR) {
			msg += R.string.handlingmsg_err_unrecover;
		}
		if (status.getErrorStatus() == Printer.AUTORECOVER_ERR) {
			if (status.getAutoRecoverError() == Printer.HEAD_OVERHEAT) {
				msg += R.string.handlingmsg_err_overheat;
				msg += R.string.handlingmsg_err_head;
			}
			if (status.getAutoRecoverError() == Printer.MOTOR_OVERHEAT) {
				msg += R.string.handlingmsg_err_overheat;
				msg += R.string.handlingmsg_err_motor;
			}
			if (status.getAutoRecoverError() == Printer.BATTERY_OVERHEAT) {
				msg += R.string.handlingmsg_err_overheat;
				msg += R.string.handlingmsg_err_battery;
			}
			if (status.getAutoRecoverError() == Printer.WRONG_PAPER) {
				msg += R.string.handlingmsg_err_wrong_paper;
			}
		}
		if (status.getBatteryLevel() == Printer.BATTERY_LEVEL_0) {
			msg += R.string.handlingmsg_err_battery_real_end;
		}

		return msg;
	}

	//@JavascriptInterface
	private void dispPrinterWarnings(PrinterStatusInfo status) {
		/*EditText edtWarnings = (EditText)findViewById(R.id.edtWarnings);
		String warningsMsg = "";

		if (status == null) {
			return;
		}

		if (status.getPaper() == Printer.PAPER_NEAR_END)
			warningsMsg += R.string.handlingmsg_warn_receipt_near_end;


		if (status.getBatteryLevel() == Printer.BATTERY_LEVEL_1)
			warningsMsg += R.string.handlingmsg_warn_battery_near_end;


		edtWarnings.setText(warningsMsg);*/
	}


	@Override
	public void onPtrReceive(final Printer printerObj, final int code, final PrinterStatusInfo status, final String printJobId) {
		multiPOSAndroid.runOnUiThread(new Runnable() {
			@Override
			public synchronized void run() {
				ShowMsg.showResult(code, makeErrorMessage(status), mContext);

				dispPrinterWarnings(status);

				//updateButtonState(true);

				new Thread(new Runnable() {
					@Override
					public void run() {
						disconnectPrinter();
					}
				}).start();
			}
		});
	}
}
