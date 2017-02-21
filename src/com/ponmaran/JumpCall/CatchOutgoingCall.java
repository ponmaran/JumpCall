package com.ponmaran.JumpCall;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.widget.Toast;

import java.util.Date;

public class CatchOutgoingCall extends BroadcastReceiver {
	private static final String TAG_BRD_REC = "OutgoingCallReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		Date callBroadcastTime = new Date();
		Bundle extras = intent.getExtras();
		String extrasData = "";
		for(String key : extras.keySet()) extrasData = extrasData.concat(" " + key + ": " + extras.getString(key));
		Log.d(TAG_BRD_REC, "onReceive Extras:" + extrasData);

		String orgNum = extras.getString(Intent.EXTRA_PHONE_NUMBER);

//		Phone number type identification
		String numSeq;
		if (orgNum.substring(0, 2).equals("+1")) return;
    	else if ( orgNum.substring(0,3).equals("011")) numSeq = numSeqBuild(context, orgNum);
    	else if (orgNum.substring(0,1).equals("+")) numSeq = numSeqBuild(context, "011" + orgNum.substring(1));
		else return;

		extras.putString("android.phone.extra.ORIGINAL_URI", "tel:" + numSeq);
		extras.putString("android.phone.extra.PHONE_NUMBER", numSeq);
		intent.putExtras(extras);

		this.setResultData(numSeq);

		Log.d(TAG_BRD_REC, "Number out: " + this.getResultData());

		//Setup CallLogObserver
		ContentResolver contentResolver = context.getContentResolver();

		CallLogObserver callLogObserver = new CallLogObserver(new Handler());
		callLogObserver.setContentResolver(contentResolver);
		callLogObserver.setCallBroadcastTime(callBroadcastTime);
		callLogObserver.setBridgeNumber(PhoneNumberUtils.extractNetworkPortion(numSeq));
		callLogObserver.setOriginalNumber(orgNum);
		Log.d(TAG_BRD_REC, callLogObserver.toString());

		contentResolver.registerContentObserver(Uri.parse("content://call_log/calls"),true,callLogObserver);

		MainActivity.BRIDGE_PAIRS = MainActivity.BRIDGE_PAIRS + "~" + orgNum + "~" + PhoneNumberUtils.extractNetworkPortion(numSeq);

		//Notify user by Toast
		Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(orgNum));
		Cursor contactSetCursor = context.getContentResolver().query(uri, new String[] {PhoneLookup.DISPLAY_NAME, PhoneLookup.LABEL, PhoneLookup.TYPE}, null, null, null);

		String contactName = new String();
		String contactLabel = new String();
		String contactType = new String();
		if(contactSetCursor != null && contactSetCursor.moveToFirst()){
			contactName = contactSetCursor.getString(contactSetCursor.getColumnIndex(PhoneLookup.DISPLAY_NAME));
			contactLabel = contactSetCursor.getString(contactSetCursor.getColumnIndex(PhoneLookup.LABEL));
			contactType = (String) Phone.getTypeLabel(context.getResources(), contactSetCursor.getInt(contactSetCursor.getColumnIndex(PhoneLookup.TYPE)), (CharSequence)"OTHER");
		}
		contactSetCursor.close();

		Toast.makeText(context, "Calling\n" +
				(contactName.length() > 0 && contactType.length() > 0 ?
						contactName + " " + (contactType == "OTHER" && contactLabel != null ? contactLabel : contactType) + " "
						:"") +
				orgNum + "\nusing JumpCall", Toast.LENGTH_LONG).show();
	}

    private String numSeqBuild(Context context, String dialedNum){
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.shared_prefs_file), Context.MODE_PRIVATE);
        String bridgeNumSeq = sharedPref.getString(context.getString(R.string.bridgeNum), context.getString(R.string.bridge_default_value));
        String delayTimeSeq = sharedPref.getString(context.getString(R.string.delayTime), context.getString(R.string.delay_default_value));

        String[] bridgeNum = bridgeNumSeq.split(":");
        String[] delayTime = delayTimeSeq.split(":", bridgeNum.length);
        
        int delayTimeNum;
        String pauses = new String();
        String numSeq = new String();
        for(int i = 0; i<bridgeNum.length ; i++){
        	delayTimeNum = delayTime[i].equals("")? 0 : Integer.parseInt(delayTime[i]);
        	pauses = "";
            for(int j=0; j < delayTimeNum ;j += 2)
            	pauses = pauses + ",";
        	numSeq = numSeq + bridgeNum[i] + pauses;
        }
        return numSeq + dialedNum;
    };
}