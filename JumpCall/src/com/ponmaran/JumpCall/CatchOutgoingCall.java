package com.ponmaran.JumpCall;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

public class CatchOutgoingCall extends BroadcastReceiver {
//	private static final String TAG = "onReceive";

	@Override
	public void onReceive(Context context, Intent intent) {
//		Log.v(TAG, "phone number: " + intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER));
//		Log.v(TAG, "Action : " + intent. getAction ());
		Bundle extData = intent.getExtras();
/*		if (extData.size() > 0){
			Log.v(TAG, "Extras : " + extData.toString());			
		}
*/
		String orgNum = new String();
        String numSeq = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
//		System.out.println("Number in" + numSeq);

//		Phone number type identification
		boolean numChangeFlag = false;
        if (numSeq.substring(0, 2).equals("+1")){
    	}
    	else if ( numSeq.substring(0,3).equals("011")){
//			System.out.println(dialedNumber + " is non US with 011");
    		orgNum = numSeq;
    		numSeq = nonUsNumSeqBuild(context, numSeq);
    		numChangeFlag = true;
    	}
    	else if (numSeq.substring(0,1).equals("+")){
//			System.out.println(dialedNumber + " is non US with +");
    		orgNum = numSeq;
    		numSeq ="011" + numSeq.substring(1);
    		numSeq = nonUsNumSeqBuild(context, numSeq);
    		numChangeFlag = true;
    	};

//        System.out.println("Number Sequence: " + numSeq);
        
        if (numChangeFlag){
//    		System.out.println("Calling listener");
//    		System.out.println(MainActivity.EXT_ORG_NUM);
//    		System.out.println(MainActivity.EXT_BUILT_NUM_SEQ);
        	Intent listenerIntent = new Intent(context, CallWatchActivity.class);

        	listenerIntent.putExtra(MainActivity.EXT_ORG_NUM,orgNum);
        	listenerIntent.putExtra(MainActivity.EXT_BUILT_NUM_SEQ, numSeq);

        	listenerIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        	listenerIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);

        	context.startActivity(listenerIntent);

    		extData.putString("android.phone.extra.ORIGINAL_URI", "tel:" + numSeq);
    		extData.putString("android.phone.extra.PHONE_NUMBER", numSeq);
    		intent.putExtras(extData);

            this.setResultData(numSeq);
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