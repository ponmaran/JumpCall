package com.ponmaran.JumpCall;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class CatchOutgoingCall extends BroadcastReceiver {
	private static final String TAG_BRD_REC = "BroadcastReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.v(TAG_BRD_REC, "New Outgoing Call");
//		Log.v(TAG, "phone number: " + intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER));
//		Log.v(TAG, "Action : " + intent. getAction ());
		Bundle extData = intent.getExtras();
/*		if (extData.size() > 0){
			Log.v(TAG, "Extras : " + extData.toString());			
		}
*/
		String orgNum = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
//		System.out.println("Number in" + orgNum);

//		Phone number type identification
		boolean numChangeFlag = false;
		
		String numSeq = new String();
		if (orgNum.substring(0, 2).equals("+1")){
    	}
    	else if ( orgNum.substring(0,3).equals("011")){
//			System.out.println(dialedNumber + " is non US with 011");
    		numSeq = nonUsNumSeqBuild(context, orgNum);
    		numChangeFlag = true;
    	}
    	else if (orgNum.substring(0,1).equals("+")){
//			System.out.println(dialedNumber + " is non US with +");
    		numSeq = nonUsNumSeqBuild(context, "011" + orgNum.substring(1));
    		numChangeFlag = true;
    	};

//        System.out.println("Number Sequence: " + numSeq);
        
        if (numChangeFlag){
//    		System.out.println("Calling listener");
//    		System.out.println(MainActivity.EXT_ORG_NUM);
//    		System.out.println(MainActivity.EXT_BUILT_NUM_SEQ);
        	Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(orgNum));
        	Cursor contactSetCursor = context.getContentResolver().query(uri, new String[] {PhoneLookup.DISPLAY_NAME, PhoneLookup.LABEL, PhoneLookup.TYPE}, null, null, null);
        	
/*        	Intent listenerIntent = new Intent(context, CallWatchActivity.class);

        	listenerIntent.putExtra(MainActivity.EXT_ORG_NUM,orgNum);
        	listenerIntent.putExtra(MainActivity.EXT_BUILT_NUM_SEQ, numSeq);

        	listenerIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        	listenerIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);

        	context.startActivity(listenerIntent);
*/
            if(((TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE)).getCallState() != TelephonyManager.CALL_STATE_OFFHOOK){
            	context.startService(new Intent(context, CallLogWatchService.class));
            }
//			else{
//            	System.out.println("Call in progress. Not starting listener");
//			}
    		
    		extData.putString("android.phone.extra.ORIGINAL_URI", "tel:" + numSeq);
    		extData.putString("android.phone.extra.PHONE_NUMBER", numSeq);
    		intent.putExtras(extData);

            this.setResultData(numSeq);
//			context.getContentResolver().query(android.provider.Contacts.CONTENT_URI, android.provider.ContactsContract.Contacts., selection, selectionArgs, sortOrder)

            MainActivity.BRIDGE_PAIRS = MainActivity.BRIDGE_PAIRS + "~" + orgNum + "~" + PhoneNumberUtils.extractNetworkPortion(numSeq);
        	
        	String contactName = new String();
        	String contactLabel = new String();
        	String contactType = new String();
        	if(contactSetCursor != null && contactSetCursor.moveToFirst()){
        		contactName = contactSetCursor.getString(contactSetCursor.getColumnIndex(PhoneLookup.DISPLAY_NAME));
        		contactLabel = contactSetCursor.getString(contactSetCursor.getColumnIndex(PhoneLookup.LABEL));
        		contactType = (String) Phone.getTypeLabel(context.getResources(), contactSetCursor.getInt(contactSetCursor.getColumnIndex(PhoneLookup.TYPE)), (CharSequence)"OTHER");
        	}

//    		Log.v(TAG_BRD_REC, "Contact: \'" + contactName +  "\' \'" + contactLabel + "\' \'" + contactType + "\'");

        	Toast.makeText(context, "Calling\n" + 
        							 (contactName.length() > 0 && contactType.length() > 0 ?
        									 contactName + " " + (contactType == "OTHER" && contactLabel != null ? contactLabel : contactType) + " "
											 :"") +
        			                 orgNum + "\nusing JumpCall", Toast.LENGTH_LONG).show();
//    		System.out.println("Number out: " + this.getResultData());
//    		System.out.println("Number in: " + intent.getExtras().toString());
		}
	}

    private String nonUsNumSeqBuild(Context context, String dialedNum){
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