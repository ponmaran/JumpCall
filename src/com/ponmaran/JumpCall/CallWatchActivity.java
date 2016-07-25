package com.ponmaran.JumpCall;

import java.util.Date;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class CallWatchActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_call_bridge);
//		getActionBar().setDisplayHomeAsUpEnabled(true);
        Intent thisIntent = this.getIntent();

        String orgNum = thisIntent.getStringExtra(MainActivity.EXT_ORG_NUM);
        String numSeq = thisIntent.getStringExtra(MainActivity.EXT_BUILT_NUM_SEQ);

        MainActivity.BRIDGE_PAIRS = MainActivity.BRIDGE_PAIRS + "~" + orgNum + "~" + PhoneNumberUtils.extractNetworkPortion(numSeq);
        
        EndCallListener callListener = new EndCallListener();
        TelephonyManager telPhMgr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        if(telPhMgr.getCallState() == TelephonyManager.CALL_STATE_OFFHOOK){
//        	System.out.println("Call in progress. Not starting listener");
        }
        else
        telPhMgr.listen(callListener, PhoneStateListener.LISTEN_CALL_STATE);
    }
    
/*	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_call_bridge, menu);
        return true;
    }
*/
    private class EndCallListener extends PhoneStateListener{
    	
    	boolean activeCallInd = false;
    	int numRowsUpdt = 0;
    	String curLogNum = new String();
    	
    	@Override
    	public void onCallStateChanged(int state, String incomingNumber) {

    		Date dte = new Date();

    		switch (state){
    		case TelephonyManager.CALL_STATE_OFFHOOK:
    			activeCallInd = true;
//    	        System.out.println("OffHook time:\t" + dte.toLocaleString() + "\t" + dte.getTime());
            	break;

    		case TelephonyManager.CALL_STATE_IDLE:
            	if (activeCallInd == true ){
//        	        System.out.println("After Call Idle state time :: Date\t" + dte.toLocaleString() + "\t" + dte.getTime());
            		((TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE)).listen(this, LISTEN_NONE); 

//This is to hold until the call log is updated
            		try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						System.out.println("Sleep interrupted :@");
						e.printStackTrace();
					}

            		Cursor c = null;

            		String[] fields = {
        		    android.provider.CallLog.Calls.NUMBER,
        		    android.provider.CallLog.Calls.DURATION,
        		    android.provider.CallLog.Calls.DATE,
        		    };

            		c = getContentResolver().query(
            				android.provider.CallLog.Calls.CONTENT_URI,
            				fields,
		        		    android.provider.CallLog.Calls.DATE + " > " + (dte.getDate() - 1),
		        		    null,
		            		android.provider.CallLog.Calls.DATE + " DESC" 
		        		    );

	        		if(c.moveToFirst()){

	        			String[] callStack = MainActivity.BRIDGE_PAIRS.split("~");

	            		int callStackIdx =callStack.length - 1; 

	            		do{
		        			curLogNum = c.getString(c.getColumnIndex(android.provider.CallLog.Calls.NUMBER));
		        			Long callDate = c.getLong(c.getColumnIndex(android.provider.CallLog.Calls.DATE));
		        			Long callDuration = c.getLong(c.getColumnIndex(android.provider.CallLog.Calls.DURATION));
		        			if (curLogNum.equals(callStack[callStackIdx])){
//		        				System.out.println("Bridge # from stack: " + callStack[callStackIdx]);

		        				ContentValues valueSet = new ContentValues();
		    	    	    	valueSet.put(android.provider.CallLog.Calls.NUMBER, callStack[--callStackIdx]);

		    	    	    	numRowsUpdt = getContentResolver().update(
		    	    	    			android.provider.CallLog.Calls.CONTENT_URI, 
		    	    	    			valueSet, 
		    	    	    			android.provider.CallLog.Calls.NUMBER + "=" + curLogNum
		    	    	    			+ " and " + android.provider.CallLog.Calls.DATE + "=" + callDate
		    	    	    			+ " and " + android.provider.CallLog.Calls.DURATION + "=" + callDuration 
		    	    	    			,null);

//		    	    	    	System.out.println("Called # from stack: " + callStack[callStackIdx]);
//			        			System.out.println("Rows updated: " + numRowsUpdt);
			        			callStackIdx--;
		        			}
//	        				System.out.println("Cursor @ " + curLogNum);
	        			}
	        			while (c.moveToNext() && callStackIdx > 0);
	        			MainActivity.BRIDGE_PAIRS = new String();
	        		}
            	}
            	else{
//            		System.out.println("Before call idle state :: Date:\t" + dte.toLocaleString());
            	};
    		}
        }
    }
    
/*    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
*/
}